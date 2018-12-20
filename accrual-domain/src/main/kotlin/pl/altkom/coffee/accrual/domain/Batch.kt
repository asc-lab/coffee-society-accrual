package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import pl.altkom.coffee.accrual.api.*
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

@Aggregate
class Batch {

    @AggregateIdentifier
    lateinit var id: String
    lateinit var resourceType: ProductResourceType
    val shares: MutableList<Share> = mutableListOf()
    val resources: MutableList<Resource> = mutableListOf()
    private lateinit var status: BatchStatus

    constructor()

    @CommandHandler
    constructor(command: CreateNewBatchCommand) {
        with(command) {
            AggregateLifecycle.apply(NewBatchCreatedEvent(batchId, resourceType, amount, unitPrice))
        }
    }

    @EventSourcingHandler
    fun handle(event: NewBatchCreatedEvent) {
        this.id = event.id
        this.status = BatchStatus.RUNNING
        this.resourceType = event.resourceType

        this.resources.add(Resource(event.amount, event.unitPrice))
    }

    @CommandHandler
    fun on(command: AddPackageToBatchCommand) {
        if (command.resourceType != this.resourceType)
            throw IllegalResourceTypeException()

        with(command) {
            AggregateLifecycle.apply(ResourceAddedToBatchEvent(amount, unitPrice))
        }
    }

    @EventSourcingHandler
    fun handle(event: ResourceAddedToBatchEvent) {
        this.resources.add(Resource(event.amount, event.unitPrice))
    }

    @CommandHandler
    fun on(command: UpdateAmountInPackageCommand) {
        if (command.resourceType != this.resourceType)
            throw IllegalResourceTypeException()

        with(command) {
            AggregateLifecycle.apply(AmountInPackageUpdatedEvent(amount))
        }
    }

    @EventSourcingHandler
    fun handle(event: AmountInPackageUpdatedEvent) {
        this.resources.last().amount = event.amount
    }

    @CommandHandler
    fun on(command: SaveStocktakingCommand) {
        if (isFinalized())
            throw BatchAlreadyFinalizedException()

        with(command) {
            AggregateLifecycle.apply(StocktakingSavedEvent(amount))
        }
    }

    @EventSourcingHandler
    fun handle(event: StocktakingSavedEvent) {
        this.resources.last().amount = this.resources.last().amount.minus(event.amount)
    }

    @CommandHandler
    fun on(command: FinalizeBatchCommand) {
        if (isFinalized())
            throw BatchAlreadyFinalizedException()

        AggregateLifecycle.apply(BatchFinalizedEvent())
    }

    @EventSourcingHandler
    fun handle(event: BatchFinalizedEvent) {
        this.status = BatchStatus.FINALIZED
    }

    private fun isFinalized(): Boolean {
        return BatchStatus.FINALIZED == this.status
    }
}

data class Share internal constructor(val customerId: String, val quantity: BigDecimal, val productId: String)

data class Resource internal constructor(var amount: BigDecimal, val unitPrice: BigDecimal)

private enum class BatchStatus {
    RUNNING, FINALIZED
}