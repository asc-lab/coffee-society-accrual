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
import pl.altkom.coffee.accrual.api.TaxCanceledEvent
import pl.altkom.coffee.product.api.ProductPreparationCancelledEvent
import pl.altkom.coffee.productcatalog.api.dto.ProductDefinitionDto
import pl.altkom.coffee.productcatalog.api.query.ProductDetailsQuery
import java.math.BigDecimal

@Saga
class CancelShareSaga : AbstractManagerSaga() {

    @Transient
    @Autowired
    lateinit var queryGateway: QueryGateway

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    fun handle(event: ProductPreparationCancelledEvent) {
        logger.info("Cancel share saga started.")
        val productDefinitionDto = queryGateway.query(
                ProductDetailsQuery(event.productDefId), InstanceResponseType(ProductDefinitionDto::class.java)).get()

        if (productDefinitionDto.tax != BigDecimal.ZERO) {
            var taxId = getTaxId(event.id)
            SagaLifecycle.associateWith("taxId", taxId)
            commandGateway.send<Void>(CancelTaxCommand(taxId))
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "taxId", keyName = "taxId")
    fun handle(event: TaxCanceledEvent) {
        logger.info("Cancel share saga ended: Handle TaxCanceledEvent")
        commandGateway.send<Void>(SaveAssetCommand(
                event.memberId,
                OperationId(event.taxId, "TAX"),
                Money(event.taxAmount)
        ))
    }

}
