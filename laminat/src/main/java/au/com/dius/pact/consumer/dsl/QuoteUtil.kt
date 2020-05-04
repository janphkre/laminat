package au.com.dius.pact.consumer.dsl

/**
 * Util class for dealing with Json format
 */
object QuoteUtil {
    /**
     * Reads the input text with possible single quotes as delimiters
     * and returns a String correctly formatted.
     *
     * For convenience, single quotes as well as double quotes
     * are allowed to delimit strings. If single quotes are
     * used, any quotes, single or double, in the string must be
     * escaped (prepend with a '\').
     *
     * @param text the input data
     * @return String without single quotes
     */
    fun convert(text: String): String {
        val builder = StringBuilder()
        var single_context = false
        var i = 0
        while (i < text.length) {
            var ch = text[i]
            if (ch == '\\') {
                i = i + 1
                if (i < text.length) {
                    ch = text[i]
                    if (!(single_context && ch == '\'')) {
                        // unescape ' inside single quotes
                        builder.append('\\')
                    }
                }
            } else if (ch == '\'') {
                // Turn ' into ", for proper string
                ch = '"'
                single_context = !single_context
            }
            builder.append(ch)
            i++
        }
        return builder.toString()
    }
}