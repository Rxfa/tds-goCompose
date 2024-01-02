import model.*
import storage.*
import mongo.MongoDriver
import org.junit.Assert
import org.junit.jupiter.api.Test
import viewModel.AppViewModel
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.IllegalArgumentException

class MatchTest {
    val driver = MongoDriver(isTest = true)
    val game = Game()
    val serializer = GameSerializer
    val gs = MongoStorage<String, Game>("testes", driver, serializer)
    @Test
    suspend fun `test two moves while alone in game`() {
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            blacktestMatch.play("a1")
            blacktestMatch.play("b2")
        }
    }

    @Test
    suspend fun `test one move each`(){
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        val whiteTestMatch = RunningMatch(gs,"teste1",Player.WHITE,game)
        org.junit.jupiter.api.assertDoesNotThrow {
            blacktestMatch.play("b3")
            whiteTestMatch.play("c1")
            blacktestMatch.play("d5")
            whiteTestMatch.play("e7")
        }
    }

    @Test
    suspend fun `test player pass twice`(){
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            blacktestMatch.pass()
            blacktestMatch.pass()
        }
    }

    @Test
    suspend fun `test if game is over with one pass each` (){
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        val whiteTestMatch = RunningMatch(gs,"teste1",Player.WHITE,game)
        blacktestMatch.pass()
        whiteTestMatch.pass()
        assertTrue(game.isOver)
    }

    @Test
    suspend fun `test capture` (){
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        val whiteTestMatch = RunningMatch(gs,"teste1",Player.WHITE,game)
        whiteTestMatch.play("b8")
        blacktestMatch.play("b9")
        whiteTestMatch.pass()
        blacktestMatch.play("a8")
        whiteTestMatch.pass()
        blacktestMatch.play("b7")
        whiteTestMatch.pass()
        blacktestMatch.play("c8")
        val testcaptures=blacktestMatch.game.captures
        assertTrue (testcaptures.black==1)

    }

    @Test
    suspend fun `test illegal capture`(){
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        val whiteTestMatch = RunningMatch(gs,"teste1",Player.WHITE,game)
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            blacktestMatch.play("a8")
            whiteTestMatch.pass()
            blacktestMatch.play("b9")
            whiteTestMatch.play("a9")
        }
    }

    @Test
    suspend fun `test score`(){
        val blacktestMatch = RunningMatch(gs, "teste1", Player.BLACK, game)
        val whiteTestMatch = RunningMatch(gs,"teste1",Player.WHITE,game)
        blacktestMatch.pass()
        whiteTestMatch.play("b8")
        blacktestMatch.play("b9")
        whiteTestMatch.play("h9")
        blacktestMatch.play("a8")
        whiteTestMatch.play("i8")
        blacktestMatch.play("b7")
        whiteTestMatch.pass()
        blacktestMatch.play("c8")
        whiteTestMatch.pass()
        blacktestMatch.pass()
        assertTrue (blacktestMatch.game.score.black==-0.5 && whiteTestMatch.game.score.white==1.0)
    }


}



