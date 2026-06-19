package io.github.mkfl3x.obdkit.commands

import kotlinx.serialization.Serializable

// Typed result of decoding an ECU response.
// The concrete type always matches the command's Codec.outputType.
@Serializable
sealed class CommandResult {
    // Numeric value with a unit of measurement (RPM, °C, km/h, etc.)
    @Serializable
    data class FloatResult(val value: Float, val unit: String) : CommandResult() {
        override fun toString() = "$value $unit"
    }

    // Text value (VIN, ASCII string, EnumLookup, Hex)
    @Serializable
    data class StringResult(val value: String) : CommandResult() {
        override fun toString() = value
    }

    // List of strings (DTC codes: ["P0301", "U0100"])
    @Serializable
    data class StringListResult(val values: List<String>) : CommandResult() {
        override fun toString() = values.joinToString(", ")
    }

    // Named boolean flags (Bitfield: "MIL" → true, "DTC_count_bit_0" → false, ...)
    @Serializable
    data class BooleanMapResult(val flags: Map<String, Boolean>) : CommandResult() {
        override fun toString() = flags.entries.filter { it.value }.joinToString(", ") { it.key }.ifEmpty { "none" }
    }

    // Raw bytes without decoding (Raw)
    @Serializable
    data class ByteArrayResult(val bytes: ByteArray) : CommandResult() {
        override fun equals(other: Any?) =
            other is ByteArrayResult && bytes.contentEquals(other.bytes)
        override fun hashCode() = bytes.contentHashCode()
        override fun toString() = bytes.joinToString(" ") { it.toHexString() }
    }

    // Multiple named channels from a single response (Multi)
    @Serializable
    data class MapResult(val channels: Map<String, CommandResult>) : CommandResult() {
        override fun toString() = channels.entries.joinToString(", ") { "${it.key}: ${it.value}" }
    }
}