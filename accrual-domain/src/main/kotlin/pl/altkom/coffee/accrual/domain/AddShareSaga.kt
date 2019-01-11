package pl.altkom.coffee.accrual.domain

import org.axonframework.messaging.responsetypes.InstanceResponseType
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import pl.altkom.coffee.accounting.api.AssetAddedEvent
import pl.altkom.coffee.accounting.api.LiabilityAddedEvent
import pl.altkom.coffee.accounting.api.Money
import pl.altkom.coffee.accounting.api.OperationId
import pl.altkom.coffee.accounting.domain.SaveLiabilityCommand
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.ShareAddedEvent
import pl.altkom.coffee.accrual.api.TaxAddedEvent
import pl.altkom.coffee.product.api.ProductPreparationRegisteredEvent
import pl.altkom.coffee.productcatalog.api.dto.ProductDefinitionDto
import pl.altkom.coffee.productcatalog.api.query.ProductDetailsQuery
import java.math.BigDecimal

@Saga
class AddShareSaga : AbstractManagerSaga() {

    @Autowired
    @Transient
    lateinit var queryGateway: QueryGateway


    var eventCounter: Int = 0
    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    fun handle(event: ProductPreparationRegisteredEvent) {
        logger.info("Add share saga started")
        val productDefinitionDto = queryGateway.query(
                ProductDetailsQuery(event.productDefId), InstanceResponseType(ProductDefinitionDto::class.java)).get()

        eventCounter += productDefinitionDto.resources.size

        if (BigDecimal.ZERO.compareTo(productDefinitionDto.tax) != 0) {
            var taxId = getTaxId(event.id)
            eventCounter++
            SagaLifecycle.associateWith("taxId", taxId)
            commandGateway.send<Void>(AddTaxCommand(
                    taxId,
                    event.productReceiverId,
                    event.id,
                    productDefinitionDto.id,
                    productDefinitionDto.tax
            ))
        }
        SagaLifecycle.associateWith("batchId", BatchId().identifier)
        productDefinitionDto.resources.forEach {
            commandGateway.send<Void>(AddShareCommand(
                    BatchId(),
                    event.productReceiverId,
                    event.id,
                    it.quantity
            ))
        }


    }

    @SagaEventHandler(associationProperty = "taxId", keyName = "taxId")
    fun handle(event: TaxAddedEvent) {
        logger.info("Add share saga ended: Handle TaxAddedEvent")
        commandGateway.send<Void>(SaveLiabilityCommand(
                event.memberId,
                OperationId(event.taxId, "TAX"),
                Money(event.taxAmount)
        ))
    }

    @SagaEventHandler(associationProperty = "batchId", keyName = "batchId")
    fun handle(event: ShareAddedEvent) {
        logger.info("AddShareSaga: Handle ShareAddedEvent")
        // if batch finished
        commandGateway.send<Void>(SaveLiabilityCommand(
                event.memberId,
                OperationId(event.batchId.identifier, "BATCH"),
                Money(BigDecimal.ZERO)
        ))
    }

    @SagaEventHandler(associationProperty = "operationId", keyName = "batchId")
    fun handle(event: LiabilityAddedEvent) {
        eventCounter--
        if (eventCounter == 0)
            SagaLifecycle.end()
    }

    @SagaEventHandler(associationProperty = "operationId", keyName = "batchId")
    fun handle(event: AssetAddedEvent) {
        eventCounter--
        if (eventCounter == 0)
            SagaLifecycle.end()
    }

}

