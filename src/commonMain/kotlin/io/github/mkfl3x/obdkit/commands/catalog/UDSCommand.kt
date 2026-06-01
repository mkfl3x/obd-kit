package io.github.mkfl3x.obdkit.commands.catalog

import io.github.mkfl3x.obdkit.commands.Command
import io.github.mkfl3x.obdkit.commands.brand.Brand
import io.github.mkfl3x.obdkit.commands.brand.CommandBrand
import io.github.mkfl3x.obdkit.commands.codec.Codec
import io.github.mkfl3x.obdkit.commands.protocol.UDSCommandProtocol
import io.github.mkfl3x.obdkit.commands.protocol.UDSService

// UDS (Unified Diagnostic Services) commands defined in ISO 14229-1.
//
// Unlike OBD-II (SAE J1979), UDS is NOT universally mandatory. Its adoption varies:
//   - Euro 5/6 vehicles (WWH-OBD, UN ECE R.83/R.49) must support UDS for
//     emissions-related diagnostics via CAN (ISO 15765-4).
//   - Outside emissions scope, each OEM decides independently which services
//     and DIDs their ECUs implement.
//
// This catalog contains only standard ISO 14229-1 services intended for
// read-only diagnostics. Write, flash and programming services (0x2E, 0x34–0x37)
// are intentionally excluded — they are destructive and manufacturer-specific.
//
// Standard ECU identification DIDs (0xF1xx) are defined in ISO 14229-1 Annex C.
// Support for any given DID must be probed at runtime — there is no bitmask
// equivalent to OBD-II's SupportedPids for UDS.

sealed class UDSCommand : Command {
    final override val branded = CommandBrand(Brand.UNIVERSAL)

    // ── Diagnostic session control (0x10) ────────────────────────────────────

    object DefaultSession : UDSCommand() {
        override val label = "Default Diagnostic Session"
        override val description = "Open the UDS default session (subFunction 0x01); resets any active extended or programming session"
        override val code = "1001"
        override val protocol = UDSCommandProtocol(UDSService.DIAGNOSTIC_SESSION_CONTROL)
        override val codec = Codec.Raw
    }

    object ExtendedSession : UDSCommand() {
        override val label = "Extended Diagnostic Session"
        override val description = "Open the extended session (subFunction 0x03) to unlock ReadDataByIdentifier, RoutineControl and similar services"
        override val code = "1003"
        override val protocol = UDSCommandProtocol(UDSService.DIAGNOSTIC_SESSION_CONTROL)
        override val codec = Codec.Raw
    }

    // ── ECU reset (0x11) ─────────────────────────────────────────────────────

    object HardReset : UDSCommand() {
        override val label = "ECU Hard Reset"
        override val description = "Full hardware reset of the ECU (subFunction 0x01); equivalent to power cycling the control unit"
        override val code = "1101"
        override val protocol = UDSCommandProtocol(UDSService.ECU_RESET)
        override val codec = Codec.Raw
    }

    object KeyOffOnReset : UDSCommand() {
        override val label = "ECU Key-Off/On Reset"
        override val description = "Simulate a key-off/key-on cycle (subFunction 0x02); causes the ECU to reinitialise as on a normal ignition cycle"
        override val code = "1102"
        override val protocol = UDSCommandProtocol(UDSService.ECU_RESET)
        override val codec = Codec.Raw
    }

    object SoftReset : UDSCommand() {
        override val label = "ECU Soft Reset"
        override val description = "Software-only restart of ECU application (subFunction 0x03); faster than hard reset, preserves some volatile state"
        override val code = "1103"
        override val protocol = UDSCommandProtocol(UDSService.ECU_RESET)
        override val codec = Codec.Raw
    }

    // ── Clear diagnostic information (0x14) ──────────────────────────────────

    object ClearAllDtcs : UDSCommand() {
        override val label = "Clear All DTCs"
        override val description = "Clear all stored UDS DTCs and freeze frame data; groupOfDTC = 0xFFFFFF (all supported groups)"
        override val code = "14FFFFFF"
        override val protocol = UDSCommandProtocol(UDSService.CLEAR_DIAGNOSTIC_INFO)
        override val codec = Codec.Raw
    }

    // ── Read DTC information (0x19) ──────────────────────────────────────────

    /** mask: DTC status bitmask (0xFF = all statuses; bit 0 = testFailed, bit 3 = confirmedDTC, etc.) */
    data class ReadDtcByStatusMask(val mask: Int = 0xFF) : UDSCommand() {
        override val label = "Read DTC by Status Mask"
        override val description = "Return all DTCs whose status byte matches the given mask (subFunction 0x02); 0xFF returns all stored DTCs"
        override val code = "1902${mask.toString(16).padStart(2, '0').uppercase()}"
        override val protocol = UDSCommandProtocol(UDSService.READ_DTC_INFO)
        override val codec = Codec.Hex
    }

    object ReadSupportedDtcs : UDSCommand() {
        override val label = "Read Supported DTCs"
        override val description = "Return the list of all DTC codes this ECU is capable of detecting (subFunction 0x0A)"
        override val code = "190A"
        override val protocol = UDSCommandProtocol(UDSService.READ_DTC_INFO)
        override val codec = Codec.Hex
    }

    object ReadDtcCount : UDSCommand() {
        override val label = "Read DTC Count by Status Mask"
        override val description = "Return only the count of DTCs matching the given status mask 0xFF, not the full list (subFunction 0x01)"
        override val code = "1901FF"
        override val protocol = UDSCommandProtocol(UDSService.READ_DTC_INFO)
        override val codec = Codec.Hex
    }

    object ReadDtcSnapshotIdentification : UDSCommand() {
        override val label = "Read DTC Snapshot Identification"
        override val description = "Return list of all DTC numbers and their associated snapshot record numbers (freeze frame index) (subFunction 0x03)"
        override val code = "1903"
        override val protocol = UDSCommandProtocol(UDSService.READ_DTC_INFO)
        override val codec = Codec.Hex
    }

    object ReadDtcExtDataAll : UDSCommand() {
        override val label = "Read DTC Extended Data — All"
        override val description = "Return extended data records for all stored DTCs (subFunction 0x06, extDataRecordNumber 0xFF)"
        override val code = "1906FFFFFF"
        override val protocol = UDSCommandProtocol(UDSService.READ_DTC_INFO)
        override val codec = Codec.Hex
    }

    // ── Read data by identifier (0x22) ───────────────────────────────────────
    // Standard DIDs from ISO 14229-1 Annex C (0xF1xx — ECU identification data)

    object ReadVin : UDSCommand() {
        override val label = "VIN (UDS)"
        override val description = "17-character Vehicle Identification Number stored in the ECU (ISO 14229 DID 0xF190)"
        override val code = "22F190"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F190")
        override val codec = Codec.Ascii
    }

    object ReadBootSoftwareId : UDSCommand() {
        override val label = "Boot Software Identification"
        override val description = "Boot loader software version or part number string (ISO 14229 DID 0xF180)"
        override val code = "22F180"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F180")
        override val codec = Codec.Ascii
    }

    object ReadApplicationSoftwareId : UDSCommand() {
        override val label = "Application Software Identification"
        override val description = "Application software version or part number string (ISO 14229 DID 0xF181)"
        override val code = "22F181"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F181")
        override val codec = Codec.Ascii
    }

    object ReadApplicationDataId : UDSCommand() {
        override val label = "Application Data Identification"
        override val description = "Application data (calibration) version or part number string (ISO 14229 DID 0xF182)"
        override val code = "22F182"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F182")
        override val codec = Codec.Ascii
    }

    object ReadSparePartNumber : UDSCommand() {
        override val label = "Spare Part Number"
        override val description = "ECU spare/service part number used for replacement ordering (ISO 14229 DID 0xF187)"
        override val code = "22F187"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F187")
        override val codec = Codec.Ascii
    }

    object ReadApplicationSoftwareVersion : UDSCommand() {
        override val label = "ECU Application Software Version"
        override val description = "ECU application software version string as defined by the ECU supplier (ISO 14229 DID 0xF189)"
        override val code = "22F189"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F189")
        override val codec = Codec.Ascii
    }

    object ReadManufacturingDate : UDSCommand() {
        override val label = "ECU Manufacturing Date"
        override val description = "Date the ECU was manufactured, BCD-encoded as YYYYMMDD (ISO 14229 DID 0xF18B)"
        override val code = "22F18B"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F18B")
        override val codec = Codec.Hex
    }

    object ReadEcuSerialNumber : UDSCommand() {
        override val label = "ECU Serial Number"
        override val description = "Unique serial number assigned to this ECU unit at manufacturing (ISO 14229 DID 0xF18C)"
        override val code = "22F18C"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F18C")
        override val codec = Codec.Ascii
    }

    object ReadEcuHardwareVersion : UDSCommand() {
        override val label = "ECU Hardware Version"
        override val description = "ECU hardware revision or PCB version string (ISO 14229 DID 0xF191)"
        override val code = "22F191"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F191")
        override val codec = Codec.Ascii
    }

    object ReadActiveSession : UDSCommand() {
        override val label = "Active Diagnostic Session"
        override val description = "Returns the currently active diagnostic session type (ISO 14229 DID 0xF186); 0x01=default, 0x02=programming, 0x03=extended"
        override val code = "22F186"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F186")
        override val codec = Codec.Hex
    }

    object ReadSystemSupplierIdentifier : UDSCommand() {
        override val label = "System Supplier Identifier"
        override val description = "Tier-1 ECU supplier identifier string (ISO 14229 DID 0xF18A)"
        override val code = "22F18A"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F18A")
        override val codec = Codec.Ascii
    }

    object ReadSystemSupplierSoftwareVersion : UDSCommand() {
        override val label = "System Supplier ECU Software Version"
        override val description = "Software version string as defined by the ECU tier-1 supplier (ISO 14229 DID 0xF195)"
        override val code = "22F195"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F195")
        override val codec = Codec.Ascii
    }

    object ReadSystemNameEngineType : UDSCommand() {
        override val label = "System Name / Engine Type"
        override val description = "Vehicle system name or engine type identifier string (ISO 14229 DID 0xF197)"
        override val code = "22F197"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F197")
        override val codec = Codec.Ascii
    }

    object ReadRepairShopCode : UDSCommand() {
        override val label = "Repair Shop Code"
        override val description = "Code identifying the repair shop or tester that last serviced this ECU (ISO 14229 DID 0xF198)"
        override val code = "22F198"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F198")
        override val codec = Codec.Ascii
    }

    object ReadProgrammingDate : UDSCommand() {
        override val label = "ECU Programming Date"
        override val description = "Date when the ECU was last reprogrammed, BCD YYYYMMDD format (ISO 14229 DID 0xF199)"
        override val code = "22F199"
        override val protocol = UDSCommandProtocol(UDSService.READ_DATA_BY_IDENTIFIER, "F199")
        override val codec = Codec.Hex
    }

    // ── Session keep-alive (0x3E) ────────────────────────────────────────────

    object TesterPresent : UDSCommand() {
        override val label = "Tester Present"
        override val description = "Keep-alive ping sent every S3 interval to prevent the ECU from dropping the active diagnostic session"
        override val code = "3E00"
        override val protocol = UDSCommandProtocol(UDSService.TESTER_PRESENT)
        override val codec = Codec.Raw
    }
}