package com.blushelf.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blushelf.app.data.MediaItem
import com.blushelf.app.data.MediaKind

private val artworkPalettes = listOf(
    listOf(Color(0xFF061A2E), Color(0xFF00639A), Color(0xFF4FD8EB)),
    listOf(Color(0xFF27143F), Color(0xFF6D3DB7), Color(0xFFE879A8)),
    listOf(Color(0xFF102A27), Color(0xFF00796B), Color(0xFFFFB74D)),
    listOf(Color(0xFF32131A), Color(0xFF9B2C45), Color(0xFFFFC857)),
    listOf(Color(0xFF17202A), Color(0xFF455A64), Color(0xFF90CAF9))
)

@Composable
fun MediaCover(
    item: MediaItem,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showLabels: Boolean = true
) {
    val palette = artworkPalettes[(item.title.hashCode() and Int.MAX_VALUE) % artworkPalettes.size]
    val isRecord = item.format.contains("Vinyl", true)
    val isDisc = isRecord || item.format.contains("CD", true)
    Box(
        modifier
            .clip(RoundedCornerShape(if (compact) 8.dp else 22.dp))
            .background(Brush.linearGradient(palette))
            .semantics { contentDescription = item.title + ", virtual cover" }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val seed = item.title.hashCode().toFloat()
            drawCircle(
                color = palette[2].copy(alpha = .2f),
                radius = size.minDimension * .46f,
                center = androidx.compose.ui.geometry.Offset(size.width * .78f, size.height * .2f)
            )
            drawLine(
                color = Color.White.copy(alpha = .16f),
                start = androidx.compose.ui.geometry.Offset(-size.width * .1f, size.height * .55f),
                end = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * (.22f + (seed % 13f) / 100f)),
                strokeWidth = size.minDimension * .11f,
                cap = StrokeCap.Round
            )
            if (isDisc) {
                val radius = size.minDimension * if (isRecord) .31f else .25f
                val center = androidx.compose.ui.geometry.Offset(size.width * .68f, size.height * .34f)
                drawCircle(Color(0xE615171A), radius, center)
                drawCircle(Color.White.copy(alpha = .18f), radius * .74f, center, style = Stroke(radius * .035f))
                drawCircle(palette[2], radius * .22f, center)
                drawCircle(Color(0xFF0A0B0C), radius * .045f, center)
            }
        }
        if (showLabels) Row(
            Modifier.fillMaxWidth().padding(if (compact) 7.dp else 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (item.kind == MediaKind.VIDEO) Icons.Outlined.Movie else Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = .9f),
                modifier = Modifier.size(if (compact) 16.dp else 24.dp)
            )
            if (!compact) {
                Surface(color = Color.Black.copy(alpha = .28f), shape = CircleShape) {
                    Text(
                        item.format,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        maxLines = 1
                    )
                }
            }
        }
        if (showLabels) Column(
            Modifier.align(Alignment.BottomStart).padding(if (compact) 8.dp else 18.dp)
        ) {
            Text(
                item.title,
                color = Color.White,
                style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                maxLines = if (compact) 2 else 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!compact) {
                Text(
                    listOfNotNull(item.year?.toString(), item.originalTitle.takeIf(String::isNotBlank)).joinToString(" · "),
                    color = Color.White.copy(alpha = .78f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
