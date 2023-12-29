package model

import kotlinx.serialization.Serializable

@Serializable
data class Captures(val black: Int = 0, val white: Int = 0) {

    operator fun plus(other: Pair<Int, Int>) =
        Captures(
            black = black + other.first,
            white = white + other.second
        )

}