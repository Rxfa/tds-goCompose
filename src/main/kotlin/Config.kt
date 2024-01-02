import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.GraphicsEnvironment
import java.awt.Rectangle

const val BOARD_SIZE = 9

const val WHITE_CELL_PATH = "white.png"
const val BLACK_CELL_PATH = "black.png"
const val BOARD_PATH = "board.png"


val WIN_WIDTH = 500
val CELL_SIZE = WIN_WIDTH / (BOARD_SIZE + 2)
val SMALL_CELL_SIZE = 30.dp
val WIN_HEIGHT = WIN_WIDTH + 70
val CELL_THICKNESS = 1.dp
val CELL_OFFSET = -(CELL_SIZE/2).dp
val GRID_THICKNESS = 3.dp
val BOARD_SIDE = CELL_SIZE.dp * (BOARD_SIZE)
val GRID_BORDER_COLOR = Color.Black
val CELL_BORDER_COLOR = Color.Red
val BOARD_LABEL_TEXT_SIZE = CELL_SIZE.sp / 2
val REAL_BOARD_SIZE = ((BOARD_SIZE - 1) * CELL_SIZE).dp
