package com.zalphion.featurecontrol.web

import kotlinx.html.FORM
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.form
import kotlinx.html.input
import kotlinx.html.span

fun FlowContent.searchBar(modelName: String, prompt: String = "", content: FORM.() -> Unit = {}) {
    form(classes = "uk-search uk-search-default") {
        content()
        span {
            attributes["uk-search-icon"] = ""
        }
        input(InputType.search, classes = "uk-search-input") {
            attributes["x-model"] = modelName
            placeholder = prompt
        }
    }
}