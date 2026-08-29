package com.glasslauncher.app.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * The core "Adaptive Glass" material: a translucent, blurred, wallpaper-tinted panel with a
 * soft edge highlight, a gentle top-left reflection and a low, wide peripheral glow.
 *
 * All visual parameters default to the user's global [LocalGlassStyle] settings, tinted by the
 * live [LocalAdaptiveGlassPalette] sampled from the wallpaper; any of them can be overridden
 * per call-site (e.g. the dock uses a larger corner radius than a folder tile).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadiusDp: Float? = null,
    tintColorOverride: Color? = null,
    blurRadiusDpOverride: Float? = null,
    transparencyOverride: Float? = null,
    showGlow: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalGlassStyle.current
    val palette = LocalAdaptiveGlassPalette.current
    val hazeState = LocalHazeState.current
    val lowPower = LocalGlassLowPower.current
    val isDark = style.resolvedIsDark(palette)

    val cornerRadius = (cornerRadiusDp ?: style.cornerRadiusDp).dp
    val shape = RoundedCornerShape(cornerRadius)
    val blurRadius = (blurRadiusDpOverride ?: style.effectiveBlurRadiusDp(lowPower)).dp
    val transparency = (transparencyOverride ?: style.transparency).coerceIn(0.05f, 0.95f)

    val basePanelColor = if (isDark) Color(0xFF12141C) else Color(0xFFF6F8FF)
    val baseTint = tintColorOverride
        ?: lerp(basePanelColor, palette.primary, (style.adaptiveGlassIntensity * 0.4f).coerceIn(0f, 1f))
    val glowColor = lerp(palette.secondary, palette.tertiary, 0.5f)
        .copy(alpha = (style.peripheralGlow * (if (lowPower) 0.5f else 1f)).coerceIn(0f, 0.6f))

    Box(modifier = modifier) {
        if (showGlow && !lowPower) {
            Box(
                Modifier
                    .matchParentSize()
                    .scale(1.12f)
                    .blur((22 * style.adaptiveGlassIntensity).dp.coerceAtLeast(0.dp))
                    .background(glowColor, shape),
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .shadow(
                    elevation = (14f * style.shadowIntensity).dp.coerceAtLeast(0.dp),
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = (0.22f * style.shadowIntensity).coerceIn(0f, 1f)),
                    spotColor = Color.Black.copy(alpha = (0.28f * style.shadowIntensity).coerceIn(0f, 1f)),
                )
                .clip(shape)
                .hazeEffect(state = hazeState) {
                    this.blurRadius = blurRadius
                    this.tints = listOf(HazeTint(baseTint.copy(alpha = transparency * style.opacity)))
                    this.fallbackTint = HazeTint(baseTint.copy(alpha = (transparency + 0.3f).coerceAtMost(0.92f)))
                    this.noiseFactor = 0.05f
                }
                .drawWithContent {
                    drawContent()
                    // Soft top-left reflection, as if light grazes the top edge of the glass.
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f * style.reflectionIntensity),
                                Color.Transparent,
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width * 0.55f, size.height * 0.7f),
                        ),
                    )
                    // Subtle inner darkening near the bottom edge for depth (fake inner shadow).
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.10f * style.shadowIntensity)),
                            startY = size.height * 0.75f,
                            endY = size.height,
                        ),
                    )
                }
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = (0.65f * style.edgeHighlightBrightness).coerceIn(0f, 1f)),
                            Color.White.copy(alpha = 0.04f),
                        ),
                    ),
                    shape = shape,
                ),
            content = content,
        )
    }
}
