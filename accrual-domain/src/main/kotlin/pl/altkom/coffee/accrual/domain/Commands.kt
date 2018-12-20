package pl.altkom.coffee.accrual.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

data class CreateNewBatchCommand(
        val batchId: BatchId,
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
        val batchId: BatchId
)

data class StartStocktakingCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class FinishStocktakingCommand(
        @TargetAggregateIdentifier
        val batchId: BatchId
)