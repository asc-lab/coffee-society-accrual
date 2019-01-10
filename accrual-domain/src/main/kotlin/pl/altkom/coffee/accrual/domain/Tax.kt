package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import pl.altkom.coffee.accrual.api.TaxAddedEvent
import pl.altkom.coffee.accrual.api.TaxCanceledEvent
import pl.altkom.coffee.accrual.api.TaxTransferedEvent
import java.math.BigDecimal

@Aggregate
class Tax {

    @AggregateIdentifier
    lateinit var taxId: String
    lateinit var memberId: String
    lateinit var productId: String
    lateinit var productDefId: String
    lateinit var taxAmount: BigDecimal

    constructor()

    @CommandHandler
    constructor(command: AddTaxCommand) {
        with(command) {
            AggregateLifecycle.apply(TaxAddedEvent(
                    taxId,
                    memberId,
                    productId,
                    productDefId,
                    taxAmount
            ))
        }
    }

    @CommandHandler
    fun on(command: CancelTaxCommand) {
        with(command) {
            AggregateLifecycle.apply(TaxCanceledEvent(
                    taxId,
                    memberId,
                    taxAmount
            ))
        }
    }

    @CommandHandler
    fun on(command: TransferTaxCommand) {
        with(command) {
            AggregateLifecycle.apply(TaxTransferedEvent(
                    taxId,
                    memberId,
                    toMemberId,
                    taxAmount
            ))
        }
    }

    @EventSourcingHandler
    fun handle(event: TaxAddedEvent) {
        taxId = event.taxId
        memberId = event.memberId
        productId = event.productId
        productDefId = event.productDefId
        taxAmount = event.taxAmount
    }

    @EventSourcingHandler
    fun handle(event: TaxTransferedEvent) {
        memberId = event.toMemberId
    }
}
