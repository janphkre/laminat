package au.com.dius.pact.matchers

sealed class RequestMatchProblem() {
    abstract override fun toString(): String

    class MethodMismatch(val expected: String, val actual: String?): RequestMatchProblem() {
        override fun toString(): String = "MismatchedMethod: Expected '$expected' but got '$actual'"
    }
    class PathMismatch(val expected: String?, val actual: String?): RequestMatchProblem() {
        override fun toString(): String = "MismatchedPath: Expected '$expected' but got '$actual'"
    }
    class QueryMismatch(val message: String, val path: String): RequestMatchProblem() {
        override fun toString(): String = "MismatchedQuery $path:\n$message"
    }
    class HeaderMismatch(val name: String, val message: String): RequestMatchProblem() {
        override fun toString(): String = "MismatchedHeader $name:\n$message"
    }
    class CookieMismatch(val expected: List<String>?, val actual: List<String>?): RequestMatchProblem() {
        override fun toString(): String = "MismatchedCookie: Expected '${expected?.joinToString(";")}' but got '${actual?.joinToString(";")}'"
    }
    class BodyTypeMismatch(val expectedMime: String, val actualMime: String?): RequestMatchProblem() {
        override fun toString(): String = "MismatchedBodyType: Expected '$expectedMime' but got '$actualMime'"
    }
    class BodyMismatch(val message: String, val path: String? = null): RequestMatchProblem() {
        override fun toString(): String = "MismatchedBody on $path:\n$message"
    }
}