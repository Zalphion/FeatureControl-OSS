package com.zalphion.featurecontrol.emails

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.events.Event
import com.zalphion.featurecontrol.events.MemberCreatedEvent
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import org.http4k.core.Uri

fun Plugin.Companion.email(
    emails: EmailSender,
    loginUri: Uri,
    onInit: (Email) -> Unit = {}
) = object: PluginFactory<Email>(onCreate = onInit) {
    override fun createInternal(core: Core) = Email(emails, loginUri)
}

class Email internal constructor(
    val emails: EmailSender,
    val loginUri: Uri
): Plugin {
    override fun onEvent(event: Event): Result4k<Unit, AppError> {
        val message = when(event) {
            is MemberCreatedEvent -> FullEmailMessage.invitation(this, event.member)
            else -> null
        } ?: return Unit.asSuccess()

        return emails.send(message).map {  }
    }
}