package com.racktrack.data

import com.racktrack.domain.InningStat
import com.racktrack.domain.MatchSummary
import com.racktrack.domain.RackStat
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import org.json.JSONArray
import org.json.JSONObject

/** Manual JSON codec so history stays dependency-light (no Room / kotlinx.serialization). */
object MatchSummaryJson {
    fun encodeStoredList(matches: List<StoredMatch>): String {
        val root = JSONObject()
        val array = JSONArray()
        matches.forEach { array.put(encodeStored(it)) }
        root.put("matches", array)
        return root.toString()
    }

    fun decodeStoredList(raw: String): List<StoredMatch> {
        if (raw.isBlank()) return emptyList()
        val root = JSONObject(raw)
        val array = root.optJSONArray("matches") ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                add(decodeStored(obj))
            }
        }
    }

    fun encodeStored(match: StoredMatch): JSONObject =
        JSONObject()
            .put("id", match.id)
            .put("completedAtMillis", match.completedAtMillis)
            .put("summary", encodeSummary(match.summary))

    fun decodeStored(obj: JSONObject): StoredMatch =
        StoredMatch(
            id = obj.getString("id"),
            completedAtMillis = obj.getLong("completedAtMillis"),
            summary = decodeSummary(obj.getJSONObject("summary")),
        )

    fun encodeSummary(summary: MatchSummary): JSONObject =
        JSONObject()
            .put("gameMode", summary.gameMode.name)
            .put("winnerName", summary.winnerName)
            .put("player1Name", summary.player1Name)
            .put("player2Name", summary.player2Name)
            .put("score1", summary.score1)
            .put("score2", summary.score2)
            .put("racksToWin", summary.racksToWin)
            .put("pointsToWin", summary.pointsToWin)
            .put("inningsLimit", summary.inningsLimit ?: JSONObject.NULL)
            .put("innings1", summary.innings1)
            .put("innings2", summary.innings2)
            .put("totalFouls1", summary.totalFouls1)
            .put("totalFouls2", summary.totalFouls2)
            .put("runOuts1", summary.runOuts1)
            .put("runOuts2", summary.runOuts2)
            .put("goldenBreaks1", summary.goldenBreaks1)
            .put("goldenBreaks2", summary.goldenBreaks2)
            .put("dryBreaks1", summary.dryBreaks1)
            .put("dryBreaks2", summary.dryBreaks2)
            .put("pushOuts1", summary.pushOuts1)
            .put("pushOuts2", summary.pushOuts2)
            .put("eightBallLosses1", summary.eightBallLosses1)
            .put("eightBallLosses2", summary.eightBallLosses2)
            .put("highRun1", summary.highRun1)
            .put("highRun2", summary.highRun2)
            .put("average1", summary.average1)
            .put("average2", summary.average2)
            .put("totalDurationMillis", summary.totalDurationMillis)
            .put("startedAtMillis", summary.startedAtMillis)
            .put("endedAtMillis", summary.endedAtMillis)
            .put("solo", summary.solo)
            .put("racks", encodeRacks(summary.racks))
            .put("inningScores1", encodeInnings(summary.inningScores1))
            .put("inningScores2", encodeInnings(summary.inningScores2))

    fun decodeSummary(obj: JSONObject): MatchSummary {
        val totalDuration = obj.getLong("totalDurationMillis")
        val endedAt = obj.optLong("endedAtMillis", 0L).takeIf { it > 0L }
            ?: obj.optLong("completedAtMillis", 0L)
        val startedAt = obj.optLong("startedAtMillis", 0L).takeIf { it > 0L }
            ?: if (endedAt > 0L) (endedAt - totalDuration).coerceAtLeast(0L) else 0L
        val resolvedEnd = if (endedAt > 0L) endedAt else startedAt + totalDuration
        return MatchSummary(
            gameMode = GameMode.valueOf(obj.getString("gameMode")),
            winnerName = obj.getString("winnerName"),
            player1Name = obj.getString("player1Name"),
            player2Name = obj.getString("player2Name"),
            score1 = obj.getInt("score1"),
            score2 = obj.getInt("score2"),
            racksToWin = obj.getInt("racksToWin"),
            pointsToWin = obj.getInt("pointsToWin"),
            inningsLimit = if (obj.isNull("inningsLimit")) null else obj.getInt("inningsLimit"),
            innings1 = obj.getInt("innings1"),
            innings2 = obj.getInt("innings2"),
            totalFouls1 = obj.getInt("totalFouls1"),
            totalFouls2 = obj.getInt("totalFouls2"),
            runOuts1 = obj.getInt("runOuts1"),
            runOuts2 = obj.getInt("runOuts2"),
            goldenBreaks1 = obj.getInt("goldenBreaks1"),
            goldenBreaks2 = obj.getInt("goldenBreaks2"),
            dryBreaks1 = obj.getInt("dryBreaks1"),
            dryBreaks2 = obj.getInt("dryBreaks2"),
            pushOuts1 = obj.optInt("pushOuts1", 0),
            pushOuts2 = obj.optInt("pushOuts2", 0),
            eightBallLosses1 = obj.getInt("eightBallLosses1"),
            eightBallLosses2 = obj.getInt("eightBallLosses2"),
            highRun1 = obj.getInt("highRun1"),
            highRun2 = obj.getInt("highRun2"),
            average1 = obj.getDouble("average1"),
            average2 = obj.getDouble("average2"),
            inningScores1 = decodeInnings(obj.getJSONArray("inningScores1")),
            inningScores2 = decodeInnings(obj.getJSONArray("inningScores2")),
            racks = decodeRacks(obj.getJSONArray("racks")),
            totalDurationMillis = totalDuration,
            startedAtMillis = startedAt,
            endedAtMillis = resolvedEnd.coerceAtLeast(startedAt),
            solo = obj.optBoolean("solo", false),
        )
    }

    private fun encodeRacks(racks: List<RackStat>): JSONArray {
        val array = JSONArray()
        racks.forEach { rack ->
            array.put(
                JSONObject()
                    .put("index", rack.index)
                    .put("winnerName", rack.winnerName)
                    .put("durationMillis", rack.durationMillis)
                    .put("endType", rack.endType.name),
            )
        }
        return array
    }

    private fun decodeRacks(array: JSONArray): List<RackStat> =
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    RackStat(
                        index = obj.getInt("index"),
                        winnerName = obj.getString("winnerName"),
                        durationMillis = obj.getLong("durationMillis"),
                        endType = MatchEventType.valueOf(obj.getString("endType")),
                    ),
                )
            }
        }

    private fun encodeInnings(innings: List<InningStat>): JSONArray {
        val array = JSONArray()
        innings.forEach { inning ->
            array.put(
                JSONObject()
                    .put("index", inning.index)
                    .put("points", inning.points)
                    .put(
                        "endType",
                        inning.endType?.name ?: JSONObject.NULL,
                    ),
            )
        }
        return array
    }

    private fun decodeInnings(array: JSONArray): List<InningStat> =
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    InningStat(
                        index = obj.getInt("index"),
                        points = obj.getInt("points"),
                        endType = if (obj.isNull("endType")) {
                            null
                        } else {
                            MatchEventType.valueOf(obj.getString("endType"))
                        },
                    ),
                )
            }
        }
}
