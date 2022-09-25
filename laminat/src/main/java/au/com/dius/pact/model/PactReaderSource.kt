package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

sealed class PactReaderSource {

    abstract fun loadPact(): Pair<Json, PactSource>

    protected fun parseJson(reader: Reader): Json {
        return Json.parse(reader)
    }

    data class FileSource(
        val file: File
    ) : PactReaderSource() {

        override fun loadPact(): Pair<Json, PactSource> {
            val pactData = parseJson(FileReader(file))
            return Pair(pactData, PactSource.FileSource(file))
        }
    }

    data class InputStreamPactSource(
        private val inputStream: InputStream
    ) : PactReaderSource() {

        override fun loadPact(): Pair<Json, PactSource> {
            val pactData = parseJson(InputStreamReader(inputStream))
            return Pair(pactData, PactSource.InputStreamPactSource)
        }
    }

    class ReaderPactSource(
        private val reader: Reader
    ) : PactReaderSource() {

        override fun loadPact(): Pair<Json, PactSource> {
            val pactData = Json.parse(reader)
            return Pair(pactData, PactSource.ReaderPactSource)
        }
    }

    data class ClosurePactSource(
        private val closure: () -> PactReaderSource
    ) : PactReaderSource() {

        override fun loadPact(): Pair<Json, PactSource> {
            return closure.invoke().loadPact()
        }
    }
}