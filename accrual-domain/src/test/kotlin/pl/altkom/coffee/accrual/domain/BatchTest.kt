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

class BatchTest : Spek({
    describe("batch creation") {

        val fixture = AggregateTestFixture(Batch::class.java)


        it("Should create new Batch") {
            withUser("executor")

            fixture
                    .`when`(CreateNewBatchCommand("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"), "122"))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(NewBatchCreatedEvent("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"), "122"))
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


        it("Should add new package") {
            withUser("executor")

            fixture
                    .andGiven(NewBatchCreatedEvent("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"),"122"))
                    .`when`(AddPackageToBatchCommand("123", ProductResourceType.COFFEE, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(ResourceAddedToBatchEvent(BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectState {
                        assertSame(2, it.resources.size)
                        assertEquals(BigDecimal("1.50"), it.resources[1].amount)
                        assertEquals(BigDecimal("150.00"), it.resources[1].unitPrice)
                    }
        }

        it("Should reject new package based on different resource type") {
            withUser("executor")

            fixture
                    .andGiven(NewBatchCreatedEvent("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"), "122"))
                    .`when`(AddPackageToBatchCommand("123", ProductResourceType.MILK, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectException(IllegalResourceTypeException::class.java)
        }
    }

    describe("resource amount change") {

        val fixture = AggregateTestFixture(Batch::class.java)


        it("Should change amount in package") {
            withUser("executor")

            fixture
                    .andGiven(
                            NewBatchCreatedEvent("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"), "122"),
                            ResourceAddedToBatchEvent(BigDecimal("1.50"), BigDecimal("150.00"))
                    )
                    .`when`(UpdateAmountInPackageCommand("123", ProductResourceType.COFFEE, BigDecimal("0.90")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(AmountInPackageUpdatedEvent(BigDecimal("0.90")))
                    .expectState {
                        assertSame(2, it.resources.size)
                        assertEquals(BigDecimal("0.90"), it.resources[1].amount)
                        assertEquals(BigDecimal("150.00"), it.resources[1].unitPrice)
                    }
        }
    }

    describe("Stocktaking saving") {

        val fixture = AggregateTestFixture(Batch::class.java)


        it("Should save stocktaking") {
            withUser("executor")

            fixture
                    .andGiven(
                            NewBatchCreatedEvent("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"), "122"),
                            ResourceAddedToBatchEvent(BigDecimal("1.50"), BigDecimal("150.00"))
                    )
                    .`when`(SaveStocktakingCommand("123", BigDecimal("1.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(StocktakingSavedEvent(BigDecimal("1.00")))
                    .expectState {
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
                            NewBatchCreatedEvent("123", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00"), "122"),
                            ResourceAddedToBatchEvent(BigDecimal("1.50"), BigDecimal("150.00")),
                            BatchFinalizedEvent("123","123")
                    )
                    .`when`(SaveStocktakingCommand("123", BigDecimal("1.00")))
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