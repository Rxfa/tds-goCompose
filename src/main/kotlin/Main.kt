import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import model.*
import mongo.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.*
import model.State
import mongo.MongoDriver
import viewModel.AppViewModel
import kotlin.reflect.KSuspendFunction1


@Composable
@Preview
fun FrameWindowScope.App(driver: MongoDriver, exitFunction: () -> Unit) {
    val scope = rememberCoroutineScope()
    val vm = remember { AppViewModel(driver, scope) }

    MenuBar {
        Menu("Game") {
            Item("Start Game", onClick = {
                scope.launch {
                    vm.showNewGameDialog()
                    vm.deleteGame(vm.gameId, vm.me)
                }
            })
            Item("Join Game", onClick = {
                scope.launch {
                    vm.showJoinGameDialog()
                    vm.deleteGame(vm.gameId, vm.me)

            }
        })
            Item("Exit", onClick = {
                scope.launch{
                    vm.deleteGame(vm.gameId,vm.me)
                }; exitFunction()})
        }
        Menu("Play") {
            Item("Pass", enabled = vm.isRunning, onClick = {
                scope.launch { vm.passRound() }
            })
            Item("Show Captures", enabled = !vm.isOver, onClick = vm::showCaptures)
            Item("Show Final Score", enabled = vm.isOver, onClick = vm::showScore)
        }
        Menu("Options"){
            CheckboxItem("Show Last Played", checked = vm.viewLastPlayed, onCheckedChange ={
                scope.launch { vm.toggleLastPlayed() }
            })
        }
    }
    MaterialTheme {
        background(vm) {
            position -> scope.launch { vm.play(position) }
        }
        if (vm.viewScore)
            ScoreDialog(vm.score, vm::hideScore)
        vm.inputName?.let {
            StartOrJoinDialog(
                scope = scope,
                type = it,
                onCancel = vm::cancelInput,
                onAction = if (it == AppViewModel.InputName.NEW) vm::newGame else vm::joinGame
            )
        }
        if (vm.viewCaptures) CapturesDialog(vm.captures, vm::hideCaptures)
        if (vm.viewLastPlayed) scope.launch { vm.refreshGame() }
        if (vm.isWaiting) waitingIndicator()
    }
}

@Composable
fun CapturesDialog(captures: Captures?, closeDialog: () -> Unit) {
    AlertDialog(
        title = { Text(text = "Captures in a", style = MaterialTheme.typography.h4) },
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick = closeDialog) { Text("Close") } },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        cell(state = Player.BLACK.state, size = SMALL_CELL_SIZE)
                        Text(text = " - ${captures?.black}", style = MaterialTheme.typography.h4)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        cell(state = Player.WHITE.state, size = SMALL_CELL_SIZE)
                        Text(text = " - ${captures?.white}", style = MaterialTheme.typography.h4)
                    }
                }
            }

        }

    )
}

@Composable
fun waitingIndicator() = CircularProgressIndicator(modifier = Modifier.fillMaxSize().padding(30.dp), strokeWidth = 15.dp)

@Composable
fun ScoreDialog(score: Score?, closeDialog: () -> Unit) {
    AlertDialog(
        title = { Text(text = "Scores in a", style = MaterialTheme.typography.h4) },
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick = closeDialog) { Text("Close") } },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Player.entries.forEach { player ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            cell(state = player.state, size = SMALL_CELL_SIZE)
                            Text(text = " - ${score?.white}", style = MaterialTheme.typography.h4)
                        }
                    }
                    Text(text = "Draws - ${score?.white}", style = MaterialTheme.typography.h4)
                }
            }
        }
    )
}

@Composable
fun StartOrJoinDialog(
    scope: CoroutineScope,
    type: AppViewModel.InputName,
    onCancel: () -> Unit,
    onAction: KSuspendFunction1<String, Unit>
) {
    var name by remember { mutableStateOf(" ") }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "Name to ${type.txt}", style = MaterialTheme.typography.h5) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name of game") }) },
        confirmButton = { TextButton(enabled = true, onClick = { scope.launch { onAction(name) } }) { Text(type.txt) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}


@Composable
fun background(vm: AppViewModel, onClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(BOARD_PATH),
            contentDescription = "board_description",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(CELL_LABEL))
            BoardOverview(
              board = vm.board, 
              onClick = onClick, 
              lastPlayed = vm.lastPlayed, 
              viewLastPlayed = vm.viewLastPlayed,
              gamevalid = vm.game == null,
              gameIsFinished = vm.game?.stateOfGame() == true
            )
            StatusBar(vm.game, vm.me)
        }
    }
}

@Composable
fun BoardOverview(
  board: Board?,
  onClick: (String) -> Unit, 
  lastPlayed: String?, 
  viewLastPlayed: Boolean, 
  gamevalid: Boolean, 
  gameIsFinished:Boolean
){
    Column {
        letters()
        Row {
            numbers()
            BoardView(
              board = board, 
              onClick = onClick, 
              lastPlayed = lastPlayed, 
              viewLastPlayed = viewLastPlayed, 
              gamevalid = gamevalid, 
              gameIsFinished = gameIsFinished
            )
        }
    }
}

@Composable
fun letters() {
    Column(modifier = Modifier.padding(start = CELL_LABEL * 3, end = CELL_LABEL * 3, bottom = CELL_LABEL * 4)) {
        Row(modifier = Modifier.width(BOARD_SIDE).padding(end = CELL_LABEL * 2)) {
            Spacer(modifier = Modifier.width(CELL_LABEL * 4))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(BOARD_SIZE) { Text("${'A' + it}", style = MaterialTheme.typography.h4) }
            }
        }
    }
}


@Composable
fun StatusBar(game: Game?, user: Player?) {
    val horizontalArrangement = Arrangement.SpaceEvenly
    val verticalAlignment = Alignment.CenterVertically
    val modifier = Modifier.fillMaxWidth().background(Color.LightGray)
    user?.let {
        return Row(
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            modifier = modifier
        ) {
            Text("You", style = MaterialTheme.typography.h4)
            cell(state = it.state)
        }
    }
    val (txt, player) = when {
        game == null -> "Waiting for game..." to null
        game.stateOfGame() -> "Winner:" to game.winner()
        else -> "Turn:" to game.showCurrentPlayer()
    }
    Row(horizontalArrangement = horizontalArrangement, verticalAlignment = verticalAlignment, modifier = modifier) {
        Text(text = txt, style = MaterialTheme.typography.h4)
        Row(verticalAlignment = verticalAlignment) {
            Text(text = "Player: ", style = MaterialTheme.typography.h4)
            cell(state = player?.state)
        }
    }
}

@Composable
fun numbers() {
    Row {
        Column {
            Column(
                modifier = Modifier.height(BOARD_SIDE).padding(bottom = CELL_LABEL * 3),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(BOARD_SIZE) { Text("${9 - it}") }
            }
        }
        Spacer(modifier = Modifier.width(CELL_LABEL * 4))
    }
}


@Composable
fun BoardView(board: Board?, onClick: (String) -> Unit, lastPlayed: String?, viewLastPlayed: Boolean, gamevalid: Boolean,gameIsFinished:Boolean) {
    val paddingStart = CELL_LABEL * 2
    val paddingTop = paddingStart
    Box {
        boardWrapper(paddingStart = paddingStart, paddingTop = paddingTop)
        boardCells(
            board = board,
            onClick = onClick,
            paddingStart = paddingStart,
            paddingTop = paddingTop,
            lastPlayed = lastPlayed,
            viewLastPlayed = viewLastPlayed,
            gamevalid = gamevalid,
            gameIsFinished = gameIsFinished
        )
    }
}


@Composable
fun boardCells(
    board: Board?,
    onClick: (String) -> Unit,
    paddingStart: Dp,
    paddingTop: Dp,
    lastPlayed: String?,
    viewLastPlayed: Boolean,
    gamevalid: Boolean,
    gameIsFinished: Boolean
) {
    Column(modifier = Modifier.padding(start = paddingStart, paddingTop)) {
        repeat(BOARD_SIZE) { row ->
            Row {
                repeat(BOARD_SIZE) { col ->
                    val modifier =
                        if (col == BOARD_SIZE - 1 || row == BOARD_SIZE - 1)
                            Modifier.size(CELL_SIZE.dp)
                        else
                            Modifier
                                .size(CELL_SIZE.dp)
                                .offset(x = -GRID_THICKNESS, y = -GRID_THICKNESS)
                                .border(GRID_THICKNESS, color = GRID_BORDER_COLOR)
                    Box(modifier = modifier) {
                        val position = "${'A' + row}${BOARD_SIZE - col}"
                        cell(
                            state = board?.get(position),
                            position = position,
                            onClick = { onClick(position) },
                            onGrid = true,
                            lastPlayed = lastPlayed,
                            viewLastPlayed = viewLastPlayed,
                            gamevalid = gamevalid,
                            gameIsFinished = gameIsFinished
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun boardWrapper(paddingStart: Dp, paddingTop: Dp) {
    Box(
        modifier = Modifier
            .padding(start = paddingStart, top = paddingTop)
            .size(((BOARD_SIZE - 1) * CELL_SIZE).dp + GRID_THICKNESS * 2)
            .offset(x = -GRID_THICKNESS * 2, y = -GRID_THICKNESS * 2)
            .border(width = GRID_THICKNESS, color = GRID_BORDER_COLOR)
    )
}

@Composable
fun cell(
    state: State?,
    position: String? = null,
    size: Dp = CELL_SIZE.dp,
    onClick: () -> Unit = {},
    onGrid: Boolean = false,
    lastPlayed: String? = null,
    viewLastPlayed: Boolean = false,
    gamevalid: Boolean = false,
    gameIsFinished: Boolean = false
) {
    val modifier = when {
        onGrid &&
        viewLastPlayed &&
        lastPlayed is String &&
        lastPlayed == position
        -> Modifier.size(size).offset(x = -size / 2, y = -size / 2).border(CELL_THICKNESS, CELL_BORDER_COLOR)
        onGrid -> Modifier.size(size).offset(x = -size / 2, y = -size / 2)
        else -> Modifier.size(size)
    }
    if ((state != State.WHITE && state != State.BLACK) && !gameIsFinished && gamevalid) {
        Box(modifier = modifier.clickable(onClick = onClick))
    } else {
        val filename = when (state) {
            State.WHITE -> WHITE_CELL_PATH
            State.BLACK -> BLACK_CELL_PATH
            else -> ""
        }
        Image(painter = painterResource(filename), contentDescription = "player $state", modifier = modifier)
    }
}

fun main() = application {
    Window(
        onCloseRequest = {  },
        title = "Go Game",
        state = rememberWindowState(width = WIN_WIDTH.dp, height = WIN_HEIGHT.dp),
        resizable = false,
    ) {
        val driver = MongoDriver()
        App(driver, ::exitApplication)
    }
}
