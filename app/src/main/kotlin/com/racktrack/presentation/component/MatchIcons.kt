package com.racktrack.presentation.component

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.domain.model.GameMode
import com.racktrack.presentation.theme.ButtonFoul
import com.racktrack.presentation.theme.ButtonFoulLight
import com.racktrack.presentation.theme.ButtonRunOutLight
import com.racktrack.presentation.theme.CueBallDeep
import com.racktrack.presentation.theme.CueBallHighlight
import com.racktrack.presentation.theme.CueBallMid
import com.racktrack.presentation.theme.CueBallShadow
import com.racktrack.presentation.theme.CueTipDark
import com.racktrack.presentation.theme.CueTipLight
import com.racktrack.presentation.theme.CueTipMid
import com.racktrack.presentation.theme.ScoreWhite

/**
 * Polished training-style cue ball (white sphere + red tip) for the break side.
 */
@Composable
fun CueBallBreakIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / HALF
        val center = Offset(this.size.width / HALF, this.size.height / HALF * BALL_CENTER_Y_FACTOR)
        val ballRadius = radius * BALL_RADIUS_FACTOR

        // Soft ambient pool-table shadow (icon-style), then tighter contact umbra.
        drawBallContactShadow(
            center = center,
            ballRadius = ballRadius,
            widthFactor = SHADOW_SOFT_WIDTH,
            heightFactor = SHADOW_SOFT_HEIGHT,
            topFactor = SHADOW_SOFT_TOP,
            coreAlpha = SHADOW_SOFT_ALPHA,
            radiusFactor = SHADOW_SOFT_RADIUS,
        )
        drawBallContactShadow(
            center = center,
            ballRadius = ballRadius,
            widthFactor = SHADOW_WIDTH_FACTOR,
            heightFactor = SHADOW_HEIGHT_FACTOR,
            topFactor = SHADOW_TOP_FACTOR,
            coreAlpha = SHADOW_CORE_ALPHA,
            radiusFactor = SHADOW_RADIUS_FACTOR,
        )

        val lightCenter = Offset(
            center.x - ballRadius * HIGHLIGHT_OFFSET_X,
            center.y - ballRadius * HIGHLIGHT_OFFSET_Y,
        )
        val sphereBrush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to CueBallHighlight,
                SPHERE_MID_STOP to CueBallMid,
                SPHERE_SHADOW_STOP to CueBallShadow,
                1f to CueBallDeep,
            ),
            center = lightCenter,
            radius = ballRadius * GRADIENT_RADIUS_FACTOR,
        )
        drawCircle(brush = sphereBrush, radius = ballRadius, center = center)

        // Red training mark painted on the sphere — same lighting as the ball, no local gloss/shadow.
        val tipCenter = Offset(center.x, center.y - ballRadius * TIP_OFFSET_Y)
        val tipRadius = ballRadius * TIP_RADIUS_FACTOR
        val ballClip = Path().apply {
            addOval(Rect(center = center, radius = ballRadius))
        }
        clipPath(ballClip) {
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to CueTipLight,
                        SPHERE_MID_STOP to CueTipMid,
                        SPHERE_SHADOW_STOP to CueTipDark,
                        1f to CueTipDark.copy(alpha = 0.92f),
                    ),
                    center = lightCenter,
                    radius = ballRadius * GRADIENT_RADIUS_FACTOR,
                ),
                radius = tipRadius,
                center = tipCenter,
            )
        }

        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.White.copy(alpha = RIM_LIGHT_ALPHA),
                    Color.Black.copy(alpha = RIM_DARK_ALPHA),
                    Color.White.copy(alpha = RIM_SOFT_ALPHA),
                    Color.Black.copy(alpha = RIM_EDGE_ALPHA),
                    Color.White.copy(alpha = RIM_LIGHT_ALPHA),
                ),
                center = center,
            ),
            radius = ballRadius,
            center = center,
            style = Stroke(width = ballRadius * BALL_STROKE_FACTOR),
        )

        // Specular over the whole ball (including the painted tip).
        val specularCenter = Offset(
            center.x - ballRadius * SPECULAR_OFFSET_X,
            center.y - ballRadius * SPECULAR_OFFSET_Y,
        )
        clipPath(ballClip) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = SPECULAR_ALPHA), Color.Transparent),
                    center = specularCenter,
                    radius = ballRadius * SPECULAR_RADIUS_FACTOR,
                ),
                radius = ballRadius * SPECULAR_RADIUS_FACTOR,
                center = specularCenter,
            )
        }
    }
}

/** Compact run-out / foul counters under the score. Tap fouls (when > 0) to clear after a legal shot. */
@Composable
fun PlayerStatIcons(
    gameMode: GameMode,
    runOuts: Int,
    consecutiveFouls: Int,
    maxConsecutiveFouls: Int,
    modifier: Modifier = Modifier,
    onClearFouls: (() -> Unit)? = null,
    iconSize: Dp = StatIconSize,
    iconGap: Dp = 22.dp,
    clearHintSp: TextUnit = 11.sp,
) {
    val performHaptic = rememberClickHaptic()
    val foulClearable = consecutiveFouls > 0 && onClearFouls != null
    val safeIconSize = iconSize.coerceAtLeast(24.dp)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatIconChip(
            countLabel = runOuts.toString(),
            emphasized = runOuts > 0,
            accent = ButtonRunOutLight,
        ) {
            RunOutIcon(
                gameMode = gameMode,
                muted = runOuts == 0,
                size = safeIconSize,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StatIconChip(
                countLabel = if (maxConsecutiveFouls > 0) {
                    "$consecutiveFouls/$maxConsecutiveFouls"
                } else {
                    consecutiveFouls.toString()
                },
                emphasized = consecutiveFouls > 0,
                accent = ButtonFoulLight,
                clearable = foulClearable,
                onClick = if (foulClearable) {
                    {
                        performHaptic()
                        onClearFouls()
                    }
                } else {
                    null
                },
            ) {
                FoulIcon(
                    tint = when {
                        consecutiveFouls >= FOUL_WARN_THRESHOLD -> ButtonFoulLight
                        consecutiveFouls > 0 -> ButtonFoul
                        else -> ScoreWhite.copy(alpha = MUTED_ICON_ALPHA)
                    },
                    size = safeIconSize,
                )
            }
            if (foulClearable) {
                Text(
                    text = "TAP TO CLEAR",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = clearHintSp),
                    color = ButtonFoulLight.copy(alpha = 0.95f),
                )
            }
        }
    }
}

@Composable
private fun StatIconChip(
    countLabel: String,
    emphasized: Boolean,
    accent: Color,
    clearable: Boolean = false,
    onClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(FOUL_CHIP_CORNER)
    val chipModifier = when {
        onClick != null -> {
            Modifier
                .semantics { contentDescription = "Clear consecutive fouls" }
                .clip(shape)
                .then(
                    if (clearable) {
                        Modifier
                            .border(2.dp, accent.copy(alpha = 0.9f), shape)
                            .background(accent.copy(alpha = 0.18f), shape)
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(bounded = true, color = accent),
                    onClick = onClick,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        }
        else -> Modifier
    }
    Row(
        modifier = chipModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
        Text(
            text = countLabel,
            style = MaterialTheme.typography.titleLarge,
            color = if (emphasized) accent else ScoreWhite.copy(alpha = MUTED_LABEL_ALPHA),
        )
    }
}

/**
 * Option C run-out mark: mode ball (8 / 9-stripe / 10-stripe) + green check badge.
 */
@Composable
fun RunOutIcon(
    gameMode: GameMode,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    size: Dp = StatIconSize,
) {
    val alpha = if (muted) MUTED_ICON_ALPHA else 1f
    Canvas(modifier = modifier.size(size)) {
        val ballCenter = Offset(this.size.width * RUNOUT_BALL_CX, this.size.height * RUNOUT_BALL_CY)
        val ballRadius = this.size.minDimension * RUNOUT_BALL_RADIUS_FACTOR
        drawModeBall(gameMode = gameMode, center = ballCenter, radius = ballRadius, alpha = alpha)
        drawCheckBadge(
            center = Offset(
                ballCenter.x + ballRadius * RUNOUT_BADGE_OFFSET,
                ballCenter.y + ballRadius * RUNOUT_BADGE_OFFSET,
            ),
            radius = ballRadius * RUNOUT_BADGE_RADIUS_FACTOR,
            alpha = alpha,
        )
    }
}

private fun DrawScope.drawModeBall(
    gameMode: GameMode,
    center: Offset,
    radius: Float,
    alpha: Float,
) {
    when (gameMode) {
        GameMode.EIGHT_BALL -> {
            drawCircle(color = BallEightBlack.copy(alpha = alpha), radius = radius, center = center)
            drawCircle(
                color = ScoreWhite.copy(alpha = alpha),
                radius = radius * NUMBER_DISK_FACTOR,
                center = center,
            )
            drawBallNumber(
                text = "8",
                center = center,
                textSizePx = radius * NUMBER_TEXT_FACTOR,
                color = BallEightBlack.copy(alpha = alpha),
            )
        }
        GameMode.NINE_BALL -> drawStripedBall(
            stripe = BallNineYellow.copy(alpha = alpha),
            number = "9",
            center = center,
            radius = radius,
            alpha = alpha,
        )
        GameMode.TEN_BALL -> drawStripedBall(
            stripe = BallTenBlue.copy(alpha = alpha),
            number = "10",
            center = center,
            radius = radius,
            alpha = alpha,
        )
        GameMode.FOURTEEN_ONE ->
            drawCircle(color = ScoreWhite.copy(alpha = alpha), radius = radius, center = center)
    }
}

private fun DrawScope.drawStripedBall(
    stripe: Color,
    number: String,
    center: Offset,
    radius: Float,
    alpha: Float,
) {
    drawCircle(color = ScoreWhite.copy(alpha = alpha), radius = radius, center = center)
    val clip = Path().apply {
        addOval(Rect(center = center, radius = radius))
    }
    clipPath(clip) {
        drawRect(
            color = stripe,
            topLeft = Offset(center.x - radius, center.y - radius * STRIPE_HALF_HEIGHT),
            size = Size(radius * 2f, radius * STRIPE_HEIGHT),
        )
    }
    drawCircle(
        color = ScoreWhite.copy(alpha = alpha),
        radius = radius * NUMBER_DISK_FACTOR,
        center = center,
    )
    drawBallNumber(
        text = number,
        center = center,
        textSizePx = radius * if (number.length > 1) NUMBER_TEXT_FACTOR_WIDE else NUMBER_TEXT_FACTOR,
        color = Color.Black.copy(alpha = alpha),
    )
}

private fun DrawScope.drawBallNumber(
    text: String,
    center: Offset,
    textSizePx: Float,
    color: Color,
) {
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )
        textAlign = Paint.Align.CENTER
        this.textSize = textSizePx
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val y = center.y - (paint.descent() + paint.ascent()) / HALF
    drawContext.canvas.nativeCanvas.drawText(text, center.x, y, paint)
}

private fun DrawScope.drawCheckBadge(center: Offset, radius: Float, alpha: Float) {
    drawCircle(color = ScoreWhite.copy(alpha = alpha), radius = radius * BADGE_RIM_FACTOR, center = center)
    drawCircle(color = CheckBadgeGreen.copy(alpha = alpha), radius = radius, center = center)
    val check = Path().apply {
        moveTo(center.x - radius * CHECK_X1, center.y + radius * CHECK_Y1)
        lineTo(center.x - radius * CHECK_X2, center.y + radius * CHECK_Y2)
        lineTo(center.x + radius * CHECK_X3, center.y - radius * CHECK_Y3)
    }
    drawPath(
        path = check,
        color = ScoreWhite.copy(alpha = alpha),
        style = Stroke(
            width = radius * CHECK_STROKE,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

/**
 * Foul mark: cue ball with a round “interdit” slash — matches the ball language of run-out icons.
 */
@Composable
fun FoulIcon(
    modifier: Modifier = Modifier,
    tint: Color = ButtonFoulLight,
    size: Dp = StatIconSize,
) {
    Canvas(modifier = modifier.size(size)) {
        val min = this.size.minDimension
        val center = Offset(this.size.width / HALF, this.size.height / HALF)
        val ballRadius = min * FOUL_BALL_RADIUS
        val ringRadius = min * FOUL_RING_RADIUS
        val stroke = min * FOUL_SLASH_STROKE

        // Soft white cue ball (same family as run-out mode balls).
        drawCircle(color = ScoreWhite.copy(alpha = FOUL_BALL_FILL_ALPHA), radius = ballRadius, center = center)
        drawCircle(
            color = tint.copy(alpha = FOUL_BALL_RIM_ALPHA),
            radius = ballRadius,
            center = center,
            style = Stroke(width = min * FOUL_BALL_RIM_STROKE, cap = StrokeCap.Round),
        )

        // Prohibition ring + diagonal slash.
        drawCircle(
            color = tint,
            radius = ringRadius,
            center = center,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        val slash = ballRadius * FOUL_SLASH_REACH
        drawLine(
            color = tint,
            start = Offset(center.x - slash, center.y + slash),
            end = Offset(center.x + slash, center.y - slash),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawBallContactShadow(
    center: Offset,
    ballRadius: Float,
    widthFactor: Float,
    heightFactor: Float,
    topFactor: Float,
    coreAlpha: Float,
    radiusFactor: Float,
) {
    val shadowWidth = ballRadius * widthFactor
    val shadowHeight = ballRadius * heightFactor
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = coreAlpha), Color.Transparent),
            center = Offset(center.x, center.y + ballRadius * SHADOW_OFFSET_Y),
            radius = ballRadius * radiusFactor,
        ),
        topLeft = Offset(
            center.x - shadowWidth / HALF,
            center.y + ballRadius * topFactor,
        ),
        size = Size(shadowWidth, shadowHeight),
    )
}

private val StatIconSize = 40.dp

private const val HALF = 2f
private const val BALL_CENTER_Y_FACTOR = 0.90f
private const val HIGHLIGHT_OFFSET_X = 0.28f
private const val HIGHLIGHT_OFFSET_Y = 0.32f
private const val GRADIENT_RADIUS_FACTOR = 1.45f
private const val BALL_RADIUS_FACTOR = 0.84f
private const val BALL_STROKE_FACTOR = 0.045f
private const val SHADOW_OFFSET_Y = 0.98f
private const val SHADOW_SOFT_ALPHA = 0.28f
private const val SHADOW_SOFT_WIDTH = 2.35f
private const val SHADOW_SOFT_HEIGHT = 0.72f
private const val SHADOW_SOFT_TOP = 0.48f
private const val SHADOW_SOFT_RADIUS = 1.55f
private const val SHADOW_CORE_ALPHA = 0.52f
private const val SHADOW_RADIUS_FACTOR = 1.2f
private const val SHADOW_WIDTH_FACTOR = 1.85f
private const val SHADOW_TOP_FACTOR = 0.62f
private const val SHADOW_HEIGHT_FACTOR = 0.48f
private const val SPHERE_MID_STOP = 0.35f
private const val SPHERE_SHADOW_STOP = 0.78f
private const val RIM_LIGHT_ALPHA = 0.35f
private const val RIM_DARK_ALPHA = 0.22f
private const val RIM_SOFT_ALPHA = 0.12f
private const val RIM_EDGE_ALPHA = 0.28f
private const val SPECULAR_ALPHA = 0.55f
private const val SPECULAR_OFFSET_X = 0.22f
private const val SPECULAR_OFFSET_Y = 0.3f
private const val SPECULAR_RADIUS_FACTOR = 0.28f
private const val TIP_OFFSET_Y = 0.34f
private const val TIP_RADIUS_FACTOR = 0.24f
private const val MUTED_ICON_ALPHA = 0.45f
private const val MUTED_LABEL_ALPHA = 0.55f
private const val FOUL_WARN_THRESHOLD = 2
private val FOUL_CHIP_CORNER = 12.dp
private val BallEightBlack = Color(0xFF1C1C1C)
private val BallNineYellow = Color(0xFFF0C12E)
private val BallTenBlue = Color(0xFF2F6FB5)
private val CheckBadgeGreen = Color(0xFF2FBF66)
// Sized to match FoulIcon’s barred footprint (ring ≈ 0.42 of canvas).
private const val RUNOUT_BALL_CX = 0.44f
private const val RUNOUT_BALL_CY = 0.44f
private const val RUNOUT_BALL_RADIUS_FACTOR = 0.42f
private const val RUNOUT_BADGE_OFFSET = 0.56f
private const val RUNOUT_BADGE_RADIUS_FACTOR = 0.40f
private const val NUMBER_DISK_FACTOR = 0.42f
private const val NUMBER_TEXT_FACTOR = 0.55f
private const val NUMBER_TEXT_FACTOR_WIDE = 0.42f
private const val STRIPE_HALF_HEIGHT = 0.4f
private const val STRIPE_HEIGHT = 0.8f
private const val BADGE_RIM_FACTOR = 1.12f
private const val CHECK_X1 = 0.42f
private const val CHECK_Y1 = 0.02f
private const val CHECK_X2 = 0.14f
private const val CHECK_Y2 = 0.28f
private const val CHECK_X3 = 0.46f
private const val CHECK_Y3 = 0.36f
private const val CHECK_STROKE = 0.28f
private const val FOUL_BALL_RADIUS = 0.30f
private const val FOUL_RING_RADIUS = 0.42f
private const val FOUL_BALL_FILL_ALPHA = 0.95f
private const val FOUL_BALL_RIM_ALPHA = 0.35f
private const val FOUL_BALL_RIM_STROKE = 0.04f
private const val FOUL_SLASH_STROKE = 0.11f
private const val FOUL_SLASH_REACH = 0.78f
