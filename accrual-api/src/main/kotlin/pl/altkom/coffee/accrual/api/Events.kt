package pl.altkom.coffee.accrual.api

import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import java.math.BigDecimal

data class NewBatchCreatedEvent(
        val batchId: BatchId,
        val previousBatchId: BatchId?,
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
        val batchId: BatchId,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class BatchFinalizedEvent(
        val batchId: BatchId,
        val nextBatchId: BatchId
)

data class TaxAddedEvent(
        val taxId: String,
        val memberId: String,
        val productId: String,
        val productDefId: String,
        val taxAmount: BigDecimal
)

data class TaxCanceledEvent(
        val taxId: String,
        val memberId: String,
        val taxAmount: BigDecimal
)

data class TaxTransferedEvent(
        val taxId: String,
        val fromMemberId: String,
        val toMemberId: String,
        val taxAmount: BigDecimal
)

