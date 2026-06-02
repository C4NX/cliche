package com.cliche.app

import com.cliche.app.models.*
import com.cliche.app.models.events.LikeEventTest
import com.cliche.app.utils.LatLngTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite that runs all unit tests in the application.
 * This allows running all tests together using JUnit4.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Model tests
    BookmarkTest::class,
    LikeTest::class,
    PostTest::class,
    ProfileTest::class,
    TimelinePostTest::class,
    LikeEventTest::class,
    
    // Utility tests
    LatLngTest::class,
)
class AllUnitTests
