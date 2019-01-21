package au.com.dius.pact

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.external.PactJsonifier
import au.com.dius.pact.model.PactMergeException
import au.com.dius.pact.model.RequestResponsePact
import org.junit.Assert
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class PactTest {

    private val expectedPact = "testconsumer:testproducer.json"

    private val defaultRequestHeaders = hashMapOf(
        Pair("We", "will have to see about this!")
    )
    private val defaultResponseHeaders = hashMapOf(
        Pair("We", "will have to see about this as well.")
    )

    private fun getInitialPacts(): List<RequestResponsePact> {
        return listOf(ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("GET testRequest")
            .path("test/path")
            .method("GET")
            .headers(defaultRequestHeaders)
            .willRespondWith()
            .status(200)
            .headers(defaultResponseHeaders)
            .body("{}")
            .toPact())
    }

    private fun getAdditionalPacts(): List<RequestResponsePact> {
        return listOf(ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("GET additionaltestRequest")
            .path("test/path/additional")
            .method("GET")
            .headers(defaultRequestHeaders)
            .willRespondWith()
            .status(200)
            .headers(defaultResponseHeaders)
            .body("{}")
            .toPact())
    }

    private fun getConflictPacts(): List<RequestResponsePact> {
        return listOf(ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("GET testRequest")
            .path("test/path")
            .method("GET")
            .headers(defaultRequestHeaders)
            .willRespondWith()
            .status(200)
            .headers(defaultResponseHeaders)
            .body("{}")
            .toPact())
    }

    private fun readFile(file: File): String {
        val text = StringBuilder()
        try {
            val br = BufferedReader(InputStreamReader(file.inputStream(), "UTF-8"))
            var line: String?

            while (true) {
                line = br.readLine()
                if(line == null) {
                    break
                }
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: Exception) {
            System.err.println("readMockFile: failed to read ${file.name}")
            e.printStackTrace()
        }

        return text.toString()
    }

    @Test
    fun pact_buildJson_correctlyBuilt() {
        PactJsonifier.generateJson(getInitialPacts(), File("pacts"))
        val outputPactFile = File("pacts/${expectedPact}")
        Assert.assertTrue("Pact was not generated!",outputPactFile.exists())

        val outputPact = readFile(outputPactFile)
        val expectedPact = readFile(File("src/test/assets/$expectedPact"))
        Assert.assertEquals("Generated pact does not match expectations!", expectedPact, outputPact)
    }

    @Test(expected=PactMergeException::class)
    fun pact_buildJson_failsMerge() {
        PactJsonifier.generateJson(getInitialPacts().plus(getConflictPacts()), File("pacts"))
    }

    @Test
    fun pact_pactsList_noConflicts() {
        val pacts = getInitialPacts().plus(getAdditionalPacts())
        for(first in pacts) {
            for(second in pacts) {
                if(first === second) {
                    continue
                }
                Assert.assertTrue("Pacts are not compatible: $first; $second", first.compatibleTo(second))
                val conflicts = first.conflictsWith(second)
                Assert.assertTrue("Pacts are incompatible as there were ${conflicts.size} conflict(s) " +
                        "between the interactions - ${conflicts.joinToString("\n")}", conflicts.isEmpty())
            }
        }
    }

    @Test
    fun pact_conflictingPactList_showConflicts() {
        val pacts = getInitialPacts().plus(getConflictPacts())
        for(first in pacts) {
            for(second in pacts) {
                if(first === second) {
                    continue
                }
                Assert.assertTrue("Pacts are not compatible: $first; $second", first.compatibleTo(second))
                val conflicts = first.conflictsWith(second)
                Assert.assertEquals("Expected 1 conflict, found ${conflicts.size}!", 1, conflicts.size)
            }
        }
    }
}