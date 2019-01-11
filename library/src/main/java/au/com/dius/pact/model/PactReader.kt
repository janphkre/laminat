package au.com.dius.pact.model

import java.net.URLDecoder

object PactReader {

    fun queryStringToMap(query: String?, decode: Boolean = true): Map<String, List<String>> {
        return query?.split('&')?.asSequence()?.map {
            it.split('=', limit= 2)
        }?.fold(HashMap()) { map, nameAndValue ->
            val name = if(decode) URLDecoder.decode(nameAndValue.first(), "UTF-8") else nameAndValue.first()
            val value = if(decode) URLDecoder.decode(nameAndValue.last(), "UTF-8") else nameAndValue.last()
            if (map.containsKey(name)) {
                (map[name]!! as MutableList<String>).add(value)
            } else {
                map[name] = mutableListOf(value)
            }
            map
        } ?: emptyMap()
    }
}