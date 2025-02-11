package io.github.wykopmobilny.models.dataclass

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.widget.TextView
import io.github.wykopmobilny.utils.getActivityContext
import io.github.wykopmobilny.utils.openBrowser
import kotlinx.parcelize.Parcelize

class Author(
    val nick: String,
    val avatarUrl: String,
    val group: Int,
    val sex: String,
    var badge: AndroidPatronBadge? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readParcelable(AndroidPatronBadge::class.java.classLoader),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nick)
        parcel.writeString(avatarUrl)
        parcel.writeInt(group)
        parcel.writeString(sex)
        parcel.writeParcelable(badge, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Author> {
        override fun createFromParcel(parcel: Parcel): Author {
            return Author(parcel)
        }

        override fun newArray(size: Int): Array<Author?> {
            return arrayOfNulls(size)
        }
    }
}

fun AndroidPatronBadge.drawBadge(view: TextView) {
    val shape = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor(hexColor), Color.parseColor(hexColor)),
    )
    view.setOnClickListener {
        view.getActivityContext()?.openBrowser("https://patronite.pl/wykop-mobilny")
    }
    shape.cornerRadius = 30f
    view.text = text
    view.background = shape
}

@Parcelize
data class AndroidPatronBadge(
    val hexColor: String,
    val text: String,
) : Parcelable
