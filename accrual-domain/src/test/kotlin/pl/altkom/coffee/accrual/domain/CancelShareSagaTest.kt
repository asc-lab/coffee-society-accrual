package pl.altkom.coffee.accrual.domain

import org.axonframework.messaging.responsetypes.InstanceResponseType
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.test.saga.SagaTestFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import pl.altkom.coffee.accrual.api.TaxCanceledEvent
import pl.altkom.coffee.product.api.ProductPreparationCancelledEvent
import pl.altkom.coffee.productcatalog.api.dto.ProductDefinitionDto
import pl.altkom.coffee.productcatalog.api.dto.ProductResourceDto
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import pl.altkom.coffee.productcatalog.api.query.ProductDetailsQuery
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class CancelShareSagaTest : Spek({

    describe("Cancel share") {

        val fixture = SagaTestFixture(CancelShareSaga::class.java)
        val queryGateway = Mockito.mock(QueryGateway::class.java)
        fixture.registerResource(queryGateway)

        val productDefId = UUID.randomUUID().toString()
        val productId = UUID.randomUUID().toString()
        val taxId = "tax_" + productId
        val memberId = UUID.randomUUID().toString()

        Mockito.`when`(queryGateway.query(ArgumentMatchers.any<ProductDetailsQuery>(), ArgumentMatchers.any<InstanceResponseType<ProductDefinitionDto>>()))
                .thenReturn(CompletableFuture.completedFuture(getProductDef(productDefId)))

        it("should start cancel share saga") {
            fixture
                    .givenAggregate(productId).published()
                    .whenAggregate(productId).publishes(ProductPreparationCancelledEvent(productId, productDefId))
                    .expectActiveSagas(1)

        }

        it("Should end cancel share saga") {
            fixture
                    .givenAggregate(taxId).published()
                    .whenAggregate(taxId).publishes(TaxCanceledEvent(taxId, memberId, BigDecimal("10.00")))
                    .expectActiveSagas(0)
        }
    }
})

private fun getProductDef(productDefId : String) : ProductDefinitionDto {
    return ProductDefinitionDto(productDefId, "kafka", Arrays.asList(ProductResourceDto(ProductResourceType.COFFEE, 15)), BigDecimal("10.00"))
}
