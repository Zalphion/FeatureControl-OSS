package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.apikeys.ApiKeyStorage
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.web.ApplicationCardComponent
import com.zalphion.featurecontrol.applications.web.ConfigCardComponent
import com.zalphion.featurecontrol.applications.web.FeatureCardComponent
import com.zalphion.featurecontrol.applications.web.NewApplicationModalComponent
import com.zalphion.featurecontrol.applications.web.UpdateApplicationModalComponent
import com.zalphion.featurecontrol.applications.web.createApplication
import com.zalphion.featurecontrol.applications.web.createCoreApplicationCreateDataLens
import com.zalphion.featurecontrol.applications.web.createCoreApplicationUpdateDataLens
import com.zalphion.featurecontrol.applications.web.deleteApplication
import com.zalphion.featurecontrol.applications.web.showApplications
import com.zalphion.featurecontrol.applications.web.updateApplication
import com.zalphion.featurecontrol.auth.PermissionsFactory
import com.zalphion.featurecontrol.auth.web.Sessions
import com.zalphion.featurecontrol.auth.web.SocialAuthorizer
import com.zalphion.featurecontrol.auth.web.createSessionCookie
import com.zalphion.featurecontrol.auth.web.csrfDoubleSubmitFilter
import com.zalphion.featurecontrol.auth.web.google
import com.zalphion.featurecontrol.auth.web.hMacJwt
import com.zalphion.featurecontrol.auth.web.loginView
import com.zalphion.featurecontrol.configs.ConfigStorage
import com.zalphion.featurecontrol.configs.dto.createCoreConfigEnvironmentDataLens
import com.zalphion.featurecontrol.configs.dto.createCoreConfigSpecDataLens
import com.zalphion.featurecontrol.configs.web.ConfigEnvironmentComponent
import com.zalphion.featurecontrol.configs.web.ConfigNavBarComponent
import com.zalphion.featurecontrol.configs.web.ConfigSpecComponent
import com.zalphion.featurecontrol.configs.web.httpGetConfigEnvironment
import com.zalphion.featurecontrol.configs.web.httpGetConfigSpec
import com.zalphion.featurecontrol.configs.web.httpPostConfigEnvironment
import com.zalphion.featurecontrol.configs.web.httpPostConfigSpec
import com.zalphion.featurecontrol.events.Event
import com.zalphion.featurecontrol.events.EventBus
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureStorage
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.web.FeatureComponent
import com.zalphion.featurecontrol.features.web.FeatureEnvironmentComponent
import com.zalphion.featurecontrol.features.web.NewFeatureModalComponent
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
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.members.MemberUpdateData
import com.zalphion.featurecontrol.members.web.InviteMemberModalComponent
import com.zalphion.featurecontrol.members.web.TeamsComponent
import com.zalphion.featurecontrol.members.web.MemberLenses
import com.zalphion.featurecontrol.members.web.MembersComponent
import com.zalphion.featurecontrol.members.web.RoleComponent
import com.zalphion.featurecontrol.members.web.acceptInvitation
import com.zalphion.featurecontrol.members.web.createMember
import com.zalphion.featurecontrol.members.web.deleteMember
import com.zalphion.featurecontrol.members.web.resendInvitation
import com.zalphion.featurecontrol.members.web.showMembers
import com.zalphion.featurecontrol.members.web.updateMember
import com.zalphion.featurecontrol.plugins.ComponentRegistry
import com.zalphion.featurecontrol.plugins.LensRegistry
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.plugins.toContainer
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamStorage
import com.zalphion.featurecontrol.teams.web.createTeam
import com.zalphion.featurecontrol.teams.web.updateTeam
import com.zalphion.featurecontrol.users.UserService
import com.zalphion.featurecontrol.users.UserStorage
import com.zalphion.featurecontrol.users.web.showUserSettings
import com.zalphion.featurecontrol.web.INDEX_PATH
import com.zalphion.featurecontrol.web.LOGIN_PATH
import com.zalphion.featurecontrol.web.LOGOUT_PATH
import com.zalphion.featurecontrol.web.PageLink
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.REDIRECT_PATH
import com.zalphion.featurecontrol.web.SESSION_COOKIE_NAME
import com.zalphion.featurecontrol.web.appIdLens
import com.zalphion.featurecontrol.web.applicationsUri
import com.zalphion.featurecontrol.web.configUri
import com.zalphion.featurecontrol.web.environmentNameLens
import com.zalphion.featurecontrol.web.featureKeyLens
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.web.isRichDelete
import com.zalphion.featurecontrol.web.isRichPut
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.permissionsLens
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.userIdLens
import dev.andrewohara.utils.http4k.logErrors
import dev.andrewohara.utils.http4k.logSummary
import dev.forkhandles.result4k.onFailure
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.core.cookie.replaceCookie
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.FlashAttributesFilter
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.filter.flash
import org.http4k.filter.withFlash
import org.http4k.format.AutoMarshalling
import org.http4k.lens.location
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Clock
import kotlin.collections.minByOrNull
import kotlin.random.Random

const val APP_NAME = "Feature Control"

fun createCore(
    clock: Clock,
    random: Random,
    config: CoreConfig,
    plugins: List<PluginFactory<*>> = emptyList(),
    storageDriver: StorageDriver,
    eventBusFn: (List<Plugin>) -> EventBus
): Core {
    val json = buildJson(plugins.mapNotNull { it.jsonExport })
    return Core(
        clock = clock,
        random = random,
        json = json,
        config = config,
        pluginFactories = plugins,
        storageDriver = storageDriver,
        eventBusFn = eventBusFn,
        teams = TeamStorage.create(config.teamsStorageName, storageDriver, json),
        users = UserStorage.create(config.usersStorageName, storageDriver, json),
        members = MemberStorage.create(config.membersStorageName, storageDriver, json),
        applications = ApplicationStorage.create(config.applicationsStorageName, storageDriver, json),
        features = FeatureStorage.create(config.featuresStorageName, storageDriver, json),
        configs = ConfigStorage.create(config.configsStorageName, config.configEnvironmentsTableName, storageDriver, json),
        apiKeys = ApiKeyStorage.create(config.apiKeysStorageName, storageDriver, json),
        sessions = Sessions.hMacJwt(
            clock = clock,
            appSecret = config.appSecret,
            issuer = config.origin.toString(),
            sessionLength = config.sessionLength,
            random = random
        )
    )
}

class Core internal constructor(
    val clock: Clock,
    val random: Random,
    val json: AutoMarshalling,
    val config: CoreConfig,
    val storageDriver: StorageDriver,
    val sessions: Sessions,
    val teams: TeamStorage,
    val users: UserStorage,
    val members: MemberStorage,
    val applications: ApplicationStorage,
    val features: FeatureStorage,
    val configs: ConfigStorage,
    val apiKeys: ApiKeyStorage,
    pluginFactories: List<PluginFactory<*>>,
    eventBusFn: (List<Plugin>) -> EventBus
) {
    val permissions = pluginFactories
        .firstNotNullOfOrNull { it.permissionsFactoryFn(this) }
        ?: PermissionsFactory.teamMembership(users, members)

    val extract = LensRegistry(pluginFactories.flatMap { it.lensExports(this) } + listOf(
        MemberLenses.coreCreate().toContainer(),
        createCoreApplicationCreateDataLens(json).toContainer(),
        createCoreApplicationUpdateDataLens(json).toContainer(),
        createCoreFeatureCreateDataLens(json).toContainer(),
        createCoreFeatureUpdateDataLens(json).toContainer(),
        createCoreFeatureEnvironmentLens(json).toContainer(),
        createCoreConfigSpecDataLens(json).toContainer(),
        createCoreConfigEnvironmentDataLens(json).toContainer()
    ))

    val render: ComponentRegistry = ComponentRegistry(pluginFactories.flatMap { it.componentExports(this) + listOf(
        TeamsComponent.core().toContainer(),
        MembersComponent.core(this).toContainer(),
        InviteMemberModalComponent.core().toContainer(),
        NewApplicationModalComponent.core(this).toContainer(),
        UpdateApplicationModalComponent.core(this).toContainer(),
        NewFeatureModalComponent.core(this).toContainer(),
        FeatureComponent.core(this).toContainer(),
        FeatureEnvironmentComponent.core(this).toContainer(),
        ConfigSpecComponent.core(this).toContainer(),
        ConfigEnvironmentComponent.core(this).toContainer(),
        ApplicationCardComponent.core().toContainer(),
        ConfigCardComponent.core().toContainer(),
        FeatureCardComponent.core().toContainer(),
        ConfigNavBarComponent.core().toContainer(),
        RoleComponent.core().toContainer(),
    )})

    private val plugins = pluginFactories.map { it.create(this) }

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

    fun getRequirements(data: MemberCreateData) = plugins
        .flatMap { it.getRequirements(data) }
        .toSet()

    fun getRequirements(data: MemberUpdateData) = plugins
        .flatMap { it.getRequirements(data) }
        .toSet()

    fun getPages(teamId: TeamId) = buildList {
        this += PageLink(PageSpec.applications, applicationsUri(teamId))

        val entitlements = getEntitlements(teamId)
        for (plugin in plugins) {
            addAll(plugin.getPages(teamId, entitlements))
        }
    }

    private val eventBus = eventBusFn(this.plugins)
    fun emitEvent(event: Event) = eventBus(event)

    fun getRoutes(): RoutingHttpHandler {
        // TODO move to plugin?
        val socialAuth = if (config.googleClientId == null) {
            SocialAuthorizer.noOp()
        } else {
            SocialAuthorizer.google(config.googleClientId, clock)
        }

        val sessionFilter = Filter { next ->
            { request ->
                request.cookie(SESSION_COOKIE_NAME)?.value
                    ?.let(sessions::verify)
                    ?.let(permissions::create)
                    ?.let { request.with(permissionsLens of it) }
                    ?.let(next)
                    ?: Response(Status.FOUND).location(Uri.of(LOGIN_PATH))
            }
        }

        return ResponseFilters
            .logSummary(clock = clock)
            .then(ServerFilters.logErrors())
            .then(routes(listOf(
                *plugins.mapNotNull { it.getRoutes(this) }.toTypedArray(),
                LOGIN_PATH bind Method.GET to {
                    Response(Status.OK).with(htmlLens of loginView())
                },
                REDIRECT_PATH bind Method.POST to fn@{ request ->
                    val userData = request.form("credential")
                        ?.let(socialAuth::invoke)
                        ?: return@fn Response(Status.UNAUTHORIZED)

                    val user = UserService(this)
                        .getOrCreate(userData)
                        .onFailure { error(it.reason) }

                    request.toIndex().replaceCookie(createSessionCookie(user.userId))
                },
                FlashAttributesFilter
                    .then(sessionFilter)
                    .then(csrfDoubleSubmitFilter(random, config.secureCookies, config.csrfTtl))
                    .then(getWebRoutes())
            )))
    }

    private fun getWebRoutes() = routes(listOf(
        // plugins can override existing routes
        *plugins.mapNotNull { it.getWebRoutes(this) }.toTypedArray(),
        INDEX_PATH bind Method.GET to { request: Request ->
            val permissions = permissionsLens(request)
            // FIXME go to team selector instead of trying to find a team
            val team = ListMembersForUser(permissions.principal.userId)
                .invoke(permissions, this)
                .onFailure { error(it) }
                .minByOrNull { it.member.teamId }
                ?.team
                ?: error("No teams available")

            Response(Status.FOUND)
                .let { request.flash()?.let(it::withFlash) ?: it }
                .location(applicationsUri(team.teamId))
        },
        "profile" bind Method.GET to showUserSettings(),
        "invitations/$teamIdLens" bind Method.POST to acceptInvitation(),
        LOGOUT_PATH bind Method.POST to {
            it.toIndex().invalidateCookie(SESSION_COOKIE_NAME, path = INDEX_PATH)
        },
        "/teams" bind routes(
            Method.POST bind createTeam(),
            "$teamIdLens" bind routes(listOf(
                Method.POST bind updateTeam(),
                Method.GET bind {
                    val teamId = teamIdLens(it)
                    Response(Status.FOUND).location(membersUri(teamId))
                },
                "members" bind routes(listOf(
                    Method.GET bind showMembers(),
                    isRichDelete bind deleteMember(),
                    isRichPut bind updateMember(),
                    Method.POST bind createMember(),
                    "$userIdLens" bind routes(listOf(
                        Method.POST bind resendInvitation()
                    ))
                )),
                "applications" bind routes(
                    Method.GET bind showApplications(),
                    Method.POST bind createApplication(),
                    "$appIdLens" bind routes(listOf(
                        Method.GET bind { request ->
                            val teamId = teamIdLens(request)
                            val appId = appIdLens(request)
                            Response(Status.FOUND).location(configUri(teamId, appId))
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
                )
            ))
        )
    ))
}