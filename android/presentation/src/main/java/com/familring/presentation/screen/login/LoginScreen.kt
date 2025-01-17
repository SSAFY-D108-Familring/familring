package com.familring.presentation.screen.login

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.familring.presentation.MainActivity
import com.familring.presentation.R
import com.familring.presentation.component.dialog.LoadingDialog
import com.familring.presentation.theme.FamilringTheme
import com.familring.presentation.theme.Typography
import com.familring.presentation.theme.White
import com.familring.presentation.util.noRippleClickable
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    navigateToFirst: () -> Unit,
    navigateToHome: () -> Unit,
    showSnackBar: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    LoginScreen(
        modifier = modifier,
        navigateToFirst = navigateToFirst,
        navigateToHome = navigateToHome,
        showSnackBar = showSnackBar,
        loginState = loginState,
        viewModel = viewModel,
        resetState = viewModel::resetState,
        handleKakaoLogin = { activity -> viewModel.handleKakaoLogin(activity) },
    )
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginState: LoginState,
    resetState: () -> Unit = {},
    navigateToFirst: () -> Unit = {},
    navigateToHome: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
    showSnackBar: (String) -> Unit = {},
    handleKakaoLogin: (Activity) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.loginEvent) {
        viewModel.loginEvent.collectLatest { event ->
            when (event) {
                is LoginEvent.None -> {
                    isLoading = false
                }

                is LoginEvent.Loading -> isLoading = true

                is LoginEvent.LoginSuccess -> {
                    isLoading = false
                    navigateToHome()
                }

                is LoginEvent.Error -> {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Init -> {
                isLoading = false
            }
            is LoginState.Success -> navigateToHome()
            is LoginState.NoRegistered -> {
                Timber.d("회원가입 화면으로 이동")
                navigateToFirst()
                resetState()
            }
            is LoginState.Error -> {
                showSnackBar(loginState.errorMessage)
                isLoading = false
            }
            is LoginState.Loading -> isLoading = true
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = White,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val pagerState =
                    rememberPagerState(pageCount = {
                        4
                    })

                Spacer(modifier = Modifier.fillMaxHeight(0.05f))

                Row(
                    modifier =
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                        Box(
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .size(10.dp)
                                    .background(color),
                        )
                    }
                }
                Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                HorizontalPager(
                    state = pagerState,
                ) { page ->
                    when (page) {
                        0 -> FirstPage()
                        1 -> SecondPage()
                        2 -> ThirdPage()
                        3 -> FourthPage()
                    }
                }
            }

            Image(
                painter = painterResource(id = R.drawable.img_img_kakao_login),
                contentDescription = "img_kakao_login",
                modifier =
                    Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 50.dp)
                        .noRippleClickable {
                            activity?.let {
                                handleKakaoLogin(it)
                            }
                        },
            )
        }
    }
    if (isLoading) {
        LoadingDialog(loadingMessage = "로그인 시도 중..")
    }
}

@Composable
private fun FirstPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "'오늘 치킨 먹을 사람은 찬성!'",
            style = Typography.headlineLarge,
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "채팅",
                style = Typography.headlineLarge,
            )
            Text(
                text = "에서 간단히 저녁 메뉴를 정해 보세요",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.1f))

        Image(
            painter = painterResource(id = R.drawable.img_img_leg),
            contentDescription = "leg_img",
        )
    }
}

@Composable
private fun SecondPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "사진을 업로드하면 ",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
            Text(
                text = "얼굴을 인식",
                style = Typography.headlineLarge,
            )
            Text(
                text = "하고,",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }
        Spacer(modifier = Modifier.padding(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "앨범",
                style = Typography.headlineLarge,
            )
            Text(
                text = "을 만들어 ",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
            Text(
                text = "분류",
                style = Typography.headlineLarge,
            )
            Text(
                text = "해 줘요!",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.1f))

        Image(
            painter = painterResource(id = R.drawable.img_family),
            contentDescription = "family_img",
        )
    }
}

@Composable
private fun ThirdPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "공유 캘린더",
                style = Typography.headlineLarge,
            )
            Text(
                text = "로 가족 일정을",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }
        Spacer(modifier = Modifier.padding(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "간편하게 ",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
            Text(
                text = "관리",
                style = Typography.headlineLarge,
            )
            Text(
                text = "하고, ",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
            Text(
                text = "기록",
                style = Typography.headlineLarge,
            )
            Text(
                text = "을 남겨요",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.1f))
        Image(
            painter = painterResource(id = R.drawable.img_calendar),
            contentDescription = "calendar_img",
        )
    }
}

@Composable
private fun FourthPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "매일 달라지는 랜덤 질문",
                style = Typography.headlineLarge,
            )
            Text(
                text = "으로",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }
        Spacer(modifier = Modifier.padding(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "우리 가족의 ",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
            Text(
                text = "속마음",
                style = Typography.headlineLarge,
            )
            Text(
                text = "을 알아볼까요?",
                style = Typography.labelLarge.copy(fontSize = 24.sp),
            )
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.1f))

        Image(
            painter = painterResource(id = R.drawable.img_question_mark),
            contentDescription = "question_img",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FamilringTheme {
        LoginScreen(
            loginState = LoginState.Loading,
            navigateToFirst = {},
            navigateToHome = {},
            showSnackBar = {},
            resetState = {},
        )
    }
}
