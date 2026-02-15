package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.applications.ApplicationService
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.configs.ConfigEnvironmentStorage
import com.zalphion.featurecontrol.configs.ConfigService
import com.zalphion.featurecontrol.configs.ConfigSpecStorage
import com.zalphion.featurecontrol.features.FeatureService
import com.zalphion.featurecontrol.features.FeatureStorage
import com.zalphion.featurecontrol.members.MemberService
import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.teams.TeamService
import com.zalphion.featurecontrol.teams.TeamStorage
import com.zalphion.featurecontrol.users.UserService
import com.zalphion.featurecontrol.users.UserStorage
import org.http4k.format.AutoMarshalling
import java.time.Clock
import kotlin.random.Random

class Core private constructor(
    val clock: Clock,
    val random: Random,
    val json: AutoMarshalling,
    val storageDriver: StorageDriver,
    config: CoreConfig,
) {
    companion object {
        fun build(
            clock: Clock,
            random: Random,
            storageDriver: StorageDriver,
            config: CoreConfig,
            plugins: List<PluginFactory<*>>
        ) = Core(clock, random, buildJson(plugins), storageDriver, config)
    }

    val teamStorage = TeamStorage.create(config.teamsStorageName, storageDriver, json)
    val userStorage = UserStorage.create(config.usersStorageName, storageDriver, json)
    val memberStorage = MemberStorage.create(config.membersStorageName, storageDriver, json)
    val applicationStorage = ApplicationStorage.create(config.applicationsStorageName, storageDriver, json)
    val featureStorage = FeatureStorage.create(config.featuresStorageName, storageDriver, json)
    val configSpecStorage = ConfigSpecStorage.create(config.configsStorageName, storageDriver, json)
    val configEnvironmentStorage = ConfigEnvironmentStorage.create(config.configEnvironmentsStorageName, storageDriver, json)

    val teams = TeamService(random, teamStorage, memberStorage)
    val users = UserService(random, teamStorage, userStorage, memberStorage)
    val members = MemberService(clock, random, config.invitationRetention, users, teamStorage, userStorage, memberStorage)
    val applications = ApplicationService(random, applicationStorage, featureStorage)
    val features = FeatureService(featureStorage, applicationStorage)
    val configs = ConfigService(config.appSecret, random,  applicationStorage,configSpecStorage, configEnvironmentStorage)
}