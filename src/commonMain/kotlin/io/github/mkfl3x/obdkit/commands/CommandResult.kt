package io.github.mkfl3x.obdkit.commands

import kotlinx.serialization.Serializable

// Typed result of decoding an ECU response.
// The concrete type always matches the command's Codec.outputType.
@Serializable
sealed class CommandResult {
    // Numeric value with a unit of measurement (RPM, °C, km/h, etc.)
    @Serializable
    data class FloatResult(val value: Float, val unit: String) : CommandResult()

    // Text value (VIN, ASCII string, EnumLookup, Hex)
    @Serializable
    data class StringResult(val value: String) : CommandResult()

    // List of strings (DTC codes: ["P0301", "U0100"])
    @Serializable
    data class StringListResult(val values: List<String>) : CommandResult()

    // Named boolean flags (Bitfield: "MIL" → true, "DTC_count_bit_0" → false, ...)
    @Serializable
    data class BooleanMapResult(val flags: Map<String, Boolean>) : CommandResult()

    // Raw bytes without decoding (Raw)
    @Serializable
    data class ByteArrayResult(val bytes: ByteArray) : CommandResult() {
        override fun equals(other: Any?) =
            other is ByteArrayResult && bytes.contentEquals(other.bytes)
        override fun hashCode() = bytes.contentHashCode()
    }

    // Multiple named channels from a single response (Multi)
    @Serializable
    data class MapResult(val channels: Map<String, CommandResult>) : CommandResult()

    override fun toString() = when (this) {
        is FloatResult -> "$value $unit"
        is StringResult -> value
        is StringListResult -> values.joinToString(", ")
        is BooleanMapResult -> flags.entries.filter { it.value }.joinToString(", ") { it.key }.ifEmpty { "none" }
        is ByteArrayResult -> bytes.joinToString(" ") { it.toHexString() }
        is MapResult -> channels.entries.joinToString(", ") { "${it.key}: ${it.value}" }
    }
}