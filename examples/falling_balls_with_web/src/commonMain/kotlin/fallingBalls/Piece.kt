package fallingBalls

import androidx.compose.runtime.Composable
import jetbrains.compose.common.shapes.CircleShape
import modifiers.position
import org.jetbrains.compose.common.core.graphics.Color
import org.jetbrains.compose.common.foundation.clickable
import org.jetbrains.compose.common.foundation.layout.Box
import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.common.ui.background
import org.jetbrains.compose.common.ui.draw.clip
import org.jetbrains.compose.common.ui.size
import org.jetbrains.compose.common.ui.unit.Dp
import org.jetbrains.compose.common.ui.unit.dp

@Composable
fun Piece(index: Int, piece: PieceData) {
    val boxSize = 40.dp
    Box(
        Modifier
            .position(Dp(boxSize.value * index * 5 / 3), Dp(piece.position))
            .size(boxSize, boxSize)
            .background(if (piece.picked) Color.Gray else piece.color)
            .clickable { piece.pick() }
            .clip(CircleShape)
    ) {}
}
