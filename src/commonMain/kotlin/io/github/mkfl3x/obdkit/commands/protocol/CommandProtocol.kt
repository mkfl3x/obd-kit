package io.github.mkfl3x.obdkit.commands.protocol

sealed interface CommandProtocol {
    val displayName: String
}

data class OBDCommandProtocol(
    val service: OBDService,
    val subfunction: String? = null
) : CommandProtocol {
    override val displayName = "OBD-II"
}

data class UDSCommandProtocol(
    val service: UDSService,
    val did: String? = null
) : CommandProtocol {
    override val displayName = "UDS"
}

data object ATCommandProtocol : CommandProtocol {
    override val displayName = "AT"
}