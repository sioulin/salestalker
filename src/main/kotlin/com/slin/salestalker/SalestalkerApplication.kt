package com.slin.salestalker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

@SpringBootApplication
class SalestalkerApplication

fun main(args: Array<String>) {
    runApplication<SalestalkerApplication>(*args)
}

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
class RoutingConfiguration {

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