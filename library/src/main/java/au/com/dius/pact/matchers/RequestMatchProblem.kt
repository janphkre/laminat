package au.com.dius.pact.matchers

sealed class RequestMatchProblem {
    object None: RequestMatchProblem()
    class MethodMismatch(val expected: String, val actual: String?): RequestMatchProblem()
    class PathMismatch(val expected: String?, val actual: String?): RequestMatchProblem()
    class QueryMismatch(val message: String, val path: String): RequestMatchProblem()
    class HeaderMismatch(val name: String, val message: String): RequestMatchProblem()
    class CookieMismatch(val expected: List<String>?, val actual: List<String>?): RequestMatchProblem()
    class BodyTypeMismatch(val expectedMime: String, val actualMime: String?): RequestMatchProblem()
    class BodyMismatch(val message: String, val path: String? = null): RequestMatchProblem()
}