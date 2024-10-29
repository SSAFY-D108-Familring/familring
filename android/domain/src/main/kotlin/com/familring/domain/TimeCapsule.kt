package com.familring.domain

data class TimeCapsule(
    val id: Int,
    val isOpenable: Boolean = true,
    val leftDay: Int = 0,
)