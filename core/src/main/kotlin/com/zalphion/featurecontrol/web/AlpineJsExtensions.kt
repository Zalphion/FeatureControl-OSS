package com.zalphion.featurecontrol.web

import kotlinx.html.FlowContent
import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.LI
import kotlinx.html.OPTION
import kotlinx.html.TR
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

fun FlowContent.onClick(js: String) {
    attributes["@click"] = js
}

fun FlowContent.onClickOutside(js: String) {
    attributes["@click.outside"] = js
}

fun FlowContent.onEnter(js: String) {
    attributes["@keydown.enter.prevent"] = js
}

fun FlowContent.onSpace(js: String) {
    attributes["@keydown.space.prevent"] = js
}

fun FlowContent.onTab(js: String) {
    attributes["@keydown.tab.prevent"] = js
}

fun FlowContent.onEscape(js: String) {
    attributes["@keydown.escape.capture"] = js
}

var HTMLTag.xModel: String?
    get() = attributes["x-model"]
    set(value) { attributes["x-model"] = value.orEmpty() }


class TEMPLATE(
    initialAttributes : Map<String, String>,
    override val consumer: TagConsumer<*>
) : HtmlBlockTag, HTMLTag(
    tagName = "template",
    consumer = consumer,
    initialAttributes = initialAttributes,
    inlineTag = false,
    emptyTag = false
)

@HtmlTagMarker
inline fun HTMLTag.template(classes: String? = null, crossinline block: TEMPLATE.() -> Unit = {}) {
    TEMPLATE(attributesMapOf("class", classes), consumer).visit(block)
}

@HtmlTagMarker
inline fun TEMPLATE.option(classes : String? = null, crossinline block : OPTION.() -> Unit = {}) {
    OPTION(attributesMapOf("class", classes), consumer).visit(block)
}

@HtmlTagMarker
inline fun TEMPLATE.tr(classes : String? = null, crossinline block : TR.() -> Unit = {}) {
    TR(attributesMapOf("class", classes), consumer).visit(block)
}

@HtmlTagMarker
inline fun TEMPLATE.li(classes : String? = null, crossinline block : LI.() -> Unit = {}) {
    LI(attributesMapOf("class", classes), consumer).visit(block)
}

data class AlpineEvent(
    val eventId: String,
    val dataKey: String
)