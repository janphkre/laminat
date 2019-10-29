package au.com.dius.pact

import au.com.dius.pact.model.HttpPart
import org.junit.Assert
import org.junit.Test

/**
 * These tests are used to verify the correct behavior for the
 * regular expression which determine the content type from a body
 * when no explicit header or body type is specified.
 *
 * @author Jan Phillip Kretzschmar
 */
class RegexTest {

    @Test
    fun jsonRegex_compiles() {
        val compiledRegex = HttpPart.JSONREGEXP
        Assert.assertTrue(compiledRegex.matches("{}"))
        Assert.assertFalse(compiledRegex.matches("ABCD"))
        Assert.assertFalse(compiledRegex.matches("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        Assert.assertFalse(compiledRegex.matches("<HTML>"))
        Assert.assertFalse(compiledRegex.matches("<manifest package=\"au.com.dius.pact\" />"))
    }

    @Test
    fun xmlRegex_compiles() {
        val compiledRegex = HttpPart.XMLREGEXP
        Assert.assertFalse(compiledRegex.matches("{}"))
        Assert.assertFalse(compiledRegex.matches("ABCD"))
        Assert.assertTrue(compiledRegex.matches("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        Assert.assertFalse(compiledRegex.matches("<HTML>"))
        Assert.assertFalse(compiledRegex.matches("<manifest package=\"au.com.dius.pact\" />"))
    }

    @Test
    fun htmlRegex_compiles() {
        val compiledRegex = HttpPart.HTMLREGEXP
        Assert.assertFalse(compiledRegex.matches("{}"))
        Assert.assertFalse(compiledRegex.matches("ABCD"))
        Assert.assertFalse(compiledRegex.matches("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        Assert.assertTrue(compiledRegex.matches("<HTML>"))
        Assert.assertFalse(compiledRegex.matches("<manifest package=\"au.com.dius.pact\" />"))
    }

    @Test
    fun xmlRegex2_compiles() {
        val compiledRegex = HttpPart.XMLREGEXP2
        Assert.assertFalse(compiledRegex.matches("{}"))
        Assert.assertFalse(compiledRegex.matches("ABCD"))
        Assert.assertTrue(compiledRegex.matches("<manifest package=\"au.com.dius.pact\" />"))
    }
}