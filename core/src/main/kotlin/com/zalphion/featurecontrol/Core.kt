package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.apikeys.ApiKeyStorage
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.web.coreConfigCard
import com.zalphion.featurecontrol.applications.web.coreFeatureCard
import com.zalphion.featurecontrol.applications.web.coreNewAppModal
import com.zalphion.featurecontrol.applications.web.coreUpdateAppModal
import com.zalphion.featurecontrol.applications.web.createApplication
import com.zalphion.featurecontrol.applications.web.createCoreApplicationCreateDataLens
import com.zalphion.featurecontrol.applications.web.createCoreApplicationUpdateDataLens
import com.zalphion.featurecontrol.applications.web.deleteApplication
import com.zalphion.featurecontrol.applications.web.showApplications
import com.zalphion.featurecontrol.applications.web.updateApplication
import com.zalphion.featurecontrol.auth.web.Sessions
import com.zalphion.featurecontrol.auth.web.SocialAuthorizer
import com.zalphion.featurecontrol.auth.web.authRoutes
import com.zalphion.featurecontrol.auth.web.csrfDoubleSubmitFilter
import com.zalphion.featurecontrol.auth.web.google
import com.zalphion.featurecontrol.auth.web.hMacJwt
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.ConfigStorage
import com.zalphion.featurecontrol.configs.Property
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.configs.web.coreConfigEnvironment
import com.zalphion.featurecontrol.configs.web.coreConfigNavBar
import com.zalphion.featurecontrol.configs.web.createCoreConfigEnvironmentDataLens
import com.zalphion.featurecontrol.configs.web.createCoreConfigSpecDataLens
import com.zalphion.featurecontrol.configs.web.httpGetConfigEnvironment
import com.zalphion.featurecontrol.configs.web.httpGetConfigSpec
import com.zalphion.featurecontrol.configs.web.httpPostConfigEnvironment
import com.zalphion.featurecontrol.configs.web.httpPostConfigSpec
import com.zalphion.featurecontrol.configs.web.renderConfigSpec
import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.events.Event
import com.zalphion.featurecontrol.events.EventBus
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.FeatureStorage
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.web.coreFeature
import com.zalphion.featurecontrol.features.web.coreFeatureEnvironment
import com.zalphion.featurecontrol.features.web.coreFeatureModal
import com.zalphion.featurecontrol.features.web.createCoreFeatureCreateDataLens
import com.zalphion.featurecontrol.features.web.createCoreFeatureEnvironmentLens
import com.zalphion.featurecontrol.features.web.createCoreFeatureUpdateDataLens
import com.zalphion.featurecontrol.features.web.httpDeleteFeature
import com.zalphion.featurecontrol.features.web.httpGetFeature
import com.zalphion.featurecontrol.features.web.httpGetFeatureEnvironment
import com.zalphion.featurecontrol.features.web.httpPostFeature
import com.zalphion.featurecontrol.features.web.httpPostFeatureEnvironment
import com.zalphion.featurecontrol.features.web.httpPutFeature
import com.zalphion.featurecontrol.members.ListMembersForUser
import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.members.web.acceptInvitation
import com.zalphion.featurecontrol.members.web.deleteMember
import com.zalphion.featurecontrol.members.web.resendInvitation
import com.zalphion.featurecontrol.members.web.showInvitations
import com.zalphion.featurecontrol.members.web.showMembers
import com.zalphion.featurecontrol.members.web.updateMember
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamStorage
import com.zalphion.featurecontrol.teams.web.createTeam
import com.zalphion.featurecontrol.teams.web.updateTeam
import com.zalphion.featurecontrol.users.UserStorage
import com.zalphion.featurecontrol.users.web.showUserSettings
import com.zalphion.featurecontrol.web.INDEX_PATH
import com.zalphion.featurecontrol.web.LOGIN_PATH
import com.zalphion.featurecontrol.web.PageLink
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.SESSION_COOKIE_NAME
import com.zalphion.featurecontrol.web.USER_SETTINGS_PATH
import com.zalphion.featurecontrol.web.appIdLens
import com.zalphion.featurecontrol.web.applicationsUri
import com.zalphion.featurecontrol.web.configUri
import com.zalphion.featurecontrol.web.environmentNameLens
import com.zalphion.featurecontrol.web.featureKeyLens
import com.zalphion.featurecontrol.web.isRichDelete
import com.zalphion.featurecontrol.web.isRichPut
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.principalLens
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.userIdLens
import dev.andrewohara.utils.http4k.logErrors
import dev.andrewohara.utils.http4k.logSummary
import dev.forkhandles.result4k.onFailure
import kotlinx.html.FlowContent
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.cookie.cookie
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.FlashAttributesFilter
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.filter.flash
import org.http4k.filter.withFlash
import org.http4k.format.AutoMarshalling
import org.http4k.lens.BodyLens
import org.http4k.lens.location
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import java.time.Clock
import kotlin.collections.minByOrNull
import kotlin.random.Random

const val APP_NAME = "Feature Control"

class CoreBuilder(
    val clock: Clock,
    val random: Random,
    origin: Uri,
    staticUri: Uri,
    appSecret: AppSecret,
    plugins: List<PluginFactory<*>> = emptyList(),
    private val storage: Storage,
    private val eventBusFn: (List<Plugin>) -> EventBus
) {
    var plugins = plugins.toMutableList()
    var config = CoreConfig(origin, staticUri, appSecret)

    fun build(fn: CoreBuilder.() -> Unit = {}): Core {
        fn()
        return Core(
            clock = clock,
            random = random,
            json = buildJson(plugins.mapNotNull { it.jsonExport }),
            config = config,
            plugins = plugins,
            storage = storage,
            eventBusFn = eventBusFn,
            sessions = Sessions.hMacJwt(
                clock = clock,
                appSecret = config.appSecret,
                issuer = config.origin.toString(),
                sessionLength = config.sessionLength,
                random = random
            )
        )
    }
}

class Core internal constructor(
    val clock: Clock,
    val random: Random,
    val json: AutoMarshalling,
    val config: CoreConfig,
    val storage: Storage,
    val sessions: Sessions,
    plugins: List<PluginFactory<*>>,
    eventBusFn: (List<Plugin>) -> EventBus
) {
    private val plugins = plugins.map { it.create(this) }

    // storage
    val teams = TeamStorage.create(storage, json)
    val features = FeatureStorage.create(storage, json)
    val apps = ApplicationStorage.create(storage, json)
    val apiKeys = ApiKeyStorage.create(storage, json)
    val users = UserStorage.create(storage, json)
    val members = MemberStorage.create(storage, json)
    val configs = ConfigStorage.create(storage, json)

    fun getEntitlements(teamId: TeamId) = plugins
        .flatMap { it.getEntitlements(teamId) }
        .toSet()

    fun getRequirements(data: FeatureCreateData) = plugins
        .flatMap { it.getRequirements(data) }
        .toSet()

    fun getRequirements(data: FeatureUpdateData) = plugins
        .flatMap { it.getRequirements(data) }
        .toSet()

    fun getRequirements(environment: Environment) = plugins
        .flatMap { it.getRequirements(environment) }
        .toSet()

    // applications
    fun renderNewApplicationModal(flow: FlowContent, team: Team) = plugins
        .firstNotNullOfOrNull { it.renderNewApplicationModal(flow, team) }
        ?: with(flow) { coreNewAppModal(this@Core, team) }

    fun renderUpdateApplicationModal(flow: FlowContent, application: Application) = plugins
        .firstNotNullOfOrNull { it.renderUpdateApplicationModal(flow, application) }
        ?: with(flow) { coreUpdateAppModal(this@Core, application) }

    fun createApplicationCreateDataLens() = plugins
        .firstNotNullOfOrNull { it.createApplicationCreateDataLens() }
        ?: createCoreApplicationCreateDataLens(json)

    fun createApplicationUpdateDataLens() = plugins
        .firstNotNullOfOrNull { it.createApplicationUpdateDataLens() }
        ?: createCoreApplicationUpdateDataLens(json)

    // features
    fun newFeatureModal(flow: FlowContent, application: Application): String = plugins
        .firstNotNullOfOrNull { it.newFeatureModal(flow, application) }
        ?: with(flow) { coreFeatureModal(this@Core, application) }

    fun createFeatureCreateDataLens() = plugins
        .firstNotNullOfOrNull { it.createFeatureCreateDataLens() }
        ?: createCoreFeatureCreateDataLens(json)

    fun createFeatureUpdateDataLens() = plugins
        .firstNotNullOfOrNull { it.createFeatureUpdateDataLens() }
        ?: createCoreFeatureUpdateDataLens(json)

    fun createFeatureEnvironmentDataLens() = plugins
        .firstNotNullOfOrNull { it.createFeatureEnvironmentDataLens() }
        ?: createCoreFeatureEnvironmentLens(json)

    fun createConfigCard(application: Application) = plugins
        .firstNotNullOfOrNull { it.configCard(application) }
        ?: coreConfigCard(application)

    fun createFeatureCard(application: Application, feature: Feature) = plugins
        .firstNotNullOfOrNull { it.featureCard(application, feature) }
        ?: coreFeatureCard(feature)

    fun featureContent(flow: FlowContent, application: Application, feature: Feature): Unit = plugins
        .firstNotNullOfOrNull { it.featureContent(flow, application, feature) }
        ?: with(flow) { coreFeature(this@Core, feature) }

    fun featureEnvironmentContent(
        flow: FlowContent, application: Application, feature: Feature,
        name: EnvironmentName, environment: FeatureEnvironment
    ): Unit = plugins
        .firstNotNullOfOrNull { it.featureEnvironmentContent(flow, application, feature, name, environment) }
        ?: with(flow) { coreFeatureEnvironment(this@Core, feature, environment, name) }

    // configs

    fun createConfigSpecDataLens(): BodyLens<Map<PropertyKey, Property>> = plugins
        .firstNotNullOfOrNull { it.createConfigSpecDataLens() }
        ?: createCoreConfigSpecDataLens(json)

    fun createConfigEnvironmentDataLens(): BodyLens<Map<PropertyKey, String>> = plugins
        .firstNotNullOfOrNull { it.createConfigEnvironmentDataLens() }
        ?: createCoreConfigEnvironmentDataLens(json)

    fun configSpecContent(flow: FlowContent, application: Application, spec: ConfigSpec): Unit = plugins
        .firstNotNullOfOrNull { it.configSpecContent(flow, application, spec) }
        ?: with(flow) {
            coreConfigNavBar(application, null)
            renderConfigSpec(this@Core, spec)
        }

    fun configEnvironmentContent(flow: FlowContent, application: Application, spec: ConfigSpec, values: ConfigEnvironment): Unit = plugins
        .firstNotNullOfOrNull { it.configEnvironmentContent(flow, application, spec, values) }
        ?: with(flow) { coreConfigEnvironment(this@Core, spec, values) }

    fun getPages(teamId: TeamId) = buildList {
        this += PageLink(PageSpec.applications, applicationsUri(teamId))
        for (plugin in plugins) {
            addAll(plugin.getPages(teamId))
        }
    }

    private val eventBus = eventBusFn(this.plugins)
    fun emitEvent(event: Event) = eventBus(event)

    fun getRoutes(): RoutingHttpHandler {
        val socialAuth = SocialAuthorizer.noOp()
            .let { if (config.googleClientId == null) it else SocialAuthorizer.google(config.googleClientId, clock) or it }

        val sessionFilter = Filter { next ->
            { request ->
                request.cookie(SESSION_COOKIE_NAME)?.value
                    ?.let(sessions::verify)
                    ?.let(users::get)
                    ?.let { request.with(principalLens of it) }
                    ?.let(next)
                    ?: Response(Status.FOUND).location(Uri.of(LOGIN_PATH))
            }
        }

        val authenticatedRoutes = routes(listOf(
            // plugins can override existing routes
            *plugins.mapNotNull { it.getWebRoutes() }.toTypedArray(),
            INDEX_PATH bind Method.GET to { request: Request ->
                val principal = principalLens(request)
                // FIXME go to team selector instead of trying to find a team
                val team = ListMembersForUser(principal.userId)
                    .invoke(principal, this)
                    .onFailure { error(it) }
                    .minByOrNull { it.member.teamId }
                    ?.team
                    ?: error("No teams available")

                Response(Status.FOUND)
                    .let { request.flash()?.let(it::withFlash) ?: it }
                    .location(applicationsUri(team.teamId))
            },
            USER_SETTINGS_PATH bind Method.GET to showUserSettings(),
            "/teams" bind routes(
                Method.POST bind createTeam(),
                "$teamIdLens" bind routes(listOf(
                    Method.GET bind { Response(Status.FOUND).location(membersUri(
                        teamIdLens(
                            it
                        )
                    )) },
                    Method.POST bind updateTeam(),
                    "applications" bind routes(listOf(
                        Method.GET bind showApplications(),
                        Method.POST bind createApplication(),
                    )),
                    "members" bind routes(listOf(
                        Method.GET bind showMembers(),
                        isRichDelete bind deleteMember(),
                        isRichPut bind updateMember(),
                        Method.POST bind acceptInvitation()
                    )),
                    "invitations" bind routes(listOf(
                        Method.GET bind showInvitations(),
                        "$userIdLens" bind Method.POST to resendInvitation()
                    ))
                ))
            ),
            "/applications/$appIdLens" bind routes(listOf(
                Method.GET bind { request ->
                    val appId = appIdLens(request)
                    Response(Status.FOUND).location(configUri(appId))
                },
                isRichDelete bind deleteApplication(),
                Method.POST bind updateApplication(),
                "config" bind routes(listOf(
                    Method.GET bind httpGetConfigSpec(),
                    Method.POST bind httpPostConfigSpec(),
                    "$environmentNameLens" bind routes(listOf(
                        Method.GET bind httpGetConfigEnvironment(),
                        Method.POST bind httpPostConfigEnvironment()
                    ))
                )),
                "features" bind routes(listOf(
                    Method.POST bind httpPostFeature(),
                    "$featureKeyLens" bind routes(listOf(
                        Method.GET bind httpGetFeature(),
                        isRichDelete bind httpDeleteFeature(),
                        isRichPut bind httpPutFeature(),
                        "environments/$environmentNameLens" bind routes(listOf(
                            Method.GET bind httpGetFeatureEnvironment(),
                            Method.POST bind httpPostFeatureEnvironment()
                        ))
                    ))
                ))
            ))
        ))

        return routes(listOf(
            *plugins.mapNotNull { it.getRoutes() }.toTypedArray(),
            ResponseFilters.logSummary(clock = clock)
                .then(ServerFilters.logErrors())
                .then(FlashAttributesFilter) // handle expiring flash message cookies
                .then(routes(listOf(
                    sessionFilter
                        .then(csrfDoubleSubmitFilter(random, config.secureCookies, config.csrfTtl))
                        .then(authenticatedRoutes),
                    // TODO is CSRF required here too?
                    authRoutes(this, socialAuth)
                ))),
            static(Classpath("/META-INF/resources/webjars")),
            static(Classpath("/static"))
        ))
    }
}