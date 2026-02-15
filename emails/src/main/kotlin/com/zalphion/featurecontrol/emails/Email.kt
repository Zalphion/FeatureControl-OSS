package com.zalphion.featurecontrol.emails

import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.events.Event
import com.zalphion.featurecontrol.events.MemberCreatedEvent
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.toFactory
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import org.http4k.core.Uri

fun Plugin.Companion.email(
    emails: EmailSender,
    appName: String,
    loginUri: Uri,
) = Email(emails, loginUri, appName).toFactory()

class Email internal constructor(
    private val emails: EmailSender,
    private val loginUri: Uri,
    private val appName: String
): Plugin {
    override fun onEvent(event: Event): Result4k<Unit, AppError> {
        val message = when(event) {
            is MemberCreatedEvent -> FullEmailMessage.invitation(appName, loginUri, event.member)
            else -> null
        } ?: return Unit.asSuccess()

        return emails.send(message).map {  }
    }
}