package hnau.common.kotlin

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable

class SingleKeyCache<K, V> private constructor(
    private val createValue: (K) -> V,
    private var cached: Pair<K, V>?,
) {

    constructor(
        createValue: (K) -> V,
    ) : this(
        createValue = createValue,
        cached = null,
    )

    @Synchronized
    operator fun get(key: K): V {
        cached
            ?.takeIf { (cachedKey) -> cachedKey == key }
            ?.let { (_, value) -> return value }
        val result = createValue(key)
        cached = key to result
        return result
    }

    open class Serializer<K, V>(
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>,
        private val createValue: (K) -> V,
    ) : KSerializer<SingleKeyCache<K, V>> by MappingKSerializer(
        base = Surrogate.serializer(keySerializer, valueSerializer).nullable,
        mapper = Mapper(
            direct = { surrogate ->
                SingleKeyCache(
                    createValue = createValue,
                    cached = surrogate?.let { (key, value) -> key to value },
                )
            },
            reverse = { singleKeyCache ->
                singleKeyCache.cached?.let { (key, value) ->
                    Surrogate(key, value)
                }
            },
        ),
    ) {

        val emptyInstance = SingleKeyCache(createValue)

        @Serializable
        private data class Surrogate<K, V>(
            val key: K,
            val value: V,
        )
    }
}
