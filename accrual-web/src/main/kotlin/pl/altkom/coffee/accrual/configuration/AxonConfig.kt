package pl.altkom.coffee.accrual.configuration

import mu.KLogging
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.ConfigurerModule
import org.axonframework.config.MessageMonitorFactory
import org.axonframework.messaging.Message
import org.axonframework.monitoring.MessageMonitor
import org.axonframework.queryhandling.QueryBus
import org.springframework.stereotype.Component

@Component
class AxonConfig : ConfigurerModule {

    override fun configureModule(configurer: Configurer?) {
        configurer!!.configureMessageMonitor(QueryBus::class.java, LoggingMessageMonitorFactory())
    }

}

class LoggingMessageMonitorFactory : MessageMonitorFactory {
    override fun create(configuration: Configuration?, componentType: Class<*>?, componentName: String?): MessageMonitor<Message<*>> {
        return LoggingMessageMonitor()
    }
}

class LoggingMessageMonitor : MessageMonitor<Message<*>> {

    private val callback = LoggingMonitorCallback()

    override fun onMessageIngested(message: Message<*>?): MessageMonitor.MonitorCallback {
        return callback
    }

}

class LoggingMonitorCallback : MessageMonitor.MonitorCallback {

    override fun reportFailure(cause: Throwable?) {
        logger.error("Error while handling query", cause)
    }

    override fun reportSuccess() {
    }

    override fun reportIgnored() {
    }

    companion object : KLogging()
}