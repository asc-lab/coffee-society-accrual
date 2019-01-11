package pl.altkom.coffee.accrual.query

import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import pl.altkom.coffee.accrual.api.BatchFinalizedEvent
import pl.altkom.coffee.accrual.api.NewBatchCreatedEvent
import pl.altkom.coffee.accrual.api.Share
import pl.altkom.coffee.accrual.api.ShareAddedEvent
import pl.altkom.coffee.accrual.api.enums.BatchStatus

@Component
class BatchEntryProjection(private val repository: BatchEntryRepository) {

    @EventHandler
    fun on(event: NewBatchCreatedEvent) {
        repository.save(BatchEntry(event.batchId, event.resourceType, BatchStatus.RUNNING))
    }

    @EventHandler
    fun on(event: BatchFinalizedEvent) {
        var batchEntry = repository.findByBatchId(event.batchId)
        batchEntry!!.status = BatchStatus.FINALIZED

        repository.save(batchEntry)
    }

    @EventHandler
    fun on(event: ShareAddedEvent) {
        var batchEntry = repository.findByBatchId(event.batchId)
        batchEntry!!.shares.add(Share(event.memberId, event.quantity, event.productId))

        repository.save(batchEntry)
    }
}
