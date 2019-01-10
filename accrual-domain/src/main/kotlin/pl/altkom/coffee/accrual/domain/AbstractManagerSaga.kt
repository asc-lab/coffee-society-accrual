package pl.altkom.coffee.accrual.domain

import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import java.io.Serializable


abstract class AbstractManagerSaga : Serializable {

    @Transient
    lateinit var commandGateway: CommandGateway
        @Autowired set

    companion object : KLogging()

    protected fun getTaxId(productId : String) : String {
        return "tax_$productId"
    }
}
