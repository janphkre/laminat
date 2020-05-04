package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.InvalidMatcherException
import au.com.dius.pact.model.generators.*
import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.matchingrules.*
import com.google.gson.Gson
import com.mifmif.common.regex.Generex
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

class PactDslJsonRootValue : DslPart("", "") {
    private var value: Any? = null
    /**
     * If the value should be encoded to be safe as JSON
     */
    /**
     * If the value should be encoded to be safe as JSON
     */
    var isEncodeJson = false
    override fun putObject(`object`: DslPart?) {
        throw UnsupportedOperationException()
    }

    protected override fun putArray(`object`: DslPart?) {
        throw UnsupportedOperationException()
    }

    override val body: Any?
        get() = if (isEncodeJson) {
            Gson().toJson(value)
        } else value

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun array(name: String?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun array(): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun closeArray(): DslPart? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun arrayLike(name: String?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun arrayLike(): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachLike(name: String?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachLike(numberExamples: Int): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachLike(name: String?, numberExamples: Int): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachLike(): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun minArrayLike(name: String?, size: Int?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun minArrayLike(size: Int?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun minArrayLike(name: String?, size: Int?, numberExamples: Int): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun minArrayLike(size: Int?, numberExamples: Int): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun maxArrayLike(name: String?, size: Int?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun maxArrayLike(size: Int?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun maxArrayLike(name: String?, size: Int?, numberExamples: Int): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun maxArrayLike(size: Int?, numberExamples: Int): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonBody for objects")
    override fun `object`(name: String?): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_BODY_FOR_OBJECTS)
    }

    @Deprecated("Use PactDslJsonBody for objects")
    override fun `object`(): PactDslJsonBody? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_BODY_FOR_OBJECTS)
    }

    @Deprecated("Use PactDslJsonBody for objects")
    override fun closeObject(): DslPart? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_BODY_FOR_OBJECTS)
    }

    override fun close(): DslPart? {
        matchers.applyMatcherRootPrefix("$")
        generators.applyRootPrefix("$")
        return this
    }

    fun setValue(value: Any?) {
        this.value = value
    }

    fun setMatcher(matcher: MatchingRule?) {
        matchers.addRule(matcher!!)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayLike(name: String?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayLike(numberExamples: Int): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMaxLike(name: String?, size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMaxLike(size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMaxLike(name: String?, numberExamples: Int, size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMaxLike(numberExamples: Int, size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMinLike(name: String?, size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMinLike(size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMinLike(name: String?, numberExamples: Int, size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayWithMinLike(numberExamples: Int, size: Int?): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayLike(name: String?, numberExamples: Int): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    @Deprecated("Use PactDslJsonArray for arrays")
    override fun eachArrayLike(): PactDslJsonArray? {
        throw UnsupportedOperationException(USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS)
    }

    companion object {
        private const val USE_PACT_DSL_JSON_ARRAY_FOR_ARRAYS = "Use PactDslJsonArray for arrays"
        private const val USE_PACT_DSL_JSON_BODY_FOR_OBJECTS = "Use PactDslJsonBody for objects"
        private const val EXAMPLE = "Example \""

        /**
         * Value that can be any string
         */
        fun stringType(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", RandomStringGenerator(20))
            value.setValue("string")
            value.setMatcher(TypeMatcher)
            return value
        }

        /**
         * Value that can be any string
         *
         * @param example example value to use for generated bodies
         */
        fun stringType(example: String?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(example)
            value.setMatcher(TypeMatcher)
            return value
        }

        /**
         * Value that can be any number
         */
        fun numberType(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", RandomIntGenerator(0, Int.MAX_VALUE))
            value.setValue(100)
            value.setMatcher(TypeMatcher)
            return value
        }

        /**
         * Value that can be any number
         * @param number example number to use for generated bodies
         */
        fun numberType(number: Number?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(number)
            value.setMatcher(TypeMatcher)
            return value
        }

        /**
         * Value that must be an integer
         */
        fun integerType(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", RandomIntGenerator(0, Int.MAX_VALUE))
            value.setValue(100)
            value.setMatcher(NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
            return value
        }

        /**
         * Value that must be an integer
         * @param number example integer value to use for generated bodies
         */
        fun integerType(number: Long?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(number)
            value.setMatcher(NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
            return value
        }

        /**
         * Value that must be an integer
         * @param number example integer value to use for generated bodies
         */
        fun integerType(number: Int?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(number)
            value.setMatcher(NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
            return value
        }

        /**
         * Value that must be a decimal value
         */
        fun decimalType(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", RandomDecimalGenerator(10))
            value.setValue(100)
            value.setMatcher(NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
            return value
        }

        /**
         * Value that must be a decimalType value
         * @param number example decimalType value
         */
        fun decimalType(number: BigDecimal?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(number)
            value.setMatcher(NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
            return value
        }

        /**
         * Value that must be a decimalType value
         * @param number example decimalType value
         */
        fun decimalType(number: Double?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(number)
            value.setMatcher(NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
            return value
        }
        /**
         * Value that must be a boolean
         * @param example example boolean to use for generated bodies
         */
        /**
         * Value that must be a boolean
         */
        @JvmOverloads
        fun booleanType(example: Boolean? = true): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue(example)
            value.setMatcher(TypeMatcher)
            return value
        }

        /**
         * Value that must match the regular expression
         * @param regex regular expression
         * @param value example value to use for generated bodies
         */
        fun stringMatcher(regex: String, value: String): PactDslJsonRootValue {
            if (!value.matches(regex)) {
                throw InvalidMatcherException(
                    EXAMPLE + value + "\" does not match regular expression \"" +
                        regex + "\""
                )
            }
            val rootValue = PactDslJsonRootValue()
            rootValue.setValue(value)
            rootValue.setMatcher(rootValue.regexp(regex))
            return rootValue
        }

        /**
         * Value that must match the regular expression
         * @param regex regular expression
         */
        @Deprecated("Use the version that takes an example value")
        fun stringMatcher(regex: String?): PactDslJsonRootValue {
            val rootValue = PactDslJsonRootValue()
            rootValue.generators.addGenerator(Category.BODY, "", RegexGenerator(regex!!))
            rootValue.setValue(Generex(regex).random())
            rootValue.setMatcher(rootValue.regexp(regex))
            return rootValue
        }
        /**
         * Value that must match the given timestamp format
         * @param format timestamp format
         */
        /**
         * Value that must be an ISO formatted timestamp
         */
        @JvmOverloads
        fun timestamp(format: String? = DateFormatUtils.ISO_DATETIME_FORMAT.pattern): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", DateTimeGenerator(format))
            val instance = FastDateFormat.getInstance(format)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = DATE_2000
            value.setValue(instance.format(calendar))
            value.setMatcher(value.matchTimestamp(format))
            return value
        }

        /**
         * Value that must match the given timestamp format
         * @param format timestamp format
         * @param example example date and time to use for generated bodies
         */
        fun timestamp(format: String?, example: Calendar?): PactDslJsonRootValue {
            val instance = FastDateFormat.getInstance(format)
            val value = PactDslJsonRootValue()
            value.setValue(instance.format(example))
            value.setMatcher(value.matchTimestamp(format))
            return value
        }
        /**
         * Value that must match the provided date format
         * @param format date format to match
         */
        /**
         * Value that must be formatted as an ISO date
         */
        @JvmOverloads
        fun date(format: String? = DateFormatUtils.ISO_DATE_FORMAT.pattern): PactDslJsonRootValue {
            val instance = FastDateFormat.getInstance(format)
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", DateGenerator(format))
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = DATE_2000
            value.setValue(instance.format(calendar))
            value.setMatcher(value.matchDate(format))
            return value
        }

        /**
         * Value that must match the provided date format
         * @param format date format to match
         * @param example example date to use for generated values
         */
        fun date(format: String?, example: Calendar?): PactDslJsonRootValue {
            val instance = FastDateFormat.getInstance(format)
            val value = PactDslJsonRootValue()
            value.setValue(instance.format(example))
            value.setMatcher(value.matchDate(format))
            return value
        }
        /**
         * Value that must match the given time format
         * @param format time format to match
         */
        /**
         * Value that must be an ISO formatted time
         */
        @JvmOverloads
        fun time(format: String? = DateFormatUtils.ISO_TIME_FORMAT.pattern): PactDslJsonRootValue {
            val instance = FastDateFormat.getInstance(format)
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", TimeGenerator(format))
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = DATE_2000
            value.setValue(instance.format(calendar))
            value.setMatcher(value.matchTime(format))
            return value
        }

        /**
         * Value that must match the given time format
         * @param format time format to match
         * @param example example time to use for generated bodies
         */
        fun time(format: String?, example: Calendar?): PactDslJsonRootValue {
            val instance = FastDateFormat.getInstance(format)
            val value = PactDslJsonRootValue()
            value.setValue(instance.format(example))
            value.setMatcher(value.matchTime(format))
            return value
        }

        /**
         * Value that must be an IP4 address
         */
        fun ipAddress(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.setValue("127.0.0.1")
            value.setMatcher(value.regexp("(\\d{1,3}\\.)+\\d{1,3}"))
            return value
        }

        /**
         * Value that must be a numeric identifier
         */
        fun id(): PactDslJsonRootValue {
            return numberType()
        }

        /**
         * Value that must be a numeric identifier
         * @param id example id to use for generated bodies
         */
        fun id(id: Long?): PactDslJsonRootValue {
            return numberType(id)
        }

        /**
         * Value that must be encoded as a hexadecimal value
         */
        fun hexValue(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", RandomHexadecimalGenerator(10))
            value.setValue("1234a")
            value.setMatcher(value.regexp("[0-9a-fA-F]+"))
            return value
        }

        /**
         * Value that must be encoded as a hexadecimal value
         * @param hexValue example value to use for generated bodies
         */
        fun hexValue(hexValue: String): PactDslJsonRootValue {
            if (!hexValue.matches(HEXADECIMAL)) {
                throw InvalidMatcherException("$EXAMPLE$hexValue\" is not a hexadecimal value")
            }
            val value = PactDslJsonRootValue()
            value.setValue(hexValue)
            value.setMatcher(value.regexp("[0-9a-fA-F]+"))
            return value
        }

        /**
         * Value that must be encoded as an UUID
         */
        fun uuid(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.generators.addGenerator(Category.BODY, "", UuidGenerator())
            value.setValue("e2490de5-5bd3-43d5-b7c4-526e33f71304")
            value.setMatcher(value.regexp(UUID_REGEX))
            return value
        }

        /**
         * Value that must be encoded as an UUID
         * @param uuid example UUID to use for generated bodies
         */
        fun uuid(uuid: UUID): PactDslJsonRootValue {
            return uuid(uuid.toString())
        }

        /**
         * Value that must be encoded as an UUID
         * @param uuid example UUID to use for generated bodies
         */
        fun uuid(uuid: String): PactDslJsonRootValue {
            if (!uuid.matches(UUID_REGEX)) {
                throw InvalidMatcherException("$EXAMPLE$uuid\" is not an UUID")
            }
            val value = PactDslJsonRootValue()
            value.setValue(uuid)
            value.setMatcher(value.regexp(UUID_REGEX))
            return value
        }

        /**
         * Combine all the matchers using AND
         * @param example Attribute example value
         * @param rules Matching rules to apply
         */
        fun and(example: Any?, vararg rules: MatchingRule?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            if (example != null) {
                value.setValue(example)
            } else {
                value.setValue(JSONObject.NULL)
            }
            value.matchers.setRules("", MatchingRuleGroup(Arrays.asList(*rules), RuleLogic.AND))
            return value
        }

        /**
         * Combine all the matchers using OR
         * @param example Attribute name
         * @param rules Matching rules to apply
         */
        fun or(example: Any?, vararg rules: MatchingRule?): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            if (example != null) {
                value.setValue(example)
            } else {
                value.setValue(JSONObject.NULL)
            }
            value.matchers.setRules("", MatchingRuleGroup(Arrays.asList(*rules), RuleLogic.OR))
            return value
        }

        /**
         * Matches Json-Null as the root value of a body.
         * This does not match no body at all!
         * However, you should consider just omitting null values.
         */
        fun matchNull(): PactDslJsonRootValue {
            val value = PactDslJsonRootValue()
            value.isEncodeJson = true
            value.setValue(null)
            value.setMatcher(NullMatcher)
            return value
        }
        /**
         * Matches a URL that is composed of a base path and a sequence of path expressions
         * @param basePath The base path for the URL (like "http://localhost:8080/") which will be excluded from the matching
         * @param pathFragments Series of path fragments to match on. These can be strings or regular expressions.
         */
        /*public PactDslJsonRootValue matchUrl(String basePath, Object... pathFragments) {
    UrlMatcherSupport urlMatcher = new UrlMatcherSupport(basePath, Arrays.asList(pathFragments));
    PactDslJsonRootValue value = new PactDslJsonRootValue();
    value.setValue(urlMatcher.getExampleValue());
    value.setMatcher(value.regexp(urlMatcher.getRegexExpression()));
    return value;
  }*/
    }
}