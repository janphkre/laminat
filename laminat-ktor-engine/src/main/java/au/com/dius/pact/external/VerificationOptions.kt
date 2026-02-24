package au.com.dius.pact.external

/**
 * Options for the [RecordingPactWebServer] to define the type of verification.
 *
 * @author Jan Phillip Kretzschmar
 */
public data class VerificationOptions(
    internal val exact: Int = 1,
    internal val matchUniqueKey: Boolean = true,
)
