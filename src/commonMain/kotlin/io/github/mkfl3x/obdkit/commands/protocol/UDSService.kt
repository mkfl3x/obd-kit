package io.github.mkfl3x.obdkit.commands.protocol

enum class UDSService(val id: Int, val displayName: String) {
    DIAGNOSTIC_SESSION_CONTROL (0x10, "Diagnostic Session Control"),
    ECU_RESET                  (0x11, "ECU Reset"),
    CLEAR_DIAGNOSTIC_INFO      (0x14, "Clear Diagnostic Information"),
    READ_DTC_INFO              (0x19, "Read DTC Information"),
    READ_DATA_BY_IDENTIFIER    (0x22, "Read Data By Identifier"),
    READ_MEMORY_BY_ADDRESS     (0x23, "Read Memory By Address"),
    SECURITY_ACCESS            (0x27, "Security Access"),
    COMMUNICATION_CONTROL      (0x28, "Communication Control"),
    WRITE_DATA_BY_IDENTIFIER   (0x2E, "Write Data By Identifier"),
    IO_CONTROL                 (0x2F, "Input Output Control By Identifier"),
    ROUTINE_CONTROL            (0x31, "Routine Control"),
    REQUEST_DOWNLOAD           (0x34, "Request Download"),
    REQUEST_UPLOAD             (0x35, "Request Upload"),
    TRANSFER_DATA              (0x36, "Transfer Data"),
    REQUEST_TRANSFER_EXIT      (0x37, "Request Transfer Exit"),
    TESTER_PRESENT             (0x3E, "Tester Present")
}