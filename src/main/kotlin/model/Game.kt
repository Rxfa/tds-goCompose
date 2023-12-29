package model

import blackScore
import kotlinx.serialization.Serializable
import plus
import storage.GameSerializer
import storage.JsonFileStorage
import kotlin.system.exitProcess

@Serializable
data class Game(
    val board: Board = Board(),
    private val captures: Captures = Captures()
    ){

    private val isOver = board.pass.all()

    internal fun IamOwner(player: Player) = player == Player.WHITE

    internal fun isMyTurn(player: Player) = board.player == player


    suspend fun execute(command:String): Game {
        val splitInput=command.split(" ")
        return when(splitInput[0]){
            "new" -> Game()
            "play" -> move(splitInput[1])
            "pass" -> pass()
            "save" -> also { saveBoard(splitInput[1]) }
            "load" -> loadBoard(splitInput[1])
            "exit" -> exitProcess(0)
            else -> throw IllegalArgumentException("Invalid command $command")
        }
    }

    fun winner():Player{
        return if(score.black > score.white) Player.BLACK
        else Player.WHITE
    }

    fun showCurrentPlayer():Player{
        return this.board.player
    }
    fun stateOfGame():Boolean{
        return isOver
    }

    fun getCaptures(): Captures{
        return captures
    }

    fun move(move: String): Game {
        require(!isOver){"Game over"}
        val (board, c) = board.play(move)
        val pair = if(board.player == Player.WHITE) (c to 0) else (0 to c)
        return copy(board = board, captures = (captures + pair))
    }

    val score: Score
        get() {
            val value = (blackScore to 0.0) +  board.countTerritory() +  captures
            return Score(black = value.first, white = value.second)
        }

    fun pass()=copy(board=board.pass())

    private suspend fun saveBoard(name:String) =
        JsonFileStorage<String, Game>("games/", GameSerializer).create(name, this).also {
            println("Game saved successfully")
        }

    private suspend fun loadBoard(name: String): Game =
        JsonFileStorage<String, Game>("games/", GameSerializer).read(name)


    fun show(){
        val turn = "Turn: ${board.player.state.value} (${board.player.name})"
        val captures = "Captures: ${State.BLACK.value}=${captures.black} - ${State.WHITE.value}=${captures.white}"
        val score = "Score: ${State.BLACK.value}=${score.black} - ${State.WHITE.value}=${score.white}"
        println(board.show())
        return when{
            board.pass.all() -> println("GAME OVER\t\t$score")
            board.pass.none() -> println("$turn\t$captures")
            else -> println("Player ${board.player.other.state} passes.\t$turn")
        }
    }
}