package au.com.dius.pact.model

import org.apache.http.Consts
import java.net.URLDecoder

object PactReader {

    fun queryStringToMap(query: String?, decode: Boolean = true): MutableMap<String, List<String>> {
        return query?.split('&')?.asSequence()?.map {
            it.split('=', limit = 2)
        }?.fold(HashMap()) { map, nameAndValue ->
            val name = if (decode) URLDecoder.decode(nameAndValue.first(), Consts.UTF_8.name()) else nameAndValue.first()
            val value = if (decode) URLDecoder.decode(nameAndValue.last(), Consts.UTF_8.name()) else nameAndValue.last()
            if (map.containsKey(name)) {
                (map[name]!! as MutableList<String>).add(value)
            } else {
                map[name] = mutableListOf(value)
            }
            map
        } ?: HashMap()
    }
}