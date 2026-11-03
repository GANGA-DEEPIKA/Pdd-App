package com.example.campusfix

import org.junit.Assert.*
import org.junit.Test

class AIDuplicateEngineTest {

    @Test
    fun testExactDuplicateDetection() {
        val existing = ComplaintData(
            ticketId = "CMP881234",
            title = "Ceiling fan making loud noise and not spinning",
            details = "The fan in Room 101 Block A is vibrating and not working properly",
            category = "Electrical",
            location = "Block A, Room 101",
            status = "In Progress"
        )

        val newComplaint = ComplaintData(
            title = "Fan not working",
            details = "Broken ceiling fan in room 101 block a",
            category = "Electrical",
            location = "Block A, Room 101"
        )

        val result = AIDuplicateEngine.evaluate(newComplaint, existing)

        assertTrue("Expected duplicate to be detected", result.isDuplicate)
        assertTrue("Expected confidence >= 70%, was ${result.confidence}%", result.confidence >= 70)
        assertEquals("CMP881234", result.matchedComplaint?.ticketId)
        assertEquals("In Progress", result.matchedComplaint?.status)
        assertTrue("Expected category score high", result.categoryScore >= 0.8)
        assertTrue("Expected location score high", result.locationScore >= 0.8)
        assertTrue("Reason should not be empty", result.matchReason.isNotEmpty())
    }

    @Test
    fun testDifferentRoomLocationConflictPreventsFalsePositive() {
        val existing = ComplaintData(
            ticketId = "CMP992211",
            title = "Ceiling fan not working",
            details = "Fan stopped spinning in room 101",
            category = "Electrical",
            location = "Block A, Room 101",
            status = "Pending"
        )

        val newComplaint = ComplaintData(
            title = "Ceiling fan not working",
            details = "Fan stopped spinning in room 204",
            category = "Electrical",
            location = "Block A, Room 204"
        )

        val result = AIDuplicateEngine.evaluate(newComplaint, existing)

        assertFalse("Different rooms in same block should NOT be marked duplicate", result.isDuplicate)
        assertTrue("Confidence should be penalized below threshold", result.confidence < 65)
    }

    @Test
    fun testCompletelyDifferentComplaint() {
        val existing = ComplaintData(
            ticketId = "CMP771122",
            title = "Projector display blurred",
            details = "HDMI signal failing in Seminar Hall",
            category = "IT / Network",
            location = "Seminar Hall 1",
            status = "Assigned"
        )

        val newComplaint = ComplaintData(
            title = "Water tap dripping constantly",
            details = "Bathroom tap broken and leaking water",
            category = "Plumbing",
            location = "Main Building Restroom"
        )

        val result = AIDuplicateEngine.evaluate(newComplaint, existing)

        assertFalse("Completely different complaints must not be duplicates", result.isDuplicate)
        assertTrue("Confidence should be very low", result.confidence < 30)
    }

    @Test
    fun testSemanticSynonymAndOntologyMatch() {
        val existing = ComplaintData(
            ticketId = "CMP554433",
            title = "Pipeline dripping heavily",
            details = "Water leakage from washbasin pipe in Ground Floor Restroom",
            category = "Plumbing",
            location = "Ground Floor Restroom",
            status = "Waiting for Staff Assignment"
        )

        val newComplaint = ComplaintData(
            title = "Water tap leaking",
            details = "Faucet overflow and leaking sink in ground floor washroom",
            category = "Plumbing",
            location = "Ground Floor Washroom"
        )

        val result = AIDuplicateEngine.evaluate(newComplaint, existing)

        assertTrue("Semantic synonyms for plumbing and washroom should match", result.isDuplicate)
        assertTrue("Confidence should be >= 65%", result.confidence >= 65)
        assertEquals("CMP554433", result.matchedComplaint?.ticketId)
    }

    @Test
    fun testFindBestDuplicateMatchFromList() {
        val complaint1 = ComplaintData(
            ticketId = "CMP111",
            title = "Broken bench",
            details = "Bench leg cracked",
            category = "Furniture",
            location = "Room 301",
            status = "Resolved"
        )
        val complaint2 = ComplaintData(
            ticketId = "CMP222",
            title = "Switch spark problem",
            details = "Wall socket throwing sparks when plugged in",
            category = "Electrical",
            location = "Block B, Room 202",
            status = "Assigned"
        )
        val complaint3 = ComplaintData(
            ticketId = "CMP333",
            title = "AC cooling not working",
            details = "Air conditioner blowing warm air",
            category = "Electrical",
            location = "Block C, Lab 1",
            status = "In Progress"
        )

        val newComplaint = ComplaintData(
            title = "Electric switch sparking",
            details = "Socket dangerous spark when plugging charger in room 202 block b",
            category = "Electrical",
            location = "Block B, Room 202"
        )

        val best = AIDuplicateEngine.findBestDuplicateMatch(newComplaint, listOf(complaint1, complaint2, complaint3))

        assertTrue("Should detect duplicate from list", best.isDuplicate)
        assertEquals("Should identify CMP222 as the true duplicate", "CMP222", best.matchedComplaint?.ticketId)
        assertEquals("Assigned", best.matchedComplaint?.status)
    }
}

