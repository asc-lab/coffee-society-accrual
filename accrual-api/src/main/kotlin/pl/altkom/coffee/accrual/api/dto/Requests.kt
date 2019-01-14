package pl.altkom.coffee.accrual.api.dto

import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType
import java.math.BigDecimal


data class CreateBatchRequest(
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal)