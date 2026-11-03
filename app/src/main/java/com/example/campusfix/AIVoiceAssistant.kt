package com.example.campusfix

data class ParsedComplaintVoice(
    val rawSpeech: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val department: String = "",
    val location: String = "",
    val confidence: Int = 95
)

object AIVoiceAssistant {

    private data class CategoryRule(
        val cat: String,
        val dept: String,
        val keywords: List<String>
    )

    private val CATEGORY_RULES = listOf(
        CategoryRule(
            cat = "Electrical",
            dept = "Electrical Maintenance Department",
            keywords = listOf("fan", "fans", "light", "lights", "bulb", "bulbs", "tube", "tubelight", "current", "power", "switch", "socket", "plug", "wiring", "wire", "spark", "ac", "aircon", "air conditioner", "cooling", "voltage", "mcb", "trip", "street light", "fuse", "geyser", "heater", "electrical")
        ),
        CategoryRule(
            cat = "Plumbing",
            dept = "Plumbing & Water Supply Department",
            keywords = listOf("water", "tap", "taps", "faucet", "leak", "leaking", "leakage", "pipe", "pipeline", "dripping", "flush", "drain", "drainage", "sink", "washbasin", "sewage", "restroom", "washroom", "toilet", "tank", "motor", "drinking water", "purifier", "cooler", "plumbing")
        ),
        CategoryRule(
            cat = "Network/WiFi",
            dept = "IT & Campus Network Services",
            keywords = listOf("wifi", "wi-fi", "internet", "network", "lan", "ethernet", "router", "connection", "signal", "hotspot", "disconnect", "disconnecting", "slow internet", "bandwidth", "portal", "wifi slow")
        ),
        CategoryRule(
            cat = "Cleanliness",
            dept = "Housekeeping & Campus Sanitation",
            keywords = listOf("dirty", "dust", "dustbin", "trash", "garbage", "waste", "cleaning", "sweep", "sweeping", "broom", "smell", "stink", "stinking", "spill", "stain", "sanitation", "unhygienic", "litter", "overflowing", "corridor dirty", "clean")
        ),
        CategoryRule(
            cat = "Furniture",
            dept = "Carpentry & Infrastructure Maintenance",
            keywords = listOf("bench", "benches", "chair", "chairs", "desk", "desks", "table", "tables", "furniture", "board", "blackboard", "whiteboard", "podium", "shelf", "cupboard", "almirah", "wooden", "cot", "broken leg")
        ),
        CategoryRule(
            cat = "Laboratory",
            dept = "Academic Laboratories & AV Support",
            keywords = listOf("lab", "laboratory", "projector", "screen", "hdmi", "vga", "display", "experiment", "system", "computer", "pc", "monitor", "cpu", "mouse", "keyboard", "apparatus", "instrument", "microscope", "chemicals")
        ),
        CategoryRule(
            cat = "Hostel",
            dept = "Hostel Administration & Estate Office",
            keywords = listOf("hostel", "mess", "warden", "bed", "mattress", "roommate", "dining", "hostel room", "boys hostel", "girls hostel")
        ),
        CategoryRule(
            cat = "Security",
            dept = "Campus Security & Safety Division",
            keywords = listOf("security", "gate", "guard", "theft", "lost", "stolen", "cctv", "camera", "id card", "trespass", "safety", "lock", "padlock", "key", "stranger", "safe")
        ),
        CategoryRule(
            cat = "Transport",
            dept = "Transport & Fleet Operations",
            keywords = listOf("bus", "transport", "van", "driver", "vehicle", "route", "stop", "timing", "pass", "parking")
        )
    )

    fun parseVoiceComplaint(speech: String?): ParsedComplaintVoice {
        if (speech.isNullOrBlank()) {
            return ParsedComplaintVoice(
                rawSpeech = "",
                title = "Maintenance Issue",
                description = "",
                category = "",
                department = "",
                location = "Campus Premises",
                confidence = 50
            )
        }

        val raw = speech.trim()
        val lower = raw.lowercase()

        // 1. Category & Department detection
        var category = ""
        var department = ""
        var bestCatScore = 0

        for (rule in CATEGORY_RULES) {
            var score = 0
            for (kw in rule.keywords) {
                if (lower.contains(kw)) {
                    score += kw.split(" ").size
                }
            }
            if (score > bestCatScore) {
                bestCatScore = score
                category = rule.cat
                department = rule.dept
            }
        }

        // 2. Location Extraction
        val blockRegex = Regex("\\b(?:(?:main|mech|mechanical|cse|ece|eee|civil|it|pg|mba|[a-zA-Z0-9])\\s+block|block\\s+[a-zA-Z0-9]{1,3})\\b", RegexOption.IGNORE_CASE)
        val roomRegex = Regex("\\b(?:room\\s+[a-zA-Z0-9]+|rm\\s+[a-zA-Z0-9]+)\\b", RegexOption.IGNORE_CASE)
        val floorRegex = Regex("\\b(?:ground|first|second|third|fourth|fifth|[0-9]+(?:st|nd|rd|th)?)\\s+floor\\b", RegexOption.IGNORE_CASE)
        val placeRegex = Regex("\\b(?:seminar\\s+hall\\s*[a-zA-Z0-9]*|auditorium|central\\s+library|library|canteen|cafeteria|mess|washroom|restroom|lab\\s*[a-zA-Z0-9]*|laboratory\\s*[a-zA-Z0-9]*|(?:boys|girls)?\\s*hostel(?:\\s+gate)?)\\b", RegexOption.IGNORE_CASE)

        val locParts = mutableListOf<String>()
        val blockMatch = blockRegex.find(raw)?.value
        val roomMatch = roomRegex.find(raw)?.value
        val floorMatch = floorRegex.find(raw)?.value
        val placeMatch = placeRegex.find(raw)?.value

        if (blockMatch != null) locParts.add(capitalizeWords(blockMatch))
        if (floorMatch != null) locParts.add(capitalizeWords(floorMatch))
        if (roomMatch != null) locParts.add(capitalizeWords(roomMatch))
        if (placeMatch != null && locParts.none { it.contains(placeMatch, ignoreCase = true) }) {
            locParts.add(capitalizeWords(placeMatch))
        }

        val location = if (locParts.isNotEmpty()) {
            locParts.joinToString(", ")
        } else {
            val prepRegex = Regex("\\b(?:in|at|near|outside|inside|on)\\s+([a-zA-Z0-9\\s,-]+?)(?:\\s+(?:is|was|are|has|have|need|needs|please|kindly|$))", RegexOption.IGNORE_CASE)
            val prepMatch = prepRegex.find(raw)?.groupValues?.getOrNull(1)?.trim()
            if (!prepMatch.isNullOrBlank() && prepMatch.length > 2) {
                capitalizeWords(prepMatch)
            } else {
                "Campus Premises"
            }
        }

        // 3. Title Generation
        val cleanSpeech = raw
            .replace(Regex("^(?:hello|hi|hey|good\\s+morning|good\\s+afternoon|good\\s+evening|campusfix|please|kindly|i\\s+want\\s+to\\s+report\\s+that|i\\s+would\\s+like\\s+to\\s+report|there\\s+is\\s+a\\s+problem\\s+with|there\\s+is\\s+an\\s+issue\\s+with|issue\\s+regarding|report\\s+regarding)\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*(?:please\\s+repair\\s+it\\s+urgently|please\\s+fix\\s+it\\s+urgently|please\\s+help|urgently|as\\s+soon\\s+as\\s+possible)\\s*$", RegexOption.IGNORE_CASE), "")

        val title = when {
            lower.contains("fan") -> when {
                lower.contains("vibrating") || lower.contains("noise") -> "Ceiling Fan Vibrating / Noisy"
                lower.contains("not spinning") || lower.contains("not working") || lower.contains("broken") -> "Ceiling Fan Not Working"
                else -> "Ceiling Fan Issue"
            }
            lower.contains("leak") || lower.contains("tap") || lower.contains("pipe") -> when {
                lower.contains("tap") -> "Water Tap Leaking"
                lower.contains("pipe") -> "Pipeline Leakage"
                else -> "Water Leakage Issue"
            }
            lower.contains("wifi") || lower.contains("internet") -> when {
                lower.contains("slow") -> "Slow WiFi Connection"
                lower.contains("disconnect") -> "WiFi Disconnecting Frequently"
                else -> "WiFi / Internet Problem"
            }
            lower.contains("projector") || lower.contains("screen") -> "Projector Display Malfunction"
            lower.contains("bench") || lower.contains("chair") || lower.contains("desk") -> "Damaged Classroom Furniture"
            lower.contains("light") || lower.contains("bulb") -> "Lighting / Tubelight Failure"
            lower.contains("dustbin") || lower.contains("dirty") || lower.contains("garbage") -> "Cleanliness and Garbage Clearance Required"
            else -> {
                val words = cleanSpeech.split(Regex("\\s+")).take(6)
                capitalizeWords(words.joinToString(" "))
            }
        }

        // 4. Polished description
        val description = raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        return ParsedComplaintVoice(
            rawSpeech = raw,
            title = title,
            description = description,
            category = category,
            department = department,
            location = location,
            confidence = if (bestCatScore > 0) 95 else 75
        )
    }

    fun detectCategory(text: String?): String {
        return parseVoiceComplaint(text).category
    }

    fun detectDepartment(text: String?): String {
        return parseVoiceComplaint(text).department
    }

    private fun capitalizeWords(str: String): String {
        return str.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}

