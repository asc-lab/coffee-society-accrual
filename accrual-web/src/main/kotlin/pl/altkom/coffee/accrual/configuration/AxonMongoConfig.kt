package pl.altkom.coffee.accrual.configuration

import com.mongodb.MongoClient
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.Serializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonMongoConfig {

    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return DefaultMongoTemplate.builder()
                .domainEventsCollectionName("domainevents_accrual")
                .sagasCollectionName("sagas_accrual")
                .snapshotEventsCollectionName("snapshotevents_accrual")
                .trackingTokensCollectionName("trackingtokens_accrual")
                .mongoDatabase(mongoClient)
                .build()
    }

    @Bean
    fun tokenStore(mongoTemplate: MongoTemplate, tokenSerializer: Serializer):TokenStore {
        return MongoTokenStore.builder()
                .mongoTemplate(mongoTemplate)
                .serializer(tokenSerializer)
                .build()
    }

}