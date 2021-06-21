package org.jetbrains.compose.web

import androidx.compose.runtime.AbstractApplier
import org.jetbrains.compose.web.attributes.WrappedEventListener
import org.jetbrains.compose.web.css.StyleHolder
import org.jetbrains.compose.web.dom.setProperty
import org.jetbrains.compose.web.dom.setVariable
import kotlinx.dom.clear
import org.jetbrains.compose.web.attributes.Options
import org.jetbrains.compose.web.css.jsObject
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.get

class DomApplier(
    root: DomNodeWrapper
) : AbstractApplier<DomNodeWrapper>(root) {

    override fun insertTopDown(index: Int, instance: DomNodeWrapper) {
        // ignored. Building tree bottom-up
    }

    override fun insertBottomUp(index: Int, instance: DomNodeWrapper) {
        current.insert(index, instance)
    }

    override fun remove(index: Int, count: Int) {
        current.remove(index, count)
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.move(from, to, count)
    }

    override fun onClear() {
        // or current.node.clear()?; in all examples it calls 'clear' on the root
        root.node.clear()
    }
}

external interface EventListenerOptions {
    var once: Boolean
    var passive: Boolean
    var capture: Boolean
}

open class DomNodeWrapper(open val node: Node) {
    private var currentListeners = emptyList<WrappedEventListener<*>>()

    fun updateEventListeners(list: List<WrappedEventListener<*>>) {
        val htmlElement = node as? HTMLElement ?: return

        currentListeners.forEach {
            htmlElement.removeEventListener(it.event, it, it.options.toJsObject())
        }

        currentListeners = list

        currentListeners.forEach {
            htmlElement.addEventListener(it.event, it, it.options.toJsObject())
        }
    }

    private fun Options.BooleanValue.toBoolean(): Boolean? = when (this) {
        Options.BooleanValue.True -> true
        Options.BooleanValue.False -> false
        Options.BooleanValue.Default -> null
    }

    // This conversion helps to preserve the default values of options parameters (defaults can vary per browser)
    private fun Options.toJsObject(): EventListenerOptions {
        val options = this
        return jsObject {
            if (options.once != Options.BooleanValue.Default) once = options.once.toBoolean()!!
            if (options.passive != Options.BooleanValue.Default) passive = options.passive.toBoolean()!!
            if (options.capture != Options.BooleanValue.Default) capture = options.capture.toBoolean()!!
        }
    }

    fun insert(index: Int, nodeWrapper: DomNodeWrapper) {
        val length = node.childNodes.length
        if (index < length) {
            node.insertBefore(nodeWrapper.node, node.childNodes[index]!!)
        } else {
            node.appendChild(nodeWrapper.node)
        }
    }

    fun remove(index: Int, count: Int) {
        repeat(count) {
            node.removeChild(node.childNodes[index]!!)
        }
    }

    fun move(from: Int, to: Int, count: Int) {
        if (from == to) {
            return // nothing to do
        }

        for (i in 0 until count) {
            // if "from" is after "to," the from index moves because we're inserting before it
            val fromIndex = if (from > to) from + i else from
            val toIndex = if (from > to) to + i else to + count - 2

            val child = node.removeChild(node.childNodes[fromIndex]!!)
            node.insertBefore(child, node.childNodes[toIndex]!!)
        }
    }
}


class DomElementWrapper(override val node: HTMLElement): DomNodeWrapper(node) {
    fun updateAttrs(attrs: Map<String, String>) {
        while (node.attributes.length > 0) {
            node.removeAttributeNode(node.attributes[0]!!)
        }

        attrs.forEach {
            node.setAttribute(it.key, it.value)
        }
    }

    fun updateProperties(list: List<Pair<(Element, Any) -> Unit, Any>>) {
        if (node.className.isNotEmpty()) node.className = ""

        list.forEach { it.first(node, it.second) }
    }

    fun updateStyleDeclarations(style: StyleHolder?) {
        node.removeAttribute("style")

        style?.properties?.forEach { (name, value) ->
            setProperty(node.style, name, value)
        }
        style?.variables?.forEach { (name, value) ->
            setVariable(node.style, name, value)
        }
    }
}
