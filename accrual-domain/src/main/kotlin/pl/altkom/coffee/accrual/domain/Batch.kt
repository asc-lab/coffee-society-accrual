package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import pl.altkom.coffee.accrual.api.*
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

@Aggregate
class Batch {

    @AggregateIdentifier
    lateinit var batchId: BatchId
    var previousBatchId: BatchId? = null
    lateinit var resourceType: ProductResourceType
    val shares: MutableList<Share> = mutableListOf()
    val resources: MutableList<Resource> = mutableListOf()
    lateinit var status: BatchStatus

    constructor()

    @CommandHandler
    constructor(command: CreateNewBatchCommand) {
        with(command) {
            apply(NewBatchCreatedEvent(batchId, previousBatchId, resourceType, amount, unitPrice))
        }
    }

    @CommandHandler
    fun on(command: AddPackageToBatchCommand) {
        if (command.resourceType != resourceType)
            throw IllegalResourceTypeException()

        with(command) {
            apply(ResourceAddedToBatchEvent(batchId, amount, unitPrice))
        }
    }

    @CommandHandler
    fun on(command: UpdateAmountInPackageCommand) {
        if (command.resourceType != resourceType)
            throw IllegalResourceTypeException()

        with(command) {
            apply(AmountInPackageUpdatedEvent(batchId, amount))
        }
    }

    @CommandHandler
    fun on(command: SaveStocktakingCommand) {
        if (isFinalized())
            throw BatchAlreadyFinalizedException()

        with(command) {
            apply(StocktakingSavedEvent(batchId, resourceType, amount, resources.last().unitPrice))
        }
    }

    @CommandHandler
    fun on(command: FinalizeBatchCommand) {
        if (isFinalized())
            throw BatchAlreadyFinalizedException()

        with(command) {
            apply(BatchFinalizedEvent(batchId, command.nextBatchId))
        }
    }

    @EventSourcingHandler
    fun handle(event: NewBatchCreatedEvent) {
        batchId = event.batchId
        previousBatchId = event.previousBatchId
        status = BatchStatus.RUNNING
        resourceType = event.resourceType

        resources.add(Resource(event.amount, event.unitPrice))
    }

    @EventSourcingHandler
    fun handle(event: ResourceAddedToBatchEvent) {
        this.resources.add(Resource(event.amount, event.unitPrice))
    }

    @EventSourcingHandler
    fun handle(event: AmountInPackageUpdatedEvent) {
        this.resources.last().amount = event.amount
    }

    @EventSourcingHandler
    fun handle(event: StocktakingSavedEvent) {
        subtractAmountFromLastPackage(event.amount)
    }

    @EventSourcingHandler
    fun handle(event: BatchFinalizedEvent) {
        finalize()
    }

    private fun isFinalized(): Boolean {
        return BatchStatus.FINALIZED == this.status
    }

    private fun finalize() {
        status = BatchStatus.FINALIZED
    }

    private fun subtractAmountFromLastPackage(amount: BigDecimal) {
        resources.last().amount = resources.last().amount.minus(amount)
    }
}

data class Share internal constructor(val customerId: String, val quantity: BigDecimal, val productId: String)

data class Resource internal constructor(var amount: BigDecimal, val unitPrice: BigDecimal)
