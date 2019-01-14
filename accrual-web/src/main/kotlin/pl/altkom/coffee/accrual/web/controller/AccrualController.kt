package pl.altkom.coffee.accrual.web.controller

import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.altkom.coffee.accrual.api.BatchId
import pl.altkom.coffee.accrual.api.dto.CreateBatchRequest
import pl.altkom.coffee.accrual.domain.CreateNewBatchCommand

@RestController
@RequestMapping("/api/accrual")
class AccrualController(private val commandGateway: CommandGateway) {

    @PostMapping("/startNewBatch")
    fun startBatch(@RequestBody request: CreateBatchRequest) {
        commandGateway.send<Void>(CreateNewBatchCommand(
                BatchId(),
               null,
                request.resourceType,
                request.amount,
                request.unitPrice
        ))

    }


}