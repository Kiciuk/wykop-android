package io.github.feelfreelinux.wykopmobilny.ui.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import io.github.feelfreelinux.wykopmobilny.databinding.ConversationListItemBinding
import io.github.feelfreelinux.wykopmobilny.models.dataclass.Conversation
import io.github.feelfreelinux.wykopmobilny.ui.modules.pm.conversation.ConversationActivity
import io.github.feelfreelinux.wykopmobilny.utils.api.getGroupColor

class ConversationViewHolder(private val binding: ConversationListItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindView(conversation: Conversation) {
        binding.authorAvatarView.setAuthor(conversation.user)
        binding.userNameTextView.apply {
            text = conversation.user.nick
            setTextColor(context.getGroupColor(conversation.user.group))
        }
        binding.entryDateTextView.text = conversation.lastUpdate
        binding.root.setOnClickListener { it.context.startActivity(ConversationActivity.createIntent(it.context, conversation.user.nick)) }
    }
}
