package com.familring.presentation.screen.mypage

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface MyPageUiEvent {
    @Immutable
    data object SignOutSuccess : MyPageUiEvent

    @Immutable
    data object EmotionUpdateSuccess : MyPageUiEvent

    @Immutable
    data object NameUpdateSuccess : MyPageUiEvent

    @Immutable
    data object ColorUpdateSuccess : MyPageUiEvent

    @Immutable
    data object FaceUpdateSuccess : MyPageUiEvent

    @Immutable
    data class Error(
        val code: String,
        val message: String,
    ) : MyPageUiEvent
}
