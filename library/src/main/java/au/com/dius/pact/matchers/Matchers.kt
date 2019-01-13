package au.com.dius.pact.matchers

import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.MatchingRules
import org.apache.commons.collections4.Predicate

object Matchers {

    fun matchPath(pathExp: String?, actualItems: List<String>): Int {
        /*return with(Parser().compile(pathExp)) {
            if(this.successful()) {
                val filter = actualItems.reversed().tails.filter( l ->
                l.reverse.corresponds(q)((pathElement, pathToken) => matchesToken(pathElement, pathToken) != 0))
                if (filter.nonEmpty) {
                    filter.maxBy(seq => seq . length).length
                } else {
                    0
                }
            }
            0
        }*/
        TODO("not implemented yet")
    }

    fun calculatePathWeight(pathExp: String?, actualItems: List<String>): String {
        /*new Parser().compile(pathExp) match {
            case Parser.Success(q, _) =>
            path.zip(q).map(entry => matchesToken(entry._1, entry._2)).product
            case ns: Parser.NoSuccess =>
            logger.warn(s"Path expression $pathExp is invalid, ignoring: $ns")
            0
        }*/
        TODO("not implemented yet")
    }

    fun definedWildcardMatchers(category: String, path: List<String>, matchers: MatchingRules): Category? {
        //TODO! JUST COPPIED FROM METHOD BELOW
        return if (category == "body") {
            matchers.getCategory(category)?.filter(Predicate { pathExp -> matchPath(
                pathExp,
                path
            ) > 0 })
        } else if (category == "header" || category == "query") {
            matchers.getCategory(category)?.filter(Predicate { pathExp -> path.size == 1 && path.first() == pathExp })
        } else {
            matchers.getCategory(category)
        }
    }

    fun definedMatchers(category: String, path: List<String>, matchers: MatchingRules): Category? {
        return if (category == "body") {
            matchers.getCategory(category)?.filter(Predicate { pathExp -> matchPath(
                pathExp,
                path
            ) > 0 })
        } else if (category == "header" || category == "query") {
            matchers.getCategory(category)?.filter(Predicate { pathExp -> path.size == 1 && path.first() == pathExp })
        } else {
            matchers.getCategory(category)
        }
    }

    fun <T> doMatch(category: Category, path: List<String>, expected: Any?, actual: Any?, mismatchFactory: MismatchFactory<T>): List<T> {
        val matcherDef = selectBestMatcher(category, path)
        return domatch(matcherDef, expected, actual, mismatchFactory)
    }

    private fun selectBestMatcher(category: Category, path: List<String>): MatchingRuleGroup {
        return if (category.name == "body")
            category.matchingRules.maxBy {
                calculatePathWeight(it.key, path)
            }!!.value
        else {
            category.matchingRules.values.first()
        }
    }
}