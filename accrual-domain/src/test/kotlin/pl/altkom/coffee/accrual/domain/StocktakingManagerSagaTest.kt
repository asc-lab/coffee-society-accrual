package pl.altkom.coffee.accrual.domain

import org.axonframework.test.aggregate.AggregateTestFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import pl.altkom.coffee.accrual.api.*
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class StocktakingManagerSagaTest : Spek({
    describe("batch creation") {

        val fixture = AggregateTestFixture(Batch::class.java)
        val batchId = BatchId("123")

        it("Should create new Batch") {
            withUser("executor")

            fixture
                    .`when`(CreateNewBatchCommand(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")))
                    .expectState {
                        assertSame(ProductResourceType.COFFEE, it.resourceType)
                        assertSame(0, it.shares.size)
                        assertSame(1, it.resources.size)
                        assertEquals(BigDecimal("1.00"), it.resources[0].amount)
                        assertEquals(BigDecimal("100.00"), it.resources[0].unitPrice)
                        assertSame(BatchStatus.RUNNING, it.status)
                    }
        }
    }

    describe("package adding") {

        val fixture = AggregateTestFixture(Batch::class.java)
        val batchId = BatchId("123")

        it("Should add new package") {
            withUser("executor")

            fixture
                    .andGiven(NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")))
                    .`when`(AddPackageToBatchCommand(batchId, ProductResourceType.COFFEE, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(ResourceAddedToBatchEvent(batchId, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectState {
                        assertSame(2, it.resources.size)
                        assertEquals(BigDecimal("1.50"), it.resources[1].amount)
                        assertEquals(BigDecimal("150.00"), it.resources[1].unitPrice)
                    }
        }

        it("Should reject new package based on different resource type") {
            withUser("executor")

            fixture
                    .andGiven(NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")))
                    .`when`(AddPackageToBatchCommand(batchId, ProductResourceType.MILK, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectException(IllegalResourceTypeException::class.java)
        }
    }

    describe("resource amount change") {

        val fixture = AggregateTestFixture(Batch::class.java)
        val batchId = BatchId("123")

        it("Should change amount in package") {
            withUser("executor")

            fixture
                    .andGiven(
                            NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")),
                            ResourceAddedToBatchEvent(batchId, BigDecimal("1.50"), BigDecimal("150.00"))
                    )
                    .`when`(UpdateAmountInPackageCommand(batchId, ProductResourceType.COFFEE, BigDecimal("0.90")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(AmountInPackageUpdatedEvent(batchId, BigDecimal("0.90")))
                    .expectState {
                        assertSame(2, it.resources.size)
                        assertEquals(BigDecimal("0.90"), it.resources[1].amount)
                        assertEquals(BigDecimal("150.00"), it.resources[1].unitPrice)
                    }
        }
    }

    describe("stocktaking saving") {

        val fixture = AggregateTestFixture(Batch::class.java)
        val batchId = BatchId("123")

        it("Should save stocktaking") {
            withUser("executor")

            fixture
                    .andGiven(
                            NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")),
                            ResourceAddedToBatchEvent(batchId, BigDecimal("1.50"), BigDecimal("150.00"))
                    )
                    .`when`(SaveStocktakingCommand(batchId, BigDecimal("1.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(StocktakingSavedEvent(batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("150.00")))
                    .expectState {
                        assertEquals(batchId, it.batchId)
                        assertSame(2, it.resources.size)
                        assertEquals(BigDecimal("1.00"), it.resources[0].amount)
                        assertEquals(BigDecimal("0.50"), it.resources[1].amount)
                        assertEquals(BigDecimal("100.00"), it.resources[0].unitPrice)
                        assertEquals(BigDecimal("150.00"), it.resources[1].unitPrice)
                        assertNotSame(BatchStatus.FINALIZED, it.status)
                    }
        }

        it("Should reject stocktaking saving when batch already finalized") {
            withUser("executor")

            fixture
                    .andGiven(
                            NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")),
                            ResourceAddedToBatchEvent(batchId, BigDecimal("1.50"), BigDecimal("150.00")),
                            BatchFinalizedEvent(batchId)
                    )
                    .`when`(SaveStocktakingCommand(batchId, BigDecimal("1.00")))
                    .expectException(BatchAlreadyFinalizedException::class.java)
        }
    }
})

private fun withUser(userName: String) {
    val authenticatedUser = UsernamePasswordAuthenticationToken(
            User(userName, "", ArrayList<GrantedAuthority>()), null)
    SecurityContextHolder.setContext(
            SecurityContextImpl(authenticatedUser))
}