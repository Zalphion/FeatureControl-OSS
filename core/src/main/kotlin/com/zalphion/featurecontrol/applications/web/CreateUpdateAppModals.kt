package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.confirmCancelButtons
import com.zalphion.featurecontrol.web.applicationsUri
import com.zalphion.featurecontrol.web.table.tableForm
import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.table.InputTableElementSchema
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.*
import org.http4k.core.Uri
import kotlin.collections.map

class NewApplicationModalComponent(val team: Team, val modalId: String) {
    companion object {
        fun core(app: FeatureControl) = Component<NewApplicationModalComponent> { flow, data ->
            flow.applicationModal(
                modalId = data.modalId,
                title = "New Application",
                formAction = applicationsUri(data.team.teamId),
                appName = null,
                environmentsTable = {
                    environmentsTable(app, emptyList())
                },
                buttons = {
                    confirmCancelButtons("Create")
                }
            )
        }
    }
}

class UpdateApplicationModalComponent(val application: Application, val modalId: String) {
    companion object {
        fun core(app: FeatureControl) = Component<UpdateApplicationModalComponent> { flow, data ->
            val application = data.application
            flow.applicationModal(
                modalId = data.modalId,
                title = "Update ${application.appName}",
                appName = application.appName,
                formAction = application.uri(),
                environmentsTable = {
                    environmentsTable(app, application.environments.map { it.toDto() })
                },
                buttons = {
                    confirmCancelButtons("Update")
                }
            )
        }
    }
}

fun FlowContent.applicationModal(
    modalId: String,
    title: String,
    formAction: Uri,
    appName: AppName?,
    environmentsTable: FlowContent.() -> Unit,
    buttons: FlowContent.() -> Unit,
): String {
    div("uk-modal uk-modal-container") {
        id = modalId

        div("uk-modal-dialog") {

            form(method = FormMethod.post, action = formAction.toString(), classes = "uk-form-stacked") {
                div("uk-modal-body") {
                    h2("uk-modal-title") { +title }

                    button(type = ButtonType.button, classes = "uk-modal-close-default") {
                        attributes["uk-close"] = ""
                    }

                    div("uk-margin") {
                        label("uk-form-label") {
                            htmlFor = "$modalId-name"
                            +"Name"
                        }
                        input(InputType.text, classes = "uk-input uk-width-medium") {
                            id = "$modalId-name"
                            name = "name"
                            value = appName?.value ?: ""
                            placeholder = "Name"
                            required = true
                        }
                    }

                    environmentsTable()
                }

                div("uk-modal-footer") {
                    buttons()
                }
            }
        }
    }

    return modalId
}

private fun FlowContent.environmentsTable(
    app: FeatureControl,
    environments: List<EnvironmentDto>
) = div("uk-margin") {
    tableForm(
        inputName = "environments",
        schema = EnvironmentDto.tableSchema,
        mapper = app.core.json.asBiDiMapping<Array<EnvironmentDto>>()
            .map(Array<EnvironmentDto>::toList, List<EnvironmentDto>::toTypedArray),
        elements = environments,
        rowAriaLabel = "Environment"
    )
}

val EnvironmentDto.Companion.tableSchema get() = listOf(
    InputTableElementSchema(
        label = "Environment",
        key = "name",
        type = InputType.text,
        placeholder = "dev, staging, prod, etc.",
        headerClasses = "uk-width-medium"
    ),
    InputTableElementSchema(
        label = "Description",
        key = "description",
        type = InputType.text,
        placeholder = "Description",
        headerClasses = "uk-width-large",
        required = false
    ),
    InputTableElementSchema(
        label = "Colour",
        key = "colour",
        type = InputType.text,
        placeholder = "#000000",
        headerClasses = "uk-width-small",
        required = false
    )
)