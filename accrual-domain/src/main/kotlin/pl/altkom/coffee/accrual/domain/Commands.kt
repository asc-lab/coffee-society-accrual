package pl.altkom.coffee.accrual.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

data class CreateNewBatchCommand(
        val batchId: String,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal,
        val previousBatchId: String
)

data class AddPackageToBatchCommand(
        @TargetAggregateIdentifier
        val batchId: String,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class UpdateAmountInPackageCommand(
        @TargetAggregateIdentifier
        val batchId: String,
        val resourceType: ProductResourceType,
        val amount: BigDecimal
)

data class SaveStocktakingCommand(
        @TargetAggregateIdentifier
        val batchId: String,
        val amount: BigDecimal
)

data class FinalizeBatchCommand(
        @TargetAggregateIdentifier
        val batchId: String,
        val nextBatchId : String
)