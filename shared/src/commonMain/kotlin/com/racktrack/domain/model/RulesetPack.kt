package com.racktrack.domain.model

/**
 * Scoreboard ruleset pack (multi-ruleset 2.0).
 * Base play follows WPA-shaped American pool; packs flip a small set of flags.
 * Authority PDFs: `resources/README.md`.
 */
enum class RulesetPack {
    /** Fédération Française de Billard — current app baseline. */
    FFB,

    /** World Pool-Billiard Association Rules of Play. */
    WPA,

    /** APA league Game Rules (8 & 9). */
    APA,

    /** CueSports International / BCA Pool League. */
    BCA_CSI,

    /** Matchroom WNT — 9-ball tour overlay only; other modes fall back to [FFB]. */
    MATCHROOM,
    ;

    /** Short chip / picker label (not translated). */
    val shortLabel: String
        get() =
            when (this) {
                FFB -> "FFB"
                WPA -> "WPA"
                APA -> "APA"
                BCA_CSI -> "BCA/CSI"
                MATCHROOM -> "Matchroom (9)"
            }

    /**
     * Pack stamped on a live match.
     * Matchroom is a 9-ball overlay; any other mode uses [FFB] (app default).
     */
    fun forMode(mode: GameMode): RulesetPack {
        return if (this == MATCHROOM && mode != GameMode.NINE_BALL) FFB else this
    }

    /**
     * 9/10 push-out after a non-foul break.
     * APA handicapped league: off. Masters-style APA is out of pack scope.
     */
    fun allowsPushOut(mode: GameMode): Boolean = mode.supportsPushOut && this != APA

    /**
     * 9/10 three consecutive fouls → lose rack.
     * APA league: off.
     */
    fun allowsThreeFoulRackLoss(mode: GameMode): Boolean = mode.supportsThreeFoulRackLoss && this != APA

    /**
     * APA 8-ball: pocketing the 8 on the break wins the rack (scratch → use EARLY 8 loss).
     * FFB/WPA/CSI: spot / re-rack (operator; no dedicated win tap).
     */
    fun eightOnBreakAwardsRack(mode: GameMode): Boolean = mode == GameMode.EIGHT_BALL && this == APA

    /**
     * 14/1: illegal open + classic foul on the same open stacks to −3 (FFB 1.6.03(g)).
     * BCA/CSI: open violation only (−2); no extra −1 stack.
     */
    fun openingIllegalPlusClassicStacks(mode: GameMode): Boolean = mode.isPointScoring && this != BCA_CSI

    fun officialRulesUrl(): String =
        when (this) {
            FFB ->
                "https://m.ffbillard.com/ext/telechargement.php?id=32249"
            WPA ->
                "https://wpapool.com/wp-content/uploads/2026/01/2026.01.02-WPA-Rules.pdf"
            APA ->
                "https://rules.poolplayers.com/"
            BCA_CSI ->
                "https://www.playcsipool.com/bcapl-rules.html"
            MATCHROOM ->
                "https://matchroompool.com/wp-content/uploads/World-Nineball-Tour-Event-Rules-1.pdf"
        }
}
