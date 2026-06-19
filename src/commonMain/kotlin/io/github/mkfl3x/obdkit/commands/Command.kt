package io.github.mkfl3x.obdkit.commands

import io.github.mkfl3x.obdkit.commands.codec.Codec
import io.github.mkfl3x.obdkit.commands.protocol.ATCommandProtocol
import io.github.mkfl3x.obdkit.commands.protocol.CommandProtocol

// Diagnostic command model — OBD-II, UDS, or AT.
abstract class Command<T : CommandResult> {

    abstract val label: String        // short name for UI: "Engine RPM"
    abstract val description: String  // detailed description for an AI agent: "Reads current engine speed in RPM via OBD-II Mode 01 PID 0x0C"
    abstract val code: String         // hex string sent to the adapter: "010C", "ATZ", "2201F190"
    abstract val protocol: CommandProtocol
    abstract val codec: Codec<T>
    abstract val branded: CommandBrand

    // Decodes a raw ELM327 response into a typed result using the command's codec.
    // The response prefix is derived automatically from code and protocol.
    fun decode(raw: String): T {
        val cleaned = raw.replace("\r", "").replace("\n", "").replace(">", "").trim()
        val bytes = when (protocol) {
            // AT responses are plain ASCII text, not hex; pass char codes directly
            is ATCommandProtocol -> cleaned.map { it.code }
            // OBD-II and UDS: response service byte = request service byte + 0x40
            else -> extractPayloadBytes(cleaned, deriveResponsePrefix(code))
        }
        if (bytes.isEmpty()) throw IllegalStateException("No data for command '$code'")
        return codec.decode(bytes)
    }

    // Derives the expected response prefix from the command code.
    // OBD-II "010C" → response service 0x01+0x40=0x41 → prefix "410C"
    // UDS "2201F1" → response service 0x22+0x40=0x62 → prefix "6201F1"
    private fun deriveResponsePrefix(code: String): String {
        val clean = code.replace(" ", "").uppercase()
        val serviceByte = clean.take(2).toIntOrNull(16) ?: return clean
        val responseService = (serviceByte + 0x40).toString(16).padStart(2, '0').uppercase()
        return responseService + clean.drop(2)
    }

    // Strips ELM327 control characters and the response header, returning only the data bytes.
    // "410C1AF8\r>" with prefix "410C" → [0x1A, 0xF8]
    private fun extractPayloadBytes(raw: String, responsePrefix: String): List<Int> {
        val cleaned = raw.uppercase()
            .replace(Regex("\\d+:"), "")     // strip frame sequence numbers from multi-frame responses
            .replace(Regex("[^0-9A-F]"), "") // keep hex characters only
        val prefix = responsePrefix.replace(" ", "").uppercase()
        val idx = cleaned.indexOf(prefix)
        if (idx == -1) return emptyList()
        return cleaned.substring(idx + prefix.length)
            .chunked(2)
            .mapNotNull { it.toIntOrNull(16) }
    }

    // Command ownership: which manufacturer it belongs to and whether it is proprietary
    data class CommandBrand(
        val brand: String = "UNIVERSAL",
        val proprietary: Boolean = false
    )
}