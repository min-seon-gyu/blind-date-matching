package com.blinddate.action.service

import com.blinddate.action.dto.ActionExecuteResponse
import com.blinddate.action.dto.ActionInfoResponse
import com.blinddate.action.entity.ActionToken
import com.blinddate.action.repository.ActionTokenRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ActionTokenService(
    private val actionTokenRepository: ActionTokenRepository
) {
    @Transactional
    fun createToken(actionType: String, targetId: Long, barOwnerId: Long): String {
        val token = ActionToken(actionType = actionType, targetId = targetId, barOwnerId = barOwnerId)
        actionTokenRepository.save(token)
        return token.token
    }

    fun getActionInfo(token: String): ActionInfoResponse {
        val actionToken = actionTokenRepository.findByToken(token)
            .orElseThrow { NotFoundException("유효하지 않은 토큰입니다") }
        return ActionInfoResponse(
            actionType = actionToken.actionType,
            targetId = actionToken.targetId,
            expired = actionToken.isExpired(),
            used = actionToken.used
        )
    }

    @Transactional
    fun executeAction(token: String): ActionExecuteResponse {
        val actionToken = actionTokenRepository.findByToken(token)
            .orElseThrow { NotFoundException("유효하지 않은 토큰입니다") }

        if (!actionToken.isValid()) {
            throw BadRequestException(
                if (actionToken.used) "이미 사용된 토큰입니다"
                else "만료된 토큰입니다"
            )
        }

        actionToken.used = true

        // The actual action (approve/reject) will be called by the controller
        // which integrates with ApplicationService
        return ActionExecuteResponse(success = true, message = "액션이 실행되었습니다")
    }
}
