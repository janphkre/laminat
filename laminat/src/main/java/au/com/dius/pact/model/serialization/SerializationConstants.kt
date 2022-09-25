package au.com.dius.pact.model.serialization

object SerializationConstants {
    const val METADATA_KEY = "metadata"
    const val INTERACTIONS_KEY = "interactions"
    const val PROVIDER_KEY = "provider"
    const val CONSUMER_KEY = "consumer"
    const val PARAMS_KEY = "params"
    const val NAME_KEY = "name"

    const val REQUEST_KEY = "request"
    const val RESPONSE_KEY = "response"
    const val PROVIDER_STATES_KEY = "providerStates"
    @Deprecated("Use explicit list of states instead!")
    const val PROVIDER_STATE_KEY = "providerState"
    @Deprecated("Use explicit list of states instead!")
    const val PROVIDER_STATE_KEY_SNAKE_CASE = "provider_state"
    const val DESCRIPTION_KEY = "description"

    const val METHOD_KEY = "method"
    const val PATH_KEY = "path"
    @Deprecated("Use HEADERS_KEY instead!")
    const val HEADER_KEY = "header"
    const val HEADERS_KEY = "headers"
    const val QUERY_KEY = "query"
    const val BODY_KEY = "body"
    const val MATCHING_RULES_KEY = "matchingRules"
    @Deprecated("Use MATCHING_RULES_KEY instead")
    const val MATCHING_RULES_REQUEST_KEY = "requestMatchingRules"
    @Deprecated("Use MATCHING_RULES_KEY instead")
    const val MATCHING_RULES_RESPONSE_KEY = "responseMatchingRules"
    const val GENERATORS_KEY = "generators"
    const val STATUS_KEY = "status"

    const val TYPE_KEY = "type"

    const val MIN_KEY = "min"
    const val MAX_KEY = "max"
    const val DIGITS_KEY = "digits"
    const val FORMAT_KEY = "format"
    const val REGEX_KEY = "regex"
    const val SIZE_KEY = "size"
    const val EXPRESSION_KEY = "expression"
    const val DATA_TYPE_KEY = "dataType"

    const val MATCHERS_KEY = "matchers"
    const val COMBINE_KEY = "combine"
    const val MATCH_KEY = "match"
    const val NULL_KEY = "null"
    const val TIMESTAMP_KEY = "timestamp"
    const val TIME_KEY = "time"
    const val INCLUDE_KEY = "include"
    const val EQUALITY_KEY = "equality"
    const val VALUE_KEY = "value"
    const val DATE_KEY = "date"
    const val NUMBER_KEY = "number"
    const val INTEGER_KEY = "integer"
    const val DECIMAL_KEY = "decimal"
    @Deprecated("Use DECIMAL_KEY instead")
    const val REAL_KEY = "real"
    const val VALUES_KEY = "values"
}