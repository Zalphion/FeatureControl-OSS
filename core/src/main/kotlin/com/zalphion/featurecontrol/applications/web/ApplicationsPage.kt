package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.MainNavBar
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.components.deleteModal
import com.zalphion.featurecontrol.web.components.modalIconButton
import com.zalphion.featurecontrol.web.components.modalTextButton
import com.zalphion.featurecontrol.web.components.moreMenu
import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.featureNotFound
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.web.NewFeatureModalComponent
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.SideNav
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.uri
import com.zalphion.featurecontrol.web.xData
import com.zalphion.featurecontrol.web.xModel
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.ul
import kotlin.collections.plus

data class ApplicationsPage<A, I, E>(
    val navBar: MainNavBar<MemberDetails>,
    val applications: List<Application>,
    val features: List<Feature>,
    val selectedApplication: A,
    val selectedItem: I,
    val selectedEnvironment: E
) {
    val filterModel = "application_element_filter"

    companion object {
        fun forTeam(
            app: FeatureControl, permissions: Permissions<User>, teamId: TeamId
        ): Result4k<ApplicationsPage<Application?, Void?, Void?>, AppError> {
            val navBar = MainNavBar
                .get(app, permissions, teamId, PageSpec.applications)
                .onFailure { return it }

            val applications = app.core.applications.list(teamId)
                .invoke(permissions, app)
                .onFailure { return it }
                .toList()

            return ApplicationsPage<Application?, Void?, Void?>(
                navBar = navBar,
                applications = applications,
                selectedApplication = null,
                features = emptyList(),
                selectedItem = null,
                selectedEnvironment = null,
            ).asSuccess()
        }

        fun forConfigSpec(
            app: FeatureControl, permissions: Permissions<User>, teamId: TeamId, appId: AppId
        ): Result4k<ApplicationsPage<Application, ConfigSpec, ConfigEnvironment?>, AppError> {
            val application = app.core.applications.get(teamId, appId)
                .invoke(permissions, app)
                .onFailure { return it }

            val features = app.core.features.list(teamId, appId)
                .invoke(permissions, app)
                .onFailure { return it }
                .toList()

            val applications = app.core.applications.list(application.teamId)
                .invoke(permissions, app)
                .onFailure { return it }
                .toList()

            val configSpec = app.core.configs.getSpec(teamId, appId)
                .invoke(permissions, app)
                .onFailure { return it }

            val navBar = MainNavBar
                .get(app, permissions, application.teamId, PageSpec.applications)
                .onFailure { return it }

            return ApplicationsPage<Application, ConfigSpec, ConfigEnvironment?>(
                navBar = navBar,
                applications = applications,
                selectedApplication = application,
                features = features,
                selectedItem = configSpec,
                selectedEnvironment = null
            ).asSuccess()
        }

        fun forConfigEnvironment(
            app: FeatureControl,
            permissions: Permissions<User>,
            teamId: TeamId,
            appId: AppId,
            environmentName: EnvironmentName
        ): Result4k<ApplicationsPage<Application, ConfigSpec, ConfigEnvironment>, AppError> {
            val model = forConfigSpec(app, permissions, teamId, appId).onFailure { return it }
            val environment = app.core.configs.getEnvironment(teamId, appId, environmentName)
                .invoke(permissions, app)
                .onFailure { return it }

            return ApplicationsPage(
                navBar = model.navBar,
                applications = model.applications,
                features = model.features,
                selectedApplication = model.selectedApplication,
                selectedItem = model.selectedItem,
                selectedEnvironment = environment
            ).asSuccess()
        }

        fun forFeature(
            app: FeatureControl, permissions: Permissions<User>, teamId: TeamId, appId: AppId, featureKey: FeatureKey
        ): Result4k<ApplicationsPage<Application, Feature, FeatureEnvironment?>, AppError> {
            val model = forConfigSpec(app, permissions, teamId, appId).onFailure { return it }

            val feature = model.features.find { it.key == featureKey } ?: return featureNotFound(appId, featureKey).asFailure()

            return ApplicationsPage<Application, Feature, FeatureEnvironment?>(
                navBar = model.navBar,
                applications = model.applications,
                features = model.features,
                selectedApplication = model.selectedApplication,
                selectedItem = feature,
                selectedEnvironment = null
            ).asSuccess()
        }

        fun forFeatureEnvironment(
            app: FeatureControl, permissions: Permissions<User>, teamId: TeamId, appId: AppId, featureKey: FeatureKey, environmentName: EnvironmentName
        ): Result4k<ApplicationsPage<Application, Feature, FeatureEnvironment>, AppError> {
            val model = forFeature(app, permissions, teamId, appId, featureKey).onFailure { return it }
            val environment = model.selectedApplication.getOrFail(environmentName)
                .map { model.selectedItem[environmentName] }
                .onFailure { return it }

            return ApplicationsPage(
                navBar = model.navBar,
                applications = model.applications,
                features = model.features,
                selectedApplication = model.selectedApplication,
                selectedItem = model.selectedItem,
                selectedEnvironment = environment
            ).asSuccess()
        }
    }
}

fun <A: Application?, I, E> ApplicationsPage<A, I, E>.render(
    app: FeatureControl,
    messages: List<FlashMessageDto>,
    selectedFeature: FeatureKey?,
    content: (FlowContent.() -> Unit)? = null,
) = app.pageSkeleton(
    messages = messages,
    topNav = navBar,
    sideNav = SideNav(
        pages = emptyList(),
        selected = null,
        topBar = {
            ariaLabel = "Applications Bar"
            xData = "{ $filterModel: ''}"
            style = "box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);"

            applicationsNavBar(
                app = app,
                team = navBar.selectedTeam.team,
                filterModel = filterModel
            )
            div {
                ariaLabel = "Application List"
                for (application in applications) {
                    app.render(this, ApplicationCardComponent(
                        application = application,
                        selected = application == selectedApplication,
                        filterModel = filterModel
                    ))
                }
            }
        }
    ),
    innerNav = if (selectedApplication == null) null else { app: FeatureControl ->
        ariaLabel = "Application Details"
        xData = "{ $filterModel: ''}"
        style = "box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);"

        applicationNavBar(app, selectedApplication, filterModel)

        app.render(this, ConfigCardComponent(
            application = selectedApplication,
            selected = selectedFeature == null,
            filterModel = filterModel
        ))

        for (feature in features) {
            app.render(this, FeatureCardComponent(
                application = selectedApplication,
                feature = feature,
                selected = selectedFeature == feature.key,
                filterModel = filterModel
            ))
        }
    },
    mainContent = { content?.invoke(this) }
)

private fun FlowContent.applicationsNavBar(
    app: FeatureControl,
    team: Team,
    filterModel: String
) {
    nav("uk-navbar-container") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h2("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                +"Applications"
            }
        }

        val newAppModalId = "team_${team.teamId}_new_application_modal"
        app.render(this, NewApplicationModalComponent(team, newAppModalId))

        div("uk-navbar-right") {
            div("uk-navbar-item") {
                modalIconButton(
                    tooltip = "New Application",
                    icon = "icon: plus",
                    modalId = newAppModalId
                )
            }
        }
    }

    form(classes = "uk-search uk-search-default uk-width-1-1 uk-margin-small-bottom") {
        span {
            attributes["uk-search-icon"] = ""
        }
        input(InputType.search, classes = "uk-search-input uk-width-1-1") {
            xModel = filterModel
            placeholder = "Search Application"
            ariaLabel = "Search"
        }
    }
}

private fun FlowContent.applicationNavBar(
    app: FeatureControl,
    application: Application,
    filterModel: String
) {
    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h2("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                attributes["aria-label"] = "Application Name"
                span {
                    attributes["uk-icon"] = "icon: album"
                }
                +application.appName.value
            }
        }

        div("uk-navbar-right") {
            ul("uk-iconnav") {
                li {
                    val modalId = "application_${application.appId}_new_feature"
                    app.render(this, NewFeatureModalComponent(application, modalId))
                    modalIconButton("New Feature", "icon: plus", modalId)
                }
                li {
                    moreMenu(application.appId) { dropdownId ->
                        li {
                            val updateModalId = "application_update_${application.appId}"
                            app.render(this, UpdateApplicationModalComponent(application, updateModalId))
                            modalTextButton(
                                label = "Update Application",
                                modalId = updateModalId,
                                icon = "icon: file-edit",
                                dropdownToCloseId = dropdownId)
                        }
                        li {
                            val deleteModalId = deleteModal(application.appName.value, application.uri())
                            modalTextButton(
                                label = "Delete Application",
                                icon = "icon: trash",
                                modalId = deleteModalId,
                                dropdownToCloseId = dropdownId
                            ) {
                                classes += "uk-text-danger"
                            }
                        }
                    }
                }
            }
        }
    }

    form(classes = "uk-search uk-search-default uk-width-1-1 uk-margin-small-bottom") {
        span {
            attributes["uk-search-icon"] = ""
        }
        input(InputType.search, classes = "uk-search-input uk-width-1-1") {
            xModel = filterModel
            placeholder = "Search Features"
            ariaLabel = "Search Features"
        }
    }
}