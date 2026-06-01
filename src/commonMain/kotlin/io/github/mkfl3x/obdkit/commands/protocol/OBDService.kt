package io.github.mkfl3x.obdkit.commands.protocol

enum class OBDService(val id: Int, val displayName: String) {
    CURRENT_DATA          (0x01, "Show Current Data"),
    FREEZE_FRAME          (0x02, "Show Freeze Frame Data"),
    STORED_DTCS           (0x03, "Request Stored DTCs"),
    CLEAR_DTCS            (0x04, "Clear Diagnostic Information"),
    OXYGEN_SENSOR_TESTS   (0x05, "Oxygen Sensor Test Results"),
    ON_BOARD_MONITORING   (0x06, "On-Board Monitoring Test Results"),
    PENDING_DTCS          (0x07, "Request Pending DTCs"),
    ON_BOARD_CONTROL      (0x08, "Control On-Board Component"),
    VEHICLE_INFO          (0x09, "Request Vehicle Information"),
    PERMANENT_DTCS        (0x0A, "Request Permanent DTCs")
}