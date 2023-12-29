package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import storage.Storage

typealias GameStorage = Storage<String, Game>

suspend fun GameStorage.slowRead(key: String): Game? {
    fun log(label: String) = println(
        "$label: thread=${Thread.currentThread().name}\ttime=${System.currentTimeMillis() / 1_000}"
    )
    log("slowRead1")
    val res = withContext(Dispatchers.IO) {
        log("slowRead2")
        Thread.sleep(5_000)
        log("SlowRead3")
        read(key)
    }
    log("SlowRead4")
    return res
}