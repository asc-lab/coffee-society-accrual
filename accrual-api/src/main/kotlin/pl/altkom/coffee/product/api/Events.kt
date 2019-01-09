package pl.altkom.coffee.product.api

data class ProductPreparationRegisteredEvent(
        val id: String,
        val productDefId: String,
        val productReceiverId: String,
        val productExecutorId: String
)