package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.*

/**
 * Abstract base class to support Object and Array JSON DSL builders
 */
abstract class DslPart {
    @JvmField
    protected val parent: DslPart?
    @JvmField
    protected val rootPath: String
    protected val rootName: String
    @JvmField
    var matchers = Category("body")
    @JvmField
    var generators = Generators()
    @JvmField
    protected var closed = false

    constructor(parent: DslPart?, rootPath: String, rootName: String) {
        this.parent = parent
        this.rootPath = rootPath
        this.rootName = rootName
    }

    constructor(rootPath: String, rootName: String) {
        parent = null
        this.rootPath = rootPath
        this.rootName = rootName
    }

    protected abstract fun putObject(`object`: DslPart?)
    abstract fun putArray(`object`: DslPart?)
    abstract val body: Any
    override fun toString(): String {
        return body.toString()
    }

    /**
     * Field which is an array
     * @param name field name
     */
    abstract fun array(name: String?): PactDslJsonArray?

    /**
     * Element as an array
     */
    abstract fun array(): PactDslJsonArray?

    /**
     * Close of the previous array element
     */
    abstract fun closeArray(): DslPart?

    /**
     * Array field where each element must match the following object
     * @param name field name
     */
    @Deprecated("Use eachLike instead")
    abstract fun arrayLike(name: String?): PactDslJsonBody?

    /**
     * Array element where each element of the array must match the following object
     */
    @Deprecated("Use eachLike instead")
    abstract fun arrayLike(): PactDslJsonBody?

    /**
     * Array field where each element must match the following object
     * @param name field name
     */
    abstract fun eachLike(name: String?): PactDslJsonBody?

    /**
     * Array element where each element of the array must match the following object
     */
    abstract fun eachLike(): PactDslJsonBody?

    /**
     * Array field where each element must match the following object
     * @param name field name
     * @param numberExamples number of examples to generate
     */
    abstract fun eachLike(name: String?, numberExamples: Int): PactDslJsonBody?

    /**
     * Array element where each element of the array must match the following object
     * @param numberExamples number of examples to generate
     */
    abstract fun eachLike(numberExamples: Int): PactDslJsonBody?

    /**
     * Array field with a minumum size and each element must match the provided object
     * @param name field name
     * @param size minimum size
     */
    abstract fun minArrayLike(name: String?, size: Int?): PactDslJsonBody?

    /**
     * Array element with a minumum size and each element of the array must match the provided object
     * @param size minimum size
     */
    abstract fun minArrayLike(size: Int?): PactDslJsonBody?

    /**
     * Array field with a minumum size and each element must match the provided object
     * @param name field name
     * @param size minimum size
     * @param numberExamples number of examples to generate
     */
    abstract fun minArrayLike(name: String?, size: Int?, numberExamples: Int): PactDslJsonBody?

    /**
     * Array element with a minumum size and each element of the array must match the provided object
     * @param size minimum size
     * @param numberExamples number of examples to generate
     */
    abstract fun minArrayLike(size: Int?, numberExamples: Int): PactDslJsonBody?

    /**
     * Array field with a maximum size and each element must match the provided object
     * @param name field name
     * @param size maximum size
     */
    abstract fun maxArrayLike(name: String?, size: Int?): PactDslJsonBody?

    /**
     * Array element with a maximum size and each element of the array must match the provided object
     * @param size minimum size
     */
    abstract fun maxArrayLike(size: Int?): PactDslJsonBody?

    /**
     * Array field with a maximum size and each element must match the provided object
     * @param name field name
     * @param size maximum size
     * @param numberExamples number of examples to generate
     */
    abstract fun maxArrayLike(name: String?, size: Int?, numberExamples: Int): PactDslJsonBody?

    /**
     * Array element with a maximum size and each element of the array must match the provided object
     * @param size minimum size
     * @param numberExamples number of examples to generate
     */
    abstract fun maxArrayLike(size: Int?, numberExamples: Int): PactDslJsonBody?

    /**
     * Array field where each element is an array and must match the following object
     * @param name field name
     */
    abstract fun eachArrayLike(name: String?): PactDslJsonArray?

    /**
     * Array element where each element of the array is an array and must match the following object
     */
    abstract fun eachArrayLike(): PactDslJsonArray?

    /**
     * Array field where each element is an array and must match the following object
     * @param name field name
     * @param numberExamples number of examples to generate
     */
    abstract fun eachArrayLike(name: String?, numberExamples: Int): PactDslJsonArray?

    /**
     * Array element where each element of the array is an array and must match the following object
     * @param numberExamples number of examples to generate
     */
    abstract fun eachArrayLike(numberExamples: Int): PactDslJsonArray?

    /**
     * Array field where each element is an array and must match the following object
     * @param name field name
     * @param size Maximum size of the outer array
     */
    abstract fun eachArrayWithMaxLike(name: String?, size: Int?): PactDslJsonArray?

    /**
     * Array element where each element of the array is an array and must match the following object
     * @param size Maximum size of the outer array
     */
    abstract fun eachArrayWithMaxLike(size: Int?): PactDslJsonArray?

    /**
     * Array field where each element is an array and must match the following object
     * @param name field name
     * @param numberExamples number of examples to generate
     * @param size Maximum size of the outer array
     */
    abstract fun eachArrayWithMaxLike(name: String?, numberExamples: Int, size: Int?): PactDslJsonArray?

    /**
     * Array element where each element of the array is an array and must match the following object
     * @param numberExamples number of examples to generate
     * @param size Maximum size of the outer array
     */
    abstract fun eachArrayWithMaxLike(numberExamples: Int, size: Int?): PactDslJsonArray?

    /**
     * Array field where each element is an array and must match the following object
     * @param name field name
     * @param size Minimum size of the outer array
     */
    abstract fun eachArrayWithMinLike(name: String?, size: Int?): PactDslJsonArray?

    /**
     * Array element where each element of the array is an array and must match the following object
     * @param size Minimum size of the outer array
     */
    abstract fun eachArrayWithMinLike(size: Int?): PactDslJsonArray?

    /**
     * Array field where each element is an array and must match the following object
     * @param name field name
     * @param numberExamples number of examples to generate
     * @param size Minimum size of the outer array
     */
    abstract fun eachArrayWithMinLike(name: String?, numberExamples: Int, size: Int?): PactDslJsonArray?

    /**
     * Array element where each element of the array is an array and must match the following object
     * @param numberExamples number of examples to generate
     * @param size Minimum size of the outer array
     */
    abstract fun eachArrayWithMinLike(numberExamples: Int, size: Int?): PactDslJsonArray?

    /**
     * Object field
     * @param name field name
     */
    abstract fun `object`(name: String?): PactDslJsonBody?

    /**
     * Object element
     */
    abstract fun `object`(): PactDslJsonBody?

    /**
     * Close off the previous object
     * @return
     */
    abstract fun closeObject(): DslPart?

    protected fun regexp(regex: String?): RegexMatcher {
        return RegexMatcher(regex!!)
    }

    protected fun matchTimestamp(format: String?): TimestampMatcher {
        return TimestampMatcher(format!!)
    }

    protected fun matchDate(format: String?): DateMatcher {
        return DateMatcher(format!!)
    }

    protected fun matchTime(format: String?): TimeMatcher {
        return TimeMatcher(format!!)
    }

    protected fun matchMin(min: Int?): MinTypeMatcher {
        return MinTypeMatcher(min!!)
    }

    protected fun matchMax(max: Int?): MaxTypeMatcher {
        return MaxTypeMatcher(max!!)
    }

    protected fun includesMatcher(value: Any): IncludeMatcher {
        return IncludeMatcher(value.toString())
    }

    fun asBody(): PactDslJsonBody {
        return this as PactDslJsonBody
    }

    fun asArray(): PactDslJsonArray {
        return this as PactDslJsonArray
    }

    /**
     * This closes off the object graph build from the DSL in case any close[Object|Array] methods have not been called.
     * @return The root object of the object graph
     */
    abstract fun close(): DslPart?

    companion object {
        const val HEXADECIMAL = "[0-9a-fA-F]+"
        const val IP_ADDRESS = "(\\d{1,3}\\.)+\\d{1,3}"
        const val UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        const val DATE_2000 = 949323600000L
    }
}