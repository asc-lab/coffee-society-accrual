package pl.altkom.coffee.accrual.web.controller

import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.altkom.coffee.product.api.dto.BeginProductPreparationRequest
import pl.altkom.coffee.product.api.dto.CancelProductPreparationRequest
import pl.altkom.coffee.product.api.dto.ChangeProductReceiverRequest
import pl.altkom.coffee.product.api.dto.EndProductPreparationRequest
import pl.altkom.coffee.product.domain.BeginProductPreparationCommand
import pl.altkom.coffee.product.domain.CancelProductPreparationCommand
import pl.altkom.coffee.product.domain.ChangeProductReceiverCommand
import pl.altkom.coffee.product.domain.EndProductPreparationCommand

@RestController
@RequestMapping("/api/accrual")
class AccrualController(private val commandGateway: CommandGateway) {


}