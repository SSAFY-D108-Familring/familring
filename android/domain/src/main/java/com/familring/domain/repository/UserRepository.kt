package com.familring.domain.repository

import com.familring.domain.model.ApiResponse
import com.familring.domain.model.JwtToken
import com.familring.domain.model.User
import com.familring.domain.request.UserEmotionRequest
import com.familring.domain.request.UserJoinRequest
import com.familring.domain.request.UserLoginRequest
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UserRepository {
    suspend fun login(request: UserLoginRequest): Flow<ApiResponse<JwtToken>>

    suspend fun join(
        request: UserJoinRequest,
        userFace: File,
    ): Flow<ApiResponse<JwtToken>>

    suspend fun getUser(): Flow<ApiResponse<User>>

    suspend fun signOut(): Flow<ApiResponse<Unit>>

    suspend fun updateEmotion(emotion: UserEmotionRequest): Flow<ApiResponse<Unit>>

    suspend fun updateNickname(nickname: String): Flow<ApiResponse<Unit>>

    suspend fun updateColor(color: String): Flow<ApiResponse<Unit>>
}