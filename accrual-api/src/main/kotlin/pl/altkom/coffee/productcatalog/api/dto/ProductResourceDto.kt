package pl.altkom.coffee.productcatalog.api.dto

import pl.altkom.coffee.productcatalog.api.enums.ProductResourceType

data class ProductResourceDto(val type: ProductResourceType, val quantity: Int)