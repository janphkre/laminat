package au.com.dius.pact.external.features

enum class Feature(val default: Boolean) {
    MERGE_EXISTING_PACTS_FILE(false),
    MERGE_REMOVE_EXACT_DUPLICATES(false)
}