package au.com.dius.pact.external

import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact

/**
 * Pact Web server that allows to validate how incoming requests were matched.
 *
 * @author Jan Phillip Kretzschmar
 */
class RecordingPactWebServer(
    private val delegate: PactWebServer,
) : PactWebServer by delegate {

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
    fun validateInteraction(interaction: RequestResponseInteraction) {
        val match = synchronized(recordings) {
            recordings.firstOrNull {
                interaction.uniqueKey() == it.interaction?.uniqueKey() &&
                    interaction.request == it.interaction?.request
            } ?: throw InteractionValidationException("Could not find any interaction that matches the given interaction")
        }
        when (match) {
            is RequestMatch.FullRequestMatch -> { }
            is RequestMatch.PartialRequestMatch -> throw InteractionValidationException(match.toErrorMessage())
            is RequestMatch.RequestMismatch -> throw InteractionValidationException(match.toErrorMessage())
        }
    }

    /**
     * Validates that all interaction in the given [pact] were executed on the web server successfully.
     * @throws PactValidationException
     */
    fun validateInteractions(pact: RequestResponsePact) {
        val interactionFailures = pact.requestResponseInteractions.mapNotNull { requestResponseInteraction ->
            try {
                validateInteraction(requestResponseInteraction)
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
    fun validateInteractions(pacts: List<RequestResponsePact>) {
        pacts.forEach { validateInteractions(it) }
    }

    override fun teardown() {
        delegate.teardown()
        synchronized(recordings) {
            recordings.clear()
        }
    }

    override fun clearPacts() {
        delegate.clearPacts()
        synchronized(recordings) {
            recordings.clear()
        }
    }

    override fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        this.observer = observer
    }
}