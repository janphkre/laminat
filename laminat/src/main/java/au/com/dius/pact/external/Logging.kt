package au.com.dius.pact.external

object Logging {

    enum class Level {
        INFO, WARN, ERROR
    }

    private var listener: ((Level, String) -> Unit)? = null

    internal fun info(lambda: () -> String) {
        listener?.invoke(Level.INFO, lambda())
    }

    internal fun warn(lambda: () -> String) {
        listener?.invoke(Level.WARN, lambda())
    }

    internal fun error(lambda: () -> String) {
        listener?.invoke(Level.ERROR, lambda())
    }

    fun setListener(lambda: ((Level, String) -> Unit)?) {
        listener = lambda
    }
}