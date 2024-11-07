package com.familring.data.repositoryImpl

import com.familring.data.network.api.QuestionApi
import com.familring.data.network.response.emitApiResponse
import com.familring.domain.model.ApiResponse
import com.familring.domain.model.QuestionList
import com.familring.domain.model.QuestionResponse
import com.familring.domain.repository.QuestionRepository
import com.familring.domain.request.QuestionAnswerRequest
import com.familring.domain.request.QuestionPatchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class QuestionRepositoryImpl
    @Inject
    constructor(
        private val api: QuestionApi,
    ) : QuestionRepository {
        override suspend fun getQuestion(questionId: Long?): Flow<ApiResponse<QuestionResponse>> =
            flow {
                val response =
                    emitApiResponse(
                        apiResponse = { api.getQuestion() },
                        default = QuestionResponse(),
                    )
                emit(response)
            }

        override suspend fun postAnswer(content: String): Flow<ApiResponse<Unit>> =
            flow {
                val response =
                    emitApiResponse(
                        apiResponse = { api.postAnswer(QuestionAnswerRequest(content)) },
                        default = Unit,
                    )
                emit(response)
            }

        override suspend fun patchAnswer(content: String): Flow<ApiResponse<Unit>> =
            flow {
                val response =
                    emitApiResponse(
                        apiResponse = { api.patchAnswer(QuestionPatchRequest(content)) },
                        default = Unit,
                    )
                emit(response)
            }

        override suspend fun getAllQuestion(
            pageNo: Int,
            order: String,
        ): Flow<ApiResponse<QuestionList>> =
            flow {
                val response =
                    emitApiResponse(
                        apiResponse = { api.getAllQuestion(pageNo, order) },
                        default = QuestionList(),
                    )
                emit(response)
            }
    }