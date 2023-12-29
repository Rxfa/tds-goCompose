package model

import kotlinx.serialization.Serializable

@Serializable
data class Score(val black: Double = 0.0, val white: Double = 0.0) {
    operator fun plus(other: Pair<Int, Int>): Score =
        Score(
            black = this.black + other.first,
            white = this.white + other.second
        )
}