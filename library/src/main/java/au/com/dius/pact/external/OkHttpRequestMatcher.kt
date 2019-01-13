package au.com.dius.pact.external

import au.com.dius.pact.model.*
import au.com.dius.pact.matchers.Matching
import au.com.dius.pact.matchers.RequestMatchProblem
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

internal class OkHttpRequestMatcher(private val allowUnexpectedKeys: Boolean) {

    fun findInteraction(
        interactions: List<RequestResponseInteraction>,
        request: RecordedRequest
    ): RequestMatch {
        val matches = interactions.map { compareRequest(it, request) }
        return matches.fold(RequestMatch.RequestMismatch as RequestMatch) { bestResult, current ->
            return@fold if(current is RequestMatch.FullRequestMatch && (bestResult is RequestMatch.RequestMismatch || bestResult is RequestMatch.PartialRequestMatch))  {
                current
            } else if(current is RequestMatch.FullRequestMatch && bestResult is RequestMatch.FullRequestMatch) {
                throw PactMergeException("Multiple interactions have matched this request: ${current.interaction.uniqueKey()} and ${current.interaction.uniqueKey()}")
            } else if (current is RequestMatch.PartialRequestMatch && bestResult is RequestMatch.RequestMismatch) {
                current
            } else if (current is RequestMatch.PartialRequestMatch && bestResult is RequestMatch.PartialRequestMatch) {
                if(current.problems.size < bestResult.problems.size) {
                    current
                } else {
                    bestResult
                }
            } else {
                bestResult
            }
        }
    }

    private fun compareRequest(expected: RequestResponseInteraction, actual: RecordedRequest): RequestMatch {
        val mismatches = requestMismatches(expected.request, actual)
        return decideRequestMatch(expected, mismatches)
    }

    private fun requestMismatches(expected: Request, actual: RecordedRequest): List<RequestMatchProblem> {
        return LinkedList<RequestMatchProblem>().apply {
            addAll(Matching.matchMethod(expected.method, actual.method))
            addAll(Matching.matchPath(expected, actual))
            addAll(Matching.matchQuery(expected, actual))
            addAll(Matching.matchCookie(expected.cookie(), actual.headers.values("cookie")))
            addAll(Matching.matchRequestHeaders(expected, actual))
            addAll(Matching.matchBody(expected, actual, allowUnexpectedKeys))
        }
    }

    private fun decideRequestMatch(expected: RequestResponseInteraction, problems: List<RequestMatchProblem>): RequestMatch {
        return when {
            problems.isEmpty() -> RequestMatch.FullRequestMatch(expected)
            isPartialMatch(problems) -> RequestMatch.PartialRequestMatch(expected, problems)
            else -> RequestMatch.RequestMismatch
        }
    }

    private fun isPartialMatch(problems: List<RequestMatchProblem>): Boolean {
        return !problems.any {
            it is RequestMatchProblem.PathMismatch || it is RequestMatchProblem.MethodMismatch
        }
    }

    sealed class RequestMatch {
        object RequestMismatch: RequestMatch()
        class FullRequestMatch(val interaction: RequestResponseInteraction): RequestMatch()
        class PartialRequestMatch(val interaction: RequestResponseInteraction, val problems: List<RequestMatchProblem>): RequestMatch()
    }
}