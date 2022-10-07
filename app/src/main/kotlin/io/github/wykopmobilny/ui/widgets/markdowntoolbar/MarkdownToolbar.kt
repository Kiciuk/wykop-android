package io.github.wykopmobilny.ui.widgets.markdowntoolbar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.WykopImageFile
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.databinding.ImagechooserBottomsheetBinding
import io.github.wykopmobilny.databinding.MarkdownToolbarBinding
import io.github.wykopmobilny.ui.dialogs.FormatDialogCallback
import io.github.wykopmobilny.ui.dialogs.editTextFormatDialog
import io.github.wykopmobilny.ui.widgets.FloatingImageView
import io.github.wykopmobilny.utils.CameraUtils
import io.github.wykopmobilny.utils.getActivityContext
import io.github.wykopmobilny.utils.layoutInflater

class MarkdownToolbar(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var photoUrl: String?
        get() = floatingImageView?.photoUrl
        set(value) {
            if (value != null) {
                remoteImageInserted()
                floatingImageView?.loadPhotoUrl(value)
            } else {
                floatingImageView?.removeImage()
            }
        }

    var photo: Uri?
        get() = floatingImageView?.photo
        set(value) {
            floatingImageView?.setImage(value)
        }

    var markdownListener: MarkdownToolbarListener? = null
    var remoteImageInserted: () -> Unit = {}
    var containsAdultContent = false
    var floatingImageView: FloatingImageView? = null
    private val markdownDialogs by lazy { MarkdownDialogs(context) }
    private val formatText: FormatDialogCallback = {
        markdownListener?.apply {
            val prefix = textBody.substring(0, selectionStart)
            textBody = prefix + it + textBody.substring(selectionStart, textBody.length)
            selectionStart = prefix.length + it.length
        }
    }

    init {
        val binding = MarkdownToolbarBinding.inflate(layoutInflater, this, true)

        val activity = getActivityContext() as BaseActivity
        val permissions = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showUploadPhotoBottomsheet()
            } else {
                Toast.makeText(
                    activity,
                    "Aplikacja wymaga uprawnień zapisu do pamięci aby wysyłać zdjęcia.",
                    Toast.LENGTH_LONG,
                )
                    .show()
            }
        }
        // Create callbacks
        markdownDialogs.apply {
            binding.formatBold.setOnClickListener { insertFormat("**", "**") }
            binding.formatQuote.setOnClickListener { insertFormat("\n>", "") }
            binding.formatItalic.setOnClickListener { insertFormat("_", "_") }
            binding.insertLink.setOnClickListener { insertFormat("[", "](www.wykop.pl)") }
            binding.insertCode.setOnClickListener { insertFormat("`", "`") }
            binding.insertSpoiler.setOnClickListener { insertFormat("\n!", "") }
            binding.insertEmoticon.setOnClickListener { showLennyfaceDialog(formatText) }
            binding.insertPhoto.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showUploadPhotoBottomsheet()
                } else {
                    permissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    fun getWykopImageFile(): WykopImageFile? = photo?.let { WykopImageFile(it, context) }

    fun hasUserEditedContent(): Boolean {
        return (
            photo != null ||
                !floatingImageView?.photoUrl.isNullOrEmpty() ||
                (markdownListener != null && markdownListener?.textBody!!.isNotEmpty())
            )
    }

    private fun insertFormat(prefix: String, suffix: String) {
        markdownListener?.apply {
            if (selectionEnd > selectionStart) {
                val bodyPrefix = textBody.substring(0, selectionStart)
                val bodySuffix = textBody.substring(selectionEnd, textBody.length)
                val selectedText = textBody.substring(selectionStart, selectionEnd)
                textBody = bodyPrefix + prefix + selectedText + suffix + bodySuffix
                setSelection(bodyPrefix.length + prefix.length, bodyPrefix.length + prefix.length + selectedText.length)
            } else {
                val bodyPrefix = textBody.substring(0, selectionStart)
                val bodySuffix = textBody.substring(selectionStart, textBody.length)
                val selectedText = "tekst"
                textBody = bodyPrefix + prefix + selectedText + suffix + bodySuffix
                setSelection(bodyPrefix.length + prefix.length, bodyPrefix.length + prefix.length + selectedText.length)
            }
        }
    }

    private fun showUploadPhotoBottomsheet() {
        val activityContext = getActivityContext()!!
        val dialog = BottomSheetDialog(activityContext)
        val bottomSheetView = ImagechooserBottomsheetBinding.inflate(activityContext.layoutInflater)
        dialog.setContentView(bottomSheetView.root)

        bottomSheetView.apply {
            insertGallery.setOnClickListener {
                markdownListener?.openGalleryImageChooser()
                dialog.dismiss()
            }

            insertCamera.setOnClickListener {
                val cameraUri = CameraUtils.createPictureUri(context)
                markdownListener?.openCamera(cameraUri!!)
                dialog.dismiss()
            }

            insertUrl.setOnClickListener {
                editTextFormatDialog(R.string.insert_photo_url, context) { insertImageFromUrl(it) }.show()
                dialog.dismiss()
            }

            markNsfwCheckbox.isChecked = containsAdultContent
            markNsfwCheckbox.setOnCheckedChangeListener { _, isChecked ->
                containsAdultContent = isChecked
            }

            markNsfw.setOnClickListener { markNsfwCheckbox.performClick() }
        }

        val mBehavior = BottomSheetBehavior.from(bottomSheetView.root.parent as View)
        dialog.setOnShowListener {
            mBehavior.peekHeight = bottomSheetView.root.height
        }
        dialog.show()
    }

    private fun insertImageFromUrl(url: String) {
        if (url.isNotBlank()) {
            remoteImageInserted()
            floatingImageView?.loadPhotoUrl(url)
        }
    }
}
