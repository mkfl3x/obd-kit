package io.github.mkfl3x.obdkit.commands.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface CommandProtocol {
    val displayName: String
}

@Serializable @SerialName("OBD")
data class OBDCommandProtocol(
    val service: OBDService,
    val subfunction: String? = null
) : CommandProtocol {
    override val displayName = "OBD-II"
}

@Serializable @SerialName("UDS")
data class UDSCommandProtocol(
    val service: UDSService,
    val did: String? = null
) : CommandProtocol {
    override val displayName = "UDS"
}

@Serializable @SerialName("AT")
data object ATCommandProtocol : CommandProtocol {
    override val displayName = "AT"
}