package pl.altkom.coffee.accrual.domain

import org.axonframework.common.IdentifierFactory
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
import pl.altkom.coffee.accounting.domain.SaveLiabilityCommand
import pl.altkom.coffee.accrual.api.TaxAddedEvent
import pl.altkom.coffee.product.api.ProductPreparationRegisteredEvent
import pl.altkom.coffee.productcatalog.api.dto.ProductDefinitionDto
import pl.altkom.coffee.productcatalog.api.query.ProductDetailsQuery
import java.math.BigDecimal

@Saga
class AddShareSaga : AbstractManagerSaga() {

    @Transient
    lateinit var queryGateway: QueryGateway
        @Autowired set

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    fun handle(event: ProductPreparationRegisteredEvent) {
        logger.info("Adding share saga started.")
        val productDefinitionDto = queryGateway.query(
                ProductDetailsQuery(event.productDefId), InstanceResponseType(ProductDefinitionDto::class.java)).get()

        if (BigDecimal.ZERO.compareTo(productDefinitionDto.tax) != 0) {
            val taxId = IdentifierFactory.getInstance().generateIdentifier()
            SagaLifecycle.associateWith("taxId", taxId)
            commandGateway.send<Void>(AddTaxCommand(
                    taxId,
                    event.productReceiverId,
                    productDefinitionDto.id,
                    productDefinitionDto.tax
            ))
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "taxId", keyName = "taxId")
    fun handle(event: TaxAddedEvent) {
        logger.info("AddShareSaga: Handle TaxAddedEvent")
        commandGateway.send<Void>(SaveLiabilityCommand(
                event.memberId,
                OperationId(event.taxId, "TAX"),
                Money(event.taxAmount)
        ))
    }

}
