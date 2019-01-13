package au.com.dius.pact.matchers

interface MismatchFactory<T> {
    fun create(expected: Any?, actual: Any?, message: String): T

    object PathMismatchFactory: MismatchFactory<RequestMatchProblem.PathMismatch> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem.PathMismatch {
            return RequestMatchProblem.PathMismatch(expected as? String, actual as? String)
        }

    }

    object QueryMismatchFactory: MismatchFactory<RequestMatchProblem.QueryMismatch> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem.QueryMismatch {
            return RequestMatchProblem.QueryMismatch(message, "")
        }

    }

    object HeaderMismatchFactory: MismatchFactory<RequestMatchProblem.HeaderMismatch> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem.HeaderMismatch {
            return RequestMatchProblem.HeaderMismatch("", message)
        }

    }

    object BodyMismatchFactory: MismatchFactory<RequestMatchProblem.BodyMismatch> {
        override fun create(expected: Any?, actual: Any?, message: String): RequestMatchProblem.BodyMismatch {
            return RequestMatchProblem.BodyMismatch(message)
        }

    }
}
