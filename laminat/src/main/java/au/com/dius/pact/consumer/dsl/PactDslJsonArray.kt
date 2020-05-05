package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.InvalidMatcherException
import au.com.dius.pact.model.generators.*
import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.matchingrules.*
import com.mifmif.common.regex.Generex
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

/**
 * DSL to define a JSON array.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class PactDslJsonArray @JvmOverloads constructor(rootPath: String = "", rootName: String = "", parent: DslPart = DslRootStub, private val wildCard: Boolean = false) :
    DslPart(parent, rootPath, rootName) {
    override var body: JSONArray = JSONArray()
    /**
     * Returns the number of example elements to generate for sample bodies
     */
    /**
     * Sets the number of example elements to generate for sample bodies
     */
    var numberExamples = 1

    /**
     * Closes the current array
     */
    override fun closeArray(): DslPart {
        parent.putArray(this)
        closed = true
        return parent
    }

    @Deprecated("")
    override fun arrayLike(name: String): PactDslJsonBody {
        throw UnsupportedOperationException("use the eachLike() form")
    }

    /**
     * Element that is an array where each item must match the following example
     */
    @Deprecated("use eachLike")
    override fun arrayLike(): PactDslJsonBody {
        return eachLike()
    }

    override fun eachLike(name: String): PactDslJsonBody {
        throw UnsupportedOperationException("use the eachLike() form")
    }

    override fun eachLike(name: String, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the eachLike(numberExamples) form")
    }

    /**
     * Element that is an array where each item must match the following example
     */
    override fun eachLike(): PactDslJsonBody {
        return eachLike(1)
    }

    /**
     * Element that is an array where each item must match the following example
     * @param numberExamples Number of examples to generate
     */
    override fun eachLike(numberExamples: Int): PactDslJsonBody {
        matchers.addRule(rootPath + appendArrayIndex(1), matchMin(0))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonBody(".", "", parent)
    }

    override fun minArrayLike(name: String, size: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the minArrayLike(Integer size) form")
    }

    /**
     * Element that is an array with a minimum size where each item must match the following example
     * @param size minimum size of the array
     */
    override fun minArrayLike(size: Int): PactDslJsonBody {
        return minArrayLike(size, size)
    }

    override fun minArrayLike(name: String, size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the minArrayLike(Integer size, int numberExamples) form")
    }

    /**
     * Element that is an array with a minimum size where each item must match the following example
     * @param size minimum size of the array
     * @param numberExamples number of examples to generate
     */
    override fun minArrayLike(size: Int, numberExamples: Int): PactDslJsonBody {
        require(numberExamples >= size) {
            String.format(
                "Number of example %d is less than the minimum size of %d",
                numberExamples, size
            )
        }
        matchers.addRule(rootPath + appendArrayIndex(1), matchMin(size))
        val parent = PactDslJsonArray("", "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonBody(".", "", parent)
    }

    override fun maxArrayLike(name: String, size: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the maxArrayLike(Integer size) form")
    }

    /**
     * Element that is an array with a maximum size where each item must match the following example
     * @param size maximum size of the array
     */
    override fun maxArrayLike(size: Int): PactDslJsonBody {
        return maxArrayLike(size, 1)
    }

    override fun maxArrayLike(name: String, size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the maxArrayLike(Integer size, int numberExamples) form")
    }

    /**
     * Element that is an array with a maximum size where each item must match the following example
     * @param size maximum size of the array
     * @param numberExamples number of examples to generate
     */
    override fun maxArrayLike(size: Int, numberExamples: Int): PactDslJsonBody {
        require(numberExamples <= size) {
            String.format(
                "Number of example %d is more than the maximum size of %d",
                numberExamples, size
            )
        }
        matchers.addRule(rootPath + appendArrayIndex(1), matchMax(size))
        val parent = PactDslJsonArray("", "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonBody(".", "", parent)
    }

    override fun putObject(`object`: DslPart) {
        for (matcherName in `object`.matchers.matchingRules.keys) {
            matchers.setRules(
                rootPath + appendArrayIndex(1) + matcherName,
                `object`.matchers.matchingRules[matcherName]!!
            )
        }
        generators.addGenerators(`object`.generators, rootPath + appendArrayIndex(1))
        for (i in 0 until numberExamples) {
            body.put(`object`.body)
        }
    }

    override fun putArray(`object`: DslPart) {
        for (matcherName in `object`.matchers.matchingRules.keys) {
            matchers.setRules(
                rootPath + appendArrayIndex(1) + matcherName,
                `object`.matchers.matchingRules[matcherName]!!
            )
        }
        generators.addGenerators(`object`.generators, rootPath + appendArrayIndex(1))
        body.put(`object`.body)
    }

    /**
     * Element that must be the specified value
     * @param value string value
     */
    fun stringValue(value: String?): PactDslJsonArray {
        body.put(value ?: JSONObject.NULL)
        return this
    }

    /**
     * Element that must be the specified value
     * @param value string value
     */
    fun string(value: String): PactDslJsonArray {
        return stringValue(value)
    }

    fun numberValue(value: Number?): PactDslJsonArray {
        body.put(value ?: JSONObject.NULL)
        return this
    }

    /**
     * Element that must be the specified value
     * @param value number value
     */
    fun number(value: Number?): PactDslJsonArray {
        return numberValue(value)
    }

    /**
     * Element that must be the specified value
     * @param value boolean value
     */
    fun booleanValue(value: Boolean?): PactDslJsonArray {
        body.put(value ?: JSONObject.NULL)
        return this
    }

    /**
     * Element that can be any string
     */
    fun stringType(): PactDslJsonArray {
        body.put("string")
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), RandomStringGenerator(20))
        matchers.addRule(rootPath + appendArrayIndex(0), TypeMatcher)
        return this
    }

    /**
     * Element that can be any string
     * @param example example value to use for generated bodies
     */
    fun stringType(example: String): PactDslJsonArray {
        body.put(example)
        matchers.addRule(rootPath + appendArrayIndex(0), TypeMatcher)
        return this
    }

    /**
     * Element that can be any number
     */
    fun numberType(): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), RandomIntGenerator(0, Int.MAX_VALUE))
        return numberType(100)
    }

    /**
     * Element that can be any number
     * @param number example number to use for generated bodies
     */
    fun numberType(number: Number?): PactDslJsonArray {
        body.put(number)
        matchers.addRule(rootPath + appendArrayIndex(0), NumberTypeMatcher(NumberTypeMatcher.NumberType.NUMBER))
        return this
    }

    /**
     * Element that must be an integer
     */
    fun integerType(): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), RandomIntGenerator(0, Int.MAX_VALUE))
        return integerType(100L)
    }

    /**
     * Element that must be an integer
     * @param number example integer value to use for generated bodies
     */
    fun integerType(number: Long?): PactDslJsonArray {
        body.put(number)
        matchers.addRule(rootPath + appendArrayIndex(0), NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
        return this
    }

    /**
     * Element that must be a real value
     */
    @Deprecated("Use decimalType instead")
    fun realType(): PactDslJsonArray {
        return decimalType()
    }

    /**
     * Element that must be a real value
     * @param number example real value
     */
    @Deprecated("Use decimalType instead")
    fun realType(number: Double?): PactDslJsonArray {
        return decimalType(number)
    }

    /**
     * Element that must be a decimal value
     */
    fun decimalType(): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), RandomDecimalGenerator(10))
        return decimalType(BigDecimal("100"))
    }

    /**
     * Element that must be a decimalType value
     * @param number example decimalType value
     */
    fun decimalType(number: BigDecimal?): PactDslJsonArray {
        body.put(number)
        matchers.addRule(rootPath + appendArrayIndex(0), NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
        return this
    }

    /**
     * Attribute that must be a decimalType value
     * @param number example decimalType value
     */
    fun decimalType(number: Double?): PactDslJsonArray {
        body.put(number)
        matchers.addRule(rootPath + appendArrayIndex(0), NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
        return this
    }

    /**
     * Element that must be a boolean
     */
    fun booleanType(): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), RandomBooleanGenerator)
        body.put(true)
        matchers.addRule(rootPath + appendArrayIndex(0), TypeMatcher)
        return this
    }

    /**
     * Element that must be a boolean
     * @param example example boolean to use for generated bodies
     */
    fun booleanType(example: Boolean?): PactDslJsonArray {
        body.put(example)
        matchers.addRule(rootPath + appendArrayIndex(0), TypeMatcher)
        return this
    }

    /**
     * Element that must match the regular expression
     * @param regex regular expression
     * @param value example value to use for generated bodies
     */
    fun stringMatcher(pattern: String, value: String): PactDslJsonArray {
        if (!value.matches(Regex(pattern))) {
            throw InvalidMatcherException(
                EXAMPLE + value + "\" does not match regular expression \"" +
                    pattern + "\""
            )
        }
        body.put(value)
        matchers.addRule(rootPath + appendArrayIndex(0), regexp(pattern))
        return this
    }

    /**
     * Element that must match the regular expression
     * @param regex regular expression
     */
    @Deprecated("Use the version that takes an example value")
    fun stringMatcher(regex: String): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), RandomStringGenerator(10))
        stringMatcher(regex, Generex(regex).random())
        return this
    }

    /**
     * Element that must be an ISO formatted timestamp
     */
    fun timestamp(): PactDslJsonArray {
        val pattern = DateFormatUtils.ISO_DATETIME_FORMAT.pattern
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DATE_2000
        body.put(DateFormatUtils.ISO_DATETIME_FORMAT.format(calendar))
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), DateTimeGenerator(pattern))
        matchers.addRule(rootPath + appendArrayIndex(0), matchTimestamp(pattern))
        return this
    }

    /**
     * Element that must match the given timestamp format
     * @param format timestamp format
     */
    fun timestamp(format: String): PactDslJsonArray {
        val instance = FastDateFormat.getInstance(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DATE_2000
        body.put(instance.format(calendar))
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), DateTimeGenerator(format))
        matchers.addRule(rootPath + appendArrayIndex(0), matchTimestamp(format))
        return this
    }

    /**
     * Element that must match the given timestamp format
     * @param format timestamp format
     * @param example example date and time to use for generated bodies
     */
    fun timestamp(format: String, example: Calendar?): PactDslJsonArray {
        val instance = FastDateFormat.getInstance(format)
        body.put(instance.format(example))
        matchers.addRule(rootPath + appendArrayIndex(0), matchTimestamp(format))
        return this
    }

    /**
     * Element that must be formatted as an ISO date
     */
    fun date(): PactDslJsonArray {
        val pattern = DateFormatUtils.ISO_DATE_FORMAT.pattern
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DATE_2000
        body.put(DateFormatUtils.ISO_DATE_FORMAT.format(calendar))
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), DateGenerator(pattern))
        matchers.addRule(rootPath + appendArrayIndex(0), matchDate(pattern))
        return this
    }

    /**
     * Element that must match the provided date format
     * @param format date format to match
     */
    fun date(format: String): PactDslJsonArray {
        val instance = FastDateFormat.getInstance(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DATE_2000
        body.put(instance.format(calendar))
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), DateTimeGenerator(format))
        matchers.addRule(rootPath + appendArrayIndex(0), matchDate(format))
        return this
    }

    /**
     * Element that must match the provided date format
     * @param format date format to match
     * @param example example date to use for generated values
     */
    fun date(format: String, example: Calendar?): PactDslJsonArray {
        val instance = FastDateFormat.getInstance(format)
        body.put(instance.format(example))
        matchers.addRule(rootPath + appendArrayIndex(0), matchDate(format))
        return this
    }

    /**
     * Element that must be an ISO formatted time
     */
    fun time(): PactDslJsonArray {
        val pattern = DateFormatUtils.ISO_TIME_FORMAT.pattern
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DATE_2000
        body.put(DateFormatUtils.ISO_TIME_FORMAT.format(calendar))
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), TimeGenerator(pattern))
        matchers.addRule(rootPath + appendArrayIndex(0), matchTime(pattern))
        return this
    }

    /**
     * Element that must match the given time format
     * @param format time format to match
     */
    fun time(format: String): PactDslJsonArray {
        val instance = FastDateFormat.getInstance(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DATE_2000
        body.put(instance.format(calendar))
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), TimeGenerator(format))
        matchers.addRule(rootPath + appendArrayIndex(0), matchTime(format))
        return this
    }

    /**
     * Element that must match the given time format
     * @param format time format to match
     * @param example example time to use for generated bodies
     */
    fun time(format: String, example: Calendar?): PactDslJsonArray {
        val instance = FastDateFormat.getInstance(format)
        body.put(instance.format(example))
        matchers.addRule(rootPath + appendArrayIndex(0), matchTime(format))
        return this
    }

    /**
     * Element that must be an IP4 address
     */
    fun ipAddress(): PactDslJsonArray {
        body.put("127.0.0.1")
        matchers.addRule(rootPath + appendArrayIndex(0), regexp("(\\d{1,3}\\.)+\\d{1,3}"))
        return this
    }

    override fun `object`(name: String): PactDslJsonBody {
        throw UnsupportedOperationException("use the object() form")
    }

    /**
     * Element that is a JSON object
     */
    override fun `object`(): PactDslJsonBody {
        return PactDslJsonBody(".", "", this)
    }

    override fun closeObject(): DslPart {
        throw UnsupportedOperationException("can't call closeObject on an Array")
    }

    override fun close(): DslPart {
        var parentToReturn: DslPart = this
        if (!closed) {
            var parent = closeArray()
            while (parent != DslRootStub) {
                parentToReturn = parent
                parent = if (parent is PactDslJsonArray) {
                    parent.closeArray()
                } else {
                    parent.closeObject()
                }
            }
        }
        parentToReturn.matchers.applyMatcherRootPrefix("$")
        parentToReturn.generators.applyRootPrefix("$")
        return parentToReturn
    }

    override fun array(name: String): PactDslJsonArray {
        throw UnsupportedOperationException("use the array() form")
    }

    /**
     * Element that is a JSON array
     */
    override fun array(): PactDslJsonArray {
        return PactDslJsonArray("", "", this)
    }

    /**
     * Element that must be a numeric identifier
     */
    fun id(): PactDslJsonArray {
        body.put(100L)
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(0), RandomIntGenerator(0, Int.MAX_VALUE))
        matchers.addRule(rootPath + appendArrayIndex(0), TypeMatcher)
        return this
    }

    /**
     * Element that must be a numeric identifier
     * @param id example id to use for generated bodies
     */
    fun id(id: Long?): PactDslJsonArray {
        body.put(id)
        matchers.addRule(rootPath + appendArrayIndex(0), TypeMatcher)
        return this
    }

    /**
     * Element that must be encoded as a hexadecimal value
     */
    fun hexValue(): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), RandomHexadecimalGenerator(10))
        return hexValue("1234a")
    }

    /**
     * Element that must be encoded as a hexadecimal value
     * @param hexValue example value to use for generated bodies
     */
    fun hexValue(hexValue: String): PactDslJsonArray {
        val pattern = HEXADECIMAL
        if (!hexValue.matches(Regex(pattern))) {
            throw InvalidMatcherException("$EXAMPLE$hexValue\" is not a hexadecimal value")
        }
        body.put(hexValue)
        matchers.addRule(rootPath + appendArrayIndex(0), regexp(pattern))
        return this
    }

    /**
     * Element that must be encoded as a GUID
     */
    @Deprecated("use uuid instead")
    fun guid(): PactDslJsonArray {
        return uuid()
    }

    /**
     * Element that must be encoded as a GUID
     * @param uuid example UUID to use for generated bodies
     */
    @Deprecated("use uuid instead")
    fun guid(uuid: String): PactDslJsonArray {
        return uuid(uuid)
    }

    /**
     * Element that must be encoded as an UUID
     */
    fun uuid(): PactDslJsonArray {
        generators.addGenerator(Category.BODY, rootPath + appendArrayIndex(1), UuidGenerator())
        return uuid("e2490de5-5bd3-43d5-b7c4-526e33f71304")
    }

    /**
     * Element that must be encoded as an UUID
     * @param uuid example UUID to use for generated bodies
     */
    fun uuid(uuid: String): PactDslJsonArray {
        val pattern = UUID_REGEX
        if (!uuid.matches(Regex(pattern))) {
            throw InvalidMatcherException("$EXAMPLE$uuid\" is not an UUID")
        }
        body.put(uuid)
        matchers.addRule(rootPath + appendArrayIndex(0), regexp(pattern))
        return this
    }

    /**
     * Adds the template object to the array
     * @param template template object
     */
    fun template(template: DslPart): PactDslJsonArray {
        putObject(template)
        return this
    }

    /**
     * Adds a number of template objects to the array
     * @param template template object
     * @param occurrences number to add
     */
    fun template(template: DslPart, occurrences: Int): PactDslJsonArray {
        for (i in 0 until occurrences) {
            template(template)
        }
        return this
    }

    private fun appendArrayIndex(offset: Int): String {
        var index = "*"
        if (!wildCard) {
            index = (body.length() - 1 + offset).toString()
        }
        return "[$index]"
    }

    /**
     * Adds a null value to the list
     */
    fun nullValue(): PactDslJsonArray {
        body.put(JSONObject.NULL)
        return this
    }

    override fun eachArrayLike(name: String): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayLike() form")
    }

    override fun eachArrayLike(name: String, numberExamples: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayLike(numberExamples) form")
    }

    override fun eachArrayLike(): PactDslJsonArray {
        return eachArrayLike(1)
    }

    override fun eachArrayLike(numberExamples: Int): PactDslJsonArray {
        matchers.addRule(rootPath + appendArrayIndex(1), matchMin(0))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonArray("", "", parent)
    }

    override fun eachArrayWithMaxLike(name: String, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMaxLike() form")
    }

    override fun eachArrayWithMaxLike(name: String, numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMaxLike(numberExamples) form")
    }

    override fun eachArrayWithMaxLike(size: Int): PactDslJsonArray {
        return eachArrayWithMaxLike(1, size)
    }

    override fun eachArrayWithMaxLike(numberExamples: Int, size: Int): PactDslJsonArray {
        require(numberExamples <= size) {
            String.format(
                "Number of example %d is more than the maximum size of %d",
                numberExamples, size
            )
        }
        matchers.addRule(rootPath + appendArrayIndex(1), matchMax(size))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonArray("", "", parent)
    }

    override fun eachArrayWithMinLike(name: String, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMinLike() form")
    }

    override fun eachArrayWithMinLike(name: String, numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMinLike(numberExamples) form")
    }

    override fun eachArrayWithMinLike(size: Int): PactDslJsonArray {
        return eachArrayWithMinLike(size, size)
    }

    override fun eachArrayWithMinLike(numberExamples: Int, size: Int): PactDslJsonArray {
        require(numberExamples >= size) {
            String.format(
                "Number of example %d is less than the minimum size of %d",
                numberExamples, size
            )
        }
        matchers.addRule(rootPath + appendArrayIndex(1), matchMin(size))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonArray("", "", parent)
    }
    /**
     * Array of values that are not objects where each item must match the provided example
     * @param value Value to use to match each item
     * @param numberExamples number of examples to generate
     */
    /**
     * Array of values that are not objects where each item must match the provided example
     * @param value Value to use to match each item
     */
    @JvmOverloads
    fun eachLike(value: PactDslJsonRootValue, numberExamples: Int = 1): PactDslJsonArray {
        matchers.addRule(rootPath + appendArrayIndex(1), matchMin(0))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        parent.putObject(value)
        return parent.closeArray() as PactDslJsonArray
    }
    /**
     * Array of values with a minimum size that are not objects where each item must match the provided example
     * @param size minimum size of the array
     * @param value Value to use to match each item
     * @param numberExamples number of examples to generate
     */
    /**
     * Array of values with a minimum size that are not objects where each item must match the provided example
     * @param size minimum size of the array
     * @param value Value to use to match each item
     */
    @JvmOverloads
    fun minArrayLike(size: Int, value: PactDslJsonRootValue, numberExamples: Int = size): PactDslJsonArray {
        require(numberExamples >= size) {
            String.format(
                "Number of example %d is less than the minimum size of %d",
                numberExamples, size
            )
        }
        matchers.addRule(rootPath + appendArrayIndex(1), matchMin(size))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        parent.putObject(value)
        return parent.closeArray() as PactDslJsonArray
    }
    /**
     * Array of values with a maximum size that are not objects where each item must match the provided example
     * @param size maximum size of the array
     * @param value Value to use to match each item
     * @param numberExamples number of examples to generate
     */
    /**
     * Array of values with a maximum size that are not objects where each item must match the provided example
     * @param size maximum size of the array
     * @param value Value to use to match each item
     */
    @JvmOverloads
    fun maxArrayLike(size: Int, value: PactDslJsonRootValue, numberExamples: Int = 1): PactDslJsonArray {
        require(numberExamples <= size) {
            String.format(
                "Number of example %d is more than the maximum size of %d",
                numberExamples, size
            )
        }
        matchers.addRule(rootPath + appendArrayIndex(1), matchMax(size))
        val parent = PactDslJsonArray(rootPath, "", this, true)
        parent.numberExamples = numberExamples
        parent.putObject(value)
        return parent.closeArray() as PactDslJsonArray
    }

    /**
     * List item that must include the provided string
     * @param value Value that must be included
     */
    fun includesStr(value: String): PactDslJsonArray {
        body.put(value)
        matchers.addRule(rootPath + appendArrayIndex(0), includesMatcher(value))
        return this
    }

    /**
     * Attribute that must be equal to the provided value.
     * @param value Value that will be used for comparisons
     */
    fun equalsTo(value: Any?): PactDslJsonArray {
        body.put(value)
        matchers.addRule(rootPath + appendArrayIndex(0), EqualsMatcher)
        return this
    }

    /**
     * Combine all the matchers using AND
     * @param value Attribute example value
     * @param rules Matching rules to apply
     */
    fun and(value: Any?, vararg rules: MatchingRule?): PactDslJsonArray {
        body.put(value ?: JSONObject.NULL)
        matchers.setRules(rootPath + appendArrayIndex(0), MatchingRuleGroup(Arrays.asList(*rules), RuleLogic.AND))
        return this
    }

    /**
     * Combine all the matchers using OR
     * @param value Attribute example value
     * @param rules Matching rules to apply
     */
    fun or(value: Any?, vararg rules: MatchingRule?): PactDslJsonArray {
        body.put(value ?: JSONObject.NULL)
        matchers.setRules(rootPath + appendArrayIndex(0), MatchingRuleGroup(Arrays.asList(*rules), RuleLogic.OR))
        return this
    }

    /**
     * Matches a URL that is composed of a base path and a sequence of path expressions
     * @param basePath The base path for the URL (like "http://localhost:8080/") which will be excluded from the matching
     * @param pathFragments Series of path fragments to match on. These can be strings or regular expressions.
     */
    /*public PactDslJsonArray matchUrl(String basePath, Object... pathFragments) {
    UrlMatcherSupport urlMatcher = new UrlMatcherSupport(basePath, Arrays.asList(pathFragments));
    body.put(urlMatcher.getExampleValue());
    matchers.addRule(rootPath + appendArrayIndex(0), regexp(urlMatcher.getRegexExpression()));
    return this;
  }*/
    companion object {
        private const val EXAMPLE = "Example \""
        /**
         * Array where each item must match the following example
         * @param numberExamples Number of examples to generate
         */
        /**
         * Array where each item must match the following example
         */
        @JvmOverloads
        fun arrayEachLike(numberExamples: Int = 1): PactDslJsonBody {
            val parent = PactDslJsonArray("", "", DslRootStub, true)
            parent.numberExamples = numberExamples
            parent.matchers.addRule("", parent.matchMin(0))
            return PactDslJsonBody(".", "", parent)
        }

        /**
         * Root level array where each item must match the provided matcher
         */
        fun arrayEachLike(rootValue: PactDslJsonRootValue): PactDslJsonArray {
            return arrayEachLike(1, rootValue)
        }

        /**
         * Root level array where each item must match the provided matcher
         * @param numberExamples Number of examples to generate
         */
        fun arrayEachLike(numberExamples: Int, value: PactDslJsonRootValue): PactDslJsonArray {
            val parent = PactDslJsonArray("", "", DslRootStub, true)
            parent.numberExamples = numberExamples
            parent.matchers.addRule("", parent.matchMin(0))
            parent.putObject(value)
            return parent
        }
        /**
         * Array with a minimum size where each item must match the following example
         * @param minSize minimum size
         * @param numberExamples Number of examples to generate
         */
        /**
         * Array with a minimum size where each item must match the following example
         * @param minSize minimum size
         */
        @JvmOverloads
        fun arrayMinLike(minSize: Int, numberExamples: Int = minSize): PactDslJsonBody {
            require(numberExamples >= minSize) {
                String.format(
                    "Number of example %d is less than the minimum size of %d",
                    numberExamples, minSize
                )
            }
            val parent = PactDslJsonArray("", "", DslRootStub, true)
            parent.numberExamples = numberExamples
            parent.matchers.addRule("", parent.matchMin(minSize))
            return PactDslJsonBody(".", "", parent)
        }

        /**
         * Root level array with minimum size where each item must match the provided matcher
         * @param minSize minimum size
         */
        fun arrayMinLike(minSize: Int, value: PactDslJsonRootValue): PactDslJsonArray {
            return arrayMinLike(minSize, minSize, value)
        }

        /**
         * Root level array with minimum size where each item must match the provided matcher
         * @param minSize minimum size
         * @param numberExamples Number of examples to generate
         */
        fun arrayMinLike(minSize: Int, numberExamples: Int, value: PactDslJsonRootValue): PactDslJsonArray {
            require(numberExamples >= minSize) {
                String.format(
                    "Number of example %d is less than the minimum size of %d",
                    numberExamples, minSize
                )
            }
            val parent = PactDslJsonArray("", "", DslRootStub, true)
            parent.numberExamples = numberExamples
            parent.matchers.addRule("", parent.matchMin(minSize))
            parent.putObject(value)
            return parent
        }
        /**
         * Array with a maximum size where each item must match the following example
         * @param maxSize maximum size
         * @param numberExamples Number of examples to generate
         */
        /**
         * Array with a maximum size where each item must match the following example
         * @param maxSize maximum size
         */
        @JvmOverloads
        fun arrayMaxLike(maxSize: Int, numberExamples: Int = 1): PactDslJsonBody {
            require(numberExamples <= maxSize) {
                String.format(
                    "Number of example %d is more than the maximum size of %d",
                    numberExamples, maxSize
                )
            }
            val parent = PactDslJsonArray("", "", DslRootStub, true)
            parent.numberExamples = numberExamples
            parent.matchers.addRule("", parent.matchMax(maxSize))
            return PactDslJsonBody(".", "", parent)
        }

        /**
         * Root level array with maximum size where each item must match the provided matcher
         * @param maxSize maximum size
         */
        fun arrayMaxLike(maxSize: Int, value: PactDslJsonRootValue): PactDslJsonArray {
            return arrayMaxLike(maxSize, 1, value)
        }

        /**
         * Root level array with maximum size where each item must match the provided matcher
         * @param maxSize maximum size
         * @param numberExamples Number of examples to generate
         */
        fun arrayMaxLike(maxSize: Int, numberExamples: Int, value: PactDslJsonRootValue): PactDslJsonArray {
            require(numberExamples <= maxSize) {
                String.format(
                    "Number of example %d is more than the maximum size of %d",
                    numberExamples, maxSize
                )
            }
            val parent = PactDslJsonArray("", "", DslRootStub, true)
            parent.numberExamples = numberExamples
            parent.matchers.addRule("", parent.matchMax(maxSize))
            parent.putObject(value)
            return parent
        }
    }

    init {
        body = JSONArray()
    }
}