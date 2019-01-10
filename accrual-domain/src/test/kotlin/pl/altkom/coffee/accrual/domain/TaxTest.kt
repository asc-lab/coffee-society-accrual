package pl.altkom.coffee.accrual.domain

import org.axonframework.test.aggregate.AggregateTestFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import pl.altkom.coffee.accrual.api.TaxAddedEvent
import pl.altkom.coffee.accrual.api.TaxCanceledEvent
import pl.altkom.coffee.accrual.api.TaxTransferedEvent
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TaxTest : Spek({
    var fixture = AggregateTestFixture(Tax::class.java)

    val productId = UUID.randomUUID().toString()
    val taxId = "tax_" + productId
    val memberId = UUID.randomUUID().toString()
    val productDefId = UUID.randomUUID().toString()

    afterEachTest { fixture = AggregateTestFixture(Tax::class.java) }

    describe("tax creation") {

        it("should create new Tax") {
            fixture.
                    `when`(AddTaxCommand(taxId, memberId, productId, productDefId, BigDecimal("10.00")))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(TaxAddedEvent(taxId, memberId, productId, productDefId, BigDecimal("10.00")))
                    .expectState {
                        assertSame(taxId, it.taxId)
                        assertSame(memberId, it.memberId)
                        assertSame(productId, it.productId)
                        assertSame(productDefId, it.productDefId)
                        assertEquals(BigDecimal("10.00"), it.taxAmount)
                    }
        }
    }

    describe("tax cancel") {

        it("should cancel Tax") {
            fixture
                    .given(TaxAddedEvent(taxId, memberId, productId, productDefId, BigDecimal("10.00")))
                    .`when`(CancelTaxCommand(taxId))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(TaxCanceledEvent(taxId, memberId, BigDecimal("10.00")))
                    .expectState {
                        assertSame(taxId, it.taxId)
                        assertSame(memberId, it.memberId)
                        assertSame(productId, it.productId)
                        assertSame(productDefId, it.productDefId)
                        assertEquals(BigDecimal("10.00"), it.taxAmount)
                    }
        }
    }

    describe("tax transfer") {
        val toMemberId = UUID.randomUUID().toString()

        it("should transfer Tax") {
            fixture
                    .given(TaxAddedEvent(taxId, memberId, productId, productDefId, BigDecimal("10.00")))
                    .`when`(TransferTaxCommand(taxId, toMemberId))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(TaxTransferedEvent(taxId, memberId, toMemberId, BigDecimal("10.00")))
                    .expectState {
                        assertSame(taxId, it.taxId)
                        assertSame(toMemberId, it.memberId)
                        assertSame(productId, it.productId)
                        assertSame(productDefId, it.productDefId)
                        assertEquals(BigDecimal("10.00"), it.taxAmount)
                    }
        }
    }
})
