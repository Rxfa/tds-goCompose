package mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import mongo.Collection
import org.dotenv.vault.dotenvVault
import java.io.Closeable
import org.bson.Document
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import kotlinx.coroutines.runBlocking





val dotenv = dotenvVault()

class MongoDriver (name: String? = null): Closeable {
    val database: MongoDatabase
    private val client: MongoClient
    init{
        /*
        val envConn =
            dotenv["MONGO_DB_CONN"] ?: throw MongoClientException("A MongoDB connection string is required")
        val dbName = requireNotNull(name ?: ConnectionString(envConn).database){
            "Database name is required in either the constructor or the connection string"
        }


        client = MongoClient.create(envConn)
        database = client.getDatabase(dbName)

         */
        val connectionString =
            dotenv["MONGO_DB_CONN"] ?: throw MongoClientException("A MongoDB connection string is required")
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .serverApi(serverApi)
            .build()
        // Create a new client and connect to the server
        MongoClient.create(mongoClientSettings).use { mongoClient ->
            client=mongoClient
            database = mongoClient.getDatabase("admin")
            runBlocking {
                database.runCommand(Document("ping", 1))
            }
            println("Pinged your deployment. You successfully connected to MongoDB!")
        }
    }


    override fun close() = client.close()
}

class Collection<T: Any>(val collection: MongoCollection<T>)

inline fun <reified T: Any> MongoDriver.getCollection(id: String) =
    Collection(database.getCollection(id, T::class.java))

suspend inline fun <reified T: Any> MongoDriver.getAllCollections() =
    database.listCollectionNames().toList().map{ getCollection<T>(it) }

suspend fun <T: Any> Collection<T>.getAllDocuments(): List<T> = collection.find().toList()

suspend fun <T: Any, K> Collection<T>.getDocument(id: K): T? = collection.find(Filters.eq(id)).firstOrNull()

suspend fun <T: Any> Collection<T>.insertDocument(doc: T): Boolean = collection.insertOne(doc).insertedId != null

suspend fun <T: Any, K> Collection<T>.replaceDocument(id: K, doc: T): Boolean =
    collection.replaceOne(Filters.eq(id), doc).modifiedCount == 1L


suspend fun <T: Any, K> Collection<T>.deleteDocument(id: K): Boolean =
    collection.deleteOne(Filters.eq("_id", id)).deletedCount == 1L

suspend fun <T: Any, K> Collection<T>.deleteAllDocuments(): Boolean =
    collection.deleteMany(Filters.exists("_id")).wasAcknowledged()


