package com.apiece.coupon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class CouponApplication

fun main(args: Array<String>) {
	runApplication<CouponApplication>(*args)
}
