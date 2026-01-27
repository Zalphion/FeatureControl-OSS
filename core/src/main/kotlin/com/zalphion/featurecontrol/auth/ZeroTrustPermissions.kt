package com.zalphion.featurecontrol.auth

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.users.UserId

open class ZeroTrustPermissions: Permissions<Unit> {
    override val principal = Unit
    override fun teamCreate() = false

    override fun teamRead(teamId: TeamId) = false
    override fun teamUpdate(teamId: TeamId) = false
    override fun teamDelete(teamId: TeamId) = false

    override fun userUpdate(userId: UserId) = false
    override fun userDelete(userId: UserId) = false

    override fun memberCreate(teamId: TeamId) = false
    override fun memberRead(teamId: TeamId, userId: UserId) = false
    override fun memberUpdate(teamId: TeamId, userId: UserId) = false
    override fun memberDelete(teamId: TeamId, userId: UserId) = false

    override fun applicationCreate(teamId: TeamId) = false
    override fun applicationRead(teamId: TeamId, appId: AppId) = false
    override fun applicationUpdate(teamId: TeamId, appId: AppId) = false
    override fun applicationDelete(teamId: TeamId, appId: AppId) = false

    override fun featureCreate(teamId: TeamId, appId: AppId) = false
    override fun featureRead(teamId: TeamId, appId: AppId, key: FeatureKey) = false
    override fun featureUpdate(teamId: TeamId, appId: AppId, key: FeatureKey) = false
    override fun featureDelete(teamId: TeamId, appId: AppId, key: FeatureKey) = false

    override fun featureRead(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName) = false
    override fun featureUpdate(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName) = false

    override fun configRead(teamId: TeamId, appId: AppId) = false
    override fun configUpdate(teamId: TeamId, appId: AppId) = false

    override fun configRead(teamId: TeamId, appId: AppId, environment: EnvironmentName) = false
    override fun configUpdate(teamId: TeamId, appId: AppId, environment: EnvironmentName) = false
}