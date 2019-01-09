package pl.altkom.coffee.accounting.api

data class AssetAddedEvent(
        val memberId: String,
        val operationId: OperationId,
        val balance: Money,
        val amount: Money
)

data class LiabilityAddedEvent(
        val memberId: String,
        val operationId: OperationId,
        val balance: Money,
        val amount: Money
)
