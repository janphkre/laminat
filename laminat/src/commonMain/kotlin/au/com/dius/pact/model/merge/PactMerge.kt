package au.com.dius.pact.model.merge

import au.com.dius.pact.model.Pact
import kotlin.jvm.JvmStatic

/**
 * Utility class for merging two pacts together, checking for conflicts
 */
object PactMerge {

    @JvmStatic
    fun merge(newPact: Pact, existing: Pact): PactMergeResult {
        if (!newPact.compatibleTo(existing)) {
            return PactMergeResult(false, "Cannot merge pacts as they are not compatible")
        }
        if (existing.interactions.isEmpty() || newPact.interactions.isEmpty()) {
            existing.mergeInteractions(newPact.interactions)
            return PactMergeResult(true, "", existing)
        }

        val conflicts = existing.conflictsWith(newPact)
        if (conflicts.isEmpty()) {
            existing.mergeInteractions(newPact.interactions)
            return PactMergeResult(true, "", existing)
        } else {
            return PactMergeResult(
                false, "Cannot merge pacts as there were ${conflicts.size} conflict(s) " +
                    "between the interactions - ${conflicts.joinToString("\n")}"
            )
        }
    }
}