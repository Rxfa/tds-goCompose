package viewModel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mongo.MongoDriver
import kotlinx.coroutines.*
import model.*
import storage.GameSerializer
import storage.MongoStorage

class AppViewModel(driver: MongoDriver, val scope: CoroutineScope) {

    private val storage = MongoStorage<String, Game>("games", driver, GameSerializer)
    private var match by mutableStateOf( Match(storage))

    //    var game by mutableStateOf(Game())
    var viewScore by mutableStateOf(false)
        private set

    var viewCaptures by mutableStateOf(false)
        private set
    var inputName by mutableStateOf<InputName?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null) //ErrorDialog state
        private set

    var viewLastPlayed by mutableStateOf(false)

    val game: Game? get() = (match as? RunningMatch)?.game

    val board:Board? get()=game?.board

    var lastPlayed: Int?=null

    val captures: Pair<Int,Int>?= game?.getCaptures()

    val isOver:Boolean get()=game?.stateOfGame()==true

    val score: Pair<Double,Double>?=  game?.score()

    val me: Player? get() = (match as? RunningMatch)?.me

    val isRunning: Boolean get() = match is RunningMatch

    private var waitingJob by mutableStateOf<Job?>(null)

    val isWaiting: Boolean get() = waitingJob != null

    private val turnAvailable: Boolean
        get() = (match as RunningMatch).isMyTurn()


    fun showScore(){ viewScore = true}
    fun hideScore(){ viewScore = false}

    fun showCaptures(){viewCaptures=true}

    fun hideCaptures(){viewCaptures=false}
    fun hideError() { errorMessage = null }

    suspend fun play(pos: String){
        try {
            match = (match as RunningMatch).play(pos)
            lastPlayed=(match as RunningMatch).game.board.toPosition(pos)
        } catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    enum class InputName(val txt: String)
    { NEW("Start"), JOIN("Join") }

    fun cancelInput() { inputName = null }
    suspend fun newGame(gameName: String) {

        cancelWaiting()

        match = match.create(gameName)
        inputName = null
    }

    fun showLastPlayed(){
        if(viewLastPlayed)viewLastPlayed=false
        else viewLastPlayed=true
    }

    suspend fun joinGame(gameName: String) {
        cancelWaiting()

        match = match.join(gameName)
        inputName = null

        waitForOtherSide()
    }

    suspend fun passRound(){
        try{
            match=(match as RunningMatch).pass()
            lastPlayed=null
        } catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    suspend fun refreshGame() {
        try {
            match = (match as RunningMatch).refresh()
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    fun showNewGameDialog() { inputName = InputName.NEW }
    fun showJoinGameDialog() { inputName = InputName.JOIN }

    suspend fun exit() {
        (match as RunningMatch).delete()
        cancelWaiting()
    }

    private fun cancelWaiting() {
        waitingJob?.cancel()
        waitingJob = null
    }

    private fun waitForOtherSide() {
        if (turnAvailable) return
        waitingJob = scope.launch(Dispatchers.IO) {
            do {
                delay(3000)
                try { match = (match as RunningMatch).refresh() }
                catch (e: NoChangesException) { /* Ignore */ }
                catch (e: Exception) {
                    errorMessage = e.message
                    if (e is GameDeletedException) match =Match(storage)
                }
            } while (!turnAvailable)
            waitingJob = null
        }
    }

}