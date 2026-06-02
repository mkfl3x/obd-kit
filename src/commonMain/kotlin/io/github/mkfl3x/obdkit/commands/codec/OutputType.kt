package io.github.mkfl3x.obdkit.commands.codec

// Kotlin type of the value produced after decoding a response.
// Not set manually — taken from Codec.outputType.
enum class OutputType {
    FLOAT,       // Numeric result with decimals (Linear, Percent, SignedPercent)
    STRING,      // Text value (Ascii, Hex, Vin, EnumLookup)
    STRING_LIST, // List of strings (DTC codes)
    BOOLEAN_MAP, // Map of flag name → true/false (Bitfield)
    BYTE_ARRAY,  // Raw bytes without decoding (Raw)
    MAP          // Named channels, each with its own result (Multi)
}