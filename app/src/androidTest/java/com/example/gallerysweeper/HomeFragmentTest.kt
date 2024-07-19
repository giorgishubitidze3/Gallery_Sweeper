package com.example.gallerysweeper

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.gallerysweeper.adapters.AllMediaAdapter

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentTest {


    @Before
    fun setup() {
        ActivityScenario.launch(MainActivity::class.java)
    }


    @Test
    fun testRecyclerViewDisplaysMediaItems() {
        Thread.sleep(2000)

        onView(withId(R.id.tv_total_item_count))
            .check(matches(isDisplayed()))

        onView(withId(R.id.card_view_photos)).perform(click())

        Thread.sleep(2000)

        onView(withId(R.id.recycler_view_all_items))
            .check(matches(isDisplayed()))

        onView(withId(R.id.recycler_view_all_items))
            .perform(RecyclerViewActions.scrollToPosition<AllMediaAdapter.ViewHolder>(10))
    }
}