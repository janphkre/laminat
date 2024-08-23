package au.com.dius.pact.external

class PactValidationException(
    interactionExceptions: List<InteractionValidationException>
) : Exception(interactionExceptions.joinToString(prefix = "Could not match pact fully. Following interactions did not match:\n", separator = "\n") { it.message.toString() })