package io.github.mkfl3x.obdkit.commands.protocol

enum class TransportProtocol(val displayName: String) {
    ISO_TP    ("ISO-TP (ISO 15765-2)"),
    K_LINE    ("K-Line (ISO 9141-2 / KWP2000)"),
    J1850_PWM ("SAE J1850 PWM"),
    J1850_VPW ("SAE J1850 VPW"),
    DIRECT    ("Direct (ELM327 only)")
}