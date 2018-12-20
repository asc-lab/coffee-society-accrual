package pl.altkom.coffee.accrual.domain

import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import pl.altkom.coffee.accrual.api.BatchId
import java.io.Serializable


abstract class ManagerSaga : Serializable {

    @Transient
    lateinit var commandGateway: CommandGateway
        @Autowired set

    lateinit var batchId: BatchId

    companion object : KLogging()
}