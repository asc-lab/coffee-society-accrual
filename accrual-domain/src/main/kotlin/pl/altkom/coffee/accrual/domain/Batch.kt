package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import pl.altkom.coffee.accrual.api.AmountInPackageUpdatedEvent
import pl.altkom.coffee.accrual.api.NewBachCreatedEvent
import pl.altkom.coffee.accrual.api.ResourceAddedToBatchEvent
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

class Batch {

    @AggregateIdentifier
    lateinit var id: String
    lateinit var resourceType: ProductResourceType
    val shares: MutableList<Share> = mutableListOf()
    val resources: MutableList<Resource> = mutableListOf()

    constructor()

    @CommandHandler
    constructor(command: CreateNewBatchCommand) {
        with(command) {
            AggregateLifecycle.apply(NewBachCreatedEvent(id, resourceType, amount, unitPrice))
        }
    }

    @EventSourcingHandler
    fun handle(event: NewBachCreatedEvent) {
        this.id = event.id
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


}

data class Share internal constructor(val customerId: String, val quantity: BigDecimal, val productId: String)

data class Resource internal constructor(var amount: BigDecimal, val unitPrice: BigDecimal)