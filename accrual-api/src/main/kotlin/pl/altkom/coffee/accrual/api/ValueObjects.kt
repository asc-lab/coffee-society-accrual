package pl.altkom.coffee.accrual.api

import org.axonframework.common.IdentifierFactory
import java.io.Serializable
import java.math.BigDecimal

data class BatchId(val identifier: String = IdentifierFactory.getInstance().generateIdentifier()) : Serializable {

    override fun toString(): String {
        return identifier
    }
}

class Share {
    var customerId: String = ""
    var quantity: Int = 0
    var productId: String = ""

    constructor()

    constructor(customerId: String,
                quantity: Int,
                productId: String) {
        this.quantity = quantity
        this.customerId = customerId
        this.productId = productId

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Share

        if (customerId != other.customerId) return false
        if (quantity != other.quantity) return false
        if (productId != other.productId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = customerId.hashCode()
        result = 31 * result + quantity
        result = 31 * result + productId.hashCode()
        return result
    }


}

data class Resource(var amount: BigDecimal, val unitPrice: BigDecimal)
