package au.com.dius.pact.external

import au.com.dius.pact.matchers.RequestMatchProblem
import au.com.dius.pact.model.RequestResponseInteraction

sealed class RequestMatch {
    class RequestMismatch(val interaction: RequestResponseInteraction? = null, val problems: List<RequestMatchProblem>? = null) : RequestMatch()
    class FullRequestMatch(val interaction: RequestResponseInteraction, val matchedCount: Int) : RequestMatch()
    class PartialRequestMatch(val interaction: RequestResponseInteraction, val problems: List<RequestMatchProblem>) : RequestMatch()
}