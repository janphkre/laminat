package au.com.dius.pact.model.generators

data class QueryResult<T>(var value: T, val key: Any? = null, val parent: T? = null)