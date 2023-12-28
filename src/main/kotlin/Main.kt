import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.modifier.modifierLocalMapOf
import kotlinx.coroutines.launch
import model.Cell
import model.State
import viewModel.AppViewModel
import kotlin.reflect.KSuspendFunction1


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
            Item("Start Game", onClick = vm::showNewGameDialog)//corrigir erro com onAction
            Item("Join Game", onClick = vm::showJoinGameDialog)//corrigir erro com onAction
            Item("Exit", onClick = {exitFunction()})//done
        }
        Menu("Play"){
            Item("Pass", enabled= (vm.isRunning), onClick = {vm::passRound})
            Item("Show Captures",/*enabled= vm.isOver == false,*/ onClick = vm::showCaptures)//done
            Item("Show Final Score", /*enabled= vm.isOver == false,*/onClick = vm::showScore)//done
        }
        Menu("Options"){
            CheckboxItem("Show Last Played",checked = vm.viewLastPlayed, onCheckedChange = { vm::toggleLastPlayed })
        }
    }


MaterialTheme{
        background()
       // Column(horizontalAlignment = Alignment.CenterHorizontally){
        if (vm.viewScore) ScoreDialog(vm.score,vm::hideScore)
        vm.inputName?.let{
            StartOrJoinDialog(
                type = it,
                onCancel = vm::cancelInput,
                onAction = if(it == AppViewModel.InputName.NEW) vm::newGame else vm::joinGame
            )
       }
        if(vm.viewCaptures) CapturesDialog(vm.captures,vm::hideCaptures)
        if(vm.viewLastPlayed) ShowLastPlayed(vm.lastPlayed, vm::cancelInput,vm.viewLastPlayed)
    }
}
@Composable
fun ShowLastPlayed(lastPlayed: Int?, closeDialog: () -> Unit,viewLastPlayed:Boolean) {
    val backgroundColor= Color.Transparent
    if(lastPlayed!=null && viewLastPlayed) {
        drawSquare(modifier = Modifier.size(CELL_SIDE), lastPlayed, backgroundColor, Color.Red, CELL_SIDE)
    }
    else{
        closeDialog
    }
}

@Composable
fun drawSquare(modifier: Modifier,position: Int, backgroundColor:Color,borderColor: Color,size:Dp){
    Box(
    modifier.offset(position.dp,position.dp).size(size).background(backgroundColor).border(1.dp,borderColor)
    )
}
@Composable
fun CapturesDialog(captures: Pair<Int, Int>?,closeDialog: () -> Unit){
    AlertDialog(
        title={Text(text="Captures in a", style= MaterialTheme.typography.h4)},
        onDismissRequest = closeDialog,
        confirmButton =  { TextButton(onClick = closeDialog){ Text("Close")}},
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Row(verticalAlignment = Alignment.CenterVertically){
                        cell(Player.BLACK.state,size = 30.dp)
                        Text(
                            text= " - ${captures?.first}",
                            style = MaterialTheme.typography.h4
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically){
                        cell(Player.WHITE.state,size = 30.dp)
                        Text(
                            text= " - ${captures?.second}",
                            style = MaterialTheme.typography.h4
                        )
                    }
                }
        }

        }

    )
}
@Composable
fun ScoreDialog(score: Pair<Double, Double>?, closeDialog:()-> Unit){
    AlertDialog(
        title={Text(text="Score in a", style= MaterialTheme.typography.h4)},
        onDismissRequest = closeDialog,
        confirmButton = {TextButton(onClick = closeDialog){Text("Close")} },
        text= {Row(
            modifier= Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
            ){
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Row(verticalAlignment = Alignment.CenterVertically){
                    cell(Player.BLACK.state,size = 30.dp)
                    Text(
                        text= " - ${score?.first}",
                        style = MaterialTheme.typography.h4
                    )
                }
                    Row(verticalAlignment = Alignment.CenterVertically){
                        cell(Player.WHITE.state,size = 30.dp)
                        Text(
                            text= " - ${score?.second}",
                            style = MaterialTheme.typography.h4
                        )
                    }
            }
        }}
    )
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StartOrJoinDialog( type: AppViewModel.InputName, onCancel: () -> Unit, onAction: KSuspendFunction1<String, Unit>){
        val newscope=rememberCoroutineScope()
        var name by remember { mutableStateOf(" ") }

        AlertDialog(
            onDismissRequest = onCancel,
            title= {Text(text = "Name to ${type.txt}",
                style = MaterialTheme.typography.h5)},

            text={ OutlinedTextField(
                value= name,
                onValueChange = {name=it},
                label= {Text("Name of game")}
            )},
            confirmButton = {
                TextButton(enabled = true,
                    onClick={newscope.launch{onAction(name)}}){Text(type.txt)}
            },
            dismissButton = {
                TextButton(onClick = onCancel){Text("Cancel")}
            })
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






@Composable
fun background(){
    Image(
        painter=painterResource("board.png"),
        contentDescription = "board",
        modifier=Modifier.size(BOARD_SIDE)
    )
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
    println("oi")
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
