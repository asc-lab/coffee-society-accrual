package pl.altkom.coffee.accrual.domain

import org.axonframework.messaging.responsetypes.InstanceResponseType
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.test.saga.SagaTestFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.TaxAddedEvent
import pl.altkom.coffee.accrual.api.dto.BatchIdByResourceTypeAndStatus
import pl.altkom.coffee.product.api.ProductPreparationRegisteredEvent
import pl.altkom.coffee.productcatalog.api.dto.ProductDefinitionDto
import pl.altkom.coffee.productcatalog.api.dto.ProductResourceDto
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import pl.altkom.coffee.productcatalog.api.query.ProductDetailsQuery
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class AddShareSagaTest : Spek({

    describe("Add share") {

        val fixture = SagaTestFixture(AddShareSaga::class.java)
        val queryGateway = Mockito.mock(QueryGateway::class.java)
        fixture.registerResource(queryGateway)

        val productDefId = UUID.randomUUID().toString()
        val productId = UUID.randomUUID().toString()
        val taxId = "tax_$productId"
        val memberId = UUID.randomUUID().toString()

        Mockito.`when`(queryGateway.query(ArgumentMatchers.any(ProductDetailsQuery::class.java), ArgumentMatchers.any<InstanceResponseType<ProductDefinitionDto>>()))
                .thenReturn(CompletableFuture.completedFuture(getProductDef(productDefId)))

        it("should start add share saga") {
            fixture
                    .givenAggregate(taxId).published()
                    .whenAggregate(taxId).publishes(ProductPreparationRegisteredEvent(productId, productDefId, memberId, memberId))
                    .expectActiveSagas(1)

        }

        it("Should TODO FIXME add share saga") {
            fixture
                    .givenAggregate(taxId).published()
                    .whenAggregate(taxId).publishes(TaxAddedEvent(taxId, memberId, productId, productDefId, BigDecimal("10.00")))
        }

        it("Should TODO FIXME saga when tax = 0") {
            Mockito.`when`(queryGateway.query(ArgumentMatchers.any(ProductDetailsQuery::class.java), ArgumentMatchers.any<InstanceResponseType<ProductDefinitionDto>>()))
                    .thenReturn(CompletableFuture.completedFuture(getProductDefWithoutTax(productDefId)))

            Mockito.`when`(queryGateway.query(ArgumentMatchers.any(BatchIdByResourceTypeAndStatus::class.java), ArgumentMatchers.any<InstanceResponseType<BatchId>>()))
                    .thenReturn(CompletableFuture.completedFuture(BatchId()))

            fixture
                    .givenAggregate(taxId).published()
                    .whenAggregate(taxId).publishes(ProductPreparationRegisteredEvent(productId, productDefId , memberId, memberId))
                    .expectActiveSagas(1)
        }
    }
})

private fun getProductDef(productDefId : String) : ProductDefinitionDto {
    return ProductDefinitionDto(productDefId, "kafka", Arrays.asList(ProductResourceDto(ProductResourceType.COFFEE, 15)), BigDecimal("10.00"))
}

private fun getProductDefWithoutTax(productDefId : String) : ProductDefinitionDto {
    return ProductDefinitionDto(productDefId, "kafka", Arrays.asList(ProductResourceDto(ProductResourceType.COFFEE, 15)), BigDecimal.ZERO)
}
