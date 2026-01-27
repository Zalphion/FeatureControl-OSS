package com.zalphion.featurecontrol.auth

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId

interface Permissions<Principal: Any> {
    val principal: Principal

    fun teamCreate(): Boolean

    fun teamRead(teamId: TeamId): Boolean
    fun teamUpdate(teamId: TeamId): Boolean
    fun teamDelete(teamId: TeamId): Boolean

    // there is no userRead; use memberRead
    fun userUpdate(userId: UserId): Boolean
    fun userDelete(userId: UserId): Boolean

    fun memberCreate(teamId: TeamId): Boolean
    fun memberRead(teamId: TeamId, userId: UserId): Boolean
    fun memberRead(member: Member) = memberRead(member.teamId, member.userId)
    fun memberUpdate(teamId: TeamId, userId: UserId): Boolean
    fun memberUpdate(member: Member) = memberUpdate(member.teamId, member.userId)
    fun memberDelete(teamId: TeamId, userId: UserId): Boolean
    fun memberDelete(member: Member) = memberDelete(member.teamId, member.userId)

    fun applicationCreate(teamId: TeamId): Boolean
    fun applicationRead(teamId: TeamId, appId: AppId): Boolean
    fun applicationUpdate(teamId: TeamId, appId: AppId): Boolean
    fun applicationDelete(teamId: TeamId, appId: AppId): Boolean

    fun featureCreate(teamId: TeamId, appId: AppId): Boolean
    fun featureRead(teamId: TeamId, appId: AppId, key: FeatureKey): Boolean
    fun featureUpdate(teamId: TeamId, appId: AppId, key: FeatureKey): Boolean
    fun featureDelete(teamId: TeamId, appId: AppId, key: FeatureKey): Boolean

    fun featureRead(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName): Boolean
    fun featureUpdate(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName): Boolean

    fun configRead(teamId: TeamId, appId: AppId): Boolean
    fun configUpdate(teamId: TeamId, appId: AppId): Boolean

    fun configRead(teamId: TeamId, appId: AppId, environment: EnvironmentName): Boolean
    fun configUpdate(teamId: TeamId, appId: AppId, environment: EnvironmentName): Boolean

    companion object
}