package pl.altkom.coffee.product.api

data class ProductPreparationRegisteredEvent(
        val id: String,
        //TODO khelman - split it into two event. One with selectedProductId and one without
        val selectedProductId: String?,
        val productDefId: String,
        val productReceiverId: String,
        val productExecutorId: String
)

data class ProductPreparationCancelledEvent(
        val id: String,
        val productDefId: String
)

data class ProductReceiverChangedEvent(
        val id: String,
        val productDefId: String,
        val productReceiverNewId: String
)
