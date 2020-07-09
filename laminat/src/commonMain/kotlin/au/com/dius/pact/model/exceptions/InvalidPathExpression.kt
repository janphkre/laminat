package au.com.dius.pact.model.exceptions

/**
 * Exception class to indicate an invalid path expression used in a matcher or generator
 */
class InvalidPathExpression(message: String) : RuntimeException(message)