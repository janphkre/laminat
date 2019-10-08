package au.com.dius.pact.matchers

interface MismatchFactory<T> {
    fun create(expected: Any?, actual: Any?, message: String): T

    object PathMismatchFactory : MismatchFactory<RequestMatchProblem> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem {
            return RequestMatchProblem.PathMismatch(expected as? String, actual as? String)
        }
    }

    object QueryMismatchFactory : MismatchFactory<RequestMatchProblem> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem {
            return RequestMatchProblem.QueryMismatch(message, "")
        }
    }

    object HeaderMismatchFactory : MismatchFactory<RequestMatchProblem> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem {
            return RequestMatchProblem.HeaderMismatch("", message)
        }
    }

    object BodyMismatchFactory : MismatchFactory<RequestMatchProblem> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem {
            return RequestMatchProblem.BodyMismatch(message)
        }
    }
}