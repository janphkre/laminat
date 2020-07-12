package au.com.dius.pact.util.path

class PathExpression(
    val path: String
): Comparable<String> {

    val compiledPath by lazy { PathToken.parsePath(path) }

    override fun compareTo(other: String): Int {
        return path.compareTo(other)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is PathExpression -> path == other.path
            else -> path == other
        }
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}