package io.github.wykopmobilny.utils.linkhandler

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.ui.modules.embedview.EmbedViewActivity
import io.github.wykopmobilny.ui.modules.embedview.YouTubeUrlParser
import io.github.wykopmobilny.ui.modules.links.linkdetails.LinkDetailsActivity
import io.github.wykopmobilny.ui.modules.mikroblog.entry.EntryActivity
import io.github.wykopmobilny.ui.modules.pm.conversation.ConversationActivity
import io.github.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.wykopmobilny.ui.modules.tag.TagActivity
import io.github.wykopmobilny.utils.linkhandler.linkparser.ConversationLinkParser
import io.github.wykopmobilny.utils.linkhandler.linkparser.EntryLinkParser
import io.github.wykopmobilny.utils.linkhandler.linkparser.LinkParser
import io.github.wykopmobilny.utils.linkhandler.linkparser.ProfileLinkParser
import io.github.wykopmobilny.utils.linkhandler.linkparser.TagLinkParser
import java.net.URI
import javax.inject.Inject

class WykopLinkHandler @Inject constructor(
    private val context: Activity,
    private val navigator: NewNavigator,
) {

    companion object {
        private const val PROFILE_PREFIX = '@'
        private const val TAG_PREFIX = '#'
        private const val ENTRY_MATCHER = "wpis"
        private const val LINK_MATCHER = "link"
        private const val PROFILE_MATCHER = "ludzie"
        private const val TAG_MATCHER = "tag"
        private const val PM_MATCHER = "wiadomosc-prywatna"
        private const val DELIMITER = "/"

        fun getLinkIntent(url: String, context: Context): Intent? {
            val parsedUrl = runCatching { URI(url.replace("\\", "")) }
                .recoverCatching { URI(url.replace("\\", "").replace("[", "").replace("]", "")) }
                .getOrElse { return null }
            val domain = parsedUrl.host
                .replace("www.", "")
                .substringBeforeLast(".")
                .substringAfterLast(".")
            return when (domain) {
                "wykop" -> {
                    val resource = url.substringAfter("wykop.pl/")
                    when (resource.substringBefore(DELIMITER)) {
                        ENTRY_MATCHER -> {
                            val entryId = EntryLinkParser.getEntryId(url)
                            if (entryId != null) {
                                EntryActivity.createIntent(context, entryId, EntryLinkParser.getEntryCommentId(url), false)
                            } else {
                                null
                            }
                        }
                        TAG_MATCHER -> TagActivity.createIntent(context, TagLinkParser.getTag(url))
                        PM_MATCHER -> ConversationActivity.createIntent(context, ConversationLinkParser.getConversationUser(url))
                        PROFILE_MATCHER -> ProfileActivity.createIntent(context, ProfileLinkParser.getProfile(url))
                        LINK_MATCHER -> {
                            val linkId = LinkParser.getLinkId(url)
                            if (linkId != null) {
                                LinkDetailsActivity.createIntent(context, linkId, LinkParser.getLinkCommentId(url))
                            } else {
                                null
                            }
                        }
                        else -> null
                    }
                }
                "gfycat", "streamable", "coub" -> EmbedViewActivity.createIntent(context, url)
                "youtu", "youtube" -> {
                    if (YouTubeUrlParser.isVideoUrl(url)) {
                        EmbedViewActivity.createIntent(context, url)
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
    }

    fun handleUrl(url: String, refreshNotifications: Boolean = false) {
        when (url.first()) {
            PROFILE_PREFIX -> handleProfile(url)
            TAG_PREFIX -> handleTag(url)
            else -> handleLink(url, refreshNotifications)
        }
    }

    private fun handleProfile(login: String) = navigator.openProfileActivity(login.removePrefix("@"))

    private fun handleTag(tag: String) = navigator.openTagActivity(tag.removePrefix(TAG_PREFIX.toString()))

    private fun handleLink(url: String, refreshNotifications: Boolean) {
        try {
            val intent = getLinkIntent(url, context)
            if (intent != null) {
                if (refreshNotifications) {
                    context.startActivityForResult(intent, NewNavigator.STARTED_FROM_NOTIFICATIONS_CODE)
                } else {
                    context.startActivity(intent)
                }
            } else {
                navigator.openBrowser(url)
            }
        } catch (_: Throwable) {
            // Something went wrong while parsing url, fallback to browser
            navigator.openBrowser(url)
        }
    }
}
