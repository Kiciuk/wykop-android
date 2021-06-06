package io.github.feelfreelinux.wykopmobilny.ui.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.feelfreelinux.wykopmobilny.databinding.BlacklistBlockedItemBinding

class BlacklistViewholder(private val binding: BlacklistBlockedItemBinding) : ViewHolder(binding.root) {
    fun bind(blockedName: String, unblockListener: (String) -> Unit) {
        binding.blockedTextView.text = blockedName
        binding.unlockImageView.setOnClickListener {
            unblockListener(blockedName)
        }
    }
}
