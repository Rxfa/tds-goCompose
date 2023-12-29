package model

import kotlinx.serialization.Serializable

@Serializable
data class Pass(val black: Boolean = false, val white: Boolean = false) {
    infix fun or(other: Pair<Boolean, Boolean>): Pass =
        Pass(
            black = this.black || other.first,
            white = this.white || other.second
        )

    fun all(): Boolean = black && white

    fun none(): Boolean = !black && !white

}