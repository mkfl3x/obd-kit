package io.github.mkfl3x.obdkit.commands

// Typed result of decoding an ECU response.
// The concrete type always matches the command's Codec.outputType.
sealed class CommandResult {
    // Numeric value with a unit of measurement (RPM, °C, km/h, etc.)
    data class FloatResult(val value: Float, val unit: String) : CommandResult()

    // Text value (VIN, ASCII string, EnumLookup, Hex)
    data class StringResult(val value: String) : CommandResult()

    // List of strings (DTC codes: ["P0301", "U0100"])
    data class StringListResult(val values: List<String>) : CommandResult()

    // Named boolean flags (Bitfield: "MIL" → true, "DTC_count_bit_0" → false, ...)
    data class BooleanMapResult(val flags: Map<String, Boolean>) : CommandResult()

    // Raw bytes without decoding (Raw)
    data class ByteArrayResult(val bytes: ByteArray) : CommandResult() {
        override fun equals(other: Any?) =
            other is ByteArrayResult && bytes.contentEquals(other.bytes)
        override fun hashCode() = bytes.contentHashCode()
    }
}