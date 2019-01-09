package pl.altkom.coffee.accounting.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import pl.altkom.coffee.accounting.api.Money
import pl.altkom.coffee.accounting.api.OperationId


data class SaveLiabilityCommand(
        @TargetAggregateIdentifier
        val memberId: String,
        val operationId: OperationId,
        val amount: Money
)