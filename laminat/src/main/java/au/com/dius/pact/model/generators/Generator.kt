package au.com.dius.pact.model.generators

import android.os.Build
import au.com.dius.pact.model.PactSpecVersion
import com.mifmif.common.regex.Generex
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatterBuilder
import java.math.BigDecimal
import java.util.Random
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

interface Generator {
    fun generate(base: Any?): Any
    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any>
}

data class RandomIntGenerator(val min: Int, val max: Int) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "RandomInt", "min" to min, "max" to max)
    }

    override fun generate(base: Any?): Any {
        return RandomUtils.nextInt(min, max)
    }
}

data class RandomDecimalGenerator(val digits: Int) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "RandomDecimal", "digits" to digits)
    }

    override fun generate(base: Any?): Any = BigDecimal(RandomStringUtils.randomNumeric(digits))
}

data class RandomHexadecimalGenerator(val digits: Int) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "RandomHexadecimal", "digits" to digits)
    }

    override fun generate(base: Any?): Any = RandomStringUtils.random(digits, "0123456789abcdef")
}

data class RandomStringGenerator(val size: Int = 20) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "RandomString", "size" to size)
    }

    override fun generate(base: Any?): Any {
        return RandomStringUtils.randomAlphanumeric(size)
    }
}

data class RegexGenerator(val regex: String) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "Regex", "regex" to regex)
    }

    override fun generate(base: Any?): Any = Generex(regex).random()
}

class UuidGenerator : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "Uuid")
    }

    override fun generate(base: Any?): Any {
        return UUID.randomUUID().toString()
    }

    override fun equals(other: Any?) = other is UuidGenerator

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

data class DateGenerator(val format: String? = null) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        if (format != null) {
            return mapOf("type" to "Date", "format" to this.format)
        }
        return mapOf("type" to "Date")
    }

    override fun generate(base: Any?): Any {
        return if (format != null) {
            DateTime.now().toString(DateTimeFormatterBuilder().appendPattern(format).toFormatter())
        } else {
            DateTime.now().toString()
        }
    }
}

data class TimeGenerator(val format: String? = null) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        if (format != null) {
            return mapOf("type" to "Time", "format" to this.format)
        }
        return mapOf("type" to "Time")
    }

    override fun generate(base: Any?): Any {
        return if (format != null) {
            DateTime.now().toString(DateTimeFormatterBuilder().appendPattern(format).toFormatter())
        } else {
            DateTime.now().toString()
        }
    }
}

data class DateTimeGenerator(val format: String? = null) : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        if (format != null) {
            return mapOf("type" to "DateTime", "format" to this.format)
        }
        return mapOf("type" to "DateTime")
    }

    override fun generate(base: Any?): Any {
        return if (format != null) {
            DateTime.now().toString(DateTimeFormatterBuilder().appendPattern(format).toFormatter())
        } else {
            DateTime.now().toString()
        }
    }
}

object RandomBooleanGenerator : Generator {
    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        return mapOf("type" to "RandomBoolean")
    }

    override fun generate(base: Any?): Any {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThreadLocalRandom.current().nextBoolean()
        } else {
            Random().nextBoolean()
        }
    }

    override fun equals(other: Any?) = other is RandomBooleanGenerator

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}