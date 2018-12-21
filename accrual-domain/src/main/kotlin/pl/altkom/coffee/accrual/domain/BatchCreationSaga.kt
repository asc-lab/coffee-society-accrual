package pl.altkom.coffee.accrual.domain

import mu.KLogging
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.StartSaga
import pl.altkom.coffee.accrual.api.BatchFinalizedEvent
import pl.altkom.coffee.accrual.api.NewBatchCreatedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class BatchCreationSaga {

    @Autowired
    @Transient
    val commandGateway: CommandGateway? = null

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    fun handle(event: NewBatchCreatedEvent) {


        logger.info("Got Batch Creation (${event.id})")

        if (event.previousBatchId != null) {
            logger.info("Requesting for old batch finalization (${event.previousBatchId})")
            SagaLifecycle.associateWith("prevId", event.previousBatchId)
            commandGateway?.send<Void>(FinalizeBatchCommand(event.previousBatchId!!, event.id))
        } else {
            SagaLifecycle.end()
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "batchId", keyName = "prevId")
    fun handle(event: BatchFinalizedEvent) {
        logger.info("Batch creation Saga finished")
        //just finish saga
    }

    companion object : KLogging()

}
