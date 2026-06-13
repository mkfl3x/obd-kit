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
            ATCommand.AutoProtocol,
            OBDCommand.SupportedPids
        ).forEach {
            executeCommand(it, protocolCheck = false)
            delay(500.milliseconds)
        }
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