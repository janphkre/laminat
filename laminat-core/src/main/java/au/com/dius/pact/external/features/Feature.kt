package au.com.dius.pact.external.features

enum class Feature(val default: Boolean) {
    /**
     * Tries to merge an existing pact file while serializing with PactJsonifier instead of overwriting the existing file.
     * See https://github.com/pact-foundation/pact-jvm/issues/804
     */
    MERGE_EXISTING_PACTS_FILE(false),

    /**
     * Removes duplicates while merging two pacts instead of throwing an error.
     * The logic of when two interactions are equal were changed between v3.5.x and v.3.6.x.
     * This logic change treates two exactly same interactions not as a pact merge conflict anymore with this flag.
     */
    MERGE_REMOVE_EXACT_DUPLICATES(false),

    /**
     * Disable this flag to not serialize null values in the body of interactions.
     * See https://github.com/pact-foundation/pact-jvm/issues/877
     */
    NULL_VALUES_JSON_BODY_GENERATOR(true), // TODO: TEST THIS

    /**
     * Enable this flag to disallow the merge of pacts that differ in their version.
     * E.g. a pact file that has a v2 pact in it may not be merged with a v3 pact serialization config.
     * See https://github.com/pact-foundation/pact-jvm/issues/877
     */
    MERGE_DISALLOW_DIFFERENT_PACT_VERSIONS(false)
}