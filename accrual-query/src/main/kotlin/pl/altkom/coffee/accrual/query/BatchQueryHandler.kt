package pl.altkom.coffee.accrual.query

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.dto.BatchIdByProductIdAndResourceType
import pl.altkom.coffee.accrual.api.dto.BatchIdByResourceTypeAndStatus

@Component
class BatchQueryHandler(private val repository: BatchEntryRepository) {

    @QueryHandler
    fun getBatchIdByResourceTypeAndStatus(query: BatchIdByResourceTypeAndStatus) : BatchId? {
        val batchEntry = repository.findByResourceTypeAndStatus(query.resourceType, query.status)
        return batchEntry!!.batchId
    }

    @QueryHandler
    fun getBatchIdByProductIdAndResourceType (query: BatchIdByProductIdAndResourceType): BatchId? {
        return repository.findBatchIdByProductIdAndResourceType(query.productId, query.resourceType)
    }
}
