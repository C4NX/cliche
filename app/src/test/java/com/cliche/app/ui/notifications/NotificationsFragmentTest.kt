package com.cliche.app.ui.notifications

import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.cliche.app.R
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [NotificationsFragment].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationsFragmentTest {

    @Test
    fun testFragmentDisplaysExpectedText() {
        val scenario: FragmentScenario<NotificationsFragment> = launchFragmentInContainer(
            themeResId = R.style.Theme_Cliché
        )

        scenario.onFragment { fragment ->
            val textView = fragment.requireView().findViewById<TextView>(R.id.text_notifications)
            TestCase.assertEquals("This is notifications Fragment", textView.text.toString())
        }
    }

    @Test
    fun testFragmentViewIsNotNullAfterLaunch() {
        val scenario: FragmentScenario<NotificationsFragment> = launchFragmentInContainer(
            themeResId = R.style.Theme_Cliché
        )

        scenario.onFragment { fragment ->
            TestCase.assertNotNull(fragment.view)
        }
    }
}