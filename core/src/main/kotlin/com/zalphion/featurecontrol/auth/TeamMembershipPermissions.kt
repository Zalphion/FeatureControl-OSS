package com.zalphion.featurecontrol.auth

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.users.UserId

fun Permissions.Companion.teamMembership(user: User, teamIds: Collection<TeamId>) = object : Permissions<User> {
    override val principal = user
    override fun teamCreate() = true

    override fun teamRead(teamId: TeamId) = teamId in teamIds
    override fun teamUpdate(teamId: TeamId) = teamId in teamIds
    override fun teamDelete(teamId: TeamId) = teamId in teamIds

    override fun userUpdate(userId: UserId) = principal.userId == userId
    override fun userDelete(userId: UserId) = principal.userId == userId

    override fun memberCreate(teamId: TeamId) = teamId in teamIds
    override fun memberRead(teamId: TeamId, userId: UserId) = teamId in teamIds
    override fun memberUpdate(teamId: TeamId, userId: UserId): Boolean {
        // cannot update self
        return teamId in teamIds && principal.userId != userId
    }
    override fun memberDelete(teamId: TeamId, userId: UserId) = teamId in teamIds

    override fun applicationCreate(teamId: TeamId) = teamId in teamIds
    override fun applicationRead(teamId: TeamId, appId: AppId) = teamId in teamIds
    override fun applicationUpdate(teamId: TeamId, appId: AppId) = teamId in teamIds
    override fun applicationDelete(teamId: TeamId, appId: AppId) = teamId in teamIds

    override fun featureCreate(teamId: TeamId, appId: AppId) = teamId in teamIds
    override fun featureRead(teamId: TeamId, appId: AppId, key: FeatureKey) = teamId in teamIds
    override fun featureUpdate(teamId: TeamId, appId: AppId, key: FeatureKey) = teamId in teamIds
    override fun featureDelete(teamId: TeamId, appId: AppId, key: FeatureKey) = teamId in teamIds

    override fun featureRead(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName) = teamId in teamIds
    override fun featureUpdate(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName) = teamId in teamIds

    override fun configRead(teamId: TeamId, appId: AppId) = teamId in teamIds
    override fun configUpdate(teamId: TeamId, appId: AppId) = teamId in teamIds

    override fun configRead(teamId: TeamId, appId: AppId, environment: EnvironmentName) = teamId in teamIds
    override fun configUpdate(teamId: TeamId, appId: AppId, environment: EnvironmentName) = teamId in teamIds
}