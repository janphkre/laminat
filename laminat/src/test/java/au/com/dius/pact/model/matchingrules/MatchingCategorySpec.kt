package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.jsonArray
import au.com.dius.pact.external.json.jsonObject
import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.serialization.RequestResponsePactV2Deserializer
import au.com.dius.pact.model.serialization.RequestResponsePactV3Deserializer
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

/// MatchingRulesSpec in pact-jvm
class MatchingCategorySpec: StringSpec({

    "fromMap handles a null map" {
        // when:
        val matchingRules = with(RequestResponsePactV3Deserializer()) {
            Json.Null.toCategoryMap()
        }

        // then:
        matchingRules.isEmpty() shouldBe true
    }

    "fromMap handles an empty map" {
        // when:
        val matchingRules = with(RequestResponsePactV3Deserializer()) {
            Json.Object().toCategoryMap()
        }

        // then:
        matchingRules.isEmpty() shouldBe true
    }

    "loads V2 matching rules" {
        // given:
        val matchingRulesMap = jsonObject(
            "$.path" to jsonObject("match" to "regex", "regex" to "\\w+"),
            "$.query.Q1" to jsonObject("match" to "regex", "regex" to "\\d+"),
            "$.header.HEADERX" to jsonObject("match" to "include", "value" to "ValueA"),
            "$.headers.HEADERY" to jsonObject("match" to "include", "value" to "ValueA"),
            "$.body.animals" to jsonObject("min" to 1, "match" to "type"),
            "$.body.animals[*].*" to jsonObject("match" to "type"),
            "$.body.animals[*].children" to jsonObject("min" to 1),
            "$.body.animals[*].children[*].*" to jsonObject("match" to "type")
        )

        // when:
        val matchingRules = with(RequestResponsePactV2Deserializer()) {
            matchingRulesMap.toCategoryMap()
        }

        // then:
        matchingRules.isEmpty() shouldBe false
        matchingRules.keys shouldBe setOf("path", "query", "header", "body")
        matchingRules["path"] shouldBe Category("path", mutableMapOf("" to MatchingRuleGroup(mutableListOf(RegexMatcher("\\w+")))))
        matchingRules["query"] shouldBe Category("query", mutableMapOf("Q1" to MatchingRuleGroup(mutableListOf(RegexMatcher("\\d+")))))
        matchingRules["header"] shouldBe Category("header", mutableMapOf(
            "HEADERX" to MatchingRuleGroup(mutableListOf(IncludeMatcher("ValueA"))),
            "HEADERY" to MatchingRuleGroup(mutableListOf(IncludeMatcher("ValueA")))
        ))
        matchingRules["body"] shouldBe Category("body", mutableMapOf(
            "$.animals" to MatchingRuleGroup(mutableListOf(MinTypeMatcher(1))),
            "$.animals[*].*" to MatchingRuleGroup(mutableListOf(TypeMatcher)),
            "$.animals[*].children" to MatchingRuleGroup(mutableListOf(MinTypeMatcher(1))),
            "$.animals[*].children[*].*" to MatchingRuleGroup(mutableListOf(TypeMatcher))
        ))
    }

    "loads V3 matching rules" {
        // given:
        val matchingRulesMap = jsonObject(
            "path" to jsonObject(
                "matchers" to jsonArray(
                    jsonObject( "match" to "regex", "regex" to "\\w+" )
                )
            ),
            "query" to jsonObject(
                "Q1" to jsonObject(
                    "matchers" to jsonArray(
                        jsonObject( "match" to "regex", "regex" to "\\d+" )
                    )
                )
            ),
            "header" to jsonObject(
                "HEADERY" to jsonObject(
                    "combine" to "AND",
                    "matchers" to jsonArray(
                        jsonObject("match" to "include", "value" to "ValueA"),
                        jsonObject("match" to "include", "value" to "ValueB")
                    )
                )
            ),
            "body" to jsonObject(
                "$.animals" to jsonObject(
                    "matchers" to jsonArray(jsonObject("min" to 1, "match" to "type"))
                ),
                "$.animals[*].*" to jsonObject(
                    "matchers" to jsonArray(jsonObject("match" to "type"))
                ),
                "$.animals[*].children" to jsonObject(
                    "matchers" to jsonArray(jsonObject("min" to 1))
                ),
                "$.animals[*].children[*].*" to jsonObject(
                    "matchers" to jsonArray(jsonObject("match" to "type"))
                )
            )
        )

        // when:
        val matchingRules = with(RequestResponsePactV3Deserializer()) {
            matchingRulesMap.toCategoryMap()
        }

        // then:
        matchingRules.isEmpty() shouldBe false
        matchingRules.keys shouldBe setOf("path", "query", "header", "body")
        matchingRules["path"] shouldBe Category("path", mutableMapOf("" to MatchingRuleGroup(mutableListOf(RegexMatcher("\\w+")))))
        matchingRules["query"] shouldBe Category("query", mutableMapOf("Q1" to MatchingRuleGroup(mutableListOf(RegexMatcher("\\d+")))))
        matchingRules["header"] shouldBe Category("header", mutableMapOf(
            "HEADERY" to MatchingRuleGroup(mutableListOf(IncludeMatcher("ValueA"), IncludeMatcher("ValueB")))
        ))
        matchingRules["body"] shouldBe Category("body", mutableMapOf(
            "$.animals" to MatchingRuleGroup(mutableListOf(MinTypeMatcher(1))),
            "$.animals[*].*" to MatchingRuleGroup(mutableListOf(TypeMatcher)),
            "$.animals[*].children" to MatchingRuleGroup(mutableListOf(MinTypeMatcher(1))),
            "$.animals[*].children[*].*" to MatchingRuleGroup(mutableListOf(TypeMatcher))
        ))
    }

    // @Issue("#743")
    "loads matching rules affected by defect #743" {
        // given:
        val matchingRulesMap = jsonObject(
            "path" to jsonObject(
                "" to jsonObject(
                    "matchers" to jsonArray(
                        jsonObject( "match" to "regex", "regex" to "\\w+" )
                    )
                )
            )
        )

        // when:
        val matchingRules = with(RequestResponsePactV3Deserializer()) {
            matchingRulesMap.toCategoryMap()
        }

        // then:
        matchingRules.isEmpty() shouldBe false
        matchingRules.keys shouldBe setOf("path")
        matchingRules["path"] shouldBe Category("path", mutableMapOf("" to MatchingRuleGroup(mutableListOf(RegexMatcher("\\w+")))))
    }

    // @Issue("#743")
    "generates path matching rules in the correct format" {
        // given:
        val matchingRules = MatchingRules()
        matchingRules.addCategory("path").addRule(RegexMatcher("\\w+"))

        // expect:
        matchingRules.toMap(PactSerializationConfig(PactSpecVersion.V3, null)) shouldBe mapOf(
            "path" to mapOf(
                "matchers" to listOf(mapOf("match" to "regex", "regex" to "\\w+")),
                "combine" to "AND"
            )
        )
    }

    "do not include empty categories" {
        // given:
        val matchingRules = MatchingRules()
        matchingRules.addCategory("path").addRule(RegexMatcher("\\w+"))
        matchingRules.addCategory("body")
        matchingRules.addCategory("header")

        // expect:
        matchingRules.toMap(PactSerializationConfig(PactSpecVersion.V3, null)) shouldBe mapOf(
            "path" to mapOf(
                "matchers" to listOf(mapOf("match" to "regex", "regex" to "\\w+")),
                "combine" to "AND"
            )
        )
    }

    // @Issue("#882")
    "With V2 format, matching rules for headers are pluralised" {
        // given:
        val matchingRules = MatchingRules()
        matchingRules.addCategory("path").addRule(RegexMatcher("\\w+"))
        matchingRules.addCategory("body")
        matchingRules.addCategory("header").addRule("X", RegexMatcher("\\w+"))

        // expect:
        matchingRules.toMap(PactSerializationConfig(PactSpecVersion.V2, null)) shouldBe mapOf(
            "$.path" to mapOf("match" to "regex", "regex" to "\\w+"),
            "$.headers.X" to mapOf("match" to "regex", "regex" to "\\w+")
        )
    }
})