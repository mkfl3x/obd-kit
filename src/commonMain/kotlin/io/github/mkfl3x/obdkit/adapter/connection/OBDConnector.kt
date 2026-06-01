package io.github.mkfl3x.obdkit.adapter.connection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Physical connection abstraction for an ELM327-compatible adapter.
// Implementations can be Bluetooth Classic, BLE, Wi-Fi, USB — the interface is the same for all.
interface OBDConnector {

    // Current connection state — StateFlow for reactive observation in UI
    val state: StateFlow<ConnectionState>

    // Start device discovery and return a flow of found devices; cancelled with the coroutine
    fun scan(): Flow<DiscoveredDevice>

    // Establish a connection to the device at the given address (MAC or IP)
    suspend fun connect(address: String)

    // Close the connection
    suspend fun disconnect()

    // Send a command string and return the raw adapter response (including \r and >)
    suspend fun sendCommand(command: String): String
}