package pl.altkom.coffee.accrual.api

import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

data class NewBatchCreatedEvent(
        val batchId: BatchId,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class ResourceAddedToBatchEvent(
        val batchId: BatchId,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

class AmountInPackageUpdatedEvent(
        val batchId: BatchId,
        val amount: BigDecimal
)

data class StocktakingSavedEvent(
        val batchId: BatchId
)

class BatchFinalizedEvent(
        val batchId: BatchId
)

data class StocktakingStartedEvent(
        val batchId: BatchId,
        val amount: BigDecimal,
        val resourceType: ProductResourceType,
        val unitPrice: BigDecimal
)

data class StocktakingFinishedEvent(
        val batchId: BatchId
)