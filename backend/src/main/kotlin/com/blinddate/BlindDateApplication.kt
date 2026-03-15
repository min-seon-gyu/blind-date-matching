package com.blinddate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BlindDateApplication

fun main(args: Array<String>) {
    runApplication<BlindDateApplication>(*args)
}
