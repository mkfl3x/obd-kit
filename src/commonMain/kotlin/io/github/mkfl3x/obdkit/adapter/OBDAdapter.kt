package io.github.mkfl3x.obdkit.adapter

import io.github.mkfl3x.obdkit.adapter.connection.ConnectionState
import io.github.mkfl3x.obdkit.adapter.connection.OBDConnector
import io.github.mkfl3x.obdkit.commands.Command
import io.github.mkfl3x.obdkit.commands.CommandResult
import io.github.mkfl3x.obdkit.commands.Writeable
import io.github.mkfl3x.obdkit.commands.catalog.ATCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class OBDAdapter(
    private val connector: OBDConnector,
    private val commandTimeout: Duration = 10.seconds
) {

    lateinit var info: AdapterInfo private set
    private val commandMutex = Mutex()

    suspend fun connect(address: String) {
        connector.connect(address)
        executeCommand(ATCommand.Reset.code).also { delay(1.seconds) }
        executeCommand(ATCommand.EchoOff.code)
        executeCommand(ATCommand.LinefeedsOff.code)
        executeCommand(ATCommand.AutoProtocol.code)
        executeCommand("0100").also { delay(1.seconds) }
        info = AdapterInfo(
            firmware = (executeCommand(ATCommand.Firmware) as CommandResult.StringResult).value,
            deviceDescription = (executeCommand(ATCommand.DeviceDescription) as CommandResult.StringResult).value,
            protocolNumber = (executeCommand(ATCommand.ProtocolNumber) as CommandResult.StringResult).value
        )
    }

    suspend fun executeCommand(command: Command, protocolCheck: Boolean = true): CommandResult {
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