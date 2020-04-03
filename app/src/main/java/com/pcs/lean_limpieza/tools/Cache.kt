package com.pcs.lean_limpieza.tools

class Cache(private val flushInterval: Long = 5) {

    private var lastFlushTime = System.currentTimeMillis()

    private val cache = HashMap<Any, Any>()

    fun set(key: Any, value: Any) {
        this.cache[key] = value
    }

    fun remove(key: Any){
        recycle()
        this.cache.remove(key)
    }

    fun get(key: Any): Any?{
        recycle()
        return this.cache[key]
    }

    private fun clear() = this.cache.clear()

    private fun recycle() {
        val shouldRecycle = (System.currentTimeMillis() - lastFlushTime) >= (flushInterval*60*1000)
        if (!shouldRecycle) return
        clear()
    }

}