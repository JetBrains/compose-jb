package org.jetbrains.compose.splitpane

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.movable.SingleDirectionMovable
import java.awt.Cursor

private fun Modifier.cursorForHorizontalResize(
    isHorizontal: Boolean
): Modifier = composed {
    var isHover by remember { mutableStateOf(false) }

    if (isHover) {
        LocalAppWindow.current.window.cursor = Cursor(
            if (isHorizontal) Cursor.E_RESIZE_CURSOR else Cursor.S_RESIZE_CURSOR
        )
    } else {
        LocalAppWindow.current.window.cursor = Cursor.getDefaultCursor()
    }
    pointerMoveFilter(
        onEnter = { isHover = true; true },
        onExit = { isHover = false; true }
    )
}

@Composable
private fun DesktopSplitPaneSeparator(
    isHorizontal: Boolean,
    color: Color = MaterialTheme.colors.background
) = Box(
    Modifier
        .run {
            if (isHorizontal) {
                this.width(1.dp)
                    .fillMaxHeight()
            } else {
                this.height(1.dp)
                    .fillMaxWidth()
            }
        }
        .background(color)
)

@Composable
private fun DesctopHandle(
    isHorizontal: Boolean,
    splitPaneState: SingleDirectionMovable
) = Box(
    Modifier
        .pointerInput(splitPaneState) {
            detectDragGestures { change, _ ->
                change.consumeAllChanges()
                splitPaneState.dispatchRawMovement(
                    if (isHorizontal) change.position.x else change.position.y
                )
            }
        }
        .cursorForHorizontalResize(isHorizontal)
        .run {
            if (isHorizontal) {
                this.width(8.dp)
                    .fillMaxHeight()
            } else {
                this.height(8.dp)
                    .fillMaxWidth()
            }
        }
)

internal actual fun defaultSplitter(
    isHorizontal: Boolean,
    splitPaneState: SingleDirectionMovable
): Splitter = Splitter(
    measuredPart = {
        DesktopSplitPaneSeparator(isHorizontal)
    },
    handlePart = {
        DesctopHandle(isHorizontal, splitPaneState)
    }
)

