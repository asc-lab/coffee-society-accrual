package pl.altkom.coffee.accrual.domain

import org.axonframework.messaging.responsetypes.InstanceResponseType
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import pl.altkom.coffee.accounting.api.Money
import pl.altkom.coffee.accounting.api.OperationId
import pl.altkom.coffee.accounting.domain.SaveAssetCommand
import pl.altkom.coffee.accounting.domain.SaveLiabilityCommand
import pl.altkom.coffee.accrual.api.TaxTransferedEvent
import pl.altkom.coffee.product.api.ProductReceiverChangedEvent
import pl.altkom.coffee.productcatalog.api.dto.ProductDefinitionDto
import pl.altkom.coffee.productcatalog.api.query.ProductDetailsQuery
import java.math.BigDecimal

@Saga
class TransferShareSaga : AbstractManagerSaga() {

    @Transient
    @Autowired
    lateinit var queryGateway: QueryGateway

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    fun handle(event: ProductReceiverChangedEvent) {
        logger.info("Transfer share saga started.")
        val productDefinitionDto = queryGateway.query(
                ProductDetailsQuery(event.productDefId), InstanceResponseType(ProductDefinitionDto::class.java)).get()

        if (productDefinitionDto.tax != BigDecimal.ZERO) {
            val taxId = getTaxId(event.id)
            SagaLifecycle.associateWith("taxId", taxId)
            commandGateway.send<Void>(TransferTaxCommand(taxId, event.productReceiverNewId))
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "taxId", keyName = "taxId")
    fun handle(event: TaxTransferedEvent) {
        logger.info("Transfer share saga ended: Handle TaxCanceledEvent")
        commandGateway.send<Void>(SaveAssetCommand(
                event.fromMemberId,
                OperationId(event.taxId, "TAX"),
                Money(event.taxAmount)
        ))
        commandGateway.send<Void>(SaveLiabilityCommand(
                event.toMemberId,
                OperationId(event.taxId, "TAX"),
                Money(event.taxAmount)
        ))
    }
}
