package au.com.dius.pact

import io.kotlintest.Spec
import io.kotlintest.TestCaseContext
import io.kotlintest.specs.StringSpec

abstract class StringSpecExt(
    lambda: (StringSpecExt.() -> Unit) = {}
) : StringSpec() {

    override val oneInstancePerTest: Boolean = false

    private val beforeTestInterceptors: MutableList<() -> Unit> = mutableListOf()
    private val afterTestInterceptors: MutableList<() -> Unit> = mutableListOf()
    private val beforeSpecInterceptors: MutableList<() -> Unit> = mutableListOf()
    private val afterSpecInterceptors: MutableList<() -> Unit> = mutableListOf()

    init {
        lambda()
    }

    override fun interceptTestCase(context: TestCaseContext, test: () -> Unit) {
        beforeTestInterceptors.forEach { it.invoke() }
        try {
            super.interceptTestCase(context, test)
        } finally {
            afterTestInterceptors.reversed().forEach { it.invoke() }
        }
    }

    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        beforeSpecInterceptors.forEach { it.invoke() }
        try {
            super.interceptSpec(context, spec)
        } finally {
            afterSpecInterceptors.reversed().forEach { it.invoke() }
        }
    }

    fun beforeTest(lambda: () -> Unit) {
        beforeTestInterceptors.add(lambda)
    }

    fun afterTest(lambda: () -> Unit) {
        afterTestInterceptors.add(lambda)
    }

    fun beforeSpec(lambda: () -> Unit) {
        beforeSpecInterceptors.add(lambda)
    }

    fun afterSpec(lambda: () -> Unit) {
        afterSpecInterceptors.add(lambda)
    }
}