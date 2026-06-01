package io.github.mkfl3x.obdkit.adapter

// ELM327 adapter information read at connection time
data class AdapterInfo(
    val firmware: String,          // response to ATI, e.g. "ELM327 v2.1"
    val deviceDescription: String, // response to AT@1, e.g. "OBDII to RS232 Interpreter"
    val protocolNumber: String     // response to ATDPN; may have an "A" prefix (auto-detected)
) {
    // Resolved protocol derived from protocolNumber using the ELM327 datasheet table
    val protocol: AdapterProtocol = when (protocolNumber.removePrefix("A")) {
        "1"      -> AdapterProtocol.J1850PWM
        "2"      -> AdapterProtocol.J1850VPW
        "3"      -> AdapterProtocol.ISO9141
        "4"      -> AdapterProtocol.KWP5Baud
        "5"      -> AdapterProtocol.KWPFast
        "6"      -> AdapterProtocol.Can(bitId = 11, baudRate = 500) // ISO 15765-4 11-bit 500k
        "7"      -> AdapterProtocol.Can(bitId = 29, baudRate = 500) // ISO 15765-4 29-bit 500k
        "8"      -> AdapterProtocol.Can(bitId = 11, baudRate = 250) // ISO 15765-4 11-bit 250k
        "9"      -> AdapterProtocol.Can(bitId = 29, baudRate = 250) // ISO 15765-4 29-bit 250k
        "A"      -> AdapterProtocol.Can(bitId = 29, baudRate = 250) // SAE J1939
        "B"      -> AdapterProtocol.Can(bitId = 11, baudRate = 125) // USER1 CAN — configurable via ATPB/ATCP, default 125 kbaud
        "C"      -> AdapterProtocol.Can(bitId = 11, baudRate = 50)  // USER2 CAN — configurable via ATPB/ATCP, default 50 kbaud
        else     -> AdapterProtocol.Unknown
    }
}