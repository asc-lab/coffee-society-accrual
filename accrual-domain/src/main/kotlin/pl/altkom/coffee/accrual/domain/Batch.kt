package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import pl.altkom.coffee.accounting.api.Money
import pl.altkom.coffee.accrual.api.*
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import java.math.BigDecimal
import java.math.RoundingMode

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
    fun on(command: AddShareCommand) {
        if (command.quantity <= 0)
            throw IllegalShareException()

        apply(ShareAddedEvent(batchId, command.memberId, command.productId, command.quantity, isFinalized()))
    }

    @CommandHandler
    fun on(command: FinalizeBatchCommand) {
        if (isFinalized())
            throw BatchAlreadyFinalizedException()

        with(command) {
            apply(BatchFinalizedEvent(batchId, command.nextBatchId, calculateCharges()))
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
    fun handle(event: ShareAddedEvent) {
        if (isFinalized()) {
            //val chargesMapBeforeAddShare = getChargesMap()
            shares.add(Share(event.memberId, event.quantity, event.productId))
            //val chargesMapAfterAddShare = getChargesMap()
        } else {
            shares.add(Share(event.memberId, event.quantity, event.productId))
        }
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

    public fun calculateCharges(): Map<String, Money> {
        val sumAllQuantity = shares.map { it.quantity }.sum()
        val resourcesPrice = resources.map { it.unitPrice }.fold(BigDecimal.ZERO, BigDecimal::add)

        return shares.groupBy { it.customerId }
                .map {
                    val memberShares = it.value
                    val memberQuantity = memberShares.map { share ->
                        share.quantity
                    }.sum()
                    val percentShare = memberQuantity.toFloat() / sumAllQuantity.toFloat()

                    val memberCharge = Money(resourcesPrice.multiply(percentShare.toBigDecimal()).setScale(2, RoundingMode.DOWN))
                    it.key to memberCharge

                }.toMap()

    }
}
