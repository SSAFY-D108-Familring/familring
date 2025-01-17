package com.familring.data.network.api

import com.familring.data.network.response.BaseResponse
import com.familring.domain.model.calendar.MonthSchedulesDailies
import com.familring.domain.model.calendar.Schedule
import com.familring.domain.request.ScheduleCreateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {
    @GET("calendars")
    suspend fun getMonthData(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): BaseResponse<MonthSchedulesDailies>

    @GET("calendars/schedules/date")
    suspend fun getDaySchedules(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("day") day: Int,
    ): BaseResponse<List<Schedule>>

    @POST("calendars/schedules")
    suspend fun createSchedule(
        @Body schedule: ScheduleCreateRequest,
    ): BaseResponse<Unit>

    @PATCH("calendars/schedules/{schedule_id}")
    suspend fun updateSchedule(
        @Path("schedule_id") id: Long,
        @Body schedule: ScheduleCreateRequest,
    ): BaseResponse<Unit>

    @DELETE("calendars/schedules/{schedule_id}")
    suspend fun deleteSchedule(
        @Path("schedule_id") id: Long,
    ): BaseResponse<Unit>
}
