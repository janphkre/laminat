package au.com.dius.pact.external

import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact

class RecordingEngineBehaviour(
    private val delegate: PactEngineBehaviour,
) : PactEngineBehaviour by delegate {

    private val recordings = mutableListOf<RequestMatch>()
    private var observer: ((IncomingRequest, RequestMatch) -> Unit)? = null

    init {
        delegate.observeMatches { incomingRequest, requestMatch ->
            synchronized(recordings) {
                recordings.add(requestMatch)
            }
            observer?.invoke(incomingRequest, requestMatch)
        }
    }

    /**
     * Validates that a given [interaction] was executed on the web server successfully.
     *
     * @throws InteractionValidationException
     */
    fun validateInteraction(interaction: RequestResponseInteraction, options: VerificationOptions) {
        val matches = synchronized(recordings) {
            recordings.filter {
                (!options.matchUniqueKey || interaction.uniqueKey() == it.interaction?.uniqueKey()) &&
                    interaction.request == it.interaction?.request
            }
        }
        val fullMatches = matches.filterIsInstance<RequestMatch.FullRequestMatch>()
        if (fullMatches.size != matches.size) {
            val message = matches.mapNotNull { match ->
                when (match) {
                    is RequestMatch.FullRequestMatch -> null
                    is RequestMatch.PartialRequestMatch -> match.toErrorMessage()
                    is RequestMatch.RequestMismatch -> match.toErrorMessage()
                }
            }.joinToString(separator = "\n", prefix = "Found mismatched requests while checking recorded interactions for ${interaction.uniqueKey()}:\n")
            throw InteractionValidationException(message)
        }
        if (options.exact != fullMatches.size) {
            throw InteractionValidationException("Found ${fullMatches.size} performed interactions when expecting ${options.exact} count for ${interaction.uniqueKey()}")
        }
    }

    /**
     * Validates that all interaction in the given [pact] were executed on the web server successfully.
     * @throws PactValidationException
     */
    fun validateInteractions(pact: RequestResponsePact, options: VerificationOptions = VerificationOptions()) {
        val interactionFailures = pact.requestResponseInteractions.mapNotNull { requestResponseInteraction ->
            try {
                validateInteraction(requestResponseInteraction, options)
                null
            } catch (e: InteractionValidationException) {
                e
            }
        }
        if (interactionFailures.isEmpty()) {
            return
        }
        throw PactValidationException(interactionFailures)
    }

    /**
     * Validates that all interaction in the given [pacts] were executed on the web server successfully.
     * @throws PactValidationException
     */
    fun validateInteractions(pacts: List<RequestResponsePact>, options: VerificationOptions = VerificationOptions()) {
        pacts.forEach { validateInteractions(it, options) }
    }

    override fun teardown() {
        delegate.teardown()
        resetRecordings()
    }

    /**
     * Resets the recordings of this web server.
     */
    fun resetRecordings() {
        synchronized(recordings) {
            recordings.clear()
        }
    }

    override fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        this.observer = observer
    }

}