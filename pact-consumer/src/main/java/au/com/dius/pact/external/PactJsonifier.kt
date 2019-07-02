package au.com.dius.pact.external

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactMerge
import au.com.dius.pact.model.PactMergeException
import au.com.dius.pact.model.PactWriter
import au.com.dius.pact.model.RequestResponsePact
import java.io.File
import java.io.PrintWriter

object PactJsonifier {
    fun generateJson(pacts: Collection<RequestResponsePact>, baseDir: File) {
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
        val mergedPact = (pacts.fold(RequestResponsePact(firstPact.provider, firstPact.consumer, emptyList())) { left, current ->
            val result = PactMerge.merge(current, left)
            if (!result.ok) {
                throw PactMergeException(result.message)
            }
            left
        }).sortInteractions()

        val file = getEmptyFileFor(mergedPact, baseDir)
        PrintWriter(file).use { printWriter ->
            PactWriter.writePact(mergedPact, printWriter)
        }
    }

    private fun getEmptyFileFor(pact: Pact, baseDir: File): File {
        val name = "${pact.consumer.name.toLowerCase().replace(' ','_')}:${pact.provider.name.toLowerCase().replace(' ','_')}.json"
        val file = File(baseDir, name)
        return file
    }
}