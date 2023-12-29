import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.awt.GraphicsEnvironment
import java.awt.Rectangle

const val BOARD_SIZE = 9
const val WHITE_CELL_PATH = "white.png"
const val BLACK_CELL_PATH = "black.png"
const val BOARD_PATH = "board.png"

val SCREEN_SIZE: Rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
val MAGIC_DIVISOR = if (SCREEN_SIZE.width > 1400) 3 else 4
val WIN_WIDTH = (SCREEN_SIZE.width / MAGIC_DIVISOR) + 120
val CELL_SIZE = WIN_WIDTH / (BOARD_SIZE + 2)
val SMALL_CELL_SIZE = 30.dp
val WIN_HEIGHT = WIN_WIDTH + (CELL_SIZE * 2) + 60
val CELL_LABEL = 10.dp
val CELL_THICKNESS = 1.dp
val GRID_THICKNESS = 3.dp
val BOARD_SIDE = CELL_SIZE.dp * (BOARD_SIZE)
val GRID_BORDER_COLOR = Color.Black
val CELL_BORDER_COLOR = Color.Red

