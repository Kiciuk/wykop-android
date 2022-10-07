package io.github.wykopmobilny.ui.adapters.viewholders

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.R
import io.github.wykopmobilny.databinding.CommentListItemBinding
import io.github.wykopmobilny.databinding.EntryCommentMenuBottomsheetBinding
import io.github.wykopmobilny.models.dataclass.Author
import io.github.wykopmobilny.models.dataclass.EntryComment
import io.github.wykopmobilny.models.dataclass.drawBadge
import io.github.wykopmobilny.ui.dialogs.confirmationDialog
import io.github.wykopmobilny.ui.fragments.entrycomments.EntryCommentActionListener
import io.github.wykopmobilny.ui.fragments.entrycomments.EntryCommentViewListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.ui.widgets.WykopEmbedView
import io.github.wykopmobilny.utils.api.getGroupColor
import io.github.wykopmobilny.utils.copyText
import io.github.wykopmobilny.utils.getActivityContext
import io.github.wykopmobilny.utils.layoutInflater
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.textview.prepareBody
import io.github.wykopmobilny.utils.textview.stripWykopFormatting
import io.github.wykopmobilny.utils.usermanager.UserManagerApi

class EntryCommentViewHolder(
    private val binding: CommentListItemBinding,
    private val userManagerApi: UserManagerApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
    private val commentActionListener: EntryCommentActionListener,
    private val commentViewListener: EntryCommentViewListener?,
    private val enableClickListener: Boolean,
) : RecyclableViewHolder(binding.root) {

    companion object {
        const val TYPE_EMBED = 9
        const val TYPE_NORMAL = 10
        const val TYPE_BLOCKED = 11

        /**
         * Inflates correct view (with embed, survey or both) depending on viewType
         */
        fun inflateView(
            parent: ViewGroup,
            viewType: Int,
            userManagerApi: UserManagerApi,
            navigator: NewNavigator,
            linkHandler: WykopLinkHandler,
            commentActionListener: EntryCommentActionListener,
            commentViewListener: EntryCommentViewListener?,
            enableClickListener: Boolean,
        ): EntryCommentViewHolder {
            val view = EntryCommentViewHolder(
                CommentListItemBinding.inflate(parent.layoutInflater, parent, false),
                userManagerApi,
                navigator,
                linkHandler,
                commentActionListener,
                commentViewListener,
                enableClickListener,
            )

            view.type = viewType

            if (viewType == TYPE_EMBED) view.inflateEmbed()

            return view
        }

        fun getViewTypeForEntryComment(comment: EntryComment): Int {
            return when {
                comment.isBlocked -> TYPE_BLOCKED
                comment.embed == null -> TYPE_NORMAL
                else -> TYPE_EMBED
            }
        }
    }

    private val userCredentials = userManagerApi.getUserCredentials()
    private val isUserAuthorized = userCredentials != null
    var type: Int = TYPE_NORMAL
    private var isAuthorComment: Boolean = false
    private var isOwnEntry: Boolean = false
    lateinit var embedView: WykopEmbedView

    private val isEmbedViewResized: Boolean
        get() = ::embedView.isInitialized && embedView.resized

    fun bindView(
        comment: EntryComment,
        entryAuthor: Author? = null,
        highlightCommentId: Long = 0,
        openSpoilersDialog: Boolean,
        enableYoutubePlayer: Boolean,
        enableEmbedPlayer: Boolean,
        showAdultContent: Boolean,
        hideNsfw: Boolean,
    ) {
        setupHeader(comment)
        setupButtons(comment)
        setupBody(
            comment = comment,
            openSpoilersDialog = openSpoilersDialog,
            enableYoutubePlayer = enableYoutubePlayer,
            enableEmbedPlayer = enableEmbedPlayer,
            showAdultContent = showAdultContent,
            hideNsfw = hideNsfw,
        )
        isOwnEntry = entryAuthor?.nick == userCredentials?.login
        isAuthorComment = entryAuthor?.nick == comment.author.nick
        setStyleForComment(comment, highlightCommentId)
    }

    private fun setupHeader(comment: EntryComment) {
        comment.author.apply {
            binding.avatarView.setAuthor(this)
            binding.avatarView.setOnClickListener { navigator.openProfileActivity(nick) }
            binding.authorTextView.apply {
                text = nick
                setTextColor(context.getGroupColor(group))
                setOnClickListener { navigator.openProfileActivity(nick) }
            }
            binding.patronBadgeTextView.isVisible = badge != null
            badge?.let {
                try {
                    badge?.drawBadge(binding.patronBadgeTextView)
                } catch (exception: Throwable) {
                    Napier.w("Couldn't draw badge", exception)
                }
            }
            binding.dateTextView.text = comment.date.replace(" temu", "")
            comment.app?.let {
                binding.dateTextView.text =
                    itemView.context.getString(
                        R.string.date_with_user_app,
                        comment.date.replace(" temu", ""),
                        comment.app,
                    )
            }
        }
    }

    private fun setupButtons(comment: EntryComment) {
        binding.moreOptionsTextView.setOnClickListener {
            openOptionsMenu(comment)
        }

        // Only show reply view in entry details
        binding.replyTextView.isVisible = isUserAuthorized && commentViewListener != null
        binding.replyTextView.setOnClickListener { commentViewListener?.addReply(comment.author) }

        // Setup vote button
        with(binding.voteButton) {
            isEnabled = true
            isButtonSelected = comment.isVoted
            voteCount = comment.voteCount
            voteListener = {
                commentActionListener.voteComment(comment)
            }
            unvoteListener = {
                commentActionListener.unvoteComment(comment)
            }
            setup(userManagerApi)
        }

        // Setup share button
        binding.shareTextView.setOnClickListener {
            navigator.shareUrl(comment.url)
        }
    }

    private fun setupBody(
        comment: EntryComment,
        openSpoilersDialog: Boolean,
        enableYoutubePlayer: Boolean,
        enableEmbedPlayer: Boolean,
        showAdultContent: Boolean,
        hideNsfw: Boolean,
    ) {
        // Add URL and click handler if body is not empty
        binding.replyTextView.isVisible = isUserAuthorized
        binding.replyTextView.setOnClickListener { commentViewListener?.addReply(comment.author) }
        binding.quoteTextView.isVisible = isUserAuthorized
        binding.quoteTextView.setOnClickListener { commentViewListener?.quoteComment(comment) }
        if (comment.body.isNotEmpty()) {
            binding.entryContentTextView.isVisible = true
            binding.entryContentTextView.prepareBody(
                comment.body,
                { linkHandler.handleUrl(it) },
                { handleClick(comment) },
                openSpoilersDialog,
            )
        } else {
            binding.entryContentTextView.isVisible = false
        }

        if (comment.embed != null && type == TYPE_EMBED) {
            embedView.setEmbed(
                embed = comment.embed,
                enableYoutubePlayer = enableYoutubePlayer,
                enableEmbedPlayer = enableEmbedPlayer,
                showAdultContent = showAdultContent,
                hideNsfw = hideNsfw,
                navigator = navigator,
                isNsfw = comment.isNsfw,
            )
        }

        if (enableClickListener) {
            itemView.setOnClickListener { handleClick(comment) }
        }
    }

    private fun openOptionsMenu(comment: EntryComment) {
        val activityContext = itemView.getActivityContext()!!
        val dialog = BottomSheetDialog(activityContext)
        val bottomSheetView = EntryCommentMenuBottomsheetBinding.inflate(activityContext.layoutInflater)
        dialog.setContentView(bottomSheetView.root)
        (bottomSheetView.root.parent as View).setBackgroundColor(Color.TRANSPARENT)
        bottomSheetView.apply {
            author.text = comment.author.nick
            date.text = comment.fullDate
            comment.app?.let {
                date.text = root.context.getString(
                    R.string.date_with_user_app,
                    comment.fullDate,
                    comment.app,
                )
            }

            entryCommentMenuCopy.setOnClickListener {
                it.context.copyText(comment.body.stripWykopFormatting(), "entry-comment-body")
                dialog.dismiss()
            }

            entryCommentMenuEdit.setOnClickListener {
                navigator.openEditEntryCommentActivity(comment.body, comment.entryId, comment.id, comment.embed)
                dialog.dismiss()
            }

            entryCommentMenuDelete.setOnClickListener {
                confirmationDialog(it.context) {
                    commentActionListener.deleteComment(comment)
                }.show()
                dialog.dismiss()
            }

            entryCommentMenuVoters.setOnClickListener {
                commentActionListener.getVoters(comment)
                dialog.dismiss()
            }

            entryCommentMenuReport.isVisible = isUserAuthorized && comment.violationUrl != null
            entryCommentMenuReport.setOnClickListener {
                navigator.openReportScreen(comment.violationUrl.let(::checkNotNull))
                dialog.dismiss()
            }

            val canUserEdit = isUserAuthorized &&
                comment.author.nick == userCredentials?.login
            entryCommentMenuDelete.isVisible = canUserEdit || isOwnEntry
            entryCommentMenuEdit.isVisible = canUserEdit
        }

        val mBehavior = BottomSheetBehavior.from(bottomSheetView.root.parent as View)
        dialog.setOnShowListener {
            mBehavior.peekHeight = bottomSheetView.root.height
        }
        dialog.show()
    }

    private fun handleClick(comment: EntryComment) {
        if (enableClickListener) {
            navigator.openEntryDetailsActivity(comment.entryId, isEmbedViewResized)
        }
    }

    fun inflateEmbed() {
        embedView = binding.entryImageViewStub.inflate() as WykopEmbedView
    }

    private fun setStyleForComment(comment: EntryComment, commentId: Long = -1) {
        val credentials = userCredentials
        if (credentials != null && credentials.login == comment.author.nick) {
            binding.authorBadgeStrip.isVisible = true
            binding.authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBadgeOwn))
        } else if (isAuthorComment) {
            binding.authorBadgeStrip.isVisible = true
            binding.authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBadgeAuthors))
        } else {
            binding.authorBadgeStrip.isVisible = false
        }

        if (commentId == comment.id) {
            binding.authorBadgeStrip.isVisible = true
            binding.authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.plusPressedColor))
        }
    }
}
