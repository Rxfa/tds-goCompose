// JsonFileStorage
package storage

import kotlin.io.path.*

class JsonFileStorage<Key, Data>(
    private val baseDirectory: String,
    private val serializer: Serializer<Data>
) : Storage<Key, Data> {
    init {
        with(Path(baseDirectory)) {
            if (!exists()) createDirectory()
            else check(isDirectory()) { "$name is not a directory" }
        }
    }

    private fun path(key: Key) = Path("$baseDirectory/$key.json")

    override suspend fun create(key: Key, data: Data) =
        path(key).let {
            check(!it.exists()) { "File $key already exists" }
            it.writeText(serializer.serialize(data))
        }

    override suspend fun read(key: Key) =
        path(key).let {
            check(it.exists()) { "File $key does not exist" }
            serializer.deserialize(it.readText())
        }

    override suspend fun update(key: Key, data: Data) =
        path(key).let {
            check(it.exists()) { "File $key does not exist" }
            it.writeText(serializer.serialize(data))
        }

    override suspend fun delete(key: Key) = check(path(key).deleteIfExists()) { "File $key does not exist" }

}