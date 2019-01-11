package pl.altkom.coffee.accrual.api.dto

import pl.altkom.coffee.accrual.api.enums.BatchStatus
import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType

class BatchIdByResourceTypeAndStatus(val resourceType: ProductResourceType, val status: BatchStatus)

class BatchIdByProductIdAndResourceType(val productId: String, val resourceType: ProductResourceType)
