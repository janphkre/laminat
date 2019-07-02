package au.com.dius.pact.model

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

        val conflicts = existing.conflictsWith(newPact)
        if (conflicts.isEmpty()) {
            existing.mergeInteractions(newPact.interactions)
            return MergeResult(true, "", existing)
        } else {
            return MergeResult(
                false, "Cannot merge pacts as there were ${conflicts.size} conflict(s) " +
                    "between the interactions - ${conflicts.joinToString("\n")}"
            )
        }
    }
}