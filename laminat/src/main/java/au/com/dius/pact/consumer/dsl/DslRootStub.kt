package au.com.dius.pact.consumer.dsl

object DslRootStub : DslPart(rootPath = "", rootName = "") {
    init {
        parent = this
    }

    private val errorText = "DslRoot should not be used as an element"

    override fun putObject(`object`: DslPart) {
        throw UnsupportedOperationException(errorText)
    }

    override fun putArray(`object`: DslPart) {
        throw UnsupportedOperationException(errorText)
    }

    override val body: Any
        get() = throw UnsupportedOperationException(errorText)

    override fun array(name: String): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun array(): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun closeArray(): DslPart {
        throw UnsupportedOperationException(errorText)
    }

    override fun arrayLike(name: String): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun arrayLike(): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachLike(name: String): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachLike(): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachLike(name: String, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachLike(numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun minArrayLike(name: String, size: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun minArrayLike(size: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun minArrayLike(name: String, size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun minArrayLike(size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun maxArrayLike(name: String, size: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun maxArrayLike(size: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun maxArrayLike(name: String, size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun maxArrayLike(size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayLike(name: String): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayLike(): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayLike(name: String, numberExamples: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayLike(numberExamples: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMaxLike(name: String, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMaxLike(size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMaxLike(name: String, numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMaxLike(numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMinLike(name: String, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMinLike(size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMinLike(name: String, numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun eachArrayWithMinLike(numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException(errorText)
    }

    override fun `object`(name: String): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun `object`(): PactDslJsonBody {
        throw UnsupportedOperationException(errorText)
    }

    override fun closeObject(): DslPart {
        throw UnsupportedOperationException(errorText)
    }

    override fun close(): DslPart {
        throw UnsupportedOperationException(errorText)
    }
}