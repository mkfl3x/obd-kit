package io.github.mkfl3x.obdkit.commands.brand

// Command ownership: which manufacturer it belongs to and whether it is proprietary
data class CommandBrand(
    val brand: Brand = Brand.UNIVERSAL,
    val proprietary: Boolean = false  // true = undocumented or reverse-engineered command
)

enum class Brand {
    UNIVERSAL,   // SAE J1979 / ISO 14229 standard — works on any vehicle
    BMW,
    VAG,         // VW, Audi, Skoda, SEAT
    MERCEDES,
    TOYOTA,
    FORD,
    GM,
    HONDA,
    HYUNDAI,
    KIA,
    VOLVO,
    STELLANTIS   // Fiat, Peugeot, Citroën, Opel, Chrysler
}