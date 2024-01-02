package viewModel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import model.*
import mongo.MongoDriver
import storage.GameSerializer
import storage.MongoStorage

class AppViewModel(driver: MongoDriver, val scope: CoroutineScope) {

    private val storage = MongoStorage<String, Game>("games", driver, GameSerializer)
    private var match by mutableStateOf(Match(storage))

    var viewScore by mutableStateOf(false)
        private set

    var viewCaptures by mutableStateOf(false)
        private set
    var inputName by mutableStateOf<InputName?>(null)
        private set
    private var errorMessage by mutableStateOf<String?>(null) //ErrorDialog state

    var viewLastPlayed by mutableStateOf(false)

    val game: Game?
        get() = (match as? RunningMatch)?.game

    val gameId:String? 
       get() =(match as? RunningMatch)?.id


    val me: Player?
        get() = (match as? RunningMatch)?.host

    val board: Board?
        get() = game?.board

    val lastPlayed: String?
        get() = game?.lastPlay

    private var changed: Boolean?=null
    val captures: Captures?
        get() = game?.captures

    val score: Score?
        get() = game?.score

    val isOver: Boolean
        get() = game?.stateOfGame() == true

    val isRunning: Boolean
        get() = match is RunningMatch

    private var waitingJob by mutableStateOf<Job?>(null)

    val isWaiting: Boolean get() = waitingJob != null

    private val turnAvailable: Boolean?
        get() = (match as? RunningMatch)?.isMyTurn()


    fun showScore() {
        viewScore = true
    }


    fun hideScore() {
        viewScore = false
    }

    fun showCaptures() {
        viewCaptures = true
    }

    fun hideCaptures() {
        viewCaptures = false
    }

    /*
    fun hideError() {
        errorMessage = null
    }
    */



    suspend fun play(pos: String) {
        try {
            match = (match as RunningMatch).play(pos)
        } catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    enum class InputName(val txt: String) {
        NEW("Start"), JOIN("Join")
    }

    fun cancelInput() {
        inputName = null
    }

    suspend fun newGame(gameName: String) {
        try {
            cancelWaiting()
            match = match.create(gameName)
            inputName = null
            changed = true
        }catch (e:Exception){
            errorMessage=e.message
        }
    }

    fun toggleLastPlayed() {
        viewLastPlayed = !viewLastPlayed
    }

    suspend fun joinGame(gameName: String) {
        try {
            cancelWaiting()

            match = match.join(gameName)
            inputName = null
            changed = true
            waitForOtherSide()
        }catch (e:Exception){
            errorMessage=e.message
        }
    }

    suspend fun deleteGame(id:String?,player: Player?,createJoin:Boolean=false){
        try {
            if(changed==true && createJoin && id != null && player == Player.BLACK)
                storage.delete(id)
            if (!createJoin && id != null && player == Player.BLACK)
                storage.delete(id)
            changed= false
        }catch (e:Exception){
            errorMessage=e.message
        }
    }
    
    suspend fun passRound(){
        try{
            match=(match as RunningMatch).pass()
            //lastPlayed=null
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

    fun showNewGameDialog() {
        inputName = InputName.NEW
    }

    fun showJoinGameDialog() {
        inputName = InputName.JOIN
    }

    private fun cancelWaiting() {
        waitingJob?.cancel()
        waitingJob = null
    }

    private fun waitForOtherSide() {
            if (turnAvailable==true)
                return
            waitingJob = scope.launch(Dispatchers.IO) {
                do {
                    delay(200)
                    try {
                        match = (match as RunningMatch).refresh()
                    } catch (e: Exception) {
                        errorMessage = e.message
                        if (e is GameDeletedException)
                            match = Match(storage)
                    }
                } while (turnAvailable==false && game?.stateOfGame()==false)
                waitingJob = null
            }
        }
}