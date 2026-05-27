package dev.marzban.admin.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormattersTest {

    @Test fun formatBytes_zero() {
        assertThat(formatBytes(0)).isEqualTo("0 B")
        assertThat(formatBytes(null)).isEqualTo("0 B")
    }

    @Test fun formatBytes_kib() {
        assertThat(formatBytes(2048)).isEqualTo("2 KiB")
    }

    @Test fun formatBytes_mib() {
        assertThat(formatBytes(5L * 1024 * 1024)).isEqualTo("5 MiB")
    }

    @Test fun formatBytes_gib() {
        assertThat(formatBytes(3L * 1024 * 1024 * 1024)).isEqualTo("3 GiB")
    }

    @Test fun formatBytesPerSecond_suffix() {
        assertThat(formatBytesPerSecond(1024)).isEqualTo("1 KiB/s")
    }

    @Test fun formatRatio_unlimited() {
        assertThat(formatRatio(used = 1024, total = null)).isEqualTo("1 KiB")
        assertThat(formatRatio(used = 1024, total = 0)).isEqualTo("1 KiB")
    }

    @Test fun formatRatio_withLimit() {
        assertThat(formatRatio(used = 1024, total = 2048)).isEqualTo("1 KiB / 2 KiB")
    }

    @Test fun formatEpoch_empty() {
        assertThat(formatEpoch(null)).isEqualTo("—")
        assertThat(formatEpoch(0)).isEqualTo("—")
    }
}
