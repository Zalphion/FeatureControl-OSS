package com.zalphion.featurecontrol.web.table

import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.xModel
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.input

class InputTableElementSchema(
    override val label: String,
    override val key: String,
    private val type: InputType,
    default: String? = null,
    private val required: Boolean = true,
    private val placeholder: String? = null,
    override val headerClasses: String? = null,
    override val headerStyles: Map<String, String> = emptyMap(),
    override val extraXData: (elementsRef: String, currentRef: String) -> List<String> = { _, _ -> emptyList() }
): TableElementSchema {
    override val defaultJson = default?.let { "'$it'" }

    override fun render(flow: FlowContent, currentRef: String) = flow.input(type) {
        classes += when (type) {
            InputType.radio, InputType.checkBox -> "uk-checkbox"
            else -> "uk-input"
        }
        ariaLabel = label
        xModel = "$currentRef.$key"
        this@InputTableElementSchema.placeholder?.let { attributes["placeholder"] = it }
        if (this@InputTableElementSchema.required) { attributes["required"] = "" }
    }
}