package com.apiece.coupon.infrastructure.cache

import com.apiece.coupon.application.CacheMetrics
import com.apiece.coupon.application.CouponPolicy
import io.netty.util.AsciiString.cached
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Repository
class CouponCacheRepository (
    private val redis: StringRedisTemplate,
    private val mapper: ObjectMapper,
    private val properties: CacheProperties,
    private val cacheMetrics: CacheMetrics,
){
    fun getIssuePolicyOrLoad(id: Long, loader: () -> CouponPolicy) =
        getOrLoad(
            key = "coupon:$id:issue-policy",
            loader = loader,
        )


    private fun getOrLoad(
        key: String,
        loader: () -> CouponPolicy,
    ): CouponPolicy {
        redis.opsForValue().get(key)?.let { cached ->
            cacheMetrics.incrementCouponCacheHits()
            return mapper.readValue(cached, CouponPolicy::class.java)
        }
        cacheMetrics.incrementCouponDbRead()
        val response = loader()
        redis.opsForValue().set(key, mapper.writeValueAsString(response), Duration.ofMillis(properties.ttlMs)) // 직렬화
        return response
    }
}