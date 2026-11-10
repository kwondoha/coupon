package com.apiece.coupon.application

import com.apiece.coupon.domain.Issuance
import com.apiece.coupon.domain.IssuanceRepository
import com.apiece.coupon.domain.IssuanceStatus
import com.apiece.coupon.support.AlreadyUsedException
import com.apiece.coupon.support.ExpiredException
import com.apiece.coupon.support.IssuanceNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IssuanceService (
    private val issuanceRepository: IssuanceRepository,
) {

    @Transactional
    fun use(issuanceId: Long, userid: Long): Issuance {
        val issuance = issuanceRepository.findById(issuanceId)
            .orElseThrow { IssuanceNotFoundException() }

        when (issuance.status) {
            IssuanceStatus.USED -> throw AlreadyUsedException()
            IssuanceStatus.EXPIRED -> throw ExpiredException()
            IssuanceStatus.ISSUED -> Unit
        }

        val now = LocalDateTime.now()
        if (issuance.isExpired(now)) {
            throw ExpiredException()
        }

        issuance.markUsed(now)
        return issuance
    }

    fun findByUser(userId: Long): List<Issuance> =
        issuanceRepository.findByUserIdOrderByIssuedAtDesc(userId)
}