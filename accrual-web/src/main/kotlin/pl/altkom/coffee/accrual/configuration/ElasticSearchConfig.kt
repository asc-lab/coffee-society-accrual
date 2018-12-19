package pl.altkom.coffee.accrual.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories("pl.altkom.coffee.accrual.provider")
class ElasticSearchConfig
