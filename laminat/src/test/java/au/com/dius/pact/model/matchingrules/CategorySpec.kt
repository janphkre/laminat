package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class CategorySpec: StringSpec({

    "generate #spec format body matchers" {
        // given:
        val category = Category("body", mutableMapOf(
            "$[0]"       to MatchingRuleGroup(mutableListOf(MaxTypeMatcher(5))),
            "$[0][*].id" to MatchingRuleGroup(mutableListOf(RegexMatcher("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
        ))
        
        // where:
        forAll(
            table(
                headers("spec", "matchers"),
                row(PactSpecVersion.V2   , mapOf<String, Any>("$.body[0]" to mapOf("match" to ("type"), "max" to (5)), "$.body[0][*].id" to mapOf("match" to ("regex"), "regex" to ("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))),
                row(PactSpecVersion.V3   , mapOf<String, Any>(
                    "$[0]" to mapOf("matchers" to listOf((mapOf("match" to ("type"), "max" to (5)))), "combine" to ("AND")),
                    "$[0][*].id" to mapOf("matchers" to listOf((mapOf("match" to ("regex"), "regex" to ("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))), "combine" to ("AND"))))
            )
        ) { spec, matchers ->
            // expect:
            category.toMap(PactSerializationConfig(spec, null)) shouldBe matchers
        }
    }

    // pact-jvm @Issue("#743")
    "writes path matchers in the correct format"() {
        // given:
        val category = Category("path", mutableMapOf(
            "" to MatchingRuleGroup(mutableListOf(RegexMatcher("\\w+")))
        ))
    
        // expect:
        category.toMap(PactSerializationConfig(PactSpecVersion.V2, null)) shouldBe mapOf("$.path" to mapOf("match" to "regex", "regex" to "\\w+"))
        category.toMap(PactSerializationConfig(PactSpecVersion.V3, null)) shouldBe mapOf("matchers" to listOf(mapOf("match" to "regex", "regex" to "\\w+")), "combine" to "AND")
    }

    // pact-jvm @Issue(mapOf("#786", "#882"))
    "writes header matchers in the correct format"() {
        // given:
        val category = Category("header", mutableMapOf(
            "Content-Type" to MatchingRuleGroup(mutableListOf(RegexMatcher("application/json;\\s?charset=(utf|UTF)-8")))
        ))
    
        // expect:
        category.toMap(PactSerializationConfig(PactSpecVersion.V2, null)) shouldBe mapOf("$.headers.Content-Type" to mapOf("match" to "regex", "regex" to "application/json;\\s?charset=(utf|UTF)-8"))
        category.toMap(PactSerializationConfig(PactSpecVersion.V3, null)) shouldBe mapOf("Content-Type" to mapOf("matchers" to listOf(mapOf("match" to "regex", "regex" to "application/json;\\s?charset=(utf|UTF)-8")), "combine" to "AND"))
    }

    // pact-jvm @Issue(mapOf("#895"))
    "when re-keying the matchers, drop any dollar from the start"() {
        // given:
        val category = Category("body", mutableMapOf(
            "$.bestandstype" to MatchingRuleGroup(mutableListOf(TypeMatcher)),
            "$.bestandsid" to MatchingRuleGroup(mutableListOf(TypeMatcher))
        ))
        category.applyMatcherRootPrefix("payload")
    
        // expect:
        category.toMap(PactSerializationConfig(PactSpecVersion.V2, null)) shouldBe mapOf(
            "$.body.payload.bestandstype" to mapOf("match" to "type"),
        "$.body.payload.bestandsid" to mapOf("match" to "type")
        )
        category.toMap(PactSerializationConfig(PactSpecVersion.V3, null)) shouldBe mapOf(
            "payload.bestandstype" to mapOf("matchers" to listOf(mapOf("match" to "type")), "combine" to "AND"),
        "payload.bestandsid" to mapOf("matchers" to listOf(mapOf("match" to "type")), "combine" to "AND")
        )
    }
    // pact-jvm @Issue(mapOf("#976"))
    "when re-keying the matchers, always prepend prefix to existing key"() {
        // given:
        val matchingRule = MatchingRuleGroup(mutableListOf(TypeMatcher))
        val category = Category("body", mutableMapOf(
            ".blueberry" to matchingRule
        ))
        category.applyMatcherRootPrefix("blue")
    
        // expect:
        category.toMap(PactSerializationConfig(PactSpecVersion.V2, null)) shouldBe mapOf(
            "$.body.blue.blueberry" to mapOf("match" to "type"))
    
        category.toMap(PactSerializationConfig(PactSpecVersion.V3, null)) shouldBe mapOf(
            "blue.blueberry" to mapOf("matchers" to listOf(mapOf("match" to "type")), "combine" to "AND"))
    }
})