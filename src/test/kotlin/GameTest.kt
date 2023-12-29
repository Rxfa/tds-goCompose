import model.Game
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `Test score`(){
        val moves = listOf("a1", "a2", "d5", "b2", "c5", "b1")
        val game = Game().seriesOfMoves(moves)
        assertEquals(blackScore to 2.0, game.score.black to game.score.white)
    }

}