package com.example.campusfix

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class DuplicateCheckResult(
    val isDuplicate: Boolean = false,
    val confidence: Int = 0, // e.g. 88%
    val matchedComplaint: ComplaintData? = null,
    val matchReason: String = "",
    val categoryScore: Double = 0.0,
    val locationScore: Double = 0.0,
    val descriptionScore: Double = 0.0
)

object AIDuplicateEngine {

    private val STOP_WORDS = setOf(
        "a", "about", "above", "after", "again", "all", "am", "an", "and", "any", "are", "as", "at",
        "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
        "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
        "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him",
        "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "me", "more",
        "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
        "our", "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some",
        "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then", "there",
        "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "very",
        "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why",
        "with", "you", "your", "yours", "yourself", "yourselves", "please", "kindly", "campus",
        "fix", "issue", "problem", "help", "urgently", "urgent", "sir", "madam", "complaint"
    )

    private val CANONICAL_MAP = mapOf(
        "fan" to "fan", "fans" to "fan", "ceilingfan" to "fan",
        "ac" to "aircon", "aircon" to "aircon", "airconditioner" to "aircon", "cooling" to "aircon", "chiller" to "aircon",
        "light" to "light", "lights" to "light", "tubelight" to "light", "bulb" to "light", "lamp" to "light", "led" to "light",
        "switch" to "switch", "socket" to "switch", "plug" to "switch", "wiring" to "switch", "wire" to "switch", "spark" to "switch",
        "leak" to "leak", "leaking" to "leak", "leakage" to "leak", "dripping" to "leak", "overflow" to "leak", "overflowing" to "leak",
        "tap" to "tap", "faucet" to "tap", "pipe" to "tap", "pipeline" to "tap", "flush" to "tap", "drain" to "tap", "sink" to "tap",
        "water" to "water", "drinkingwater" to "water", "purifier" to "water",
        "broken" to "broken", "break" to "broken", "broke" to "broken", "damage" to "broken", "damaged" to "broken", "repair" to "broken",
        "notworking" to "broken", "faulty" to "broken", "spoiled" to "broken", "defective" to "broken", "cracked" to "broken",
        "chair" to "furniture", "chairs" to "furniture", "bench" to "furniture", "benches" to "furniture", "desk" to "furniture", "table" to "furniture",
        "projector" to "projector", "screen" to "projector", "display" to "projector", "monitor" to "projector", "hdmi" to "projector",
        "wifi" to "network", "internet" to "network", "lan" to "network", "network" to "network", "router" to "network", "connection" to "network",
        "door" to "door", "handle" to "door", "lock" to "door", "latch" to "door", "key" to "door",
        "window" to "window", "glass" to "window",
        "dirty" to "cleanliness", "garbage" to "cleanliness", "trash" to "cleanliness", "waste" to "cleanliness", "dustbin" to "cleanliness", "smell" to "cleanliness",
        "lift" to "lift", "elevator" to "lift"
    )

    private val CATEGORY_GROUPS = mapOf(
        "electrical" to listOf("electrical", "power", "electronics", "ac / air conditioning", "air conditioning"),
        "plumbing" to listOf("plumbing", "water supply", "washroom", "restroom", "drainage"),
        "civil" to listOf("civil", "infrastructure", "civil / infrastructure", "carpentry", "masonry", "painting"),
        "it" to listOf("it", "network", "it / network", "wifi", "internet", "computer lab", "audio / visual"),
        "furniture" to listOf("furniture", "classroom maintenance"),
        "cleanliness" to listOf("cleanliness", "housekeeping", "sanitation", "waste management"),
        "hostel" to listOf("hostel", "hostel maintenance", "mess", "canteen"),
        "security" to listOf("security", "safety", "safety & security")
    )

    fun normalizeText(text: String): List<String> {
        val words = text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 1 && it !in STOP_WORDS }
        return words.map { CANONICAL_MAP[it] ?: it }
    }

    fun calculateCategorySimilarity(cat1: String, cat2: String): Double {
        val c1 = cat1.trim().lowercase()
        val c2 = cat2.trim().lowercase()
        if (c1.isEmpty() || c2.isEmpty()) return 0.0
        if (c1 == c2) return 1.0

        for ((_, group) in CATEGORY_GROUPS) {
            val has1 = group.any { c1.contains(it) || it.contains(c1) }
            val has2 = group.any { c2.contains(it) || it.contains(c2) }
            if (has1 && has2) return 0.85
        }

        if (c1.contains("general") || c2.contains("general")) return 0.50
        return 0.0
    }

    data class LocationNorm(val tokens: List<String>, val numbers: List<String>, val rawNorm: String)

    fun normalizeLocation(loc: String): LocationNorm {
        var s = loc.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")
        s = s.replace(Regex("\\bblk\\b"), "block")
            .replace(Regex("\\brm\\b"), "room")
            .replace(Regex("\\bflr\\b"), "floor")
            .replace(Regex("\\bbldg\\b"), "building")
            .replace(Regex("\\bdept\\b"), "department")
            .replace(Regex("\\blab\\b"), "laboratory")
            .replace(Regex("\\baudi\\b"), "auditorium")
        val tokens = s.split(Regex("\\s+")).filter { it.isNotEmpty() && it !in STOP_WORDS }
        val numbers = Regex("\\b\\d+\\b").findAll(s).map { it.value }.toList()
        return LocationNorm(tokens, numbers, tokens.joinToString(" "))
    }

    fun calculateLocationSimilarity(loc1: String, loc2: String): Double {
        val n1 = normalizeLocation(loc1)
        val n2 = normalizeLocation(loc2)

        if (n1.tokens.isEmpty() || n2.tokens.isEmpty()) return 0.0
        if (n1.rawNorm == n2.rawNorm) return 1.0

        // Conflict penalty if different room numbers
        if (n1.numbers.isNotEmpty() && n2.numbers.isNotEmpty()) {
            val set1 = n1.numbers.toSet()
            val hasCommonNumber = n2.numbers.any { it in set1 }
            if (!hasCommonNumber) {
                return 0.15
            }
        }

        // Substring containment
        if (n1.rawNorm.contains(n2.rawNorm) || n2.rawNorm.contains(n1.rawNorm)) {
            return 0.90
        }

        // Jaccard similarity
        val setA = n1.tokens.toSet()
        val setB = n2.tokens.toSet()
        val intersection = setA.intersect(setB).size
        val union = setA.union(setB).size
        val jaccard = if (union > 0) intersection.toDouble() / union else 0.0

        return min(1.0, jaccard * 1.25)
    }

    fun calculateDescriptionSimilarity(title1: String, desc1: String, title2: String, desc2: String): Double {
        val text1 = "$title1 $desc1"
        val text2 = "$title2 $desc2"

        val tokens1 = normalizeText(text1)
        val tokens2 = normalizeText(text2)

        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0

        // TF Vector computation
        val tf1 = mutableMapOf<String, Int>()
        tokens1.forEach { tf1[it] = (tf1[it] ?: 0) + 1 }
        val tf2 = mutableMapOf<String, Int>()
        tokens2.forEach { tf2[it] = (tf2[it] ?: 0) + 1 }

        var dot = 0.0
        var mag1 = 0.0
        var mag2 = 0.0

        val allTokens = tf1.keys + tf2.keys
        for (t in allTokens) {
            val v1 = tf1[t]?.toDouble() ?: 0.0
            val v2 = tf2[t]?.toDouble() ?: 0.0
            dot += v1 * v2
            mag1 += v1 * v1
            mag2 += v2 * v2
        }

        val cosine = if (mag1 > 0 && mag2 > 0) dot / (sqrt(mag1) * sqrt(mag2)) else 0.0

        // Bigram overlap
        fun getBigrams(tokens: List<String>): Set<String> {
            val set = mutableSetOf<String>()
            for (i in 0 until tokens.size - 1) {
                set.add("${tokens[i]}_${tokens[i + 1]}")
            }
            return set
        }

        val bg1 = getBigrams(tokens1)
        val bg2 = getBigrams(tokens2)
        val bgOverlap = bg1.intersect(bg2).size
        val bgUnion = bg1.union(bg2).size
        val bigramSim = if (bgUnion > 0) bgOverlap.toDouble() / bgUnion else 0.0

        return (0.70 * cosine) + (0.30 * bigramSim)
    }

    fun evaluate(newComplaint: ComplaintData, existingComplaint: ComplaintData): DuplicateCheckResult {
        val sCat = calculateCategorySimilarity(newComplaint.category, existingComplaint.category)
        val sLoc = calculateLocationSimilarity(newComplaint.location, existingComplaint.location)
        val sDesc = calculateDescriptionSimilarity(
            newComplaint.title, newComplaint.details,
            existingComplaint.title, existingComplaint.details
        )

        var score = (0.45 * sDesc) + (0.35 * sLoc) + (0.20 * sCat)

        // Boost for high locality and category with matching topic
        if (sLoc >= 0.85 && sCat >= 0.85 && sDesc >= 0.35) {
            score = min(0.98, score + 0.15)
        }

        // Location conflict penalty: if different rooms, cannot be duplicate
        val loc1 = normalizeLocation(newComplaint.location)
        val loc2 = normalizeLocation(existingComplaint.location)
        if (loc1.numbers.isNotEmpty() && loc2.numbers.isNotEmpty()) {
            val set1 = loc1.numbers.toSet()
            if (!loc2.numbers.any { it in set1 }) {
                score = min(score, 0.40)
            }
        }

        val confidence = (score * 100).toInt()
        val isDuplicate = confidence >= 65

        val reason = if (isDuplicate) {
            val reasons = mutableListOf<String>()
            if (sCat >= 0.8) reasons.add("matching category (\"${existingComplaint.category}\")")
            if (sLoc >= 0.8) reasons.add("same location (\"${existingComplaint.location}\")")
            if (sDesc >= 0.4) reasons.add("similar description")
            "AI detected $confidence% match: ${reasons.joinToString(", ")}."
        } else ""

        return DuplicateCheckResult(
            isDuplicate = isDuplicate,
            confidence = confidence,
            matchedComplaint = existingComplaint,
            matchReason = reason,
            categoryScore = sCat,
            locationScore = sLoc,
            descriptionScore = sDesc
        )
    }

    fun findBestDuplicateMatch(newComplaint: ComplaintData, existingList: List<ComplaintData>): DuplicateCheckResult {
        if (existingList.isEmpty()) {
            return DuplicateCheckResult()
        }

        var bestResult: DuplicateCheckResult? = null

        for (existing in existingList) {
            if (existing.ticketId.isNotEmpty() && existing.ticketId == newComplaint.ticketId) continue
            val st = existing.status.lowercase()
            if (st.contains("rejected")) continue

            val res = evaluate(newComplaint, existing)
            if (bestResult == null || res.confidence > bestResult.confidence) {
                bestResult = res
            }
        }

        return if (bestResult != null && bestResult.isDuplicate) {
            bestResult
        } else {
            DuplicateCheckResult(
                isDuplicate = false,
                confidence = bestResult?.confidence ?: 0,
                matchedComplaint = null,
                matchReason = ""
            )
        }
    }
}

