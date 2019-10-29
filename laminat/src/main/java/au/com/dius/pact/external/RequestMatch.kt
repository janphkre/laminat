package au.com.dius.pact.external

import au.com.dius.pact.matchers.RequestMatchProblem
import au.com.dius.pact.model.RequestResponseInteraction

/**
 * A request match can either be a complete mismatch, a partial mismatch or a regular match.
 * Each of these matches have the interaction in them with which a match was tried.
 * The match object also contains detailed informations regarding the type of match e.g.
 * which problems where found or how well the match was matching.
 *
 * @author Jan Philip Kretzschmar
 */
sealed class RequestMatch {
    class RequestMismatch(val interaction: RequestResponseInteraction? = null, val problems: List<RequestMatchProblem>? = null) : RequestMatch()
    class FullRequestMatch(val interaction: RequestResponseInteraction, val matchedCount: Int) : RequestMatch()
    class PartialRequestMatch(val interaction: RequestResponseInteraction, val problems: List<RequestMatchProblem>) : RequestMatch()
}