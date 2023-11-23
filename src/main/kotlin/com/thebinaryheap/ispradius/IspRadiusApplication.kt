package com.thebinaryheap.ispradius

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(IspConfiguration::class)
class IspRadiusApplication

fun main(args: Array<String>) {
	runApplication<IspRadiusApplication>(*args)
}