package pl.altkom.coffee.accrual.query

import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.dto.BatchIdByProductIdAndResourceType
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType

interface BatchEntryRepository : ElasticsearchCrudRepository<BatchEntry, String> {

    fun findByBatchId(batchId: BatchId): BatchEntry?

    fun findByResourceTypeAndStatus(resourceType: ProductResourceType, status: BatchStatus): BatchEntry?

    @Query("{\n" +
            "query\": {\n" +
            "        \"nested\" : {\n" +
            "            \"path\" : \"shares\",\n" +
            "            \"query\" : {\n" +
            "                \"bool\" : {\n" +
            "                    \"must\" : [\n" +
            "                    { \"match\" : {\"shares.productId\" : \"?0\"} },\n" +
            "                    { \"match\" : {\"shares.resourceType\" : \"?1\"} },\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}")
    fun findBatchIdByProductIdAndResourceType(productId: String, resourceType: ProductResourceType): BatchId?

}
