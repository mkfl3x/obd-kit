package io.github.mkfl3x.obdkit.commands.codec

import io.github.mkfl3x.obdkit.commands.CommandResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Codec<out T : CommandResult> {

    abstract val outputType: OutputType
    abstract fun decode(bytes: List<Int>): T

    // Raw bytes without decoding — for debugging or proprietary formats
    @Serializable @SerialName("Raw")
    object Raw : Codec<CommandResult.ByteArrayResult>() {
        override val outputType = OutputType.BYTE_ARRAY
        override fun decode(bytes: List<Int>) =
            CommandResult.ByteArrayResult(bytes.map { it.toByte() }.toByteArray())
    }

    // ASCII text (calibration identifiers, free-form text)
    @Serializable @SerialName("Ascii")
    object Ascii : Codec<CommandResult.StringResult>() {
        override val outputType = OutputType.STRING
        override fun decode(bytes: List<Int>) =
            CommandResult.StringResult(bytes.joinToString("") { it.toChar().toString() })
    }

    // Bytes as a space-separated hex string, e.g. "4A 2F 00"
    @Serializable @SerialName("Hex")
    object Hex : Codec<CommandResult.StringResult>() {
        override val outputType = OutputType.STRING
        override fun decode(bytes: List<Int>) =
            CommandResult.StringResult(
                bytes.joinToString(" ") { it.toString(16).padStart(2, '0').uppercase() }
            )
    }

    // Vehicle Identification Number (Mode 09 PID 02).
    // First byte after the header is the block count (always 0x01), followed by 17 ASCII VIN characters.
    @Serializable @SerialName("Vin")
    object Vin : Codec<CommandResult.StringResult>() {
        override val outputType = OutputType.STRING
        override fun decode(bytes: List<Int>) =
            CommandResult.StringResult(
                bytes.drop(1).filter { it in 0x20..0x7E }.joinToString("") { it.toChar().toString() }
            )
    }

    /**
     * Multiple named channels decoded from a single response.
     * Each channel applies its own codec starting at a given byte offset.
     *
     * Example — O2 sensor (PID 0x14):
     *   channels = [Channel("voltage", 0, Linear(0.005, 0.0, "V")),
     *               Channel("fuel_trim", 1, SignedPercent())]
     *   bytes [0x7E, 0x80] → { "voltage": 0.63 V, "fuel_trim": 0.0 % }
     */
    @Serializable @SerialName("Multi")
    data class Multi(val channels: List<Channel>) : Codec<CommandResult.MapResult>() {
        @Serializable
        data class Channel(val name: String, val startByte: Int, val codec: Codec<CommandResult>)

        override val outputType = OutputType.MAP

        override fun decode(bytes: List<Int>): CommandResult.MapResult =
            CommandResult.MapResult(
                channels.associate { ch ->
                    val slice = bytes.drop(ch.startByte)
                    ch.name to ch.codec.decode(slice)
                }
            )
    }

    // OBD-II / UDS diagnostic trouble codes — returns a list like ["P0301", "U0100"].
    // Mode 03 response format (after stripping "43"): [count, b1, b2, b1, b2, ...]
    @Serializable @SerialName("DTC")
    object DTC : Codec<CommandResult.StringListResult>() {
        override val outputType = OutputType.STRING_LIST

        override fun decode(bytes: List<Int>): CommandResult.StringListResult {
            if (bytes.isEmpty()) return CommandResult.StringListResult(emptyList())
            val count = bytes[0]
            val dtcs = bytes.drop(1)
                .chunked(2)
                .take(count)
                .filter { it.size == 2 && (it[0] != 0 || it[1] != 0) } // skip empty slots
                .map { (b1, b2) -> decodeDTCCode(b1, b2) }
            return CommandResult.StringListResult(dtcs)
        }

        // Format: system (P/C/B/U) + subclass digit + 3 hex digits
        private fun decodeDTCCode(b1: Int, b2: Int): String {
            val system = when ((b1 shr 6) and 0x03) { 0 -> 'P'; 1 -> 'C'; 2 -> 'B'; else -> 'U' }
            val sub = (b1 shr 4) and 0x03
            val d1 = (b1 and 0x0F).toString(16).uppercase()
            val d23 = b2.toString(16).padStart(2, '0').uppercase()
            return "$system$sub$d1$d23"
        }
    }

    /**
     * Linear formula: result = (rawValue * factor) + offset
     *
     * rawValue is an unsigned integer assembled from [length] bytes starting at [startByte].
     *
     * Example — RPM (PID 0x0C):
     *   startByte=0, length=2, factor=0.25, offset=0.0, unit="rpm"
     *   bytes [0x1A, 0xF8] → rawValue=6904 → 6904 * 0.25 = 1726 rpm
     *
     * Example — coolant temperature (PID 0x05):
     *   startByte=0, length=1, factor=1.0, offset=-40.0, unit="°C"
     *   byte [0x7B] → rawValue=123 → 123 - 40 = 83 °C
     */
    @Serializable @SerialName("Linear")
    data class Linear(
        val factor: Double,
        val offset: Double = 0.0,
        val unit: String,
        val startByte: Int = 0,
        val length: Int = 1
    ) : Codec<CommandResult.FloatResult>() {
        override val outputType = OutputType.FLOAT

        override fun decode(bytes: List<Int>): CommandResult.FloatResult {
            var rawValue = 0
            for (i in 0 until length) rawValue = (rawValue shl 8) or bytes[startByte + i]
            return CommandResult.FloatResult((rawValue * factor + offset).toFloat(), unit)
        }
    }

    /**
     * Single unsigned byte → 0.0–100.0 %
     * Formula: byte * 100.0 / 255.0
     * Example: calculated engine load (PID 0x04)
     */
    @Serializable @SerialName("Percent")
    data class Percent(val startByte: Int = 0) : Codec<CommandResult.FloatResult>() {
        override val outputType = OutputType.FLOAT
        override fun decode(bytes: List<Int>) =
            CommandResult.FloatResult((bytes[startByte] * 100.0 / 255.0).toFloat(), "%")
    }

    /**
     * Single signed byte → -100.0–+100.0 %
     * Formula: (byte - 128) * 100.0 / 128.0
     * Example: short-term fuel trim (PID 0x06–0x09)
     */
    @Serializable @SerialName("SignedPercent")
    data class SignedPercent(val startByte: Int = 0) : Codec<CommandResult.FloatResult>() {
        override val outputType = OutputType.FLOAT
        override fun decode(bytes: List<Int>) =
            CommandResult.FloatResult(((bytes[startByte] - 128) * 100.0 / 128.0).toFloat(), "%")
    }

    /**
     * A single byte where each bit is a named boolean flag.
     * [bits] is a map of bit index (0 = LSB) → human-readable flag name.
     *
     * Example — MIL status (PID 0x01, byte A):
     *   bits = mapOf(7 to "MIL")
     * Result: Map<String, Boolean> — flag name → whether the bit is set
     */
    @Serializable @SerialName("Bitfield")
    data class Bitfield(
        val startByte: Int = 0,
        val bits: Map<Int, String>
    ) : Codec<CommandResult.BooleanMapResult>() {
        override val outputType = OutputType.BOOLEAN_MAP

        override fun decode(bytes: List<Int>): CommandResult.BooleanMapResult {
            val byte = bytes[startByte]
            return CommandResult.BooleanMapResult(
                bits.entries.associate { (bitIndex, flagName) -> flagName to ((byte shr bitIndex) and 1 == 1) }
            )
        }
    }

    /**
     * A byte is looked up in [table] and returned as a human-readable string.
     *
     * Example — fuel system status (PID 0x03):
     *   table = mapOf(0x01 to "Open loop", 0x02 to "Closed loop", 0x04 to "Open loop — fault")
     */
    @Serializable @SerialName("EnumLookup")
    data class EnumLookup(
        val startByte: Int = 0,
        val table: Map<Int, String>
    ) : Codec<CommandResult.StringResult>() {
        override val outputType = OutputType.STRING

        override fun decode(bytes: List<Int>): CommandResult.StringResult {
            val key = bytes[startByte]
            return CommandResult.StringResult(
                table[key] ?: "Unknown (0x${key.toString(16).padStart(2, '0').uppercase()})"
            )
        }
    }
}