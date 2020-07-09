package au.com.dius.pact.external

import au.com.dius.pact.matchers.Matching
import au.com.dius.pact.matchers.RequestMatchProblem
import au.com.dius.pact.model.exceptions.PactMergeException
import au.com.dius.pact.model.requests.Request
import au.com.dius.pact.model.requests.RequestResponseInteraction
import java.util.LinkedList

/**
 * This request matcher is able to match an incoming request with a given list of interactions.
 * It returns the best possible match between one request response interaction and the request it was able to find.
 *
 * @author Jan Phillip Kretzschmar
 */
class RequestMatcher(private val allowUnexpectedKeys: Boolean) {

    fun findInteraction(
        interactions: List<RequestResponseInteraction>,
        request: IncomingRequest
    ): RequestMatch {
        val matches = interactions.map { compareRequest(it, request) }
        val bestMatch = matches.fold(RequestMatch.RequestMismatch() as RequestMatch) { bestResult, current ->
            return@fold if (current is RequestMatch.FullRequestMatch && (bestResult is RequestMatch.RequestMismatch || bestResult is RequestMatch.PartialRequestMatch)) {
                current
            } else if (current is RequestMatch.FullRequestMatch && bestResult is RequestMatch.FullRequestMatch) {
                if (current.matchedCount > bestResult.matchedCount) {
                    current
                } else {
                    bestResult
                }
            } else if (current is RequestMatch.PartialRequestMatch && bestResult is RequestMatch.RequestMismatch) {
                current
            } else if (current is RequestMatch.PartialRequestMatch && bestResult is RequestMatch.PartialRequestMatch) {
                if (current.problems.size < bestResult.problems.size) {
                    current
                } else {
                    bestResult
                }
            } else if (current is RequestMatch.RequestMismatch && bestResult is RequestMatch.RequestMismatch) {
                if (current.problems?.size ?: Int.MAX_VALUE < bestResult.problems?.size ?: Int.MAX_VALUE) {
                    current
                } else {
                    bestResult
                }
            } else {
                bestResult
            }
        }
        val conflictingMatch = matches.firstOrNull { it !== bestMatch && it is RequestMatch.FullRequestMatch && it.matchedCount == (bestMatch as RequestMatch.FullRequestMatch).matchedCount }
        if (conflictingMatch != null) {
            throw PactMergeException(
                "Multiple interactions have matched this request: " +
                    "${(bestMatch as RequestMatch.FullRequestMatch).interaction.uniqueKey()} and " +
                    (conflictingMatch as RequestMatch.FullRequestMatch).interaction.uniqueKey()
            )
        }
        return bestMatch
    }

    private fun compareRequest(expected: RequestResponseInteraction, actual: IncomingRequest): RequestMatch {
        val mismatches = requestMismatches(expected.request, actual)
        return decideRequestMatch(expected, mismatches)
    }

    private fun requestMismatches(expected: Request, actual: IncomingRequest): List<RequestMatchProblem> {
        return LinkedList<RequestMatchProblem>().apply {
            addAll(Matching.matchMethod(expected.method, actual.getMethod()))
            addAll(Matching.matchPath(expected, actual))
            addAll(Matching.matchQuery(expected, actual))
            addAll(Matching.matchCookie(expected.cookie(), actual.getCookie()))
            addAll(Matching.matchRequestHeaders(expected, actual))
            addAll(Matching.matchBody(expected, actual, allowUnexpectedKeys))
        }
    }

    private fun decideRequestMatch(expected: RequestResponseInteraction, problems: List<RequestMatchProblem>): RequestMatch {
        return when {
            problems.all { it == RequestMatchProblem.None } -> RequestMatch.FullRequestMatch(expected, problems.size)
            isPartialMatch(problems) -> RequestMatch.PartialRequestMatch(expected, problems.filter { it != RequestMatchProblem.None })
            else -> RequestMatch.RequestMismatch(expected, problems.filter { it != RequestMatchProblem.None })
        }
    }

    private fun isPartialMatch(problems: List<RequestMatchProblem>): Boolean {
        return !problems.any {
            it is RequestMatchProblem.PathMismatch || it is RequestMatchProblem.MethodMismatch
        }
    }
}