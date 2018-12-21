package pl.altkom.coffee.accrual.api

import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

data class NewBatchCreatedEvent(
        val id: String,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal,
        val previousBatchId: String?
)

data class ResourceAddedToBatchEvent(val amount: BigDecimal, val unitPrice: BigDecimal)

class AmountInPackageUpdatedEvent(val amount: BigDecimal)

data class StocktakingSavedEvent(val amount: BigDecimal)

data class BatchFinalizedEvent(val batchId: String, val nextBatchId: String)