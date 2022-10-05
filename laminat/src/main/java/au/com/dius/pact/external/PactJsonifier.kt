package au.com.dius.pact.external

import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags
import au.com.dius.pact.model.InvalidPactException
import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactMerge
import au.com.dius.pact.model.PactMergeException
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.PactReaderSource
import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.PactWriter
import au.com.dius.pact.model.RequestResponsePact
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.Locale

/**
 * This PactJsonifier singleton allows the generation of a pact json from
 * the given pact collection.
 * Since a pact json may only denote contain a single pact, the pacts in the collection
 * are folded into a single pact by merging them through PactMerge.
 * If a pact can not be merged a PactMergeException is thrown.
 *
 * @author Jan Phillip Kretzschmar
 */
object PactJsonifier {

    fun generateJson(pact: Pact, baseDir: File, serializationConfig: PactSerializationConfig = PactSerializationConfig(PactSpecVersion.V3, null)) {
        generateJson(listOf(pact), baseDir, serializationConfig)
    }

    fun generateJson(pacts: Collection<Pact>, baseDir: File, serializationConfig: PactSerializationConfig = PactSerializationConfig(PactSpecVersion.V3, null)) {
        baseDir.mkdir()
        pacts.forEach {
            val conflicts = it.conflictsWithSelf()
            if (conflicts.isNotEmpty()) {
                throw PactMergeException(
                    "Cannot merge pacts as there were ${conflicts.size} conflict(s) " +
                        "between the interactions - ${conflicts.joinToString("\n")}"
                )
            }
        }
        val firstPact = pacts.first()
        var mergedPact = RequestResponsePact(firstPact.provider, firstPact.consumer, emptyList())
        pacts.forEach { current ->
            val result = PactMerge.merge(current, mergedPact)
            if (!result.ok) {
                throw PactMergeException(result.message)
            }
        }

        val file = getFileFor(mergedPact, baseDir)
        if (FeatureFlags.isFeatureEnabled(Feature.MERGE_EXISTING_PACTS_FILE) && file.exists()) {
            val source = PactReaderSource.FileSource(file)
            val (originalPact, pactVersion) = PactReader.readPactWithVersion(source)
            if (FeatureFlags.isFeatureEnabled(Feature.MERGE_DISALLOW_DIFFERENT_PACT_VERSIONS) && pactVersion != serializationConfig.specVersion) {
               throw InvalidPactException(
                   "Cannot merge pacts as they are not compatible:\n" +
                   "File already contains pact in version ${pactVersion.value}, but serialization config with version ${serializationConfig.specVersion.value} was defined"
               )
            }
            val result = PactMerge.merge(mergedPact, originalPact)
            if (!result.ok) {
                throw PactMergeException(result.message)
            }
            mergedPact = originalPact as RequestResponsePact
        }
        mergedPact.sortInteractions()
        saveToPactFile(file, mergedPact, serializationConfig)
    }

    private fun getFileFor(pact: Pact, baseDir: File): File {
        val name = "${pact.consumer.name.lowercase(Locale.ROOT).replace(' ','_')}___${pact.provider.name.lowercase(Locale.ROOT).replace(' ','_')}.json"
        return File(baseDir, name)
    }

    private fun saveToPactFile(file: File, pact: Pact, serializationConfig: PactSerializationConfig) {
        PrintWriter(
            OutputStreamWriter(
                FileOutputStream(file),
                Charsets.UTF_8
            )
        ).use { printWriter ->
            PactWriter.writePact(pact, printWriter, serializationConfig)
        }
    }
}