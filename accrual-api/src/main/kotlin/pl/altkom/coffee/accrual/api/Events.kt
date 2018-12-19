package pl.altkom.coffee.accrual.api

import pl.altkom.coffee.accrual.api.enums.ProductResourceType
import java.math.BigDecimal

data class NewBachCreatedEvent(
        val id: String,
        val resourceType: ProductResourceType,
        val amount: BigDecimal,
        val unitPrice: BigDecimal
)

data class ResourceAddedToBatchEvent(val amount: BigDecimal, val unitPrice: BigDecimal)

class AmountInPackageUpdatedEvent(val amount: BigDecimal)