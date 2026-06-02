package com.cliche.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for [LatLng] data class.
 */
class LatLngTest {

    @Test
    fun testLatLngCreation() {
        val location = LatLng(45.0, -73.0)

        assertEquals(45.0, location.latitude, 0.001)
        assertEquals(-73.0, location.longitude, 0.001)
    }

    @Test
    fun testLatLngWithZeroValues() {
        val location = LatLng(0.0, 0.0)

        assertEquals(0.0, location.latitude, 0.001)
        assertEquals(0.0, location.longitude, 0.001)
    }

    @Test
    fun testLatLngWithNegativeValues() {
        val location = LatLng(-45.0, -73.0)

        assertEquals(-45.0, location.latitude, 0.001)
        assertEquals(-73.0, location.longitude, 0.001)
    }

    @Test
    fun testLatLngWithHighPrecisionValues() {
        val location = LatLng(45.123456789, -73.987654321)

        assertEquals(45.123456789, location.latitude, 0.000000001)
        assertEquals(-73.987654321, location.longitude, 0.000000001)
    }

    @Test
    fun testLatLngEqualsAndHashCode() {
        val location1 = LatLng(45.0, -73.0)
        val location2 = LatLng(45.0, -73.0)
        val location3 = LatLng(45.0, -74.0)

        assertEquals(location1, location2)
        assertEquals(location1.hashCode(), location2.hashCode())
        assertNotEquals(location1, location3)
    }

    @Test
    fun testLatLngCopy() {
        val original = LatLng(45.0, -73.0)
        val copied = original.copy(latitude = 46.0)

        assertEquals(46.0, copied.latitude, 0.001)
        assertEquals(-73.0, copied.longitude, 0.001)
    }

    @Test
    fun testLatLngToString() {
        val location = LatLng(45.0, -73.0)

        val string = location.toString()
        assert(string.contains("latitude=45.0"))
        assert(string.contains("longitude=-73.0"))
    }

    @Test
    fun testLatLngAtPoles() {
        val northPole = LatLng(90.0, 0.0)
        val southPole = LatLng(-90.0, 0.0)

        assertEquals(90.0, northPole.latitude, 0.001)
        assertEquals(-90.0, southPole.latitude, 0.001)
    }

    @Test
    fun testLatLngAtGreenwich() {
        val greenwich = LatLng(51.4778, 0.0015)

        assertEquals(51.4778, greenwich.latitude, 0.001)
        assertEquals(0.0015, greenwich.longitude, 0.001)
    }
}
