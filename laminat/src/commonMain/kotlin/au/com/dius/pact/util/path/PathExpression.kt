package au.com.dius.pact.util.path

class PathExpression(
    val path: String
): Comparable<String> {
    val compiledPath by lazy { computeCompiledPath() }

    private fun computeCompiledPath(): ArrayList<AST.PathToken>? {
        return Parser().compile(path).let {
            if (it.successful() && !it.isEmpty) {
                val parseList = it.get()
                val result = ArrayList<AST.PathToken>(parseList?.size() ?: 0)
                for (i in 0 until parseList.size()) {
                    result.add(i, parseList.apply(i))
                }
                result
            } else {
                null
            }
        }
    }

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