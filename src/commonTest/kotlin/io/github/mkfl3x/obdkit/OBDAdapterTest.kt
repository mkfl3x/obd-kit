package io.github.mkfl3x.obdkit

import io.github.mkfl3x.obdkit.adapter.OBDAdapter
import io.github.mkfl3x.obdkit.adapter.OBDAdapterNotConnectedException
import io.github.mkfl3x.obdkit.adapter.connection.ConnectionState
import io.github.mkfl3x.obdkit.adapter.connection.OBDConnector
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class OBDAdapterTest {

    // ── supportedPids ─────────────────────────────────────────────────────────

    @Test
    fun `supportedPids walks ranges while the next-range bit is set`() = runBlocking {
        val connector = FakeConnector(
            "0100" to "41 00 80 00 00 01", // PID 01 + next range
            "0120" to "41 20 00 00 00 01", // only next range flag
            "0140" to "41 40 40 00 00 00"  // PID 42, no next range
        )
        assertEquals(setOf(0x01, 0x42), OBDAdapter(connector).supportedPids())
        assertEquals(listOf("0100", "0120", "0140"), connector.sent)
    }

    @Test
    fun `supportedPids decodes MSB-first mask`() = runBlocking {
        // 0xBE = 1011 1110 → 01, 03..07; 0x1F = 0001 1111 → 0C..10; 0xA8 = 1010 1000 → 11, 13, 15; 0x12 → 1C, 1F
        val connector = FakeConnector("0100" to "4100BE1FA812")
        assertEquals(
            setOf(0x01, 0x03, 0x04, 0x05, 0x06, 0x07, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x13, 0x15, 0x1C, 0x1F),
            OBDAdapter(connector).supportedPids()
        )
    }

    @Test
    fun `supportedPids stops at a range the vehicle does not answer`() = runBlocking {
        val connector = FakeConnector("0100" to "41 00 80 00 00 01", "0120" to "NO DATA")
        assertEquals(setOf(0x01), OBDAdapter(connector).supportedPids())
    }

    @Test
    fun `supportedPids stops at a range that times out`() = runBlocking {
        val connector = FakeConnector("0100" to "41 00 80 00 00 01") // 0120 never answers
        assertEquals(setOf(0x01), OBDAdapter(connector, commandTimeout = 100.milliseconds).supportedPids())
    }

    @Test
    fun `supportedPids stops after the last range`() = runBlocking {
        // every range claims the next one is available
        val connector = FakeConnector(*(0x00..0xC0 step 0x20).map { base ->
            val pid = base.toString(16).padStart(2, '0').uppercase()
            "01$pid" to "41 $pid 00 00 00 01"
        }.toTypedArray())
        OBDAdapter(connector).supportedPids()
        assertEquals(listOf("0100", "0120", "0140", "0160", "0180", "01A0", "01C0"), connector.sent)
    }

    @Test
    fun `supportedPids requires a connected adapter`() = runBlocking<Unit> {
        val connector = FakeConnector().apply { state.value = ConnectionState.Disconnected }
        assertFailsWith<OBDAdapterNotConnectedException> { OBDAdapter(connector).supportedPids() }
    }

    // Answers commands from a fixed table; unknown commands never respond (adapter times out)
    private class FakeConnector(vararg responses: Pair<String, String>) : OBDConnector {
        private val responses = responses.toMap()
        val sent = mutableListOf<String>()
        override val state = MutableStateFlow(ConnectionState.Connected)
        override suspend fun connect(address: String) {}
        override suspend fun disconnect() {}
        override suspend fun sendCommand(command: String): String {
            sent += command
            return responses[command]?.let { "$it\r>" } ?: awaitCancellation()
        }
    }
}
