package io.github.mkfl3x.obdkit.adapter

import io.github.mkfl3x.obdkit.commands.protocol.ATCommandProtocol
import io.github.mkfl3x.obdkit.commands.protocol.CommandProtocol
import io.github.mkfl3x.obdkit.commands.protocol.OBDCommandProtocol
import io.github.mkfl3x.obdkit.commands.protocol.UDSCommandProtocol

// The bus protocol that ELM327 detected when connecting to the vehicle (via ATDPN).
sealed class AdapterProtocol {

    abstract fun isCompatibleWith(protocol: CommandProtocol): Boolean

    // Protocol not determined (ATDPN returned an unrecognised value) — skip compatibility check
    object Unknown : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) = true
        override fun toString() = "Unknown"
    }

    // J1850 and K-Line support OBD-II and AT but not UDS (UDS requires CAN/ISO-TP)
    object J1850PWM : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) =
            protocol is OBDCommandProtocol || protocol is ATCommandProtocol
        override fun toString() = "SAE J1850 PWM"
    }

    object J1850VPW : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) =
            protocol is OBDCommandProtocol || protocol is ATCommandProtocol
        override fun toString() = "SAE J1850 VPW"
    }

    object ISO9141 : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) =
            protocol is OBDCommandProtocol || protocol is ATCommandProtocol
        override fun toString() = "ISO 9141-2"
    }

    object KWP5Baud : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) =
            protocol is OBDCommandProtocol || protocol is ATCommandProtocol
        override fun toString() = "ISO 14230-4 KWP (5 baud init)"
    }

    object KWPFast : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) =
            protocol is OBDCommandProtocol || protocol is ATCommandProtocol
        override fun toString() = "ISO 14230-4 KWP (fast init)"
    }

    // CAN supports all protocols including UDS
    data class Can(val bitId: Int, val baudRate: Int) : AdapterProtocol() {
        override fun isCompatibleWith(protocol: CommandProtocol) =
            protocol is OBDCommandProtocol || protocol is UDSCommandProtocol || protocol is ATCommandProtocol
        override fun toString() = "CAN (${bitId}-bit ID, $baudRate kbaud)"
    }
}