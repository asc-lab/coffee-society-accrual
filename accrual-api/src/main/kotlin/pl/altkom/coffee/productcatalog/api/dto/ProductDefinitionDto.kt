package pl.altkom.coffee.productcatalog.api.dto

import java.io.Serializable
import java.math.BigDecimal

data class ProductDefinitionDto(
        val id: String,
        val name: String,
        val resources: List<ProductResourceDto>,
        val tax: BigDecimal
) : Serializable