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
import pl.altkom.coffee.accrual.api.dto.BatchIdByProductIdAndResourceType
import pl.altkom.coffee.accrual.api.dto.BatchIdByResourceTypeAndStatus
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.product.api.ProductPreparationRegisteredEvent
import pl.altkom.coffee.product.api.ProductPreparationRegisteredForClosedBatchEvent
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
                ProductDetailsQuery(event.productDefId), InstanceResponseType(ProductDefinitionDto::class.java)).join()

        validateAndSendTaxCommand(productDefinitionDto, event.id, event.productReceiverId)


        productDefinitionDto.resources.forEach {
            val batchId: String = queryGateway.query(BatchIdByResourceTypeAndStatus(
                    it.type,
                    BatchStatus.RUNNING
            ), InstanceResponseType(String::class.java)).get()

            SagaLifecycle.associateWith("productId", event.id)
            commandGateway.send<Void>(AddShareCommand(
                    BatchId(batchId),
                    event.id,
                    event.id,
                    it.quantity
            ))
        }
    }


    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    fun handle(event: ProductPreparationRegisteredForClosedBatchEvent) {
        logger.info("Add share saga started")

        val productDefinitionDto = queryGateway.query(
                ProductDetailsQuery(event.productDefId), InstanceResponseType(ProductDefinitionDto::class.java)).join()

        validateAndSendTaxCommand(productDefinitionDto, event.id, event.productReceiverId)
        productDefinitionDto.resources.forEach {
            val batchId: String = queryGateway.query(BatchIdByProductIdAndResourceType(
                    event.selectedProductId,
                    it.type
            ), InstanceResponseType(String::class.java)).join()


            SagaLifecycle.associateWith("productId", event.id)
            commandGateway.send<Void>(AddShareCommand(
                    BatchId(batchId),
                    event.productReceiverId,
                    event.id,
                    it.quantity
            ))
        }
    }


    private fun validateAndSendTaxCommand(productDefinitionDto: ProductDefinitionDto, productId: String, productReceiverId: String) {
        if (BigDecimal.ZERO.compareTo(productDefinitionDto.tax) != 0) {
            val taxId = getTaxId(productId)
            eventCounter++
            SagaLifecycle.associateWith("taxId", taxId)
            commandGateway.send<Void>(AddTaxCommand(
                    taxId,
                    productReceiverId,
                    productId,
                    productDefinitionDto.id,
                    productDefinitionDto.tax
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

    @SagaEventHandler(associationProperty = "productId", keyName = "productId")
    fun handle(event: ShareAddedEvent) {
        logger.info("AddShareSaga: Handle ShareAddedEvent")

        if (event.batchFinalized) {
            commandGateway.send<Void>(SaveLiabilityCommand(
                    event.memberId,
                    OperationId(event.batchId.identifier, "BATCH"),
                    Money(BigDecimal.ZERO)
            ))
        } else {
            finishSagaIfAllEventsExecuted()
        }
    }

    @SagaEventHandler(associationProperty = "operationId", keyName = "batchId")
    fun handle(event: LiabilityAddedEvent) {
        finishSagaIfAllEventsExecuted()
    }

    @SagaEventHandler(associationProperty = "operationId", keyName = "batchId")
    fun handle(event: AssetAddedEvent) {
        finishSagaIfAllEventsExecuted()
    }

    private fun finishSagaIfAllEventsExecuted() {
        eventCounter--
        if (eventCounter == 0) {
            AbstractManagerSaga.logger.info("Finish AddShareSaga")
            SagaLifecycle.end()
        }
    }

}

