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
fun FrameWindowScope.app(driver: MongoDriver, exitFunction: () -> Unit) {
    val scope = rememberCoroutineScope()
    val vm = remember { AppViewModel(driver, scope) }

    MenuBar {
        Menu("Game") {
            Item("Start Game", onClick = {
                scope.launch { vm.showNewGameDialog() } })
            Item(
                "Join Game", onClick = { scope.launch { vm.showJoinGameDialog() } })
            Item("Exit", onClick = {
                scope.launch{ vm.deleteGame(vm.gameId, vm.me) }; exitFunction()})
        }
        Menu("Play") {
            Item("Pass", enabled = vm.isRunning && !vm.isOver && !vm.isWaiting, onClick = {
                scope.launch { vm.passRound() }
            })
            Item("Captures", enabled = !vm.isOver && vm.isRunning , onClick = vm::showCaptures)
            Item("Final Score", enabled = vm.isOver, onClick = vm::showScore)
        }
        Menu("Options"){
            CheckboxItem("Last Played", checked = vm.viewLastPlayed, onCheckedChange ={
                scope.launch { vm.toggleLastPlayed() }
            })
        }
    }
    MaterialTheme {
        ui(vm) {
            position -> scope.launch { vm.play(position) }
        }
        vm.inputName?.let {
            startOrJoinDialog(
                scope = scope,
                type = it,
                onCancel = vm::cancelInput,
                onAction = if (it == AppViewModel.InputName.NEW) vm::newGame else vm::joinGame,
                vm=vm
            )
        }
        if (vm.viewScore) scoreDialog(vm.score, vm::hideScore)
        if (vm.viewCaptures) capturesDialog(vm.captures, vm::hideCaptures)
        if (vm.viewLastPlayed) scope.launch { vm.refreshGame() }
        if (vm.isWaiting) waitingIndicator()
    }
}


@Composable
fun capturesDialog(captures: Captures?, closeDialog: () -> Unit) {
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
fun scoreDialog(score: Score?, closeDialog: () -> Unit) {
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
                            if(player.state==State.WHITE)Text(text = " - ${score?.white}", style = MaterialTheme.typography.h4)
                            else Text(text = " - ${score?.black}", style = MaterialTheme.typography.h4)
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun startOrJoinDialog(
    scope: CoroutineScope,
    type: AppViewModel.InputName,
    onCancel: () -> Unit,
    onAction: KSuspendFunction1<String, Unit>,
    vm:AppViewModel
) {
    var name by remember { mutableStateOf("") }
    val lastGameId=vm.gameId
    val lastPlayer=vm.me
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "${type.txt} Game", style = MaterialTheme.typography.h5) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name of game") }) },
        confirmButton = { TextButton(enabled = true, onClick = {
            scope.launch {
                onAction(name)
                vm.deleteGame(lastGameId,lastPlayer,true)} }) { Text(type.txt) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}

@Composable
fun ui(vm: AppViewModel, onClick: (String) -> Unit) {
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
            Spacer(modifier = Modifier.height(CELL_SIZE.dp)) // Just so the board stays centered vertically
            boardOverview(vm = vm, onClick = onClick)
            statusBar(vm.game, vm.me)
        }
    }
}

@Composable
fun boardOverview(vm: AppViewModel, onClick: (String) -> Unit){
    Box(
        modifier = Modifier
            .offset(x = CELL_SIZE.dp / 2, y = CELL_SIZE.dp / 2) // to account for the 'invisible' cells we have in the grid
    ){
        letters()
        numbers()
        boardView(vm = vm, onClick = onClick)
    }
}

@Composable
fun letters() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(BOARD_SIDE - CELL_SIZE.dp + (CELL_SIZE.dp / 4))
            .offset(y = (- ((CELL_SIZE.dp / 4) - GRID_THICKNESS)) - CELL_SIZE.dp, x = - CELL_SIZE.dp/8)
    ) {
        repeat(BOARD_SIZE) {
            Text("${'A' + it}", fontSize = BOARD_LABEL_TEXT_SIZE)
        }
    }
}

@Composable
fun numbers() {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .height(BOARD_SIDE - CELL_SIZE.dp + (CELL_SIZE.dp / 2))
            .offset(y = (- ((CELL_SIZE.dp / 4) + GRID_THICKNESS)), x = - CELL_SIZE.dp)
    ) {
        repeat(BOARD_SIZE) {
            Text("${9 - it}", fontSize = BOARD_LABEL_TEXT_SIZE)
        }
    }
}

@Composable
fun statusBar(game: Game?, user: Player?) {
    val horizontalArrangement = Arrangement.SpaceEvenly
    val verticalAlignment = Alignment.CenterVertically
    val modifier = Modifier.fillMaxWidth().background(Color.LightGray)
    val (txt, player) = when {
        game == null -> "No Game" to null
        game.stateOfGame() -> "Winner:" to game.winner()
        !game.board.pass.none() && game.isMyTurn(user!!) -> "Passed:" to user.other
        else -> "Turn:" to game.showCurrentPlayer()
    }
    Row(horizontalArrangement = horizontalArrangement, verticalAlignment = verticalAlignment, modifier = modifier) {
        if(txt!="No Game") {
            Row(verticalAlignment = verticalAlignment) {
                Text(text = "Player: ", style = MaterialTheme.typography.h4)
                cell(state = user?.state)
            }
        }
        Text(text = txt, style = MaterialTheme.typography.h4)
        if(player?.state!=null) cell(state= player.state)
    }
}

@Composable
fun boardView(vm: AppViewModel, onClick: (String) -> Unit) {
    Box {
        boardWrapper()
        boardCells(
            vm = vm,
            onClick = onClick,
        )
    }
}


@Composable
fun boardCells(onClick: (String) -> Unit, vm: AppViewModel) {
    Column(modifier = Modifier) {
        repeat(BOARD_SIZE) { row ->
            Row {
                repeat(BOARD_SIZE) { col ->
                    val modifier =
                        if (col == BOARD_SIZE - 1 || row == BOARD_SIZE - 1)
                            Modifier.size(CELL_SIZE.dp)
                        else
                            Modifier
                                .size(CELL_SIZE.dp)
                                .border(GRID_THICKNESS, color = GRID_BORDER_COLOR)
                    Box{
                        val position = "${'A' + row}${BOARD_SIZE - col}"
                        Box(modifier = modifier)
                        cell(
                            state = vm.board?.get(position),
                            position = position,
                            onClick = { onClick(position) },
                            onGrid = true,
                            lastPlayed = vm.lastPlayed,
                            viewLastPlayed = vm.viewLastPlayed,
                            gameIsValid = vm.game == null,
                            gameIsFinished = vm.game?.stateOfGame() == true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun boardWrapper() {
    Box(
        modifier = Modifier
            .size(REAL_BOARD_SIZE + GRID_THICKNESS * 2)
            .offset(x = -GRID_THICKNESS, y = -GRID_THICKNESS)
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
    gameIsValid: Boolean = false,
    gameIsFinished: Boolean = false
) {
    val modifier = when {
        onGrid &&
        viewLastPlayed &&
        lastPlayed is String &&
        lastPlayed == position
        -> Modifier.size(size).offset(x = CELL_OFFSET, y = CELL_OFFSET).border(CELL_THICKNESS, CELL_BORDER_COLOR)
        onGrid -> Modifier.size(size).offset(x = CELL_OFFSET, y = CELL_OFFSET)
        else -> Modifier.size(size)
    }

    when{
        gameIsValid  || (gameIsFinished && state == State.FREE) -> Box(modifier = modifier)
        state == null || state == State.FREE -> Box(modifier = modifier.clickable(onClick = onClick))
        else -> {
            val filename = when (state) {
                State.WHITE -> WHITE_CELL_PATH
                State.BLACK -> BLACK_CELL_PATH
                else -> ""
            }
            Image(painter = painterResource(filename), contentDescription = "player $state", modifier = modifier)
        }
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
        app(driver, ::exitApplication)
    }
}
