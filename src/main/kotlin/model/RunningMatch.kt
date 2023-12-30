package model

class RunningMatch(
    gs: GameStorage,
    val id: String,
    val host: Player,
    val game: Game,
) : Match(gs) {

    suspend fun play(move: String): Match {
        check(game.isMyTurn(host)) { "It's not your turn" }
        val updatedGame = game.move(move)
        gs.update(id, updatedGame)
        // TODO: Try illegal moves and see how it affects lastPlayed.
        return RunningMatch(gs, id, host, updatedGame)
    }

    suspend fun pass(): Match {
        check(game.isMyTurn(host)) { "It's not your turn" }
        val updatedGame = game.pass()
        gs.update(id, updatedGame)
        return RunningMatch(gs, id, host, updatedGame)
    }

    suspend fun refresh(): RunningMatch {
        val updatedGame = gs.slowRead(id) ?: throw GameDeletedException()

        if (game == updatedGame)
            throw NoChangesException()
        return RunningMatch(gs, id, host, updatedGame)
    }

    suspend fun delete() {
        if (game.isOwner(host))
            gs.delete(id)
    }

    fun isOver(): Boolean {
        return game.stateOfGame()
    }

    fun newBoard(): RunningMatch {
        val newGame = game //TODO
        return RunningMatch(gs, id, host, newGame)
    }

    fun isMyTurn() = game.isMyTurn(host)
}
