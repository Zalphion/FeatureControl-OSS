package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.applications.ApplicationCreateData
import com.zalphion.featurecontrol.applications.ApplicationService
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.applications.ApplicationUpdateData
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.web.ApplicationCardComponent
import com.zalphion.featurecontrol.applications.web.ConfigCardComponent
import com.zalphion.featurecontrol.applications.web.FeatureCardComponent
import com.zalphion.featurecontrol.applications.web.NewApplicationModalComponent
import com.zalphion.featurecontrol.applications.web.UpdateApplicationModalComponent
import com.zalphion.featurecontrol.applications.web.createCoreApplicationCreateDataLens
import com.zalphion.featurecontrol.applications.web.createCoreApplicationUpdateDataLens
import com.zalphion.featurecontrol.applications.web.httpDeleteApplication
import com.zalphion.featurecontrol.applications.web.httpGetApplications
import com.zalphion.featurecontrol.applications.web.httpPostApplication
import com.zalphion.featurecontrol.applications.web.httpPostApplications
import com.zalphion.featurecontrol.auth.PermissionsFactory
import com.zalphion.featurecontrol.auth.web.Sessions
import com.zalphion.featurecontrol.auth.web.SocialAuthorizer
import com.zalphion.featurecontrol.auth.web.csrfDoubleSubmitFilter
import com.zalphion.featurecontrol.auth.web.google
import com.zalphion.featurecontrol.auth.web.hMacJwt
import com.zalphion.featurecontrol.auth.web.loginView
import com.zalphion.featurecontrol.configs.ConfigEnvironmentStorage
import com.zalphion.featurecontrol.configs.ConfigService
import com.zalphion.featurecontrol.configs.ConfigSpecStorage
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
import com.zalphion.featurecontrol.features.FeatureService
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
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberService
import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.members.MemberUpdateData
import com.zalphion.featurecontrol.members.web.InviteMemberModalComponent
import com.zalphion.featurecontrol.members.web.MemberLenses
import com.zalphion.featurecontrol.members.web.MembersComponent
import com.zalphion.featurecontrol.members.web.RoleComponent
import com.zalphion.featurecontrol.members.web.TeamsComponent
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
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamService
import com.zalphion.featurecontrol.teams.TeamStorage
import com.zalphion.featurecontrol.teams.web.createTeam
import com.zalphion.featurecontrol.teams.web.updateTeam
import com.zalphion.featurecontrol.users.UserId
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
import kotlinx.html.FlowContent
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
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
import org.http4k.lens.location
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Clock
import kotlin.collections.minByOrNull
import kotlin.random.Random
import kotlin.reflect.KClass

class Core(
    val appName: String,
    val clock: Clock,
    val random: Random,
    val storageDriver: StorageDriver,
    private val plugins: List<PluginFactory<*>>,
    val config: CoreConfig,
    eventBusFn: (List<Plugin>) -> EventBus
) {
    val json = buildJson(plugins)

    val teamStorage = TeamStorage.create(config.teamsStorageName, storageDriver, json)
    val userStorage = UserStorage.create(config.usersStorageName, storageDriver, json)
    val memberStorage = MemberStorage.create(config.membersStorageName, storageDriver, json)
    val applicationStorage = ApplicationStorage.create(config.applicationsStorageName, storageDriver, json)
    val featureStorage = FeatureStorage.create(config.featuresStorageName, storageDriver, json)
    val configSpecStorage = ConfigSpecStorage.create(config.configsStorageName, storageDriver, json)
    val configEnvironmentStorage = ConfigEnvironmentStorage.create(config.configEnvironmentsStorageName, storageDriver, json)

    val teams = TeamService(random, teamStorage, memberStorage)
    val users = UserService(random, teamStorage, userStorage, memberStorage)
    val members = MemberService(this, users, teamStorage, userStorage, memberStorage)
    val applications = ApplicationService(this, applicationStorage, featureStorage)
    val features = FeatureService(this, featureStorage, applicationStorage)
    val configs = ConfigService(this,  applicationStorage,configSpecStorage, configEnvironmentStorage)

    private val sessions = Sessions.hMacJwt(
        clock = clock,
        appSecret = config.appSecret,
        issuer = config.origin.toString(),
        sessionLength = config.sessionLength,
        random = random
    )

    fun createSessionCookie(userId: UserId): Cookie {
        val (sessionId, expires) = sessions.create(userId)
        return Cookie(
            name = SESSION_COOKIE_NAME,
            value = sessionId,
            secure = config.secureCookies,
            sameSite = SameSite.Lax,
            httpOnly = true,
            expires = expires,
            path = INDEX_PATH
        )
    }

    val extract: LensRegistry = LensRegistry()
        .with(MemberLenses.coreCreate())
        .with(createCoreApplicationCreateDataLens(json))
        .with(createCoreApplicationUpdateDataLens(json))
        .with(createCoreFeatureCreateDataLens(json))
        .with(createCoreFeatureUpdateDataLens(json))
        .with(createCoreFeatureEnvironmentLens(json))
        .with(createCoreConfigSpecDataLens(json))
        .with(createCoreConfigEnvironmentDataLens(json))
        .plus(plugins.map { it.getLensRegistry(json) })

    private val components: ComponentRegistry = ComponentRegistry()
        .with(TeamsComponent.core())
        .with(MembersComponent.core())
        .with(InviteMemberModalComponent.core())
        .with(NewApplicationModalComponent.core())
        .with(UpdateApplicationModalComponent.core())
        .with(NewFeatureModalComponent.core())
        .with(FeatureComponent.core())
        .with(FeatureEnvironmentComponent.core(json))
        .with(ConfigSpecComponent.core())
        .with(ConfigEnvironmentComponent.core())
        .with(ApplicationCardComponent.core())
        .with(ConfigCardComponent.core())
        .with(FeatureCardComponent.core())
        .with(ConfigNavBarComponent.core())
        .with(RoleComponent.core())
        .plus(plugins.map { it.getComponentRegistry(json, storageDriver) })

    inline fun <reified T: Any> render(flow: FlowContent, data: T) = render(T::class, flow, data)
    fun <T: Any> render(type: KClass<T>, flow: FlowContent, data: T) = components.render(type, flow, this, data)

    fun getEntitlements(teamId: TeamId) = plugins
        .flatMap { it.getEntitlements(teamId) }
        .toSet()

    fun getRequirements(data: ApplicationCreateData) = plugins
        .flatMap { it.getRequirements(data) }
        .toSet()

    fun getRequirements(data: ApplicationUpdateData) = plugins
        .flatMap { it.getRequirements(data) }
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

    val permissions = plugins
        .firstNotNullOfOrNull { it.buildPermissionsFactory(this) }
        ?: PermissionsFactory.teamMembership(userStorage, memberStorage)

    private val runtimePlugins = plugins.map { it.create(this) }
    private val eventBus = eventBusFn(runtimePlugins)
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
                    ?.let { permissions.create(this, it) }
                    ?.let { request.with(permissionsLens of it) }
                    ?.let(next)
                    ?: Response(Status.FOUND).location(Uri.of(LOGIN_PATH))
            }
        }

        return ResponseFilters
            .logSummary(clock = clock)
            .then(ServerFilters.logErrors())
            .then(routes(listOf(
                *runtimePlugins.mapNotNull { it.getRoutes(this) }.toTypedArray(),
                LOGIN_PATH bind Method.GET to {
                    Response(Status.OK).with(htmlLens of loginView())
                },
                REDIRECT_PATH bind Method.POST to fn@{ request ->
                    val userData = request.form("credential")
                        ?.let(socialAuth::invoke)
                        ?: return@fn Response(Status.UNAUTHORIZED)

                    val user = users.getOrCreate(userData).onFailure { error(it.reason) }

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
        *runtimePlugins.mapNotNull { it.getWebRoutes(this) }.toTypedArray(),
        INDEX_PATH bind Method.GET to { request: Request ->
            val permissions = permissionsLens(request)
            // FIXME go to team selector instead of trying to find a team
            val team = members.list(permissions.principal.userId)
                .invoke(permissions)
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
                    isRichPut bind updateMember(),
                    Method.POST bind createMember(),
                    "$userIdLens" bind routes(listOf(
                        isRichDelete bind deleteMember(),
                        Method.POST bind resendInvitation()
                    ))
                )),
                "applications" bind routes(
                    Method.GET bind httpGetApplications(),
                    Method.POST bind httpPostApplications(),
                    "$appIdLens" bind routes(listOf(
                        Method.GET bind { request ->
                            val teamId = teamIdLens(request)
                            val appId = appIdLens(request)
                            Response(Status.FOUND).location(configUri(teamId, appId))
                        },
                        isRichDelete bind httpDeleteApplication(),
                        Method.POST bind httpPostApplication(),

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