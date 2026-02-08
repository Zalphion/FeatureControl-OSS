package com.zalphion.featurecontrol.web.table

import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.xText
import kotlinx.html.FlowContent
import kotlinx.html.h5

class StaticTableElementSchema(
    override val label: String,
    override val key: String,
    override val headerClasses: String? = null,
    override val headerStyles: Map<String, String> = emptyMap(),
    override val extraXData: (elementsRef: String, currentRef: String) -> List<String> = { _, _ -> emptyList() }
): TableElementSchema {
    override val defaultJson = null

    override fun render(flow: FlowContent, currentRef: String) = flow.h5 {
        ariaLabel = label
        xText = "$currentRef.$key"
    }
}