package com.apiece.coupon.application

import com.apiece.coupon.api.dto.CreateCouponRequest
import com.apiece.coupon.domain.Coupon
import com.apiece.coupon.domain.CouponRepository
import com.apiece.coupon.domain.Issuance
import com.apiece.coupon.infrastructure.messaging.IssuanceRequestProducer
import com.apiece.coupon.infrastructure.messaging.IssuanceRequested
import com.apiece.coupon.support.CouponNotFoundException
import com.apiece.coupon.support.NotStartedException
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    // private val issuanceRepository: IssuanceRepository,
    private val couponIssuer: CouponIssuer,
    //private val eventPublisher: ApplicationEventPublisher,
    private val issuanceRequestProducer: IssuanceRequestProducer,
) {

    @Transactional
    fun createCoupon(request: CreateCouponRequest): Coupon {
        val coupon = couponRepository.save(
            Coupon(
                name = request.name,
                totalQuantity = request.totalQuantity,
                validityDays = request.validityDays,
                startsAt = request.startsAt,
            )
        )
        couponIssuer.initStock(coupon.id!!, coupon.totalQuantity)
        return coupon
    }

    @Transactional
    fun issue(couponId: Long, userId: Long): Issuance {
        val coupon = couponRepository.findById(couponId)
            .orElseThrow { CouponNotFoundException() }

        val now = LocalDateTime.now()
        if (!coupon.isBookingOpen(now)) {
            throw NotStartedException()
        }
        // redis에서 매진을 이미 체크하고 있음
//        if (coupon.isSoldOut()) {
//            throw SoldOutException()
//        }

        couponIssuer.tryIssue(couponId, userId)

        val expiresAt = now.plusDays(coupon.validityDays.toLong())
        // 퍼블리셔로 변경
        issuanceRequestProducer.publish(
            IssuanceRequested(
                couponId = couponId,
                userId = userId,
                issuedAt = now,
                expiresAt = expiresAt,
            )
        )

        return Issuance(
            userId = userId,
            couponId = couponId,
            issuedAt = now,
            expiresAt = expiresAt,
        )
        // worker로 이동
//        couponRepository.incrementIssuedQuantity(couponId)
//
//        // 이슈 발행
//        return issuanceRepository.save(
//            Issuance(
//                userId = userId,
//                couponId = couponId,
//                issuedAt = now,
//                expiresAt = now.plusDays(coupon.validityDays.toLong()),
//            )
//        )
    }
}