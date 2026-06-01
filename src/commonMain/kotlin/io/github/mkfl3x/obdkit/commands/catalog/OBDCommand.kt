package io.github.mkfl3x.obdkit.commands.catalog

import io.github.mkfl3x.obdkit.commands.Command
import io.github.mkfl3x.obdkit.commands.brand.Brand
import io.github.mkfl3x.obdkit.commands.brand.CommandBrand
import io.github.mkfl3x.obdkit.commands.codec.Codec
import io.github.mkfl3x.obdkit.commands.protocol.OBDCommandProtocol
import io.github.mkfl3x.obdkit.commands.protocol.OBDService

// OBD-II commands mandated by SAE J1979 / ISO 15031-5.
//
// All passenger vehicles sold in the USA (MY 1996+) and Europe (MY 2001+ EOBD) must
// support these PIDs and modes. Manufacturers are only required to report a PID when
// the corresponding hardware/system is present on the vehicle (e.g. a diesel with no
// MAF sensor is exempt from PID 0110). Use the SupportedPids bitmasks (0100, 0120,
// 0140, 0160) to discover which PIDs a specific ECU actually implements before
// requesting them.
//
// Manufacturer-specific enhanced PIDs (Toyota Mode 21, BMW UDS, VAG KWP, etc.) are
// NOT part of this catalog — store them on your side.

sealed class OBDCommand : Command {
    final override val branded = CommandBrand(Brand.UNIVERSAL)

    // ── Mode 01 — Current data ───────────────────────────────────────────────

    object SupportedPids : OBDCommand() {
        override val label = "Supported PIDs [01–20]"
        override val description = "Bitmask of supported Mode 01 PIDs in the range 01–20 (32 bits, 4 bytes)"
        override val code = "0100"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Hex
    }

    object MonitorStatus : OBDCommand() {
        override val label = "Monitor Status"
        override val description = "MIL (check-engine) on/off, DTC count and readiness monitor flags since last DTC clear"
        override val code = "0101"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Hex
    }

    object FuelSystemStatus : OBDCommand() {
        override val label = "Fuel System Status"
        override val description = "Fuel loop state for banks 1 and 2: open-loop, closed-loop, or fault (2-byte bitmask)"
        override val code = "0103"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Hex
    }

    object EngineLoad : OBDCommand() {
        override val label = "Calculated Engine Load"
        override val description = "Calculated engine load as a percentage of peak volumetric efficiency (0–100 %)"
        override val code = "0104"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object CoolantTemperature : OBDCommand() {
        override val label = "Engine Coolant Temperature"
        override val description = "Engine coolant temperature in °C; formula: A − 40 (range −40–215 °C)"
        override val code = "0105"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, -40.0, "°C")
    }

    object ShortTermFuelTrimBank1 : OBDCommand() {
        override val label = "Short Term Fuel Trim — Bank 1"
        override val description = "Short-term closed-loop fuel correction for bank 1; formula: (A − 128) × 100/128 (−100 to +99.2 %)"
        override val code = "0106"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.SignedPercent()
    }

    object LongTermFuelTrimBank1 : OBDCommand() {
        override val label = "Long Term Fuel Trim — Bank 1"
        override val description = "Long-term adaptive fuel correction for bank 1; formula: (A − 128) × 100/128 (−100 to +99.2 %)"
        override val code = "0107"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.SignedPercent()
    }

    object ShortTermFuelTrimBank2 : OBDCommand() {
        override val label = "Short Term Fuel Trim — Bank 2"
        override val description = "Short-term closed-loop fuel correction for bank 2; formula: (A − 128) × 100/128 (−100 to +99.2 %)"
        override val code = "0108"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.SignedPercent()
    }

    object LongTermFuelTrimBank2 : OBDCommand() {
        override val label = "Long Term Fuel Trim — Bank 2"
        override val description = "Long-term adaptive fuel correction for bank 2; formula: (A − 128) × 100/128 (−100 to +99.2 %)"
        override val code = "0109"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.SignedPercent()
    }

    object IntakeManifoldPressure : OBDCommand() {
        override val label = "Intake Manifold Absolute Pressure"
        override val description = "Absolute pressure at the intake manifold in kPa; formula: A kPa (0–255 kPa)"
        override val code = "010B"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "kPa")
    }

    object EngineRpm : OBDCommand() {
        override val label = "Engine RPM"
        override val description = "Current engine speed in RPM; formula: (A × 256 + B) / 4 (0–16 383.75 rpm)"
        override val code = "010C"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(0.25, 0.0, "rpm", 0, 2)
    }

    object VehicleSpeed : OBDCommand() {
        override val label = "Vehicle Speed"
        override val description = "Current vehicle speed in km/h; formula: A km/h (0–255 km/h)"
        override val code = "010D"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "km/h")
    }

    object TimingAdvance : OBDCommand() {
        override val label = "Timing Advance"
        override val description = "Ignition timing advance for cylinder #1 before TDC; formula: A/2 − 64 (−64 to +63.5 °)"
        override val code = "010E"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(0.5, -64.0, "°")
    }

    object IntakeAirTemperature : OBDCommand() {
        override val label = "Intake Air Temperature"
        override val description = "Temperature of air entering the intake manifold; formula: A − 40 (−40–215 °C)"
        override val code = "010F"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, -40.0, "°C")
    }

    object MafRate : OBDCommand() {
        override val label = "MAF Air Flow Rate"
        override val description = "Mass air flow rate measured by the MAF sensor; formula: (A × 256 + B) / 100 g/s (0–655.35 g/s)"
        override val code = "0110"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(0.01, 0.0, "g/s", 0, 2)
    }

    object ThrottlePosition : OBDCommand() {
        override val label = "Throttle Position"
        override val description = "Absolute throttle position sensor (TPS) value; formula: A × 100/255 (0–100 %)"
        override val code = "0111"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object OBDStandards : OBDCommand() {
        override val label = "OBD Standards"
        override val description = "OBD standard(s) this vehicle conforms to (SAE OBD-II, EOBD, JOBD, etc.) — raw enum byte"
        override val code = "011C"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Hex
    }

    object RuntimeSinceStart : OBDCommand() {
        override val label = "Run Time Since Engine Start"
        override val description = "Elapsed time since the engine was started; formula: A × 256 + B seconds (0–65 535 s)"
        override val code = "011F"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "s", 0, 2)
    }

    object DistanceWithMil : OBDCommand() {
        override val label = "Distance Traveled with MIL On"
        override val description = "Total distance driven while MIL (check-engine lamp) was illuminated; formula: A × 256 + B km"
        override val code = "0121"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "km", 0, 2)
    }

    object FuelRailPressure : OBDCommand() {
        override val label = "Fuel Rail Pressure"
        override val description = "Fuel rail pressure relative to intake manifold vacuum; formula: (A × 256 + B) × 0.079 kPa"
        override val code = "0122"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(0.079, 0.0, "kPa", 0, 2)
    }

    object FuelTankLevel : OBDCommand() {
        override val label = "Fuel Tank Level"
        override val description = "Fuel tank fill level from the fuel level sensor; formula: A × 100/255 (0–100 %)"
        override val code = "012F"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object DistanceSinceDTCClear : OBDCommand() {
        override val label = "Distance Since DTCs Cleared"
        override val description = "Total distance driven since the last DTC clear/MIL reset; formula: A × 256 + B km"
        override val code = "0131"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "km", 0, 2)
    }

    object BarometricPressure : OBDCommand() {
        override val label = "Barometric Pressure"
        override val description = "Absolute ambient barometric pressure; formula: A kPa (0–255 kPa)"
        override val code = "0133"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "kPa")
    }

    object ControlModuleVoltage : OBDCommand() {
        override val label = "Control Module Voltage"
        override val description = "Supply voltage at the ECU/PCM power pin; formula: (A × 256 + B) × 0.001 V (0–65.535 V)"
        override val code = "0142"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(0.001, 0.0, "V", 0, 2)
    }

    object RelativeThrottlePosition : OBDCommand() {
        override val label = "Relative Throttle Position"
        override val description = "Throttle position relative to the learned closed-stop; formula: A × 100/255 (0–100 %)"
        override val code = "0145"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object AmbientAirTemperature : OBDCommand() {
        override val label = "Ambient Air Temperature"
        override val description = "Outside ambient air temperature; formula: A − 40 (−40–215 °C)"
        override val code = "0146"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, -40.0, "°C")
    }

    object AcceleratorPedalPositionD : OBDCommand() {
        override val label = "Accelerator Pedal Position D"
        override val description = "Accelerator pedal position from sensor D; formula: A × 100/255 (0–100 %)"
        override val code = "0149"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object AcceleratorPedalPositionE : OBDCommand() {
        override val label = "Accelerator Pedal Position E"
        override val description = "Accelerator pedal position from sensor E; formula: A × 100/255 (0–100 %)"
        override val code = "014A"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object CommandedThrottleActuator : OBDCommand() {
        override val label = "Commanded Throttle Actuator"
        override val description = "Target throttle actuator control position commanded by the ECU; formula: A × 100/255 (0–100 %)"
        override val code = "014C"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Percent()
    }

    object TimeWithMilOn : OBDCommand() {
        override val label = "Time Run with MIL On"
        override val description = "Total engine-on time accumulated while MIL was illuminated; formula: A × 256 + B min"
        override val code = "014D"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "min", 0, 2)
    }

    object TimeSinceDTCClear : OBDCommand() {
        override val label = "Time Since DTCs Cleared"
        override val description = "Engine-on minutes elapsed since the last DTC clear; formula: A × 256 + B min (0–65 535 min)"
        override val code = "014E"
        override val protocol = OBDCommandProtocol(OBDService.CURRENT_DATA)
        override val codec = Codec.Linear(1.0, 0.0, "min", 0, 2)
    }

    // ── Mode 03 — Stored DTCs ────────────────────────────────────────────────

    object StoredDTCs : OBDCommand() {
        override val label = "Stored DTCs"
        override val description = "List of currently stored (confirmed) diagnostic trouble codes (SAE J1979 Mode 03)"
        override val code = "03"
        override val protocol = OBDCommandProtocol(OBDService.STORED_DTCS)
        override val codec = Codec.DTC
    }

    // ── Mode 04 — Clear DTCs ─────────────────────────────────────────────────

    object ClearDTCs : OBDCommand() {
        override val label = "Clear DTCs"
        override val description = "Clear all stored and pending DTCs and reset MIL, freeze frame data and readiness monitors"
        override val code = "04"
        override val protocol = OBDCommandProtocol(OBDService.CLEAR_DTCS)
        override val codec = Codec.Raw
    }

    // ── Mode 07 — Pending DTCs ───────────────────────────────────────────────

    object PendingDTCs : OBDCommand() {
        override val label = "Pending DTCs"
        override val description = "DTCs detected in the current or last completed drive cycle but not yet confirmed (Mode 07)"
        override val code = "07"
        override val protocol = OBDCommandProtocol(OBDService.PENDING_DTCS)
        override val codec = Codec.DTC
    }

    // ── Mode 09 — Vehicle information ────────────────────────────────────────

    object Vin : OBDCommand() {
        override val label = "Vehicle Identification Number"
        override val description = "17-character VIN stored in the ECU (ISO 3779); Mode 09 PID 02, 1 count byte + 17 ASCII chars"
        override val code = "0902"
        override val protocol = OBDCommandProtocol(OBDService.VEHICLE_INFO)
        override val codec = Codec.Vin
    }

    object CalibrationId : OBDCommand() {
        override val label = "Calibration ID"
        override val description = "ECU calibration/tune identifier, up to 16 ASCII chars; Mode 09 PID 04, 1 count byte + ASCII"
        override val code = "0904"
        override val protocol = OBDCommandProtocol(OBDService.VEHICLE_INFO)
        override val codec = Codec.Vin
    }

    object EcuName : OBDCommand() {
        override val label = "ECU Name"
        override val description = "ECU module name, up to 20 ASCII chars; Mode 09 PID 0A, 1 count byte + ASCII"
        override val code = "090A"
        override val protocol = OBDCommandProtocol(OBDService.VEHICLE_INFO)
        override val codec = Codec.Vin
    }

    // ── Mode 0A — Permanent DTCs ─────────────────────────────────────────────

    object PermanentDTCs : OBDCommand() {
        override val label = "Permanent DTCs"
        override val description = "DTCs that survive Mode 04 clear and can only be erased after the monitor has run and passed"
        override val code = "0A"
        override val protocol = OBDCommandProtocol(OBDService.PERMANENT_DTCS)
        override val codec = Codec.DTC
    }
}
