package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.TableElementSchema
import com.zalphion.featurecontrol.web.confirmCancelButtons
import com.zalphion.featurecontrol.web.applicationsUri
import com.zalphion.featurecontrol.web.tableForm
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.*
import org.http4k.core.Uri

internal fun FlowContent.coreNewAppModal(core: Core, team: Team) = applicationModal(
    modalId = "new-application-modal-${team.teamId}",
    title = "New Application",
    formAction = applicationsUri(team.teamId),
    appName = null,
    environmentsTable = {
        environmentsTable(core, emptyList())
    },
    buttons = {
        confirmCancelButtons("Create")
    }
)

internal fun FlowContent.coreUpdateAppModal(core: Core, application: Application) = applicationModal(
    modalId = "update-application-modal-${application.appId}",
    title = "Update ${application.appName}",
    appName = application.appName,
    formAction = application.uri(),
    environmentsTable = {
        environmentsTable(core, application.environments.map { it.toDto() })
    },
    buttons = {
        confirmCancelButtons("Update")
    }
)

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
    core: Core,
    environments: List<EnvironmentDto>
) = div("uk-margin") {
    tableForm(
        inputName = "environments",
        schema = EnvironmentDto.tableSchema,
        mapper = core.json.asBiDiMapping<Array<EnvironmentDto>>()
            .map(Array<EnvironmentDto>::toList, List<EnvironmentDto>::toTypedArray),
        elements = environments,
        rowAriaLabel = "Environment"
    )
}

val EnvironmentDto.Companion.tableSchema get() = listOf(
    TableElementSchema.Input(
        label = "Environment",
        key = "name",
        type = InputType.text,
        placeholder = "dev, staging, prod, etc.",
        headerClasses = "uk-width-medium"
    ),
    TableElementSchema.Input(
        label = "Description",
        key = "description",
        type = InputType.text,
        placeholder = "Description",
        headerClasses = "uk-width-large",
        required = false
    ),
    TableElementSchema.Input(
        label = "Colour",
        key = "colour",
        type = InputType.text,
        placeholder = "#000000",
        headerClasses = "uk-width-small",
        required = false
    )
)