package io.github.wykopmobilny.ui.widgets.drawerheaderview

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.github.wykopmobilny.databinding.DrawerHeaderViewLayoutBinding
import io.github.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.wykopmobilny.utils.getActivityContext
import io.github.wykopmobilny.utils.layoutInflater
import io.github.wykopmobilny.utils.loadImage
import io.github.wykopmobilny.utils.usermanager.UserCredentials

class DrawerHeaderWidget(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    companion object {
        // @TODO Put this in memory
        private const val PLACEHOLDER_IMAGE_URL = "https://i.imgur.com/aSm6pSJ.jpg"
    }

    private val binding = DrawerHeaderViewLayoutBinding.inflate(layoutInflater, this)

    var hashTagsNotificationsCount: Int?
        get() = binding.navNotificationsTag.text.toString().toIntOrNull()
        set(value) {
            binding.navNotificationsTag.text = value.toString()
        }

    var notificationCount: Int?
        get() = binding.navNotifications.text.toString().toIntOrNull()
        set(value) {
            binding.navNotifications.text = value.toString()
        }

    fun showDrawerHeader(isUserAuthorized: Boolean, credentials: UserCredentials?) {
        if (isUserAuthorized && credentials != null) {
            isVisible = true
            binding.navProfileImage.loadImage(credentials.avatarUrl)
            binding.navProfileImage.setOnClickListener {
                val context = getActivityContext()!!
                context.startActivity(ProfileActivity.createIntent(context, credentials.login))
            }
            binding.backgroundImage.loadImage(credentials.backgroundUrl ?: PLACEHOLDER_IMAGE_URL)
        } else {
            isVisible = false
        }
    }
}
