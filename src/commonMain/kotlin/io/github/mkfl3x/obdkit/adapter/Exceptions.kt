package io.github.mkfl3x.obdkit.adapter

import io.github.mkfl3x.obdkit.commands.Command
import kotlin.time.Duration

// Command did not receive a response within the allotted time
class OBDCommandTimeoutException : Exception {
    constructor(command: Command, timeout: Duration) :
        super("Command '${command.label}' timed out after $timeout")
    constructor(data: String, timeout: Duration) :
        super("Command '$data' timed out after $timeout")
}

// Attempt to execute a command without an active connection
class OBDAdapterNotConnectedException :
    IllegalStateException("OBD adapter is not connected")

// Command requires a transport protocol that does not match the one detected by the adapter
class IncompatibleTransportException(command: Command, protocol: AdapterProtocol) :
    UnsupportedOperationException(
        "Command '${command.label}' requires ${command.protocol.displayName}, " +
            "but adapter is connected via $protocol"
    )

// Response matches neither a positive nor a negative prefix — unknown format
class UnexpectedResponseException(raw: String) :
    Exception("Unexpected response: $raw")

// ECU returned a negative response (e.g. 7F xx xx in UDS)
class NegativeResponseException(raw: String) :
    Exception("Negative response from ECU: $raw")