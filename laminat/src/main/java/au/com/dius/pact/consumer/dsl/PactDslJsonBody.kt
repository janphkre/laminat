package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.InvalidMatcherException
import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.generators.DateGenerator
import au.com.dius.pact.model.generators.DateTimeGenerator
import au.com.dius.pact.model.generators.RandomDecimalGenerator
import au.com.dius.pact.model.generators.RandomHexadecimalGenerator
import au.com.dius.pact.model.generators.RandomIntGenerator
import au.com.dius.pact.model.generators.RandomStringGenerator
import au.com.dius.pact.model.generators.RegexGenerator
import au.com.dius.pact.model.generators.TimeGenerator
import au.com.dius.pact.model.generators.UuidGenerator
import au.com.dius.pact.model.matchingrules.EqualsMatcher
import au.com.dius.pact.model.matchingrules.MatchingRule
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.NumberTypeMatcher
import au.com.dius.pact.model.matchingrules.RuleLogic
import au.com.dius.pact.model.matchingrules.TypeMatcher
import com.mifmif.common.regex.Generex
import io.gatling.jsonpath.`Parser$`
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Arrays
import java.util.Calendar
import java.util.UUID
import java.util.regex.Pattern

/**
 * DSL to define a JSON Object
 */
class PactDslJsonBody(rootPath: String = ".", rootName: String = "", parent: DslPart? = null) : DslPart(parent, rootPath, rootName) {

    private val EXAMPLE = "Example \""
    private var body = JSONObject()

    override fun putObject(`object`: DslPart) {
        for (matcherName in `object`.matchers.matchingRules.keys) {
            matchers.setRules(matcherName, `object`.matchers.matchingRules[matcherName]!!)
        }
        generators.addGenerators(`object`.generators)
        val elementBase = StringUtils.difference(this.rootPath, `object`.rootPath)
        if (StringUtils.isNotEmpty(`object`.rootName)) {
            body.put(`object`.rootName, `object`.body)
        } else {
            val name = StringUtils.strip(elementBase, ".")
            val p = Pattern.compile("\\['(.+)'\\]")
            val matcher = p.matcher(name)
            if (matcher.matches()) {
                body.put(matcher.group(1), `object`.body)
            } else {
                body.put(name, `object`.body)
            }
        }
    }

    override fun putArray(`object`: DslPart) {
        for (matcherName in `object`.matchers.matchingRules.keys) {
            matchers.setRules(matcherName, `object`.matchers.matchingRules[matcherName]!!)
        }
        generators.addGenerators(`object`.generators)
        if (StringUtils.isNotEmpty(`object`.rootName)) {
            body.put(`object`.rootName, `object`.body)
        } else {
            body.put(StringUtils.difference(this.rootPath, `object`.rootPath), `object`.body)
        }
    }

    override fun getBody(): Any {
        return body
    }

    /**
     * Attribute that must be the specified value
     * @param name attribute name
     * @param value string value
     */
    fun stringValue(name: String, value: String?): PactDslJsonBody {
        if (value == null) {
            body.put(name, JSONObject.NULL)
        } else {
            body.put(name, value)
        }
        return this
    }

    /**
     * Attribute that must be the specified number
     * @param name attribute name
     * @param value number value
     */
    fun numberValue(name: String, value: Number): PactDslJsonBody {
        body.put(name, value)
        return this
    }

    /**
     * Attribute that must be the specified boolean
     * @param name attribute name
     * @param value boolean value
     */
    fun booleanValue(name: String, value: Boolean?): PactDslJsonBody {
        body.put(name, value!!)
        return this
    }

    /**
     * Attribute that can be any string
     * @param name attribute name
     */
    fun stringType(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RandomStringGenerator(20))
        return stringType(name, "string")
    }

    /**
     * Attributes that can be any string
     * @param names attribute names
     */
    fun stringType(vararg names: String): PactDslJsonBody {
        for (name in names) {
            stringType(name)
        }
        return this
    }

    /**
     * Attribute that can be any string
     * @param name attribute name
     * @param example example value to use for generated bodies
     */
    fun stringType(name: String, example: String): PactDslJsonBody {
        body.put(name, example)
        matchers.addRule(matcherKey(name), TypeMatcher)
        return this
    }

    private fun matcherKey(name: String): String {
        var key = rootPath + name
        if (name != "*" && !name.matches(`Parser$`.`MODULE$`.FieldRegex().toString().toRegex())) {
            key = StringUtils.stripEnd(rootPath, ".") + "['" + name + "']"
        }
        return key
    }

    /**
     * Attribute that can be any number
     * @param name attribute name
     */
    fun numberType(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RandomIntGenerator(0, Integer.MAX_VALUE))
        return numberType(name, 100)
    }

    /**
     * Attributes that can be any number
     * @param names attribute names
     */
    fun numberType(vararg names: String): PactDslJsonBody {
        for (name in names) {
            numberType(name)
        }
        return this
    }

    /**
     * Attribute that can be any number
     * @param name attribute name
     * @param number example number to use for generated bodies
     */
    fun numberType(name: String, number: Number): PactDslJsonBody {
        body.put(name, number)
        matchers.addRule(matcherKey(name), NumberTypeMatcher(NumberTypeMatcher.NumberType.NUMBER))
        return this
    }

    /**
     * Attribute that must be an integer
     * @param name attribute name
     */
    fun integerType(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RandomIntGenerator(0, Integer.MAX_VALUE))
        return integerType(name, 100 as Int)
    }

    /**
     * Attributes that must be an integer
     * @param names attribute names
     */
    fun integerType(vararg names: String): PactDslJsonBody {
        for (name in names) {
            integerType(name)
        }
        return this
    }

    /**
     * Attribute that must be an integer
     * @param name attribute name
     * @param number example integer value to use for generated bodies
     */
    fun integerType(name: String, number: Long?): PactDslJsonBody {
        body.put(name, number!!)
        matchers.addRule(matcherKey(name), NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
        return this
    }

    /**
     * Attribute that must be an integer
     * @param name attribute name
     * @param number example integer value to use for generated bodies
     */
    fun integerType(name: String, number: Int?): PactDslJsonBody {
        body.put(name, number!!)
        matchers.addRule(matcherKey(name), NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
        return this
    }

    /**
     * Attribute that must be a real value
     * @param name attribute name
     */
    @Deprecated("Use decimal instead")
    fun realType(name: String): PactDslJsonBody {
        return decimalType(name)
    }

    /**
     * Attribute that must be a real value
     * @param name attribute name
     * @param number example real value
     */
    @Deprecated("Use decimal instead")
    fun realType(name: String, number: Double?): PactDslJsonBody {
        return decimalType(name, number)
    }

    /**
     * Attribute that must be a decimal value
     * @param name attribute name
     */
    fun decimalType(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RandomDecimalGenerator(10))
        return decimalType(name, 100.0)
    }

    /**
     * Attributes that must be a decimal values
     * @param names attribute names
     */
    fun decimalType(vararg names: String): PactDslJsonBody {
        for (name in names) {
            decimalType(name)
        }
        return this
    }

    /**
     * Attribute that must be a decimalType value
     * @param name attribute name
     * @param number example decimalType value
     */
    fun decimalType(name: String, number: BigDecimal): PactDslJsonBody {
        body.put(name, number)
        matchers.addRule(matcherKey(name), NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
        return this
    }

    /**
     * Attribute that must be a decimalType value
     * @param name attribute name
     * @param number example decimalType value
     */
    fun decimalType(name: String, number: Double?): PactDslJsonBody {
        body.put(name, number!!)
        matchers.addRule(matcherKey(name), NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
        return this
    }

    /**
     * Attribute that must be a boolean
     * @param name attribute name
     */
    fun booleanType(name: String): PactDslJsonBody {
        return booleanType(name, true)
    }

    /**
     * Attributes that must be a boolean
     * @param names attribute names
     */
    fun booleanType(vararg names: String): PactDslJsonBody {
        for (name in names) {
            booleanType(name)
        }
        return this
    }

    /**
     * Attribute that must be a boolean
     * @param name attribute name
     * @param example example boolean to use for generated bodies
     */
    fun booleanType(name: String, example: Boolean?): PactDslJsonBody {
        body.put(name, example!!)
        matchers.addRule(matcherKey(name), TypeMatcher)
        return this
    }

    /**
     * Attribute that must match the regular expression
     * @param name attribute name
     * @param regex regular expression
     * @param value example value to use for generated bodies
     */
    fun stringMatcher(name: String, regex: String, value: String): PactDslJsonBody {
        if (!value.matches(regex.toRegex())) {
            throw InvalidMatcherException(
                EXAMPLE + value + "\" does not match regular expression \"" +
                        regex + "\""
            )
        }
        body.put(name, value)
        matchers.addRule(matcherKey(name), regexp(regex))
        return this
    }

    /**
     * Attribute that must match the regular expression
     * @param name attribute name
     * @param regex regular expression
     */
    @Deprecated("Use the version that takes an example value")
    fun stringMatcher(name: String, regex: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RegexGenerator(regex))
        stringMatcher(name, regex, Generex(regex).random())
        return this
    }

    /**
     * Attribute named 'timestamp' that must be an ISO formatted timestamp
     */
    fun timestamp(): PactDslJsonBody {
        return timestamp("timestamp")
    }

    /**
     * Attribute that must be an ISO formatted timestamp
     * @param name
     */
    fun timestamp(name: String): PactDslJsonBody {
        val pattern = DateFormatUtils.ISO_DATETIME_FORMAT.pattern
        generators.addGenerator(Category.BODY, matcherKey(name), DateTimeGenerator(pattern))
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DslPart.DATE_2000
        body.put(name, DateFormatUtils.ISO_DATETIME_FORMAT.format(calendar))
        matchers.addRule(matcherKey(name), matchTimestamp(pattern))
        return this
    }

    /**
     * Attribute that must match the given timestamp format
     * @param name attribute name
     * @param format timestamp format
     */
    fun timestamp(name: String, format: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), DateTimeGenerator(format))
        val instance = FastDateFormat.getInstance(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DslPart.DATE_2000
        body.put(name, instance.format(calendar))
        matchers.addRule(matcherKey(name), matchTimestamp(format))
        return this
    }

    /**
     * Attribute that must match the given timestamp format
     * @param name attribute name
     * @param format timestamp format
     * @param example example date and time to use for generated bodies
     */
    fun timestamp(name: String, format: String, example: Calendar): PactDslJsonBody {
        val instance = FastDateFormat.getInstance(format)
        body.put(name, instance.format(example))
        matchers.addRule(matcherKey(name), matchTimestamp(format))
        return this
    }

    /**
     * Attribute named 'date' that must be formatted as an ISO date
     */
    fun date(): PactDslJsonBody {
        return date("date")
    }

    /**
     * Attribute that must be formatted as an ISO date
     * @param name attribute name
     */
    fun date(name: String): PactDslJsonBody {
        val pattern = DateFormatUtils.ISO_DATE_FORMAT.pattern
        generators.addGenerator(Category.BODY, matcherKey(name), DateGenerator(pattern))
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DslPart.DATE_2000
        body.put(name, DateFormatUtils.ISO_DATE_FORMAT.format(calendar))
        matchers.addRule(matcherKey(name), matchDate(pattern))
        return this
    }

    /**
     * Attribute that must match the provided date format
     * @param name attribute date
     * @param format date format to match
     */
    fun date(name: String, format: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), DateGenerator(format))
        val instance = FastDateFormat.getInstance(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DslPart.DATE_2000
        body.put(name, instance.format(calendar))
        matchers.addRule(matcherKey(name), matchDate(format))
        return this
    }

    /**
     * Attribute that must match the provided date format
     * @param name attribute date
     * @param format date format to match
     * @param example example date to use for generated values
     */
    fun date(name: String, format: String, example: Calendar): PactDslJsonBody {
        val instance = FastDateFormat.getInstance(format)
        body.put(name, instance.format(example))
        matchers.addRule(matcherKey(name), matchDate(format))
        return this
    }

    /**
     * Attribute named 'time' that must be an ISO formatted time
     */
    fun time(): PactDslJsonBody {
        return time("time")
    }

    /**
     * Attribute that must be an ISO formatted time
     * @param name attribute name
     */
    fun time(name: String): PactDslJsonBody {
        val pattern = DateFormatUtils.ISO_TIME_FORMAT.pattern
        generators.addGenerator(Category.BODY, matcherKey(name), TimeGenerator(pattern))
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DslPart.DATE_2000
        body.put(name, DateFormatUtils.ISO_TIME_FORMAT.format(calendar))
        matchers.addRule(matcherKey(name), matchTime(pattern))
        return this
    }

    /**
     * Attribute that must match the given time format
     * @param name attribute name
     * @param format time format to match
     */
    fun time(name: String, format: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), TimeGenerator(format))
        val instance = FastDateFormat.getInstance(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = DslPart.DATE_2000
        body.put(name, instance.format(calendar))
        matchers.addRule(matcherKey(name), matchTime(format))
        return this
    }

    /**
     * Attribute that must match the given time format
     * @param name attribute name
     * @param format time format to match
     * @param example example time to use for generated bodies
     */
    fun time(name: String, format: String, example: Calendar): PactDslJsonBody {
        val instance = FastDateFormat.getInstance(format)
        body.put(name, instance.format(example))
        matchers.addRule(matcherKey(name), matchTime(format))
        return this
    }

    /**
     * Attribute that must be an IP4 address
     * @param name attribute name
     */
    fun ipAddress(name: String): PactDslJsonBody {
        body.put(name, "127.0.0.1")
        matchers.addRule(matcherKey(name), regexp("(\\d{1,3}\\.)+\\d{1,3}"))
        return this
    }

    /**
     * Attribute that is a JSON object
     * @param name field name
     */
    override fun `object`(name: String): PactDslJsonBody {
        var base = rootPath + name
        if (!name.matches(`Parser$`.`MODULE$`.FieldRegex().toString().toRegex())) {
            base = StringUtils.substringBeforeLast(rootPath, ".") + "['" + name + "']"
        }
        return PactDslJsonBody("$base.", "", this)
    }

    override fun `object`(): PactDslJsonBody {
        throw UnsupportedOperationException("use the object(String name) form")
    }

    /**
     * Closes the current JSON object
     */
    override fun closeObject(): DslPart? {
        parent?.putObject(this)
        closed = true
        return parent
    }

    override fun close(): DslPart {
        var parentToReturn: DslPart = this
        if (!closed) {
            var parent = closeObject()
            while (parent != null) {
                parentToReturn = parent
                if (parent is PactDslJsonArray) {
                    parent = parent.closeArray()
                } else {
                    parent = parent.closeObject()
                }
            }
        }

        parentToReturn.matchers.applyMatcherRootPrefix("$")
        parentToReturn.generators.applyRootPrefix("$")

        return parentToReturn
    }

    /**
     * Attribute that is an array
     * @param name field name
     */
    override fun array(name: String): PactDslJsonArray {
        return PactDslJsonArray(matcherKey(name), name, this)
    }

    override fun array(): PactDslJsonArray {
        throw UnsupportedOperationException("use the array(String name) form")
    }

    /**
     * Closes the current array
     */
    override fun closeArray(): DslPart {
        if (parent is PactDslJsonArray) {
            closeObject()
            return parent.closeArray()
        } else {
            throw UnsupportedOperationException("can't call closeArray on an Object")
        }
    }

    /**
     * Attribute that is an array where each item must match the following example
     * @param name field name
     */
    @Deprecated("use eachLike")
    override fun arrayLike(name: String): PactDslJsonBody {
        matchers.addRule(matcherKey(name), TypeMatcher)
        return PactDslJsonBody(".", ".", PactDslJsonArray(matcherKey(name), "", this, true))
    }

    @Deprecated("")
    override fun arrayLike(): PactDslJsonBody {
        throw UnsupportedOperationException("use the arrayLike(String name) form")
    }

    /**
     * Attribute that is an array where each item must match the following example
     * @param name field name
     */
    override fun eachLike(name: String): PactDslJsonBody {
        return eachLike(name, 1)
    }

    override fun eachLike(): PactDslJsonBody {
        throw UnsupportedOperationException("use the eachLike(String name) form")
    }

    /**
     * Attribute that is an array where each item must match the following example
     * @param name field name
     * @param numberExamples number of examples to generate
     */
    override fun eachLike(name: String, numberExamples: Int): PactDslJsonBody {
        matchers.addRule(matcherKey(name), matchMin(0))
        val parent = PactDslJsonArray(matcherKey(name), "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonBody(".", ".", parent)
    }

    override fun eachLike(numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the eachLike(String name, int numberExamples) form")
    }

    /**
     * Attribute that is an array of values that are not objects where each item must match the following example
     * @param name field name
     * @param value Value to use to match each item
     */
    fun eachLike(name: String, value: PactDslJsonRootValue): PactDslJsonBody {
        return eachLike(name, value, 1)
    }

    /**
     * Attribute that is an array of values that are not objects where each item must match the following example
     * @param name field name
     * @param value Value to use to match each item
     * @param numberExamples number of examples to generate
     */
    fun eachLike(name: String, value: PactDslJsonRootValue, numberExamples: Int): PactDslJsonBody {
        matchers.addRule(matcherKey(name), matchMin(0))
        val parent = PactDslJsonArray(matcherKey(name), "", this, true)
        parent.numberExamples = numberExamples
        parent.putObject(value)
        return parent.closeArray() as PactDslJsonBody
    }

    /**
     * Attribute that is an array with a minimum size where each item must match the following example
     * @param name field name
     * @param size minimum size of the array
     */
    override fun minArrayLike(name: String, size: Int): PactDslJsonBody {
        return minArrayLike(name, size, size)
    }

    override fun minArrayLike(size: Int?): PactDslJsonBody {
        throw UnsupportedOperationException("use the minArrayLike(String name, Integer size) form")
    }

    /**
     * Attribute that is an array with a minimum size where each item must match the following example
     * @param name field name
     * @param size minimum size of the array
     * @param numberExamples number of examples to generate
     */
    override fun minArrayLike(name: String, size: Int, numberExamples: Int): PactDslJsonBody {
        if (numberExamples < size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of example %d is less than the minimum size of %d",
                    numberExamples, size
                )
            )
        }
        matchers.addRule(matcherKey(name), matchMin(size))
        val parent = PactDslJsonArray(matcherKey(name), "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonBody(".", "", parent)
    }

    override fun minArrayLike(size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the minArrayLike(String name, Integer size, int numberExamples) form")
    }

    /**
     * Attribute that is an array of values with a minimum size that are not objects where each item must match the following example
     * @param name field name
     * @param size minimum size of the array
     * @param value Value to use to match each item
     */
    fun minArrayLike(name: String, size: Int, value: PactDslJsonRootValue): PactDslJsonBody {
        return minArrayLike(name, size, value, 2)
    }

    /**
     * Attribute that is an array of values with a minimum size that are not objects where each item must match the following example
     * @param name field name
     * @param size minimum size of the array
     * @param value Value to use to match each item
     * @param numberExamples number of examples to generate
     */
    fun minArrayLike(name: String, size: Int, value: PactDslJsonRootValue, numberExamples: Int): PactDslJsonBody {
        if (numberExamples < size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of example %d is less than the minimum size of %d",
                    numberExamples, size
                )
            )
        }
        matchers.addRule(matcherKey(name), matchMin(size))
        val parent = PactDslJsonArray(matcherKey(name), "", this, true)
        parent.numberExamples = numberExamples
        parent.putObject(value)
        return parent.closeArray() as PactDslJsonBody
    }

    /**
     * Attribute that is an array with a maximum size where each item must match the following example
     * @param name field name
     * @param size maximum size of the array
     */
    override fun maxArrayLike(name: String, size: Int): PactDslJsonBody {
        return maxArrayLike(name, size, 1)
    }

    override fun maxArrayLike(size: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the maxArrayLike(String name, Integer size) form")
    }

    /**
     * Attribute that is an array with a maximum size where each item must match the following example
     * @param name field name
     * @param size maximum size of the array
     * @param numberExamples number of examples to generate
     */
    override fun maxArrayLike(name: String, size: Int, numberExamples: Int): PactDslJsonBody {
        if (numberExamples > size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of example %d is more than the maximum size of %d",
                    numberExamples, size
                )
            )
        }
        matchers.addRule(matcherKey(name), matchMax(size))
        val parent = PactDslJsonArray(matcherKey(name), "", this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonBody(".", "", parent)
    }

    override fun maxArrayLike(size: Int, numberExamples: Int): PactDslJsonBody {
        throw UnsupportedOperationException("use the maxArrayLike(String name, Integer size, int numberExamples) form")
    }

    /**
     * Attribute that is an array of values with a maximum size that are not objects where each item must match the following example
     * @param name field name
     * @param size maximum size of the array
     * @param value Value to use to match each item
     */
    fun maxArrayLike(name: String, size: Int, value: PactDslJsonRootValue): PactDslJsonBody {
        return maxArrayLike(name, size, value, 1)
    }

    /**
     * Attribute that is an array of values with a maximum size that are not objects where each item must match the following example
     * @param name field name
     * @param size maximum size of the array
     * @param value Value to use to match each item
     * @param numberExamples number of examples to generate
     */
    fun maxArrayLike(name: String, size: Int, value: PactDslJsonRootValue, numberExamples: Int): PactDslJsonBody {
        if (numberExamples > size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of example %d is more than the maximum size of %d",
                    numberExamples, size
                )
            )
        }
        matchers.addRule(matcherKey(name), matchMax(size))
        val parent = PactDslJsonArray(matcherKey(name), "", this, true)
        parent.numberExamples = numberExamples
        parent.putObject(value)
        return parent.closeArray() as PactDslJsonBody
    }

    /**
     * Attribute named 'id' that must be a numeric identifier
     */
    fun id(): PactDslJsonBody {
        return id("id")
    }

    /**
     * Attribute that must be a numeric identifier
     * @param name attribute name
     */
    fun id(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RandomIntGenerator(0, Integer.MAX_VALUE))
        body.put(name, 1234567890L)
        matchers.addRule(matcherKey(name), TypeMatcher)
        return this
    }

    /**
     * Attribute that must be a numeric identifier
     * @param name attribute name
     * @param id example id to use for generated bodies
     */
    fun id(name: String, id: Long?): PactDslJsonBody {
        body.put(name, id!!)
        matchers.addRule(matcherKey(name), TypeMatcher)
        return this
    }

    /**
     * Attribute that must be encoded as a hexadecimal value
     * @param name attribute name
     */
    fun hexValue(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), RandomHexadecimalGenerator(10))
        return hexValue(name, "1234a")
    }

    /**
     * Attribute that must be encoded as a hexadecimal value
     * @param name attribute name
     * @param hexValue example value to use for generated bodies
     */
    fun hexValue(name: String, hexValue: String): PactDslJsonBody {
        if (!hexValue.matches(DslPart.HEXADECIMAL.toRegex())) {
            throw InvalidMatcherException("$EXAMPLE$hexValue\" is not a hexadecimal value")
        }
        body.put(name, hexValue)
        matchers.addRule(matcherKey(name), regexp("[0-9a-fA-F]+"))
        return this
    }

    /**
     * Attribute that must be encoded as a GUID
     * @param name attribute name
     */
    @Deprecated("use uuid instead")
    fun guid(name: String): PactDslJsonBody {
        return uuid(name)
    }

    /**
     * Attribute that must be encoded as a GUID
     * @param name attribute name
     * @param uuid example UUID to use for generated bodies
     */
    @Deprecated("use uuid instead")
    fun guid(name: String, uuid: UUID): PactDslJsonBody {
        return uuid(name, uuid)
    }

    /**
     * Attribute that must be encoded as a GUID
     * @param name attribute name
     * @param uuid example UUID to use for generated bodies
     */
    @Deprecated("use uuid instead")
    fun guid(name: String, uuid: String): PactDslJsonBody {
        return uuid(name, uuid)
    }

    /**
     * Attribute that must be encoded as an UUID
     * @param name attribute name
     */
    fun uuid(name: String): PactDslJsonBody {
        generators.addGenerator(Category.BODY, matcherKey(name), UuidGenerator())
        return uuid(name, "e2490de5-5bd3-43d5-b7c4-526e33f71304")
    }

    /**
     * Attribute that must be encoded as an UUID
     * @param name attribute name
     * @param uuid example UUID to use for generated bodies
     */
    fun uuid(name: String, uuid: UUID): PactDslJsonBody {
        return uuid(name, uuid.toString())
    }

    /**
     * Attribute that must be encoded as an UUID
     * @param name attribute name
     * @param uuid example UUID to use for generated bodies
     */
    fun uuid(name: String, uuid: String): PactDslJsonBody {
        if (!uuid.matches(DslPart.UUID_REGEX.toRegex())) {
            throw InvalidMatcherException("$EXAMPLE$uuid\" is not an UUID")
        }
        body.put(name, uuid)
        matchers.addRule(matcherKey(name), regexp(DslPart.UUID_REGEX))
        return this
    }

    /**
     * Sets the field to a null value
     * @param fieldName field name
     */
    fun nullValue(fieldName: String): PactDslJsonBody {
        body.put(fieldName, JSONObject.NULL)
        return this
    }

    override fun eachArrayLike(name: String): PactDslJsonArray {
        return eachArrayLike(name, 1)
    }

    override fun eachArrayLike(): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayLike(String name) form")
    }

    override fun eachArrayLike(name: String, numberExamples: Int): PactDslJsonArray {
        matchers.addRule(matcherKey(name), matchMin(0))
        val parent = PactDslJsonArray(matcherKey(name), name, this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonArray("", "", parent)
    }

    override fun eachArrayLike(numberExamples: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayLike(String name, int numberExamples) form")
    }

    override fun eachArrayWithMaxLike(name: String, size: Int): PactDslJsonArray {
        return eachArrayWithMaxLike(name, 1, size)
    }

    override fun eachArrayWithMaxLike(size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMaxLike(String name, Integer size) form")
    }

    override fun eachArrayWithMaxLike(name: String, numberExamples: Int, size: Int): PactDslJsonArray {
        if (numberExamples > size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of example %d is more than the maximum size of %d",
                    numberExamples, size
                )
            )
        }
        matchers.addRule(matcherKey(name), matchMax(size))
        val parent = PactDslJsonArray(matcherKey(name), name, this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonArray("", "", parent)
    }

    override fun eachArrayWithMaxLike(numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMaxLike(String name, int numberExamples, Integer size) form")
    }

    override fun eachArrayWithMinLike(name: String, size: Int): PactDslJsonArray {
        return eachArrayWithMinLike(name, size, size)
    }

    override fun eachArrayWithMinLike(size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMinLike(String name, Integer size) form")
    }

    override fun eachArrayWithMinLike(name: String, numberExamples: Int, size: Int): PactDslJsonArray {
        if (numberExamples < size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of example %d is less than the minimum size of %d",
                    numberExamples, size
                )
            )
        }
        matchers.addRule(matcherKey(name), matchMin(size))
        val parent = PactDslJsonArray(matcherKey(name), name, this, true)
        parent.numberExamples = numberExamples
        return PactDslJsonArray("", "", parent)
    }

    override fun eachArrayWithMinLike(numberExamples: Int, size: Int): PactDslJsonArray {
        throw UnsupportedOperationException("use the eachArrayWithMinLike(String name, int numberExamples, Integer size) form")
    }

    /**
     * Accepts any key, and each key is mapped to a list of items that must match the following object definition
     * @param exampleKey Example key to use for generating bodies
     */
    fun eachKeyMappedToAnArrayLike(exampleKey: String): PactDslJsonBody {
        matchers.addRule(".*", matchMin(0))
        val parent = PactDslJsonArray(".*", exampleKey, this, true)
        return PactDslJsonBody(".", "", parent)
    }

    /**
     * Accepts any key, and each key is mapped to a map that must match the following object definition
     * @param exampleKey Example key to use for generating bodies
     */
    fun eachKeyLike(exampleKey: String): PactDslJsonBody {
        matchers.addRule("$rootPath*", TypeMatcher)
        return PactDslJsonBody("$rootPath*.", exampleKey, this)
    }

    /**
     * Accepts any key, and each key is mapped to a map that must match the provided object definition
     * @param exampleKey Example key to use for generating bodies
     * @param value Value to use for matching and generated bodies
     */
    fun eachKeyLike(exampleKey: String, value: PactDslJsonRootValue): PactDslJsonBody {
        body.put(exampleKey, value.body)
        for (matcherName in value.matchers.matchingRules.keys) {
            matchers.addRules("$rootPath*$matcherName", value.matchers.matchingRules[matcherName]!!.rules)
        }
        return this
    }

    /**
     * Attribute that must include the provided string value
     * @param name attribute name
     * @param value Value that must be included
     */
    fun includesStr(name: String, value: String): PactDslJsonBody {
        body.put(name, value)
        matchers.addRule(matcherKey(name), includesMatcher(value))
        return this
    }

    /**
     * Attribute that must be equal to the provided value.
     * @param name attribute name
     * @param value Value that will be used for comparisons
     */
    fun equalTo(name: String, value: Any): PactDslJsonBody {
        body.put(name, value)
        matchers.addRule(matcherKey(name), EqualsMatcher)
        return this
    }

    /**
     * Combine all the matchers using AND
     * @param name Attribute name
     * @param value Attribute example value
     * @param rules Matching rules to apply
     */
    fun and(name: String, value: Any?, vararg rules: MatchingRule): PactDslJsonBody {
        if (value != null) {
            body.put(name, value)
        } else {
            body.put(name, JSONObject.NULL)
        }
        matchers.setRules(matcherKey(name), MatchingRuleGroup(Arrays.asList(*rules), RuleLogic.AND))
        return this
    }

    /**
     * Combine all the matchers using OR
     * @param name Attribute name
     * @param value Attribute example value
     * @param rules Matching rules to apply
     */
    fun or(name: String, value: Any?, vararg rules: MatchingRule): PactDslJsonBody {
        if (value != null) {
            body.put(name, value)
        } else {
            body.put(name, JSONObject.NULL)
        }
        matchers.setRules(matcherKey(name), MatchingRuleGroup(Arrays.asList(*rules), RuleLogic.OR))
        return this
    }

    /**
     * Matches a URL that is composed of a base path and a sequence of path expressions
     * @param name Attribute name
     * @param basePath The base path for the URL (like "http://localhost:8080/") which will be excluded from the matching
     * @param pathFragments Series of path fragments to match on. These can be strings or regular expressions.
     */
    /*public PactDslJsonBody matchUrl(String name, String basePath, Object... pathFragments) {
      UrlMatcherSupport urlMatcher = new UrlMatcherSupport(basePath, Arrays.asList(pathFragments));
      body.put(name, urlMatcher.getExampleValue());
      matchers.addRule(matcherKey(name), regexp(urlMatcher.getRegexExpression()));
      return this;
    }*/
}