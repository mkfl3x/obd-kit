package io.github.mkfl3x.obdkit.adapter

import io.github.mkfl3x.obdkit.adapter.connection.ConnectionState
import io.github.mkfl3x.obdkit.adapter.connection.OBDConnector
import io.github.mkfl3x.obdkit.commands.Command
import io.github.mkfl3x.obdkit.commands.CommandResult
import io.github.mkfl3x.obdkit.commands.Writeable
import io.github.mkfl3x.obdkit.commands.catalog.ATCommand
import io.github.mkfl3x.obdkit.commands.catalog.OBDCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OBDAdapter(
    private val connector: OBDConnector,
    private val commandTimeout: Duration = 10.seconds
) {

    val connectionState get() = connector.state.value
    lateinit var info: AdapterInfo private set
    private val commandMutex = Mutex()

    suspend fun connect(address: String) {
        connector.connect(address)
        listOf(
            ATCommand.Reset,
            ATCommand.EchoOff,
            ATCommand.LinefeedsOff,
            ATCommand.AutoProtocol
        ).forEach {
            executeCommand(it, protocolCheck = false)
            delay(500.milliseconds)
        }
        executeCommand("0100") // triggers protocol auto-detection
        info = AdapterInfo(
            firmware = executeCommand(ATCommand.Firmware, protocolCheck = false).value,
            deviceDescription = executeCommand(ATCommand.DeviceDescription, protocolCheck = false).value,
            protocolNumber = executeCommand(ATCommand.ProtocolNumber, protocolCheck = false).value
        )
    }

    suspend fun <T : CommandResult> executeCommand(command: Command<T>, protocolCheck: Boolean = true): T {
        if (protocolCheck && !info.protocol.isCompatibleWith(command.protocol))
            throw IncompatibleTransportException(command, info.protocol)
        return executeCommand(command.code + if (command is Writeable) command.payload else "")
            .let { command.decode(it) }
    }

    // Mode 01 PIDs this vehicle supports, read from the "Supported PIDs" bitmasks (0100, 0120, …).
    // Each 4-byte mask is MSB first: bit 7 of the first byte is PID base+1, LSB of the last is base+32.
    // PID base+32 (0x20, 0x40, …) flags that the next range exists; such flags are excluded from the result.
    suspend fun supportedPids(): Set<Int> {
        val pids = mutableSetOf<Int>()
        for (base in 0x00..0xC0 step 0x20) {
            val command = OBDCommand.SupportedPids(base)
            val raw = try {
                executeCommand(command.code)
            } catch (_: OBDCommandTimeoutException) {
                break
            }
            // "NO DATA" or a malformed answer — the vehicle doesn't report this range
            val bytes = runCatching { command.decode(raw).bytes }.getOrNull() ?: break
            val mask = bytes.take(4).fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
            pids += (1..32).filter { n -> (mask ushr (32 - n)) and 1L == 1L }.map { n -> base + n }
            if (base + 0x20 !in pids) break
        }
        return pids.filterTo(mutableSetOf()) { it % 0x20 != 0 }
    }

    suspend fun executeCommand(code: String, normalizedResponse: Boolean = true): String {

        fun String.normalize() = this
            .replace("\r", "")
            .replace("\n", "")
            .replace(">", "")
            .trim()

        if (connector.state.value != ConnectionState.Connected)
            throw OBDAdapterNotConnectedException()
        return commandMutex.withLock {
            withTimeoutOrNull(commandTimeout) {
                connector.sendCommand(code).let { if (normalizedResponse) it.normalize() else it }
            } ?: throw OBDCommandTimeoutException(code, commandTimeout)
        }
    }

    suspend fun disconnect() {
        connector.disconnect()
    }
}