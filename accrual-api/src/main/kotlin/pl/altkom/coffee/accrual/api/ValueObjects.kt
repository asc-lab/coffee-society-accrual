package pl.altkom.coffee.accrual.api

import org.axonframework.common.IdentifierFactory
import java.io.Serializable
import java.math.BigDecimal

data class BatchId(val identifier: String = IdentifierFactory.getInstance().generateIdentifier()) : Serializable {

    override fun toString(): String {
        return identifier
    }
}

data class Share(val customerId: String, val quantity: Int, val productId: String)

data class Resource(var amount: BigDecimal, val unitPrice: BigDecimal)
