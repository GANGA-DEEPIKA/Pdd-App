package com.example.campusfix

import org.junit.Assert.*
import org.junit.Test

class AIVoiceAssistantTest {

    @Test
    fun testElectricalFanVoiceInput() {
        val speech = "The ceiling fan in Room 101 Block A is vibrating and not spinning, please repair it urgently"
        val result = AIVoiceAssistant.parseVoiceComplaint(speech)

        assertEquals("Electrical", result.category)
        assertEquals("Electrical Maintenance Department", result.department)
        assertTrue("Location should contain Block A", result.location.contains("Block A", ignoreCase = true))
        assertTrue("Location should contain Room 101", result.location.contains("Room 101", ignoreCase = true))
        assertTrue("Title should mention fan", result.title.contains("Fan", ignoreCase = true))
        assertTrue("Confidence should be high", result.confidence >= 90)
    }

    @Test
    fun testPlumbingWaterLeakVoiceInput() {
        val speech = "Water is leaking from the tap in mechanical block second floor washroom"
        val result = AIVoiceAssistant.parseVoiceComplaint(speech)

        assertEquals("Plumbing", result.category)
        assertEquals("Plumbing & Water Supply Department", result.department)
        assertTrue("Location should contain Mechanical Block", result.location.contains("Mechanical Block", ignoreCase = true))
        assertTrue("Location should contain Washroom or Floor", result.location.contains("Washroom", ignoreCase = true) || result.location.contains("Floor", ignoreCase = true))
        assertTrue("Title should indicate water/tap/leak", result.title.contains("Water", ignoreCase = true) || result.title.contains("Tap", ignoreCase = true))
    }

    @Test
    fun testWifiNetworkVoiceInput() {
        val speech = "WiFi connection is very slow and disconnecting frequently in Central Library"
        val result = AIVoiceAssistant.parseVoiceComplaint(speech)

        assertEquals("Network/WiFi", result.category)
        assertEquals("IT & Campus Network Services", result.department)
        assertTrue("Location should contain Library", result.location.contains("Library", ignoreCase = true))
        assertTrue("Title should mention WiFi", result.title.contains("WiFi", ignoreCase = true))
    }

    @Test
    fun testFurnitureVoiceInput() {
        val speech = "Broken wooden bench in B block room 305"
        val result = AIVoiceAssistant.parseVoiceComplaint(speech)

        assertEquals("Furniture", result.category)
        assertEquals("Carpentry & Infrastructure Maintenance", result.department)
        assertTrue("Location should contain B Block", result.location.contains("B Block", ignoreCase = true) || result.location.contains("Block", ignoreCase = true))
        assertTrue("Location should contain Room 305", result.location.contains("Room 305", ignoreCase = true))
        assertTrue("Title should indicate furniture", result.title.contains("Furniture", ignoreCase = true) || result.title.contains("Bench", ignoreCase = true))
    }

    @Test
    fun testCleanlinessVoiceInput() {
        val speech = "Dustbins are overflowing and corridor is dirty outside Lab 4"
        val result = AIVoiceAssistant.parseVoiceComplaint(speech)

        assertEquals("Cleanliness", result.category)
        assertEquals("Housekeeping & Campus Sanitation", result.department)
        assertTrue("Location should contain Lab 4", result.location.contains("Lab 4", ignoreCase = true))
        assertTrue("Title should indicate cleanliness/garbage", result.title.contains("Cleanliness", ignoreCase = true) || result.title.contains("Garbage", ignoreCase = true))
    }

    @Test
    fun testBlankVoiceInputFallback() {
        val result = AIVoiceAssistant.parseVoiceComplaint("")
        assertEquals("General Maintenance", result.category)
        assertEquals("Estate Maintenance Department", result.department)
        assertEquals("Campus Premises", result.location)
    }
}
