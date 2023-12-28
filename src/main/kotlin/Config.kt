import androidx.compose.ui.unit.dp
import java.awt.GraphicsEnvironment
import java.awt.Rectangle


const val BOARD_SIZE = 9

val SCREEN_SIZE: Rectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
val MAGIC_DIVISOR = if(SCREEN_SIZE.width > 1400) 3 else 4
val WIN_WIDTH = (SCREEN_SIZE.width / MAGIC_DIVISOR) + 120
val CELL_SIZE = WIN_WIDTH / (BOARD_SIZE + 2)
val WIN_HEIGHT = WIN_WIDTH + (CELL_SIZE * 2)

val GRID_THICKNESS = 3.dp
val CELL_LABEL = 10.dp
val BOARD_SIDE = CELL_SIZE.dp * (BOARD_SIZE)



//if needed

val CELL_SIDE = 100.dp