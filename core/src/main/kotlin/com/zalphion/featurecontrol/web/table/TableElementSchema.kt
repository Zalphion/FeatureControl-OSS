package com.zalphion.featurecontrol.web.table

import kotlinx.html.FlowContent

interface TableElementSchema {
    val key: String
    val label: String
    val defaultJson: String?
    val headerClasses: String?
    val headerStyles: Map<String, String>
    // each string is a row, e.g. foo: 'bar', increment: (value) => value + 1
    // the 'elements' key allows the xData defined here to use the elements alpine state
    val extraXData: (elementsRef: String, currentRef: String) -> List<String>

    fun render(flow: FlowContent, currentRef: String)
}