package model

open class Match(val gs: GameStorage) {

    suspend fun create(id: String): RunningMatch {
        val game = Game()

        gs.create(id, game)
        return RunningMatch(gs, id, Player.BLACK, game)
    }

    suspend fun join(id: String): RunningMatch {
        val game = gs.read(id) ?: error("Match $id not found")
        return RunningMatch(gs, id, Player.WHITE, game)
    }
}
