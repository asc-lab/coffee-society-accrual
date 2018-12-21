package pl.altkom.coffee.accrual.domain

import org.axonframework.commandhandling.CommandMessage
import org.axonframework.test.matchers.Matchers
import org.axonframework.test.saga.SagaTestFixture
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import pl.altkom.coffee.accrual.api.NewBatchCreatedEvent
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal
import java.util.*

class BatchCreationSagaTest : Spek({
    describe("batch creation") {

        val fixture = SagaTestFixture(BatchCreationSaga::class.java)


        it("Should finalize old batch") {
            withUser("executor")

            fixture
                    .givenAggregate("batch1").published()
                    .whenAggregate("batch1").publishes(NewBatchCreatedEvent("batch2", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("80.00"), "batch1"))
                    .expectActiveSagas(1)
                    .expectDispatchedCommandsMatching(Matchers.listWithAllOf(FinalizeBatchCommandMatcher("batch1","batch2")))

        }

        it("Should end if there was no old batch") {
            withUser("executor")

            fixture
                    .givenAggregate("batch1").published()
                    .whenAggregate("batch1").publishes(NewBatchCreatedEvent("batch2", ProductResourceType.COFFEE, BigDecimal("1.00"), BigDecimal("80.00"), null))
                    .expectActiveSagas(0)
                    .expectDispatchedCommandsMatching(Matchers.noCommands())

        }
    }
})

private class FinalizeBatchCommandMatcher(val batchId: String, val nextBatchId: String) : BaseMatcher<CommandMessage<*>>() {


    override fun describeTo(description: Description) {
        description
                .appendText("FinalizeBatchCommand with batchId:")
                .appendValue(batchId)
                .appendText(" nextBatchId:")
                .appendValue(nextBatchId)
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