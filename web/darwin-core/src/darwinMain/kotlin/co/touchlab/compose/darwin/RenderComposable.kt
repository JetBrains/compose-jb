package org.jetbrains.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.DefaultMonotonicFrameClock
import androidx.compose.runtime.Recomposer
import co.touchlab.compose.darwin.UIKitApplier
import co.touchlab.compose.darwin.UIViewWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import platform.UIKit.UIView

/**
 * Use this method to mount the composition at the certain [root]
 *
 * @param root - the [Element] that will be the root of the DOM tree managed by Compose
 * @param content - the Composable lambda that defines the composition content
 *
 * @return the instance of the [Composition]
 */
fun <TView : UIView> renderComposable(
    root: TView,
    content: @Composable DOMScope<TElement>.() -> Unit
): Composition {
    GlobalSnapshotManager.ensureStarted()

    val context = DefaultMonotonicFrameClock + JsMicrotasksDispatcher()
    val recomposer = Recomposer(context)
    val composition = ControlledComposition(
        applier = UIKitApplier(UIViewWrapper(root)),
        parent = recomposer
    )
    val scope = object : DOMScope<TElement> {}
    composition.setContent @Composable {
        content(scope)
    }

    CoroutineScope(context).launch(start = CoroutineStart.UNDISPATCHED) {
        recomposer.runRecomposeAndApplyChanges()
    }
    return composition
}

///**
// * Use this method to mount the composition at the element with id - [rootElementId].
// *
// * @param rootElementId - the id of the [Element] that will be the root of the DOM tree managed
// * by Compose
// * @param content - the Composable lambda that defines the composition content
// *
// * @return the instance of the [Composition]
// */
//@Suppress("UNCHECKED_CAST")
//fun renderComposable(
//    rootElementId: String,
//    content: @Composable DOMScope<Element>.() -> Unit
//): Composition = renderComposable(
//    root = document.getElementById(rootElementId)!!,
//    content = content
//)
//
///**
// * Use this method to mount the composition at the [HTMLBodyElement] of the current document
// *
// * @param content - the Composable lambda that defines the composition content
// *
// * @return the instance of the [Composition]
// */
//fun renderComposableInBody(
//    content: @Composable DOMScope<HTMLBodyElement>.() -> Unit
//): Composition = renderComposable(
//    root = document.getElementsByTagName("body")[0] as HTMLBodyElement,
//    content = content
//)