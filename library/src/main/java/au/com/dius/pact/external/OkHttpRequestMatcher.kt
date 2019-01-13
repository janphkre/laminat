package au.com.dius.pact.external

import au.com.dius.pact.model.*
import au.com.dius.pact.matchers.Matching
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

internal class OkHttpRequestMatcher {

    fun findInteraction(
        interactions: List<RequestResponseInteraction>,
        request: RecordedRequest
    ): RequestResponseInteraction {
        val matchResult = matchInteraction(interactions, request)
        when (matchResult) {
            is RequestMatch.FullRequestMatch -> {
                return matchResult.interaction
            }
            is RequestMatch.PartialRequestMatch -> {
                val interaction = matchResult.interaction
                throw PartialRequestMismatchException(interaction)
            }
            else -> {
                throw RequestMismatchException()
            }
        }
    }

    private fun matchInteraction(interactions: List<RequestResponseInteraction>, actual: RecordedRequest): RequestMatch {
        val matches = interactions.map { compareRequest(it, actual) }
        return matches.fold(RequestMatch.RequestMismatch as RequestMatch) { bestResult, current ->
            return@fold if((current is RequestMatch.PartialRequestMatch && bestResult is RequestMatch.RequestMismatch) ||
                    (current is RequestMatch.FullRequestMatch && (bestResult is RequestMatch.RequestMismatch || bestResult is RequestMatch.PartialRequestMatch))) {
                current
            } else {
                bestResult
            }
        }
    }

    private fun compareRequest(expected: RequestResponseInteraction, actual: RecordedRequest): RequestMatch {
        val mismatches = requestMismatches(expected.request, actual)
        return decideRequestMatch(expected, mismatches)
    }

    private fun requestMismatches(expected: Request, actual: RecordedRequest): List<Matching.RequestMatchProblem> {
        return LinkedList<Matching.RequestMatchProblem>().apply {
            add(Matching.matchMethod(expected.method, actual.method))
            add(Matching.matchPath(expected, actual))
            addAll(Matching.matchQuery(expected, actual))
            addAll(Matching.matchCookie(toOptionalList(expected.cookie), toOptionalList(actual.cookie)))
            addAll(Matching.matchRequestHeaders(expected, actual))
            addAll(Matching.matchBody(expected, actual, allowUnexpectedKeys))
        }
    }

    private fun decideRequestMatch(expected: RequestResponseInteraction, problems: List<Matching.RequestMatchProblem>): RequestMatch {
        return if (problems.isEmpty()) {
            RequestMatch.FullRequestMatch(expected)
        }
        else if (isPartialMatch(problems)) {
            RequestMatch.PartialRequestMatch(expected, problems)
        }
        else {
            RequestMatch.RequestMismatch
        }
    }

    private fun isPartialMatch(problems: List<Matching.RequestMatchProblem>): Boolean {
        return !problems.exists {
            case PathMismatch(_,_,_) | MethodMismatch(_,_) => true
            case _ => false
        }
    }

    class PartialRequestMismatchException(val interaction: RequestResponseInteraction): Exception()

    class RequestMismatchException: Exception()

    private sealed class RequestMatch {
        object RequestMismatch: RequestMatch()
        class FullRequestMatch(val interaction: RequestResponseInteraction): RequestMatch()
        class PartialRequestMatch(val interaction: RequestResponseInteraction, val problems: List<Matching.RequestMatchProblem>): RequestMatch()
    }
}