package io.github.mkfl3x.obdkit

import io.github.mkfl3x.obdkit.commands.Command
import io.github.mkfl3x.obdkit.commands.CommandResult
import io.github.mkfl3x.obdkit.commands.brand.CommandBrand
import io.github.mkfl3x.obdkit.commands.codec.Codec
import io.github.mkfl3x.obdkit.commands.protocol.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandDecodeTest {

    // ── OBD-II prefix derivation ──────────────────────────────────────────────

    @Test
    fun `OBD-II RPM response is decoded correctly`() {
        // code "010C" → response prefix "410C", payload [0x1A, 0xF8] → 1726 rpm
        val cmd = obdCommand("010C", Codec.Linear(factor = 0.25, unit = "rpm", length = 2))
        val result = cmd.decode("410C1AF8\r>") as CommandResult.FloatResult
        assertEquals(1726.0f, result.value)
    }

    @Test
    fun `OBD-II response with spaces and noise is decoded`() {
        val cmd = obdCommand("010C", Codec.Linear(factor = 0.25, unit = "rpm", length = 2))
        val result = cmd.decode("41 0C 1A F8\r>") as CommandResult.FloatResult
        assertEquals(1726.0f, result.value)
    }

    @Test
    fun `OBD-II multi-frame response strips frame numbers`() {
        // multi-frame lines like "0:410C1AF8" — numbers+colon must be stripped
        val cmd = obdCommand("010C", Codec.Linear(factor = 0.25, unit = "rpm", length = 2))
        val result = cmd.decode("0:410C1AF8\r>") as CommandResult.FloatResult
        assertEquals(1726.0f, result.value)
    }

    @Test
    fun `OBD-II DTC response decoded correctly`() {
        // Mode 03 → response prefix "43", payload count=1, code P0123
        val cmd = obdCommand("03", Codec.DTC)
        val result = cmd.decode("430101 23\r>") as CommandResult.StringListResult
        assertEquals(listOf("P0123"), result.values)
    }

    // ── UDS prefix derivation ─────────────────────────────────────────────────

    @Test
    fun `UDS ReadDataByIdentifier response is decoded`() {
        // code "2201F190" → service 0x22+0x40=0x62 → prefix "6201F190"
        val cmd = udsCommand("2201F190", Codec.Vin)
        val vin = "1HGCM82633A123456"
        val payload = listOf(0x01) + vin.map { it.code }
        val hex = payload.joinToString("") { it.toString(16).padStart(2, '0').uppercase() }
        val result = cmd.decode("6201F190$hex\r>") as CommandResult.StringResult
        assertEquals(vin, result.value)
    }

    // ── AT responses ──────────────────────────────────────────────────────────

    @Test
    fun `AT command response is decoded as ASCII string`() {
        val cmd = atCommand("ATI", Codec.Ascii)
        val result = cmd.decode("ELM327 v2.1\r>") as CommandResult.StringResult
        assertEquals("ELM327 v2.1", result.value)
    }

    @Test
    fun `AT command response strips carriage return and prompt`() {
        val cmd = atCommand("ATRV", Codec.Ascii)
        val result = cmd.decode("12.3V\r>") as CommandResult.StringResult
        assertEquals("12.3V", result.value)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun obdCommand(code: String, codec: Codec) = object : Command {
        override val label = "test"
        override val description = "test"
        override val code = code
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = codec
        override val branded = CommandBrand()
    }

    private fun udsCommand(code: String, codec: Codec) = object : Command {
        override val label = "test"
        override val description = "test"
        override val code = code
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER)
        override val codec = codec
        override val branded = CommandBrand()
    }

    private fun atCommand(code: String, codec: Codec) = object : Command {
        override val label = "test"
        override val description = "test"
        override val code = code
        override val protocol = ATCommandProtocol
        override val codec = codec
        override val branded = CommandBrand()
    }
}