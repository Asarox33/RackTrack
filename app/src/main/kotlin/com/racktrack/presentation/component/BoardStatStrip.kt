package com.racktrack.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.presentation.theme.LocalAppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Mini glyph beside a framed stat label (HUD strip). */
enum class BoardStatGlyph {
    CLOCK,
    STAR,
    WARNING,
    CHECK,
}

/**
 * Unified HUD strip under the score: one outer frame, column dividers, mini icons.
 * No ball icons — mock B/C direction.
 */
@Composable
fun BoardStatStrip(
    cells: List<BoardStatCell>,
    modifier: Modifier = Modifier,
) {
    val theme = LocalAppTheme.current
    val shape = RoundedCornerShape(14.dp)
    val rim = theme.rim.copy(alpha = if (theme.isDark) 0.72f else 0.55f)
    val fill = theme.surfaceElevated.copy(alpha = if (theme.isDark) 0.42f else 0.78f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(1.25.dp, rim, shape)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(rim.copy(alpha = rim.alpha * 0.75f)),
                )
            }
            BoardStatColumn(
                cell = cell,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

data class BoardStatCell(
    val label: String,
    val value: String,
    val glyph: BoardStatGlyph,
    val emphasized: Boolean = false,
    val accent: Color? = null,
    val onClick: (() -> Unit)? = null,
    val contentDescription: String? = null,
)

@Composable
private fun BoardStatColumn(
    cell: BoardStatCell,
    modifier: Modifier = Modifier,
    labelSp: TextUnit = 11.sp,
    valueSp: TextUnit = 16.sp,
) {
    val theme = LocalAppTheme.current
    val labelColor = theme.rim.copy(alpha = if (theme.isDark) 0.92f else 0.85f)
    val valueColor = when {
        cell.emphasized && cell.accent != null -> cell.accent
        else -> theme.textPrimary
    }
    val interaction = remember { MutableInteractionSource() }
    val columnModifier = modifier
        .then(
            if (cell.onClick != null) {
                Modifier
                    .semantics {
                        contentDescription = cell.contentDescription
                            ?: "${cell.label} ${cell.value}"
                    }
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(bounded = true),
                        role = Role.Button,
                        onClick = cell.onClick,
                    )
            } else {
                Modifier
            },
        )
        .padding(horizontal = 6.dp, vertical = 2.dp)

    Column(
        modifier = columnModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BoardStatMiniIcon(
                glyph = cell.glyph,
                tint = labelColor,
                size = 12.dp,
            )
            Text(
                text = cell.label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = labelSp),
                color = labelColor,
                maxLines = 1,
            )
        }
        Text(
            text = cell.value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = valueSp),
            color = valueColor,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BoardStatMiniIcon(
    glyph: BoardStatGlyph,
    tint: Color,
    size: Dp,
) {
    Canvas(modifier = Modifier.size(size)) {
        when (glyph) {
            BoardStatGlyph.CLOCK -> drawClock(tint)
            BoardStatGlyph.STAR -> drawStar(tint)
            BoardStatGlyph.WARNING -> drawWarning(tint)
            BoardStatGlyph.CHECK -> drawCheck(tint)
        }
    }
}

private fun DrawScope.drawClock(tint: Color) {
    val r = min(size.width, size.height) * 0.42f
    val c = Offset(size.width / 2f, size.height / 2f)
    drawCircle(color = tint, radius = r, center = c, style = Stroke(width = r * 0.18f))
    drawLine(
        color = tint,
        start = c,
        end = Offset(c.x, c.y - r * 0.55f),
        strokeWidth = r * 0.16f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = tint,
        start = c,
        end = Offset(c.x + r * 0.42f, c.y + r * 0.12f),
        strokeWidth = r * 0.16f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawStar(tint: Color) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val outer = min(size.width, size.height) * 0.46f
    val inner = outer * 0.45f
    val path = Path()
    for (i in 0 until 5) {
        val aOuter = (-PI / 2.0 + i * 2.0 * PI / 5.0).toFloat()
        val aInner = (aOuter + PI / 5.0).toFloat()
        val ox = c.x + cos(aOuter) * outer
        val oy = c.y + sin(aOuter) * outer
        val ix = c.x + cos(aInner) * inner
        val iy = c.y + sin(aInner) * inner
        if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
        path.lineTo(ix, iy)
    }
    path.close()
    drawPath(path, color = tint)
}

private fun DrawScope.drawWarning(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.92f, h * 0.88f)
        lineTo(w * 0.08f, h * 0.88f)
        close()
    }
    drawPath(
        path,
        color = tint,
        style = Stroke(width = min(w, h) * 0.12f, join = StrokeJoin.Round, cap = StrokeCap.Round),
    )
    val cx = w * 0.5f
    drawLine(
        color = tint,
        start = Offset(cx, h * 0.38f),
        end = Offset(cx, h * 0.58f),
        strokeWidth = min(w, h) * 0.12f,
        cap = StrokeCap.Round,
    )
    drawCircle(color = tint, radius = min(w, h) * 0.06f, center = Offset(cx, h * 0.72f))
}

private fun DrawScope.drawCheck(tint: Color) {
    val w = size.width
    val h = size.height
    drawCircle(
        color = tint,
        radius = min(w, h) * 0.42f,
        center = Offset(w / 2f, h / 2f),
        style = Stroke(width = min(w, h) * 0.14f),
    )
    val path = Path().apply {
        moveTo(w * 0.28f, h * 0.52f)
        lineTo(w * 0.44f, h * 0.68f)
        lineTo(w * 0.74f, h * 0.34f)
    }
    drawPath(
        path,
        color = tint,
        style = Stroke(
            width = min(w, h) * 0.14f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

/** Quiet secondary chip (On Table, Opening Break) — not competing with the score. */
@Composable
fun BoardQuietChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val theme = LocalAppTheme.current
    val shape = RoundedCornerShape(999.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
        color = if (accent) theme.accentLight else theme.textSecondary,
        maxLines = 1,
        modifier = modifier
            .clip(shape)
            .background(
                if (accent) {
                    theme.accent.copy(alpha = 0.16f)
                } else {
                    theme.surfaceDeep.copy(alpha = if (theme.isDark) 0.55f else 0.35f)
                },
            )
            .border(
                1.dp,
                if (accent) theme.accent.copy(alpha = 0.45f) else theme.outline.copy(alpha = 0.35f),
                shape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
