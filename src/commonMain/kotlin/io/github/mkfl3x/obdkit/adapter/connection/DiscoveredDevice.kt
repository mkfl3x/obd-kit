package io.github.mkfl3x.obdkit.adapter.connection

// Device found during a scan (Bluetooth, Wi-Fi, etc.)
data class DiscoveredDevice(
    val name: String,
    val address: String // MAC address or IP depending on transport
)