package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import storage.Storage

typealias GameStorage = Storage<String, Game>

open class Match(val gs: GameStorage)
    suspend fun Match.create(id: String): RunningMatch {
        val game = Game()

        gs.create(id, game)
        return RunningMatch(gs, id, Player.BLACK, game)
    }

    suspend fun Match.join(id: String): RunningMatch {
        val game = gs.read(id) ?: error("Match $id not found")
        return RunningMatch(gs, id, Player.WHITE, game)
    }


class RunningMatch(
    gs: GameStorage,
    val id: String,
    val me: Player,
    val game: Game,
): Match(gs)

    suspend fun RunningMatch.play(move: String): Match {
        check(game.isMyTurn(me)){ "It's not your turn" }
        val updatedGame = game.move(move)
        gs.update(id, updatedGame)
        return RunningMatch(gs, id, me, updatedGame)
    }

    suspend fun RunningMatch.pass():Match{
        check(game.isMyTurn(me)){ "It's not your turn" }
        val updatedGame = game.pass()
        gs.update(id, updatedGame)
        return RunningMatch(gs, id, me, updatedGame)
    }

    suspend fun RunningMatch.refresh(): RunningMatch {
        val updatedGame = gs.slowRead(id) ?: throw GameDeletedException()

        if(game == updatedGame)
            throw NoChangesException()
        return RunningMatch(gs, id , me, updatedGame)
    }

    suspend fun RunningMatch.delete(){
        if(game.IamOwner(me))
            gs.delete(id)
    }

    fun RunningMatch.isOver():Boolean{
        return game.stateOfGame()
    }

    fun RunningMatch.newBoard(): RunningMatch {
        val newGame = game //TODO
        return RunningMatch(gs, id, me, newGame)
    }

    fun RunningMatch.isMyTurn() = game.isMyTurn(me)


suspend fun GameStorage.slowRead(key: String): Game? {
    fun log(label: String) = println(
        "$label: thread=${Thread.currentThread().name}\ttime=${System.currentTimeMillis()/1_000}"
    )
    log("slowRead1")
    val res = withContext(Dispatchers.IO){
        log("slowRead2")
        Thread.sleep(2_00)
        log("SlowRead3")
        read(key)
    }
    log("SlowRead4")
    return res
}