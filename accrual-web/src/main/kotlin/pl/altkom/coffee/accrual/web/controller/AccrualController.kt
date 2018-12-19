package pl.altkom.coffee.accrual.web.controller

import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accrual")
class AccrualController(private val commandGateway: CommandGateway) {


}