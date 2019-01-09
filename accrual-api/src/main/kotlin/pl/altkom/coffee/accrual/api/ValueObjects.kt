package pl.altkom.coffee.accrual.api

import org.axonframework.common.IdentifierFactory
import java.io.Serializable

data class BatchId(val identifier: String = IdentifierFactory.getInstance().generateIdentifier()) : Serializable {

    override fun toString(): String {
        return identifier
    }
}