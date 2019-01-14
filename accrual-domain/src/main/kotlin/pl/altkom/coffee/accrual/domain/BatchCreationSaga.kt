package pl.altkom.coffee.accrual.domain

import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.SagaLifecycle.associateWith
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import pl.altkom.coffee.accounting.api.LiabilityAddedEvent
import pl.altkom.coffee.accounting.api.OperationId
import pl.altkom.coffee.accounting.domain.SaveLiabilityCommand
import pl.altkom.coffee.accrual.api.BatchFinalizedEvent
import pl.altkom.coffee.accrual.api.NewBatchCreatedEvent

@Saga
class BatchCreationSaga {

    @Autowired
    @Transient
    val commandGateway: CommandGateway? = null

    var eventCounter: Int = 0


    @StartSaga
    @SagaEventHandler(associationProperty = "batchId")
    fun handle(event: NewBatchCreatedEvent) {
        logger.info("Got Batch Creation (${event.batchId.identifier})")

        if (event.previousBatchId != null) {
            logger.info("Requesting for old batch finalization (${event.previousBatchId!!.identifier})")
            associateWith("prevId", event.previousBatchId!!.identifier)
            commandGateway?.send<Void>(FinalizeBatchCommand(event.previousBatchId!!, event.batchId))
        } else {
            SagaLifecycle.end()
        }
    }

    @SagaEventHandler(associationProperty = "batchId", keyName = "prevId")
    fun handle(event: BatchFinalizedEvent) {

        val charges = event.charges

        eventCounter += charges.size
        charges.forEach {
            commandGateway?.send<Void>(SaveLiabilityCommand(
                    it.key,
                    OperationId(event.batchId.identifier, "BATCH"),
                    it.value
            ))
        }
    }


    //TODO  FIX associationProperty
    @EndSaga
    @SagaEventHandler(associationProperty = "operationId", keyName = "batchId")
    fun handle(event: LiabilityAddedEvent) {
//        eventCounter--
//        if (eventCounter == 0) {
            logger.info("Batch creation Saga finished")
//            SagaLifecycle.end()
//        }
    }


    companion object : KLogging()

}
