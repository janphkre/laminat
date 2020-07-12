package au.com.dius.pact.matchers

import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.util.path.PathExpression
import au.com.dius.pact.util.path.PathToken
import au.com.dius.pact.util.zipFirstNullable

object Matchers {

    private val arrayRegex = Regex("\\d+")

    private fun matchesToken(pathElement: String?, token: PathToken): Int {
        if (pathElement == null) {
            return 0
        }
        return when (token) {
            is PathToken.Root -> if (pathElement == "$") 2 else 0
            is PathToken.Field -> if (pathElement == token.name) 2 else 0
            is PathToken.Index -> if (pathElement.matches(arrayRegex) && token.indices.contains(pathElement.toInt())) 2 else 0 //TODO: WHAT ABOUT INDICES?
            is PathToken.StarIndex -> if (pathElement.matches(arrayRegex)) 1 else 0
            is PathToken.Star -> 1
        }
    }

    private fun matchPathExact(pathExp: PathExpression?, actualItems: List<String>): Int {
        return pathExp?.compiledPath?.let { compiledPath ->
            val filter = actualItems.tailsFilter { list ->
                list.allIndexed { index, element ->
                    matchesToken(element, compiledPath.elementAt(index)) != 0
                }
            }
            if (filter.isNotEmpty()) {
                filter.maxBy { it.size }?.size ?: 0
            } else {
                0
            }
        } ?: 0
    }

    private fun matchPath(pathExp: PathExpression?, actualItems: List<String>): Int {
        return pathExp?.compiledPath?.let { compiledPath ->
            val filter = actualItems.tailsFilter { list ->
                list.allIndexed { index, element ->
                    if (compiledPath.size <= index) {
                        false
                    } else {
                        matchesToken(element, compiledPath.elementAt(index)) != 0
                    }
                }
            }
            if (filter.isNotEmpty()) {
                filter.maxBy { it.size }?.size ?: 0
            } else {
                0
            }
        } ?: 0
    }

    private fun <T> List<T>.tailsFilter(lambda: (List<T>) -> Boolean): List<List<T>> {
        val result = mutableListOf<List<T>>()
        for (i in size downTo 1) {
            val currentList = this.subList(0, i)
            if (lambda.invoke(currentList)) {
                result.add(currentList)
            }
        }
        return result
    }

    private fun <T> List<T>.allIndexed(predicate: (Int, T) -> Boolean): Boolean {
        if (isEmpty()) {
            return true
        }
        forEachIndexed { index, element ->
            if (!predicate(index, element)) return false
        }
        return true
    }

//  SCALA:
//  def calculatePathWeight(pathExp: String, path: Seq[String]) = {
//      new Parser().compile(pathExp) match {
//          case Parser.Success(q, _) =>
//          path.zip(q).map(entry => matchesToken(entry._1, entry._2)).product
//          case ns: Parser.NoSuccess =>
//          logger.warn(s"Path expression $pathExp is invalid, ignoring: $ns")
//          0
//      }
//  }
    private fun calculatePathWeight(pathExp: PathExpression?, actualItems: List<String>): Int {
        return pathExp?.compiledPath?.let { compiledPath ->
            actualItems.zipFirstNullable(compiledPath).asSequence().map { entry -> matchesToken(entry.first, entry.second) }.fold(1) { result, element -> result * element }
        } ?: 0
    }

    fun definedWildcardMatchers(category: String, path: List<String>, matchers: MatchingRules): Boolean {
        val resolvedMatchers = matchers.getCategory(category)?.filter { pathExp -> matchPath(
            pathExp,
            path
        ) == path.size }
        return resolvedMatchers?.matchingRules?.keys?.any { key -> key.path.endsWith(".*") } ?: false
    }

    fun definedMatchers(category: String, path: List<String>, matchers: MatchingRules): Category? {
        return if (category == "body") {
            matchers.getCategory(category)?.filter { pathExp -> matchPath(
                pathExp,
                path
            ) > 0 }
        } else if (category == "header" || category == "query") {
            matchers.getCategory(category)?.filter { pathExp -> path.size == 1 && pathExp.path == path.first() }
        } else {
            matchers.getCategory(category)
        }
    }

    fun doMatch(category: Category, path: List<String>, expected: Any?, actual: Any?, mismatchFactory: MismatchFactory<RequestMatchProblem>): List<RequestMatchProblem> {
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