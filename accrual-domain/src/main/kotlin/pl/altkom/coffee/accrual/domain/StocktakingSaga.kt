package pl.altkom.coffee.accrual.domain

import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.NewBatchCreatedEvent
import pl.altkom.coffee.accrual.api.StocktakingSavedEvent


@Saga
class StocktakingSaga : AbstractManagerSaga() {

    @StartSaga
    @SagaEventHandler(associationProperty = "batchId")
    fun handle(event: StocktakingSavedEvent) {

        val nextBatchId = BatchId()
        commandGateway.send<Void>(CreateNewBatchCommand(
                nextBatchId,
                event.batchId,
                event.resourceType,
                event.amount,
                event.unitPrice
        ))
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "previousBatchId")
    fun handle(event: NewBatchCreatedEvent) {
        commandGateway.send<Void>(FinalizeBatchCommand(
                event.previousBatchId!!,
                event.batchId
        ))
    }
}
