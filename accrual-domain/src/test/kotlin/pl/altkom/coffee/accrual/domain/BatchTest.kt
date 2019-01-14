package pl.altkom.coffee.accrual.domain

import org.axonframework.test.aggregate.AggregateTestFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import pl.altkom.coffee.accounting.api.Money
import pl.altkom.coffee.accrual.api.*
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class BatchTest : Spek({

    var fixture = AggregateTestFixture(Batch::class.java)

    afterEachTest { fixture = AggregateTestFixture(Batch::class.java) }

    describe("batch creation") {
        val batchId = BatchId("123")

        it("should create new Batch") {
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
        val batchId = BatchId("123")

        it("should add new package") {
            fixture
                    .given(NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")))
                    .`when`(AddPackageToBatchCommand(batchId, ProductResourceType.COFFEE, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(ResourceAddedToBatchEvent(batchId, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectState {
                        assertSame(2, it.resources.size)
                        assertEquals(BigDecimal("1.50"), it.resources[1].amount)
                        assertEquals(BigDecimal("150.00"), it.resources[1].unitPrice)
                    }
        }

        it("should reject new package based on different resource type") {
            fixture
                    .given(NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")))
                    .`when`(AddPackageToBatchCommand(batchId, ProductResourceType.MILK, BigDecimal("1.50"), BigDecimal("150.00")))
                    .expectException(IllegalResourceTypeException::class.java)
        }
    }

    describe("resource amount change") {
        val batchId = BatchId("123")

        it("should change amount in package") {
            fixture
                    .given(
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
        val batchId = BatchId("123")

        it("should save stocktaking") {
            fixture
                    .given(
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

        it("should reject stocktaking saving when batch already finalized") {
            fixture
                    .given(
                            NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")),
                            ResourceAddedToBatchEvent(batchId, BigDecimal("1.50"), BigDecimal("150.00")),
                            BatchFinalizedEvent(batchId, batchId, HashMap())
                    )
                    .`when`(SaveStocktakingCommand(batchId, BigDecimal("1.00")))
                    .expectException(BatchAlreadyFinalizedException::class.java)
        }
    }

    describe("calculate charges ") {
        val batchId = BatchId("123")
        val nextBatchID = BatchId("1235")
        val member1 = "memeber1"
        val member2 = "memeber2"
        val productId = "product"

        val result = Arrays.asList(member1 to Money(BigDecimal.valueOf(75.00)), member2 to Money(BigDecimal.valueOf(25.00))).toMap()
        it("should set charges") {
            fixture
                    .given(
                            NewBatchCreatedEvent(batchId, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("100.00")),
                            ShareAddedEvent(batchId, member1, productId, 1, false),
                            ShareAddedEvent(batchId, member1, productId, 1, false),
                            ShareAddedEvent(batchId, member1, productId, 1, false),
                            ShareAddedEvent(batchId, member1, productId, 1, false),
                            ShareAddedEvent(batchId, member1, productId, 1, false),
                            ShareAddedEvent(batchId, member1, productId, 1, false),
                            ShareAddedEvent(batchId, member2, productId, 1, false),
                            ShareAddedEvent(batchId, member2, productId, 1, false)
                    )
                    .`when`(FinalizeBatchCommand(batchId, nextBatchID))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(BatchFinalizedEvent(batchId, nextBatchID, result))
                    .expectState {
                        val charges = it.calculateCharges()
                        assertEquals(charges[member1]!!.value.compareTo(BigDecimal.valueOf(75.00)), 0)
                        assertEquals(charges[member2]!!.value.compareTo(BigDecimal.valueOf(25.00)), 0)
                        assertEquals(charges.size, 2)

                    }
        }
    }

})
