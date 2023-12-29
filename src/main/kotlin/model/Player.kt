package model

enum class Player {
     BLACK,WHITE;

    val other get() = if (this == WHITE) BLACK else WHITE
    val state get() = if (this == WHITE) State.WHITE else State.BLACK
}