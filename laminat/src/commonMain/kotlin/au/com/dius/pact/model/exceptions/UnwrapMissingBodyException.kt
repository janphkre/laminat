package au.com.dius.pact.model.exceptions

/**
 * Exception class to indicate unwrap of a missing body value
 */
class UnwrapMissingBodyException(message: String) : RuntimeException(message)