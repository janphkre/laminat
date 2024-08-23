package au.com.dius.pact.model

import au.com.dius.pact.shouldBeException
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class PathExpressionsSpec : StringSpec({

    "Parse Path Exp Handles Empty String" {
        // expect:
        parsePath("") shouldBe emptyList<PathToken>()
    }

    "Parse Path Exp Handles Root" {
        // expect:
        parsePath("$") shouldBe listOf(PathToken.Root)
    }

    "Parse Path Exp Handles Missing Root" {
        // when:
        val result = runCatching { parsePath("adsjhaskjdh") }

        // then:
        val ex = result.exceptionOrNull()
        ex shouldBeException InvalidPathExpression::class
        ex!!.message shouldBe "Path expression \"adsjhaskjdh\" does not start with a root marker \"$\""
    }

    "Parse Path Exp Handles Missing Path" {
        // when:
        val result = runCatching { parsePath("\$adsjhaskjdh") }

        // then:
        val ex = result.exceptionOrNull()
        ex shouldBeException InvalidPathExpression::class
        ex!!.message shouldBe "Expected a \".\" or \"[\" instead of \"a\" in path expression \"\$adsjhaskjdh\" at index 1"
    }

    "Parse Path Exp Handles Missing Path Name in \"#expression\"" {
        forAll(
            table(
                headers("expression", "message"),
                row("$.", "Expected a path after \".\" in path expression \"\$.\" at index 1"),
                row("\$.a.b.c.", "Expected a path after \".\" in path expression \"\$.a.b.c.\" at index 7")
            )
        ) { expression, message ->
            // when:
            val result = runCatching { parsePath(expression) }

            // then:
            val ex = result.exceptionOrNull()
            ex shouldBeException InvalidPathExpression::class
            ex!!.message shouldBe message
        }
    }

    "Parse Path Exp Handles Invalid Identifiers in \"#expression\"" {
        forAll(
            table(
                headers("expression", "message"),
                row("\$.abc!", "\"!\" is not allowed in an identifier in path expression \"\$.abc!\" at index 5"),
                row("\$.a.b.c.}", "Expected either a \"*\" or path identifier in path expression \"\$.a.b.c.}\" at index 8")
            )
        ) { expression, message ->
            // when:
            val result = runCatching { parsePath(expression) }

            // then:
            val ex = result.exceptionOrNull()
            ex shouldBeException InvalidPathExpression::class
            ex!!.message shouldBe message
        }
    }

    "Parse Path Exp With Simple Identifiers - #expression" {
        forAll(
            table(
                headers("expression", "result"),
                row("\$.a", listOf(PathToken.Root, PathToken.Field("a"))),
                row("\$.a-b", listOf(PathToken.Root, PathToken.Field("a-b"))),
                row("\$.a_b", listOf(PathToken.Root, PathToken.Field("a_b"))),
                row("\$._b", listOf(PathToken.Root, PathToken.Field("_b"))),
                row("\$.a.b.c", listOf(PathToken.Root, PathToken.Field("a"), PathToken.Field("b"), PathToken.Field("c")))
            )
        ) { expression, result ->
            // expect:
            parsePath(expression) shouldBe result
        }
    }

    "Parse Path Exp With Punctuation in Identifiers - #expression" {
        forAll(
            table(
                headers("expression", "result"),
                row("\$.container_records.example-ABC", listOf(PathToken.Root, PathToken.Field("container_records"), PathToken.Field("example-ABC"))),
                row("\$.container_records.example_ABC", listOf(PathToken.Root, PathToken.Field("container_records"), PathToken.Field("example_ABC"))),
                row("\$.container_records.example:ABC", listOf(PathToken.Root, PathToken.Field("container_records"), PathToken.Field("example:ABC"))),
                row("\$.container_records['example:ABC']", listOf(PathToken.Root, PathToken.Field("container_records"), PathToken.Field("example:ABC"))),
                row("\$.container_records['example/ABC']", listOf(PathToken.Root, PathToken.Field("container_records"), PathToken.Field("example/ABC")))
            )
        ) { expression, result ->
            // expect:
            parsePath(expression) shouldBe result
        }
    }

    "Parse Path Exp With Star Instead Of Identifiers - #expression" {
        forAll(
            table(
                headers("expression", "result"),
                row("\$.*", listOf(PathToken.Root, PathToken.Star)),
                row("\$.a.*.c", listOf(PathToken.Root, PathToken.Field("a"), PathToken.Star, PathToken.Field("c")))
            )
        ) { expression, result ->
            // expect:
            parsePath(expression) shouldBe result
        }
    }

    "Parse Path Exp With Bracket Notation - #expression" {
        forAll(
            table(
                headers("expression", "result"),
                row("\$['val1']", listOf(PathToken.Root, PathToken.Field("val1"))),
                row("\$.a['val@1.'].c", listOf(PathToken.Root, PathToken.Field("a"), PathToken.Field("val@1."), PathToken.Field("c"))),
                row("\$.a[1].c", listOf(PathToken.Root, PathToken.Field("a"), PathToken.Index(1), PathToken.Field("c"))),
                row("\$.a[*].c", listOf(PathToken.Root, PathToken.Field("a"), PathToken.StarIndex, PathToken.Field("c")))
            )
        ) { expression, result ->
            // expect:
            parsePath(expression) shouldBe result
        }
    }

    "Parse Path Exp With Invalid Bracket Notation - #expression" {
        forAll(
            table(
                headers("expression", "message"),
                row("\$[", "Expected a \"'\" (single quote) or a digit in path expression \"\$[\" after index 1"),
                row("\$['", "Unterminated string in path expression \"\$['\" at index 2"),
                row("\$['Unterminated string", "Unterminated string in path expression \"\$['Unterminated string\" at index 21"),
                row("\$['']", "Empty strings are not allowed in path expression \"\$['']\" at index 3"),
                row("\$['test'.b.c", "Unterminated brackets, found \".\" instead of \"]\" in path expression \"\$['test'.b.c\" at index 8"),
                row("\$['test'", "Unterminated brackets in path expression \"\$['test'\" at index 2"),
                row("\$['test']b.c", "Expected a \".\" or \"[\" instead of \"b\" in path expression \"\$['test']b.c\" at index 9")
            )
        ) { expression, message ->
            // when:
            val result = runCatching { parsePath(expression) }

            // then:
            val ex = result.exceptionOrNull()
            ex shouldBeException InvalidPathExpression::class
            ex!!.message shouldBe message
        }
    }

    "Parse Path Exp With Invalid Bracket Index Notation - #expression" {
        forAll(
            table(
                headers("expression", "message"),
                row("\$[dhghh]", "Indexes can only consist of numbers or a \"*\", found \"d\" instead in path expression \"\$[dhghh]\" at index 2"),
                row("\$[12abc]", "Indexes can only consist of numbers or a \"*\", found \"a\" instead in path expression \"\$[12abc]\" at index 4"),
                row("\$[]", "Empty bracket expressions are not allowed in path expression \"\$[]\" at index 2"),
                row("\$[-1]", "Indexes can only consist of numbers or a \"*\", found \"-\" instead in path expression \"\$[-1]\" at index 2")
            )
        ) { expression, message ->
            // when:
            val result = runCatching { parsePath(expression) }

            // then:
            val ex = result.exceptionOrNull()
            ex shouldBeException InvalidPathExpression::class
            ex!!.message shouldBe message
        }
    }
})