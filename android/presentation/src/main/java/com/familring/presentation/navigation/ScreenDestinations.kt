package com.familring.presentation.navigation

/*
 받는 변수가 있는 스크린 추가
    data object sample : ScreenDestination(route = "화면이름"){
        override val route: String
            get() = "화면이름/인자변수명"
        val arguments = listOf(
            navArgument(name = "id") { type = NavType.StringType },
        )
        fun createRoute(
            id : String,
        ) = "화면이름/$id"
    }
 */

sealed class ScreenDestinations(
    open val route: String,
) {
    // 첫 번째 화면 (초대코드 입력)
    data object First : ScreenDestinations(route = "First")

    // 생년월일 입력
    data object Birth : ScreenDestinations(route = "Birth")

    // 로그인
    data object Login : ScreenDestinations(route = "Login")

    // 질문 화면
    data object Question : ScreenDestinations(route = "Question")

    // 질문 목록 화면
    data object QuestionList : ScreenDestinations(route = "QuestionList")

    // 프로필 색상 선택
    data object ProfileColor : ScreenDestinations(route = "ProfileColor")

    // 닉네임
    data object Nickname : ScreenDestinations(route = "Nickname")

    // 사진 촬영
    data object Picture : ScreenDestinations(route = "Picture")

    // 구성원 수 입력
    data object FamilyInfo : ScreenDestinations(route = "FamilyCount")

    // 회원가입 완료
    data object Done : ScreenDestinations(route = "Done")

    // 타임캡슐
    data object TimeCapsule : ScreenDestinations(route = "TimeCapsule")

    // 타임캡슐 생성 화면
    data object TimeCapsuleCreate : ScreenDestinations(route = "TimeCapsuleCreate")

    // 홈
    data object Home : ScreenDestinations(route = "Home")

    // 채팅
    data object Chat : ScreenDestinations(route = "Chat")

    // 캘린더
    data object Calendar : ScreenDestinations(route = "Calendar")

    // 일정 생성
    data object ScheduleCreate : ScreenDestinations(route = "ScheduleCreate")

    // 일상 업로드
    data object DailyUpload : ScreenDestinations(route = "DailyUpload")

    // 갤러리
    data object Gallery : ScreenDestinations(route = "Gallery")

    // 앨범
    data object Album : ScreenDestinations(route = "Album")

    // 관심사 공유
    data object Interest : ScreenDestinations(route = "Interest")

    // 관심사 공유 목록
    data object InterestList : ScreenDestinations(route = "InterestList")

    // 선정 안 된 관심사
    data object OtherInterest : ScreenDestinations(route = "OtherInterest")

    // 알림
    data object Notification : ScreenDestinations(route = "Notification")
}