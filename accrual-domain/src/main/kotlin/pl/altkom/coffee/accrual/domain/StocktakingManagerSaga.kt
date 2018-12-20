package pl.altkom.coffee.accrual.domain

import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.StocktakingFinishedEvent
import pl.altkom.coffee.accrual.api.StocktakingSavedEvent
import pl.altkom.coffee.accrual.api.StocktakingStartedEvent
import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal


@Saga
class StocktakingManagerSaga : ManagerSaga() {

    lateinit var resourceType: ProductResourceType
    lateinit var amount: BigDecimal
    lateinit var unitPrice: BigDecimal

    @StartSaga
    @SagaEventHandler(associationProperty = "batchId")
    fun handle(event: StocktakingStartedEvent) {
        batchId = event.batchId
        amount = event.amount
        resourceType = event.resourceType
        unitPrice = event.unitPrice

        commandGateway.send<Void>(SaveStocktakingCommand(batchId, amount))
    }

    @SagaEventHandler(associationProperty = "batchId")
    fun handle(event: StocktakingSavedEvent) {

        commandGateway.send<Void>(CreateNewBatchCommand(BatchId(), resourceType, amount, unitPrice))
        commandGateway.send<Void>(FinishStocktakingCommand(batchId))
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "batchId")
    fun handle(event: StocktakingFinishedEvent) {
        commandGateway.send<Void>(FinalizeBatchCommand(batchId))
    }
}
