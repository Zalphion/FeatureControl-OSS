package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.auth.web.CSRF_COOKIE_NAME
import com.zalphion.featurecontrol.auth.web.CSRF_FORM_PARAM
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.web.components.navButton
import kotlinx.html.FlowContent
import kotlinx.html.SECTION
import kotlinx.html.ScriptCrossorigin
import kotlinx.html.aside
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.script
import kotlinx.html.section
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import org.http4k.core.Uri
import org.http4k.core.extend

fun FeatureControl.pageSkeleton(
    messages: List<FlashMessageDto>,
    subTitle: String? = null,
    topNav: MainNavBar<out MemberDetails?>? = null,
    sideNav: SideNav? = null,
    innerNav: (SECTION.(FeatureControl) -> Unit)? = null,
    mainContent: (FlowContent.(FeatureControl) -> Unit),
) = createHTML().html {
    head {
        if (subTitle != null) {
            title("$subTitle - $appName")
        } else {
            title(appName)
        }

        unsafe { // required for x-cloak to work
            raw("<style>[x-cloak] { display: none !important; }</style>")
        }

        // UI Kit
        link(config.staticUri.extend(Uri.of("uikit/${WebAssetVersions.UI_KIT}/dist/css/uikit.min.css")).toString(), "stylesheet", "text/css")
        script(src = config.staticUri.extend(Uri.of("/uikit/${WebAssetVersions.UI_KIT}/dist/js/uikit.min.js")).toString(), crossorigin = ScriptCrossorigin.anonymous) {}
        script(src = config.staticUri.extend(Uri.of("/uikit/${WebAssetVersions.UI_KIT}/dist/js/uikit-icons.min.js")).toString(), crossorigin = ScriptCrossorigin.anonymous) {}

        // alpine.js
        script(src = config.staticUri.extend(Uri.of("/alpinejs/${WebAssetVersions.ALPINE_JS}/dist/cdn.min.js")).toString(), crossorigin = ScriptCrossorigin.anonymous) {
            defer = true
        }

        // Day.js
        script(src = config.staticUri.extend(Uri.of("/dayjs/${WebAssetVersions.DAY_JS}/dayjs.min.js")).toString(), crossorigin = ScriptCrossorigin.anonymous) {}
        script(src = config.staticUri.extend(Uri.of("/dayjs/${WebAssetVersions.DAY_JS}/plugin/utc.js")).toString(), crossorigin = ScriptCrossorigin.anonymous) {}
    }

    body {
        attributes["x-data"] = "" // required for x-cloak to work
        attributes["x-cloak"] = ""  // signal for automated testing readiness; alpine.js will remove this when it's complete

        if (topNav != null) {
            this.renderNavbar( topNav)
        }

        div("uk-flex uk-height-viewport") {
            if (sideNav != null) {
                aside("uk-width-large uk-background-muted uk-padding-small uk-overflow-auto") {
                    style = "box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);"

                    sideNav.topBar(this, this@pageSkeleton)

                    if (sideNav.pages.isNotEmpty()) {
                        div {
                            for (page in sideNav.pages) {
                                navButton(page, selected = page.spec == sideNav.selected)
                            }
                        }
                    }
                }
            }

            if (innerNav != null) {
                section("uk-width-large uk-padding-small uk-overflow-auto") {
                    innerNav(this, this@pageSkeleton)
                }
            }

            main("uk-width-expand uk-padding-small uk-overflow-auto") {
                mainContent(this@pageSkeleton)
            }
        }

        val messagesScript = messages.joinToString("\n") { message ->
            val status = when (message.type) {
                FlashMessageDto.Type.Error -> "danger"
                FlashMessageDto.Type.Success -> "success"
                FlashMessageDto.Type.Info -> "primary"
                FlashMessageDto.Type.Warning -> "warning"
            }

            """UIkit.notification({
                message: '${message.message}',
                status: '$status',
            })"""
        }

        script {
            unsafe { raw($$"""
                function convertTimestamps() {
                    document.querySelectorAll('.timestamp').forEach(el => {
                        const utcTime = dayjs.utc(el.textContent.trim())
                        const format = el.dataset.format || 'MMM DD, YYYY HH:mm'
                        el.textContent = utcTime.local().format(format)
                        
                        el.setAttribute('uk-tooltip', `title: ${utcTime}; delay: 500;`)
                        UIkit.tooltip(el)
                    })
                }
                
                function setupCsrf() {
                    const csrfCookie = document.cookie.match(`(^|;)\\s*$$CSRF_COOKIE_NAME\\s*=\\s*["']?([^;"']+)["']?`)
                    const csrfToken = csrfCookie ? csrfCookie.pop() : null
                    if (!csrfToken) {
                        throw new Error('Could not find CSRF token')
                    }
                
                    document.querySelectorAll('form').forEach(form => {
                        let input = form.querySelector(`input[name="$$CSRF_FORM_PARAM"]`)
                        if (!input) {
                            input = document.createElement('input')
                            input.type = 'hidden'
                            input.name = '$$CSRF_FORM_PARAM'
                            form.appendChild(input)
                        }
                        input.value = csrfToken
                    })
                }
                
                dayjs.extend(dayjs_plugin_utc)
                document.addEventListener('DOMContentLoaded', convertTimestamps)
                document.addEventListener('DOMContentLoaded', setupCsrf)
                $$messagesScript
            """.trimIndent()
            ) }
        }
    }
}
