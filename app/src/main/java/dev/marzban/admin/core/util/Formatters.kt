package dev.marzban.admin.core.util

import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

private val unitFormat = DecimalFormat("#,##0.##")

fun formatBytes(bytes: Long?): String {
    if (bytes == null || bytes == 0L) return "0 B"
    val unit = 1024.0
    val absBytes = abs(bytes.toDouble())
    if (absBytes < unit) return "$bytes B"
    val exp = (ln(absBytes) / ln(unit)).toInt().coerceAtMost(6)
    val pre = "KMGTPE"[exp - 1]
    return "${unitFormat.format(bytes / unit.pow(exp.toDouble()))} ${pre}iB"
}

fun formatBytesPerSecond(bytes: Long?): String = "${formatBytes(bytes)}/s"

fun formatEpoch(epochSeconds: Long?): String {
    if (epochSeconds == null || epochSeconds <= 0) return "—"
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochSecond(epochSeconds))
}

fun formatIso(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return runCatching {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(if (iso.endsWith("Z")) iso else "${iso}Z"))
    }.getOrDefault(iso)
}

fun formatPercent(value: Double): String = "${unitFormat.format(value)}%"

fun formatRatio(used: Long, total: Long?): String {
    if (total == null || total <= 0) return formatBytes(used)
    return "${formatBytes(used)} / ${formatBytes(total)}"
}
