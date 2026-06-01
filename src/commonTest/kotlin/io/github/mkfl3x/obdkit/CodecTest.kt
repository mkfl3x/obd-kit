package io.github.mkfl3x.obdkit

import io.github.mkfl3x.obdkit.commands.CommandResult
import io.github.mkfl3x.obdkit.commands.codec.Codec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodecTest {

    // ── Linear ───────────────────────────────────────────────────────────────

    @Test
    fun `Linear RPM two bytes`() {
        // PID 0x0C: rawValue = 0x1A * 256 + 0xF8 = 6904; rpm = 6904 * 0.25 = 1726.0
        val result = Codec.Linear(factor = 0.25, unit = "rpm", length = 2)
            .decode(listOf(0x1A, 0xF8)) as CommandResult.FloatResult
        assertEquals(1726.0f, result.value)
        assertEquals("rpm", result.unit)
    }

    @Test
    fun `Linear coolant temperature with offset`() {
        // PID 0x05: rawValue = 0x7B = 123; °C = 123 - 40 = 83
        val result = Codec.Linear(factor = 1.0, offset = -40.0, unit = "°C")
            .decode(listOf(0x7B)) as CommandResult.FloatResult
        assertEquals(83.0f, result.value)
    }

    @Test
    fun `Linear startByte offset`() {
        // second byte only
        val result = Codec.Linear(factor = 1.0, unit = "x", startByte = 1)
            .decode(listOf(0x00, 0x2A)) as CommandResult.FloatResult
        assertEquals(42.0f, result.value)
    }

    // ── Percent ───────────────────────────────────────────────────────────────

    @Test
    fun `Percent zero`() {
        val result = Codec.Percent().decode(listOf(0x00)) as CommandResult.FloatResult
        assertEquals(0.0f, result.value)
        assertEquals("%", result.unit)
    }

    @Test
    fun `Percent full scale`() {
        val result = Codec.Percent().decode(listOf(0xFF)) as CommandResult.FloatResult
        assertEquals(100.0f, result.value, absoluteTolerance = 0.01f)
    }

    // ── SignedPercent ─────────────────────────────────────────────────────────

    @Test
    fun `SignedPercent midpoint is zero`() {
        val result = Codec.SignedPercent().decode(listOf(0x80)) as CommandResult.FloatResult
        assertEquals(0.0f, result.value, absoluteTolerance = 0.01f)
    }

    @Test
    fun `SignedPercent minimum is -100`() {
        val result = Codec.SignedPercent().decode(listOf(0x00)) as CommandResult.FloatResult
        assertEquals(-100.0f, result.value, absoluteTolerance = 0.01f)
    }

    // ── Dtc ──────────────────────────────────────────────────────────────────

    @Test
    fun `Dtc decodes powertrain code`() {
        // count=1, bytes=[0x01, 0x23] → P0123
        val result = Codec.DTC.decode(listOf(0x01, 0x01, 0x23)) as CommandResult.StringListResult
        assertEquals(listOf("P0123"), result.values)
    }

    @Test
    fun `Dtc decodes body code`() {
        // 0x80 → system bits 10 = 'B', sub=0 → B0
        val result = Codec.DTC.decode(listOf(0x01, 0x80, 0xAB)) as CommandResult.StringListResult
        assertEquals(listOf("B00AB"), result.values)
    }

    @Test
    fun `Dtc skips zero padding slots`() {
        // count=1 but second pair is 00 00 — should be ignored
        val result = Codec.DTC.decode(listOf(0x01, 0x01, 0x23, 0x00, 0x00)) as CommandResult.StringListResult
        assertEquals(listOf("P0123"), result.values)
    }

    @Test
    fun `Dtc empty bytes returns empty list`() {
        val result = Codec.DTC.decode(emptyList()) as CommandResult.StringListResult
        assertTrue(result.values.isEmpty())
    }

    // ── Ascii ─────────────────────────────────────────────────────────────────

    @Test
    fun `Ascii decodes version string`() {
        val bytes = "ELM327 v2.1".map { it.code }
        val result = Codec.Ascii.decode(bytes) as CommandResult.StringResult
        assertEquals("ELM327 v2.1", result.value)
    }

    // ── Hex ──────────────────────────────────────────────────────────────────

    @Test
    fun `Hex formats bytes with spaces`() {
        val result = Codec.Hex.decode(listOf(0x4A, 0x2F, 0x00)) as CommandResult.StringResult
        assertEquals("4A 2F 00", result.value)
    }

    // ── Bitfield ──────────────────────────────────────────────────────────────

    @Test
    fun `Bitfield maps bits to named flags`() {
        val codec = Codec.Bitfield(bits = mapOf(7 to "MIL", 0 to "ready"))
        val result = codec.decode(listOf(0b10000001)) as CommandResult.BooleanMapResult
        assertEquals(true, result.flags["MIL"])
        assertEquals(true, result.flags["ready"])
    }

    @Test
    fun `Bitfield unset bits are false`() {
        val codec = Codec.Bitfield(bits = mapOf(7 to "MIL", 0 to "ready"))
        val result = codec.decode(listOf(0x00)) as CommandResult.BooleanMapResult
        assertEquals(false, result.flags["MIL"])
        assertEquals(false, result.flags["ready"])
    }

    // ── EnumLookup ────────────────────────────────────────────────────────────

    @Test
    fun `EnumLookup returns matching label`() {
        val codec = Codec.EnumLookup(table = mapOf(0x02 to "Closed loop"))
        val result = codec.decode(listOf(0x02)) as CommandResult.StringResult
        assertEquals("Closed loop", result.value)
    }

    @Test
    fun `EnumLookup returns Unknown for missing key`() {
        val codec = Codec.EnumLookup(table = mapOf(0x01 to "Open loop"))
        val result = codec.decode(listOf(0xFF)) as CommandResult.StringResult
        assertEquals("Unknown (0xFF)", result.value)
    }

    // ── Vin ───────────────────────────────────────────────────────────────────

    @Test
    fun `Vin drops count byte and returns 17 chars`() {
        val vin = "1HGCM82633A123456"
        val bytes = listOf(0x01) + vin.map { it.code }
        val result = Codec.Vin.decode(bytes) as CommandResult.StringResult
        assertEquals(vin, result.value)
    }
}