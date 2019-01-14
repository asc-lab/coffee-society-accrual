package pl.altkom.coffee.accrual.query

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import pl.altkom.coffee.accrual.api.Share
import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType


@Document(indexName = "batch", type = "batch")
data class BatchEntry(
        @Id
        var batchId: String? = null,
        var resourceType: ProductResourceType? = null,
        var status: BatchStatus? = null,
        var shares: MutableList<Share> = mutableListOf()
)
