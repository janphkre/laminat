package au.com.dius.pact.util.date

import au.com.dius.pact.model.exceptions.ParseException
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse

object DateUtil {

    fun parse(input: String, pattern: String): Date {
        val dateFormat = DateFormat.invoke(pattern)
        try {
            val result = dateFormat.parse(input)
            return Date(result)
        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

}