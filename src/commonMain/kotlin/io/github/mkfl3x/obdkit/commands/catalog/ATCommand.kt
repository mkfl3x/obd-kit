package io.github.mkfl3x.obdkit.commands.catalog

import io.github.mkfl3x.obdkit.commands.Command
import io.github.mkfl3x.obdkit.commands.CommandResult
import io.github.mkfl3x.obdkit.commands.codec.Codec
import io.github.mkfl3x.obdkit.commands.protocol.ATCommandProtocol

abstract class ATCommand : Command<CommandResult.StringResult>() {

    final override val protocol = ATCommandProtocol
    final override val codec = Codec.Ascii
    final override val branded = CommandBrand()

    // ── Reset and initialisation ─────────────────────────────────────────────

    object Reset : ATCommand() {
        override val label = "Reset"
        override val description = "Full software reset of the ELM327 adapter; restores all factory defaults"
        override val code = "ATZ"
    }

    object WarmStart : ATCommand() {
        override val label = "Warm Start"
        override val description = "Quick restart without full reset; faster than ATZ but preserves fewer settings"
        override val code = "ATWS"
    }

    // ── Response format ──────────────────────────────────────────────────────

    object EchoOff : ATCommand() {
        override val label = "Echo Off"
        override val description = "Disable command echo so responses don't repeat the sent command"
        override val code = "ATE0"
    }

    object EchoOn : ATCommand() {
        override val label = "Echo On"
        override val description = "Enable command echo (default after reset)"
        override val code = "ATE1"
    }

    object LinefeedsOff : ATCommand() {
        override val label = "Linefeeds Off"
        override val description = "Disable linefeed characters in responses"
        override val code = "ATL0"
    }

    object LinefeedsOn : ATCommand() {
        override val label = "Linefeeds On"
        override val description = "Enable linefeed characters in responses (default after reset)"
        override val code = "ATL1"
    }

    object SpacesOff : ATCommand() {
        override val label = "Spaces Off"
        override val description = "Remove space separators between hex bytes in responses for more compact output"
        override val code = "ATS0"
    }

    object SpacesOn : ATCommand() {
        override val label = "Spaces On"
        override val description = "Include space separators between hex bytes in responses (default)"
        override val code = "ATS1"
    }

    object HeadersOff : ATCommand() {
        override val label = "Headers Off"
        override val description = "Hide CAN/bus frame headers in responses; return data bytes only"
        override val code = "ATH0"
    }

    object HeadersOn : ATCommand() {
        override val label = "Headers On"
        override val description = "Show CAN/bus frame headers in responses for low-level inspection"
        override val code = "ATH1"
    }

    object AllowLong : ATCommand() {
        override val label = "Allow Long"
        override val description = "Allow responses longer than 7 data bytes (disables the standard ISO-TP length check)"
        override val code = "ATAL"
    }

    // ── Protocol control ─────────────────────────────────────────────────────

    object AutoProtocol : ATCommand() {
        override val label = "Auto Protocol"
        override val description = "Let ELM327 automatically detect the vehicle OBD protocol on first request"
        override val code = "ATSP0"
    }

    /** number: protocol number 1–C per ELM327 datasheet table */
    data class SetProtocol(val number: String) : ATCommand() {
        override val label = "Set Protocol"
        override val description = "Manually select OBD protocol by number (1–C); use ATSP0 to restore auto-detect"
        override val code = "ATSP$number"
    }

    /** number: protocol number to test without making it the default */
    data class TryProtocol(val number: String) : ATCommand() {
        override val label = "Try Protocol"
        override val description = "Attempt to connect using protocol number without saving it as the default"
        override val code = "ATTP$number"
    }

    object ProtocolClose : ATCommand() {
        override val label = "Protocol Close"
        override val description = "Explicitly close the current OBD protocol session and release the bus"
        override val code = "ATPC"
    }

    object UseStandardSearch : ATCommand() {
        override val label = "Use Standard Search"
        override val description = "Reset protocol search order to factory default instead of last-used"
        override val code = "ATSS"
    }

    object MemoryOff : ATCommand() {
        override val label = "Memory Off"
        override val description = "Don't remember the last used protocol across power cycles"
        override val code = "ATSM0"
    }

    object MemoryOn : ATCommand() {
        override val label = "Memory On"
        override val description = "Remember the last used protocol across power cycles for faster reconnect"
        override val code = "ATSM1"
    }

    // ── Timings ──────────────────────────────────────────────────────────────

    /** value: 0x00–0xFF; actual timeout = value × 4 ms (0x00 ≈ 20 ms minimum) */
    data class SetTimeout(val value: Int) : ATCommand() {
        override val label = "Set Timeout"
        override val description = "Set bus response timeout; each unit is 4 ms (0x00 ≈ 20 ms, 0xFF ≈ 1020 ms)"
        override val code = "ATST${value.toString(16).padStart(2, '0').uppercase()}"
    }

    object AdaptiveTimingOff : ATCommand() {
        override val label = "Adaptive Timing Off"
        override val description = "Disable adaptive timing; use fixed timeout set by ATST"
        override val code = "ATAT0"
    }

    object AdaptiveTimingAuto : ATCommand() {
        override val label = "Adaptive Timing Auto"
        override val description = "Enable adaptive timeout that adjusts based on observed bus response times (default)"
        override val code = "ATAT1"
    }

    object AdaptiveTimingAggressive : ATCommand() {
        override val label = "Adaptive Timing Aggressive"
        override val description = "Enable aggressive adaptive timing; maximises throughput at the risk of timeouts on slow buses"
        override val code = "ATAT2"
    }

    // ── CAN settings ─────────────────────────────────────────────────────────

    /** header: 3-byte hex string for 11-bit CAN (e.g. "7DF") or 4-byte for 29-bit */
    data class SetHeader(val header: String) : ATCommand() {
        override val label = "Set Header"
        override val description = "Set the CAN transmit header (source address) for outgoing request frames"
        override val code = "ATSH$header"
    }

    /** filter: hex CAN ID; incoming frame is accepted if (id AND mask) == filter */
    data class CanFilter(val filter: String) : ATCommand() {
        override val label = "CAN Filter"
        override val description = "Set CAN receive ID filter; frames pass if (id AND mask) == filter"
        override val code = "ATCF$filter"
    }

    /** mask: hex bitmask; 1-bits are checked against the filter, 0-bits are ignored */
    data class CanMask(val mask: String) : ATCommand() {
        override val label = "CAN Mask"
        override val description = "Set CAN receive ID mask; 1-bits in the mask mean that bit of the ID is checked against the filter"
        override val code = "ATCM$mask"
    }

    object CanSilentMonOff : ATCommand() {
        override val label = "CAN Silent Monitor Off"
        override val description = "Disable silent monitoring; adapter sends ACK bits normally (default)"
        override val code = "ATCSM0"
    }

    object CanSilentMonOn : ATCommand() {
        override val label = "CAN Silent Monitor On"
        override val description = "Enable silent monitoring; adapter listens without sending ACK bits (non-intrusive sniffing)"
        override val code = "ATCSM1"
    }

    /** priority: 5-bit hex value (e.g. "E0") prepended to 29-bit CAN transmit headers */
    data class SetCanPriority(val priority: String) : ATCommand() {
        override val label = "Set CAN Priority"
        override val description = "Set the priority bits prepended to 29-bit CAN transmit headers"
        override val code = "ATCP$priority"
    }

    /** address: 1-byte hex string; enables CAN extended addressing with this address byte */
    data class SetExtendedAddress(val address: String) : ATCommand() {
        override val label = "Set Extended Address"
        override val description = "Enable CAN extended addressing and set the extended address byte appended to each frame"
        override val code = "ATCEA$address"
    }

    object DisableExtendedAddress : ATCommand() {
        override val label = "Disable Extended Address"
        override val description = "Disable CAN extended addressing and return to standard frame format"
        override val code = "ATCEA"
    }

    /** address: hex CAN ID; filter incoming frames to this receiver address only */
    data class SetReceiveAddress(val address: String) : ATCommand() {
        override val label = "Set Receive Address"
        override val description = "Filter incoming CAN frames to only accept frames addressed to the given CAN ID"
        override val code = "ATCRA$address"
    }

    object ResetReceiveAddress : ATCommand() {
        override val label = "Reset Receive Address"
        override val description = "Clear the receive address filter (ATCRA); accept all incoming frames again"
        override val code = "ATCRA"
    }

    // ── ISO-TP Flow Control ──────────────────────────────────────────────────

    /** mode: 0 = off, 1 = auto (default), 2 = manual (requires FlowControlHeader + FlowControlData) */
    data class FlowControlMode(val mode: Int) : ATCommand() {
        override val label = "Flow Control Mode"
        override val description = "Set ISO-TP flow control mode: 0=off, 1=auto (default), 2=manual (configure with ATFC SH and ATFC SD)"
        override val code = "ATFC SM$mode"
    }

    /** header: 3-byte hex CAN ID used in outgoing flow control frames */
    data class FlowControlHeader(val header: String) : ATCommand() {
        override val label = "Flow Control Header"
        override val description = "Set CAN header used in ISO-TP flow control frames (active when FlowControlMode = 2)"
        override val code = "ATFC SH$header"
    }

    /** data: hex payload bytes for the flow control frame, e.g. "300000" */
    data class FlowControlData(val data: String) : ATCommand() {
        override val label = "Flow Control Data"
        override val description = "Set data bytes sent in ISO-TP flow control frames (active when FlowControlMode = 2)"
        override val code = "ATFC SD$data"
    }

    // ── K-Line settings ──────────────────────────────────────────────────────

    object BypassInit : ATCommand() {
        override val label = "Bypass Initialization"
        override val description = "Skip the K-Line slow/fast init sequence; useful if the ECU is already initialized"
        override val code = "ATBI"
    }

    /** rate: 10, 12, 48, or 96 (kbaud) for ISO 9141-2 / KWP2000 K-Line */
    data class IsoBaudRate(val rate: Int) : ATCommand() {
        override val label = "ISO Baud Rate"
        override val description = "Set K-Line baud rate: 10 (ISO 9141 default), 12 (some ECUs), 48, or 96 kbaud"
        override val code = "ATIB $rate"
    }

    // ── Bus monitoring ───────────────────────────────────────────────────────

    object MonitorAll : ATCommand() {
        override val label = "Monitor All"
        override val description = "Passively print all frames on the bus in real time; send any character to stop"
        override val code = "ATMA"
    }

    /** address: 1-byte hex transmitter address to filter on */
    data class MonitorTransmitter(val address: String) : ATCommand() {
        override val label = "Monitor Transmitter"
        override val description = "Monitor bus frames from a specific transmitter address only"
        override val code = "ATMT$address"
    }

    /** address: 1-byte hex receiver address to filter on */
    data class MonitorReceiver(val address: String) : ATCommand() {
        override val label = "Monitor Receiver"
        override val description = "Monitor bus frames addressed to a specific receiver address only"
        override val code = "ATMR$address"
    }

    // ── Adapter information ──────────────────────────────────────────────────

    object Firmware : ATCommand() {
        override val label = "Firmware Version"
        override val description = "ELM327 firmware version string, e.g. \"ELM327 v2.1\""
        override val code = "ATI"
    }

    object DeviceDescription : ATCommand() {
        override val label = "Device Description"
        override val description = "Human-readable adapter description programmed by manufacturer"
        override val code = "AT@1"
    }

    object DeviceIdentifier : ATCommand() {
        override val label = "Device Identifier"
        override val description = "Adapter serial/identifier string written via AT@2 (empty if not programmed)"
        override val code = "AT@2"
    }

    object ProtocolNumber : ATCommand() {
        override val label = "Protocol Number"
        override val description = "Number of the currently active OBD protocol (see ELM327 datasheet table)"
        override val code = "ATDPN"
    }

    object DescribeProtocol : ATCommand() {
        override val label = "Describe Protocol"
        override val description = "Human-readable name of the currently active OBD protocol, e.g. \"ISO 15765-4 (CAN 11/500)\""
        override val code = "ATDP"
    }

    object ParametersSummary : ATCommand() {
        override val label = "Programmable Parameters Summary"
        override val description = "Dump all ELM327 programmable parameters (PP xx) and their current on/off state and values"
        override val code = "ATPPS"
    }

    object BufferDump : ATCommand() {
        override val label = "Buffer Dump"
        override val description = "Print the contents of the ELM327 internal receive buffer in hex; useful for low-level debugging"
        override val code = "ATBD"
    }

    // ── Adapter diagnostics ──────────────────────────────────────────────────

    object BatteryVoltage : ATCommand() {
        override val label = "Battery Voltage"
        override val description = "Vehicle battery voltage measured by the adapter, e.g. \"12.3V\""
        override val code = "ATRV"
    }

    object IgnitionState : ATCommand() {
        override val label = "Ignition State"
        override val description = "Ignition line state as seen by the adapter: \"ON\" or \"OFF\""
        override val code = "ATIGN"
    }
}