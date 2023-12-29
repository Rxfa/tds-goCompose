package model

class RunningMatch(
    gs: GameStorage,
    private val id: String,
    val me: Player,
    val game: Game,
): Match(gs) {


    suspend fun play(move: String): Match {
        check(game.isMyTurn(me)){ "It's not your turn" }
        val updatedGame = game.move(move)
        gs.update(id, updatedGame)
        return RunningMatch(gs, id, me, updatedGame)
    }

    suspend fun pass():Match{
        check(game.isMyTurn(me)){ "It's not your turn" }
        val updatedGame = game.pass()
        gs.update(id, updatedGame)
        return RunningMatch(gs, id, me, updatedGame)
    }

    suspend fun refresh(): RunningMatch {
        val updatedGame = gs.slowRead(id) ?: throw GameDeletedException()

        if(game == updatedGame)
            throw NoChangesException()
        return RunningMatch(gs, id , me, updatedGame)
    }

    suspend fun delete(){
        if(game.IamOwner(me))
            gs.delete(id)
    }

    fun isOver():Boolean{
        return game.stateOfGame()
    }

    fun newBoard(): RunningMatch {
        val newGame = game //TODO
        return RunningMatch(gs, id, me, newGame)
    }

    fun isMyTurn() = game.isMyTurn(me)
}
