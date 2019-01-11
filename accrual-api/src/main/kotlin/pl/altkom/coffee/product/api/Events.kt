package pl.altkom.coffee.product.api

data class ProductPreparationRegisteredEvent(
        val id: String,
        val selectedProductId: String,
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
