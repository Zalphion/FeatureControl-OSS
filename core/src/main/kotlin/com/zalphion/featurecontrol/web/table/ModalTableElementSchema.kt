package com.zalphion.featurecontrol.web.table

import com.zalphion.featurecontrol.web.AriaPopup
import com.zalphion.featurecontrol.web.ariaControls
import com.zalphion.featurecontrol.web.ariaHasPopup
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.onClick
import com.zalphion.featurecontrol.web.xText
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.button

class ModalTableElementSchema(
    override val label: String,
    override val key: String,
    // js expression returning string
    private val labelExpression: (currentRef: String) -> String = { "'$label'" },
    // js expression returning an object
    private val dispatchExpression: (currentRef: String) -> String = { "$it.$key" },
    private val modalId: String,
    private val dispatchEventId: String,
    private val enabled: Boolean = true,
    override val headerClasses: String? = null,
    override val headerStyles: Map<String, String> = emptyMap(),
    override val extraXData: (elementsRef: String, currentRef: String) -> List<String> = { _, _ -> emptyList() }
): TableElementSchema {
    override val defaultJson = null

    override fun render(flow: FlowContent, currentRef: String) {
        // nowrap so the button text never wraps to a new line when the table tries to shrink
        flow.button(type = ButtonType.button, classes = "uk-button uk-button-default uk-text-nowrap") {
            ariaHasPopup = AriaPopup.Dialog
            ariaLabel = label
            ariaControls = modalId
            disabled = !enabled
            xText = labelExpression(currentRef)

            val event = dispatchExpression(currentRef)
            onClick($$"$dispatch('$$dispatchEventId', $$event); $nextTick(() => UIkit.modal('#$$modalId').show())")
        }
    }
}