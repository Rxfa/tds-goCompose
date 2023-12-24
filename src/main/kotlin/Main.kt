import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import model.Board
import model.Game
import model.Player
import mongo.MongoDriver
import androidx.compose.ui.graphics.Color
import model.Cell
import model.State
import viewModel.AppViewModel




val CELL_SIDE = 100.dp
val GRID_THICKNESS = 5.dp
val BOARD_SIDE = CELL_SIDE * BOARD_SIZE + GRID_THICKNESS* (BOARD_SIZE-1)
@Composable
@Preview
fun FrameWindowScope.App(driver: MongoDriver, exitFunction: () -> Unit) {
    val scope= rememberCoroutineScope()
    val vm = remember { AppViewModel(driver,scope) }

    MenuBar {
        Menu("Game") {
            Item("Start Game", onClick = vm::showNewGameDialog)
            Item("Join Game", onClick = vm::showJoinGameDialog)
            Item("Exit", onClick = {vm::exit;exitFunction()})
        }
        Menu("Play"){
            Item("Pass", onClick = vm::passRoundGame)
            Item("Show Captures", onClick = vm::showCaptures)
            Item("Show Final Score", onClick = vm::showScore)
        }
        Menu("Options"){
            Item("Show Last Played", onClick = vm::showLastPlayed)
        }
    }
    MaterialTheme{
        Column(horizontalAlignment)
    }




    /*
    var text by remember { mutableStateOf("Hello, World!") }
    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }

     */



}

@Composable
fun BoardView(board: Board, onClick: (Cell)->Unit) =
    Column(
        modifier = Modifier
            .background(Color.Black)
            .size(BOARD_SIDE),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        var idx=0
        repeat(BOARD_SIZE){ row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(CELL_SIDE),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                repeat(BOARD_SIZE){col ->
                    val pos = board.board[idx++]
                    cell(
                        pos.state,
                        onClick = { onClick(pos)} )
                }
            }
        }
    }




@Composable
fun DownBar(game: Game?, user:Player?){
    Row {
        user?.let{
            Text("You", style = MaterialTheme.typography.h4)
            cell(state = it.state,size=50.dp)
            Spacer(Modifier.width(30.dp))
        }
        val (txt, player) = when{
            game == null -> "Game not started" to null
            game.stateOfGame()-> "Winner:" to game.winner()
            else -> "Turn:" to game.showCurrentPlayer()
        }
        Text(text=txt, style=MaterialTheme.typography.h4 )
        cell(player?.state, size = 50.dp)

    }
}


@Composable
fun cell(state: State?, size: Dp = 100.dp, onClick:() -> Unit={} ){
    val modifier=Modifier.size(size)
        if(state==null) return

        val filename = when (state){
            State.WHITE -> "white.png"
            State.BLACK -> "black.png"
            else -> return
        }
        Image(
            painter = painterResource(filename),
            contentDescription = "player $state",
            modifier = modifier
        )

}

fun main() = application {
    MongoDriver().use { driver ->
        Window(
            onCloseRequest = ::exitApplication,
            title = "Go Game",
            state = WindowState(size = DpSize.Unspecified)
        ) {
            App(driver, ::exitApplication)

        }
    }
}

/*
fun main(){
    var game = Game()

            game.show()
            print(">")
            game = game.execute(readln())
        } catch (e:Exception) {
            println(e.message)
        }
    } while (true)
}
 */
