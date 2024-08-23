package au.com.dius.pact.model

import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags

data class MergeResult(val ok: Boolean, val message: String, val result: Pact? = null)

/**
 * Utility class for merging two pacts together, checking for conflicts
 */
object PactMerge {

    @JvmStatic
    fun merge(newPact: Pact, existing: Pact): MergeResult {
        if (!newPact.compatibleTo(existing)) {
            return MergeResult(false, "Cannot merge pacts as they are not compatible")
        }
        if (existing.interactions.isEmpty() || newPact.interactions.isEmpty()) {
            existing.mergeInteractions(newPact.interactions)
            return MergeResult(true, "", existing)
        }

        var conflicts = existing.conflictsWith(newPact)
        var adaptedNewPact = newPact
        if (FeatureFlags.isFeatureEnabled(Feature.MERGE_REMOVE_EXACT_DUPLICATES)) {
            val exactMatches = conflicts.filter { (first, second) ->
                first.conflictsExactlyWith(second)
            }.toSet()
            adaptedNewPact = FilteredPact(adaptedNewPact) { interaction ->
                !exactMatches.any { it.second === interaction }
            }

            conflicts = conflicts.minus(exactMatches)
        }
        return if (conflicts.isEmpty()) {
            existing.mergeInteractions(adaptedNewPact.interactions)
            MergeResult(true, "", existing)
        } else {
            MergeResult(
                false,
                "Cannot merge pacts as there were ${conflicts.size} conflict(s) " +
                    "between the interactions - ${conflicts.joinToString("\n")}"
            )
        }
    }
}