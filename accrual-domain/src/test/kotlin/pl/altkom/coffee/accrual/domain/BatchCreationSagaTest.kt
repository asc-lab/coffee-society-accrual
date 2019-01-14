package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandMessage
import org.axonframework.test.matchers.Matchers
import org.axonframework.test.saga.SagaTestFixture
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import pl.altkom.coffee.accrual.api.BatchFinalizedEvent
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.NewBatchCreatedEvent
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import java.math.BigDecimal
import java.util.*
import kotlin.collections.HashMap


class BatchCreationSagaTest : Spek({
    describe("batch creation") {

        val batchId = BatchId("batchId")
        val batchId2 = BatchId("batchId2")
        val fixture = SagaTestFixture(BatchCreationSaga::class.java)


        it("Should send finalization command") {
            withUser("executor")

            fixture
                    .givenAggregate(batchId.identifier).published()
                    .whenAggregate(batchId.identifier).publishes(NewBatchCreatedEvent(batchId2, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("80.00")))
                    .expectActiveSagas(1)
                    .expectDispatchedCommandsMatching(Matchers.listWithAllOf(FinalizeBatchCommandMatcher(batchId, batchId2)))

        }

        xit("Should finish saga after finalization") {
            withUser("executor")

            fixture
                    .givenAggregate(batchId.identifier).published()
                    .andThenAPublished(NewBatchCreatedEvent(batchId2, batchId, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("80.00")))
                    .whenAggregate(batchId.identifier).publishes(BatchFinalizedEvent(batchId, batchId2,HashMap()))
                    .expectActiveSagas(0)
                    .expectDispatchedCommandsMatching(Matchers.noCommands())

        }

        it("Should end if there was no old batch") {
            withUser("executor")

            fixture
                    .givenAggregate(batchId.identifier).published()
                    .whenAggregate(batchId.identifier).publishes(NewBatchCreatedEvent(batchId2, null, ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("80.00")))
                    .expectActiveSagas(0)
                    .expectDispatchedCommandsMatching(Matchers.noCommands())

        }
    }
})

private class FinalizeBatchCommandMatcher(val batchId: BatchId, val nextBatchId: BatchId) : BaseMatcher<CommandMessage<*>>() {


    override fun describeTo(description: Description) {
        description
                .appendText("FinalizeBatchCommand with batchId:")
                .appendValue(batchId.identifier)
                .appendText(" nextBatchId:")
                .appendValue(nextBatchId.identifier)
    }

    override fun matches(commandMessage: Any?): Boolean {
        if (commandMessage is CommandMessage<*>) {
            val command = commandMessage.payload
            return command is FinalizeBatchCommand && command.batchId == this.batchId && command.nextBatchId == this.nextBatchId
        }

        return false

    }

}

private fun withUser(userName: String) {
    val authenticatedUser = UsernamePasswordAuthenticationToken(
            User(userName, "", ArrayList<GrantedAuthority>()), null)
    SecurityContextHolder.setContext(
            SecurityContextImpl(authenticatedUser))
}