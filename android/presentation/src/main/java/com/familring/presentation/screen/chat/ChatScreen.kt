package com.familring.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familring.domain.model.ChatItem
import com.familring.presentation.R
import com.familring.presentation.component.TopAppBar
import com.familring.presentation.component.button.RoundLongButton
import com.familring.presentation.component.chat.ChatInputBar
import com.familring.presentation.component.chat.MyMessage
import com.familring.presentation.component.chat.OtherMessage
import com.familring.presentation.component.textfield.CustomTextField
import com.familring.presentation.theme.Black
import com.familring.presentation.theme.Brown01
import com.familring.presentation.theme.Green02
import com.familring.presentation.theme.Green04
import com.familring.presentation.theme.Green05
import com.familring.presentation.theme.Typography
import com.familring.presentation.theme.White
import com.familring.presentation.util.noRippleClickable
import kotlinx.coroutines.launch

@Composable
fun ChatRoute(
    modifier: Modifier,
    popUpBackStack: () -> Unit,
    showSnackBar: (String) -> Unit,
) {
    ChatScreen(
        modifier = modifier,
        popUpBackStack = popUpBackStack,
        showSnackBar = showSnackBar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    popUpBackStack: () -> Unit = {},
    showSnackBar: (String) -> Unit = {},
) {
    val myId = 1L
    val chatList =
        remember {
            mutableStateListOf(
                ChatItem(
                    userId = 1,
                    message = "잘들 지내시는가",
                    nickname = "나갱이",
                    profileImg = "",
                    color = "0xFF949494",
                ),
                ChatItem(
                    userId = 2,
                    message = "나경이 저녁 먹었니?",
                    nickname = "엄마미",
                    profileImg = "https://familring-bucket.s3.ap-northeast-2.amazonaws.com/zodiac-sign/닭.png",
                    color = "0xFFFFE1E1",
                ),
                ChatItem(
                    userId = 3,
                    message = "밥 잘 챙겨먹구 다녀",
                    nickname = "아빵이",
                    profileImg = "https://familring-bucket.s3.ap-northeast-2.amazonaws.com/zodiac-sign/원숭이.png",
                    color = "0xFFC9D0FF",
                ),
                ChatItem(
                    userId = 3,
                    message = "요즘은 뭐하고 지내 안 심심해?",
                    nickname = "아빵이",
                    profileImg = "https://familring-bucket.s3.ap-northeast-2.amazonaws.com/zodiac-sign/원숭이.png",
                    color = "0xFFC9D0FF",
                ),
            )
        }

    var inputMessage by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatList.size) {
        if (chatList.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatList.size - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = White,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "채팅",
                        style = Typography.headlineMedium.copy(fontSize = 22.sp),
                    )
                },
                onNavigationClick = popUpBackStack,
            )
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 15.dp),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(chatList.size) { index ->
                    val item = chatList[index]
                    if (item.userId == myId) {
                        MyMessage(message = item.message)
                    } else {
                        OtherMessage(
                            nickname = item.nickname,
                            profileImg = item.profileImg,
                            color = item.color,
                            message = item.message,
                        )
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            WindowInsets.ime
                                .exclude(WindowInsets.navigationBars)
                                .asPaddingValues(),
                        ).padding(vertical = 5.dp)
                        .padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { showBottomSheet = true },
                    content = {
                        Icon(
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(1f),
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "add",
                            tint = Green02,
                        )
                    },
                )
                ChatInputBar(
                    value = inputMessage,
                    onValueChanged = {
                        inputMessage = it
                        scope.launch {
                            lazyListState.animateScrollToItem(chatList.size - 1)
                        }
                    },
                    sendMessage = {
                        chatList.add(ChatItem(userId = 1, message = it))
                        inputMessage = ""
                    },
                )
            }
        }
    }
    if (showBottomSheet) {
        var clickedItem by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = White,
        ) {
            when (clickedItem) {
                "vote" -> {
                    var title by remember { mutableStateOf("") }

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.45f),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "찬반투표 만들기",
                                style = Typography.headlineMedium,
                                color = Black,
                            )
                            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                value = title,
                                onValueChanged = {
                                    title = it
                                },
                                placeHolder = "제목을 작성해 주세요",
                                borderColor = Brown01,
                                focusManager = LocalFocusManager.current,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            RoundLongButton(
                                text = "투표 올리기",
                                backgroundColor = Brown01,
                                onClick = {
                                    // 투표 전송 api 실행
                                    showBottomSheet = false
                                },
                                enabled = title.isNotBlank(),
                            )
                        }
                    }
                }

                "voice" -> {
                    VoiceRecordScreen(
                        onDismiss = { showBottomSheet = false },
                        onRecordingComplete = {
                            // 음성 메시지 전송 api 호출
                        },
                        showSnackBar = showSnackBar,
                        popUpBackStack = { clickedItem = "" },
                    )
                }

                else -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.3f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .wrapContentSize()
                                    .padding(bottom = 35.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .wrapContentSize()
                                        .noRippleClickable {
                                            // 찬반투표 클릭 시
                                            clickedItem = "vote"
                                        },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .clip(shape = CircleShape)
                                            .background(color = Green05)
                                            .padding(15.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_vote),
                                        contentDescription = "vote",
                                        tint = Black,
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "찬반투표",
                                    style = Typography.displayMedium.copy(fontSize = 16.sp),
                                    color = Black,
                                )
                            }
                            Spacer(modifier = Modifier.width(30.dp))
                            Column(
                                modifier =
                                    Modifier
                                        .wrapContentSize()
                                        .noRippleClickable {
                                            // 음성메시지 클릭 시
                                            clickedItem = "voice"
                                        },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .clip(shape = CircleShape)
                                            .background(color = Green04)
                                            .padding(15.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_voice),
                                        contentDescription = "voice_message",
                                        tint = Black,
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "음성메시지",
                                    style = Typography.displayMedium.copy(fontSize = 16.sp),
                                    color = Black,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}