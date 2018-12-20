package pl.altkom.coffee.accrual.api

import org.axonframework.common.IdentifierFactory
import java.io.Serializable

data class BatchId(val identifier: String = IdentifierFactory.getInstance().generateIdentifier()) : Serializable {

    companion object {
        private const val serialVersionUID = -5267104328616955617L
    }

}