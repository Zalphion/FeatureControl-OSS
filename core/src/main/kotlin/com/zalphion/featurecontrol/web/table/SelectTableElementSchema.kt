package com.zalphion.featurecontrol.web.table

import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.xModel
import kotlinx.html.FlowContent
import kotlinx.html.option
import kotlinx.html.select

class SelectTableElementSchema(
    override val label: String,
    override val key: String,
    val options: List<String>,
    default: String?,
    private val isRequired: Boolean = true,
    override val headerClasses: String? = null,
    override val headerStyles: Map<String, String> = emptyMap(),
    override val extraXData: (elementsRef: String, currentRef: String) -> List<String> = { _, _ -> emptyList() }
): TableElementSchema {
    override val defaultJson = default?.let { "'$it'" }

    override fun render(flow: FlowContent, currentRef: String) = flow.select("uk-select") {
        xModel = "$currentRef.$key"
        ariaLabel = label
        required = isRequired

        option("uk-text-muted") {
            disabled = true
            selected = defaultJson == null
            +this@SelectTableElementSchema.label
        }

        for (option in options) {
            option {
                selected = option == defaultJson
                +option
            }
        }
    }
}