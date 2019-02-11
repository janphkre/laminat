package au.com.dius.pact.matchers

sealed class RequestMatchProblem(val message: String) {

    override fun toString(): String = message

    object None: RequestMatchProblem(
        "No mismatch"
    )
    class MethodMismatch(expected: String, actual: String?): RequestMatchProblem(
        "MismatchedMethod: Expected '$expected' but got '$actual'"
    )
    class PathMismatch(expected: String?, actual: String?): RequestMatchProblem(
        "MismatchedPath: Expected '$expected' but got '$actual'"
    )
    class QueryMismatch(message: String, path: String): RequestMatchProblem(
        "MismatchedQuery $path:\n$message"
    )
    class HeaderMismatch(name: String, message: String): RequestMatchProblem(
        "MismatchedHeader $name:\n$message"
    )
    class CookieMismatch(expected: List<String>?, actual: List<String>?): RequestMatchProblem(
        "MismatchedCookie: Expected '${expected?.joinToString(";")}' but got '${actual?.joinToString(";")}'"
    )
    class BodyTypeMismatch(expectedMime: String?, actualMime: String?): RequestMatchProblem(
        "MismatchedBodyType: Expected '$expectedMime' but got '$actualMime'"
    )
    class BodyMismatch(message: String, path: String? = null): RequestMatchProblem(
        "MismatchedBody on $path:\n$message"
    )
}