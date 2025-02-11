package io.github.wykopmobilny.tests.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.tests.base.Page
import io.github.wykopmobilny.utils.waitVisible
import org.hamcrest.Matchers.startsWith

object AboutDialogRegion : Page {

    private val appInfo = withText(startsWith("Wypok"))

    fun tapAppInfo() {
        onView(appInfo).waitVisible().perform(click())
    }
}
