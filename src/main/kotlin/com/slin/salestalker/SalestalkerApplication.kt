package com.slin.salestalker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import javax.xml.parsers.DocumentBuilderFactory
import org.jsoup.Jsoup




@SpringBootApplication
class SalestalkerApplication

fun main(args: Array<String>) {
    runApplication<SalestalkerApplication>(*args)
}

val strategies = ExchangeStrategies
        .builder()
        .codecs { clientDefaultCodecsConfigurer ->
            clientDefaultCodecsConfigurer.registerDefaults(true)
        }.build()

val client = WebClient.builder()
        .exchangeStrategies(strategies)
        .baseUrl("https://us.louisvuitton.com/eng-us/products/pochette-metis-monogram-006115")
        .build()

val response = client.get()
        .retrieve()
        .bodyToMono(java.lang.String::class.java)
//            .doOnError(UnsupportedMediaTypeException::class.java, { ex -> println(ex.supportedMediaTypes) })
        .block()
//.then { response -> println(response) }

@Component
class ReactiveHandler(val repo: StringRepo) {
    fun getText(search: String): Mono<String> =
            repo.get(search).toMono().map { "Result: $it!" }

    fun addText(text: String): Mono<String> =
            repo.add(text).toMono().map { "Result: $it!" }

    fun getAllTexts(): Flux<String> =
            repo.getAll().toFlux().map { "Result: $it" }
}

@Component
class StringRepo {
    private val entities = mutableListOf<String>()
    fun add(s: String) = entities.add(s)
    fun get(s: String) = entities.find { it == s } ?: "not found!"
    fun getAll() = listOf(entities)
}

@Configuration
@EnableWebFlux
class RoutingConfiguration : WebFluxConfigurer {

    @Bean
    fun routerFunction(handler: ReactiveHandler): RouterFunction<ServerResponse> = router {
        ("/reactive").nest {
            val searchPathName = "search"
            val savePathName = "save"
            GET("/{$searchPathName}") { req ->
                val pathVar = req.pathVariable(searchPathName)
                ServerResponse.ok().body(
                        handler.getText(pathVar)
                )
            }
            GET("/") {
                println(response)
//                val doc = Jsoup.connect("https://us.louisvuitton.com/eng-us/products/pochette-metis-monogram-006115").get()

                val doc = Jsoup.connect("https://us.louisvuitton.com/eng-us/products/pochette-felicie-monogram-vernis-010586#M61293").get()

                if (doc.select("#notInStock").hasClass("hide")) {
                    println("in stock!!")
                } else {
                    println("not in stock :(")
                }

                ServerResponse.ok().body(handler.getAllTexts())
            }
            PUT("/{$savePathName}") { req ->
                val pathVar = req.pathVariable(savePathName)
                ServerResponse.ok().body(
                        handler.addText(pathVar)
                )
            }
        }
    }
}