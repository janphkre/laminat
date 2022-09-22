package au.com.dius.pact.external.json


inline fun <reified T> Json.getValue(): T {
    return getValueOrNull<T>() ?: throw UnsupportedOperationException("Can not get a value from null!")
}

inline fun <reified T> Json.getValueOrNull(): T? {
    when(this) {
        is Json.Null -> return null
        is Json.StringPrimitive -> {
            if (T::class == String::class) {
                return asString() as T
            }
        }
        is Json.BooleanPrimitive -> {
            if (T::class == Boolean::class) {
                return asBoolean() as T
            }
        }
        is Json.NumberPrimitive -> {
            when(T::class) {
                Int::class -> return asInt() as T
                Float::class -> return asFloat() as T
                Number::class -> return asNumber() as T
            }
        }
        else -> {}
    }
    throw UnsupportedOperationException("Can not get a value of type ${T::class} from a ${this::class.simpleName}")
}