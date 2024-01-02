import mongo.MongoDriver
import org.junit.jupiter.api.Test

class StorageTest {
    val driver: MongoDriver = MongoDriver()
        //MongoStorage<String, Game>("games", driver, GameSerializer)

    @Test
    fun `test game creation`(){

    }
}