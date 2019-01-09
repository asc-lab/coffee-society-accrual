package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import pl.altkom.coffee.accrual.api.TaxAddedEvent
import java.math.BigDecimal

@Aggregate
class Tax {

    @AggregateIdentifier
    lateinit var taxId: String

    lateinit var memberId: String
    lateinit var productDefId: String
    lateinit var taxAmount: BigDecimal

    constructor()

    @CommandHandler
    constructor(command: AddTaxCommand) {
        with(command) {
            AggregateLifecycle.apply(TaxAddedEvent(
                    taxId,
                    memberId,
                    productDefId,
                    taxAmount
            ))
        }
    }

    @EventSourcingHandler
    fun handle(event: TaxAddedEvent) {
        taxId = event.taxId
        memberId = event.memberId
        productDefId = event.productDefId
        taxAmount = event.taxAmount
    }

}
