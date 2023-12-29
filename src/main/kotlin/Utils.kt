import model.Board
import model.Captures
import model.Game

@Suppress("KotlinConstantConditions")
val blackScore: Double
    get() = when(BOARD_SIZE){
        9 -> -3.5
        13 -> -4.5
        19 -> -5.5
        else -> throw IllegalArgumentException("Invalid board size.")
    }

fun Board.seriesOfPlays(moves: List<String>): Board {
    var board = this
    for(move in moves)
        board = board.play(move).first
    return board
}

fun Game.seriesOfMoves(moves: List<String>): Game {
    var game = this
    for(move in moves)
        game = game.move(move)
    return game
}

operator fun <T: Number> Pair<Double, Double>.plus(other: Pair<T, T>): Pair<Double, Double> =
    when(other.first){
        is Double -> (first + other.first as Double) to (second + other.second as Double)
        is Int -> (first + other.first as Int) to (second + other.second as Int)
        else -> throw IllegalArgumentException("Only run with Integers and Doubles")
    }

operator fun Pair<Double, Double>.plus(other: Captures): Pair<Double, Double> = (first + other.black) to (second + other.white)

