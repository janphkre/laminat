package au.com.dius.pact.model.merge

import au.com.dius.pact.model.Pact

data class PactMergeResult(val ok: Boolean, val message: String, val result: Pact? = null)