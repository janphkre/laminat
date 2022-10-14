package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.external.json.Json
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class MatchingRuleGroupSpec : StringSpec({

    "matchers lookup returns #matcherClass.simpleName #condition" {
        // where:
        forAll(
            table(
                headers("map"                              , "matcherClass"      , "condition"),
                row(Json.Object()                             , EqualsMatcher::class     , "if the definition is empty"),
                row(Json.Object("other" to Json.StringPrimitive("value"))                 , EqualsMatcher::class     , "if the definition is invalid"),
                row(Json.Object("match" to Json.StringPrimitive("something"))             , EqualsMatcher::class     , "if the matcher type is unknown"),
                row(Json.Object("match" to Json.StringPrimitive("equality"))              , EqualsMatcher::class     , "if the matcher type is equality"),
                row(Json.Object("match" to Json.StringPrimitive("regex"), "regex" to Json.StringPrimitive(".*"))    , RegexMatcher::class      , "if the matcher type is regex"),
                row(Json.Object("regex" to Json.StringPrimitive("\\w+"))                  , RegexMatcher::class      , "if the matcher definition contains a regex"),
                row(Json.Object("match" to Json.StringPrimitive("type"))                  , TypeMatcher::class       , "if the matcher type is \"type\" and there is no min or max"),
                row(Json.Object("match" to Json.StringPrimitive("number"))                , NumberTypeMatcher::class , "if the matcher type is \"number\""),
                row(Json.Object("match" to Json.StringPrimitive("integer"))               , NumberTypeMatcher::class , "if the matcher type is \"integer\""),
                row(Json.Object("match" to Json.StringPrimitive("real"))                  , NumberTypeMatcher::class , "if the matcher type is \"real\""),
                row(Json.Object("match" to Json.StringPrimitive("decimal"))               , NumberTypeMatcher::class , "if the matcher type is \"decimal\""),
                row(Json.Object("match" to Json.StringPrimitive("type"), "min" to Json.NumberPrimitive(1))          , MinTypeMatcher::class    , "if the matcher type is \"type\" and there is a min"),
                row(Json.Object("match" to Json.StringPrimitive("min"), "min" to Json.NumberPrimitive(1))           , MinTypeMatcher::class    , "if the matcher type is \"min\""),
                row(Json.Object("min" to Json.NumberPrimitive(1))                         , MinTypeMatcher::class    , "if the matcher definition contains a min"),
                row(Json.Object("match" to Json.StringPrimitive("type"), "max" to Json.NumberPrimitive(1))          , MaxTypeMatcher::class    , "if the matcher type is \"type\" and there is a max"),
                row(Json.Object("match" to Json.StringPrimitive("max"), "max" to Json.NumberPrimitive(1))           , MaxTypeMatcher::class    , "if the matcher type is \"max\""),
                row(Json.Object("max" to Json.NumberPrimitive(1))                         , MaxTypeMatcher::class    , "if the matcher definition contains a max"),
                row(Json.Object("match" to Json.StringPrimitive("type"), "max" to Json.NumberPrimitive(3), "min" to Json.NumberPrimitive(2))  , MinMaxTypeMatcher::class , "if the matcher definition contains both a min and max"),
                row(Json.Object("match" to Json.StringPrimitive("timestamp"))             , TimestampMatcher::class  , "if the matcher type is \"timestamp\""),
                row(Json.Object("timestamp" to Json.StringPrimitive("1"))                 , TimestampMatcher::class  , "if the matcher definition contains a timestamp"),
                row(Json.Object("match" to Json.StringPrimitive("time"))                  , TimeMatcher::class       , "if the matcher type is \"time\""),
                row(Json.Object("time" to Json.StringPrimitive("1"))                      , TimeMatcher::class       , "if the matcher definition contains a time"),
                row(Json.Object("match" to Json.StringPrimitive("date"))                  , DateMatcher::class       , "if the matcher type is \"date\""),
                row(Json.Object("date" to Json.StringPrimitive("1"))                      , DateMatcher::class       , "if the matcher definition contains a date"),
                row(Json.Object("match" to Json.StringPrimitive("include"), "include" to Json.StringPrimitive("A")) , IncludeMatcher::class    , "if the matcher type is include"),
                row(Json.Object("match" to Json.StringPrimitive("values"))                , ValuesMatcher::class     , "if the matcher type is values")
            )
        ) { map ,matcherClass, _ ->
            // expect:
            val result = MatchingRulesSerialization.fromJson(map)
            result::class shouldBe matcherClass
        }
    }

    "defaults to AND for combining rules"() {
        // expect:
        MatchingRuleGroup().ruleLogic shouldBe RuleLogic.AND
    }
})