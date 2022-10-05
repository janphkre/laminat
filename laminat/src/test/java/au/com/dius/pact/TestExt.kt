package au.com.dius.pact

import org.junit.Assert
import kotlin.reflect.KClass

inline infix fun <T: Any, reified U: Any> T?.shouldBeInstance(uClass : KClass<U>) {
    Assert.assertTrue("$this is not instance of ${uClass.qualifiedName}", this is U)
}

inline infix fun <T: Throwable, reified U: Throwable> T?.shouldBeException(uClass: KClass<U>) {
    Assert.assertThrows("$this is not instance of ${uClass.qualifiedName}", uClass.java) {
        throw this!!
    }
}
