package pl.altkom.coffee.accrual.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import java.math.BigDecimal

data class CreateNewBatchCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val previousBatchId: BatchId?,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class AddPackageToBatchCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class UpdateAmountInPackageCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val resourceType: ProductResourceType,
        val amount: BigDecimal
)

data class SaveStocktakingCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val amount: BigDecimal
)

data class FinalizeBatchCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val nextBatchId: BatchId
)

data class AddTaxCommand(
        @TargetAggregateIdentifier
        val taxId: String,
        val memberId: String,
        val productId: String,
        val productDefId: String,
        val taxAmount: BigDecimal)

data class AddShareCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val memberId: String,
        val productId: String,
        val quantity: Int
)

data class CancelTaxCommand(
        @TargetAggregateIdentifier
        val taxId: String
)

data class TransferTaxCommand(
        @TargetAggregateIdentifier
        val taxId: String,
        val toMemberId: String
)
