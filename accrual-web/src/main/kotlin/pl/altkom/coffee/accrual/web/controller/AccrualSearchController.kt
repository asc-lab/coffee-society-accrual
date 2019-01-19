package pl.altkom.coffee.accrual.web.controller

import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accrual")
class AccrualSearchController(private val queryGateway: QueryGateway) {

}
