package com.familring.presentation.screen.interest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familring.presentation.R
import com.familring.presentation.theme.Black
import com.familring.presentation.theme.Green01
import com.familring.presentation.theme.Typography
import com.familring.presentation.theme.White

@Composable
fun InterestSelectDialog(
    modifier: Modifier = Modifier,
    count: Int = 0,
    saveInterest: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Black.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
            Image(
                painter = painterResource(id = R.drawable.img_clap),
                contentDescription = "clap",
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.08f))
            Text(
                text = "${count}명의 가족이 관심사를 작성했네요!",
                style = Typography.displayMedium,
                color = White,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Text(
                    text = "이제 작성된 관심사 중",
                    style = Typography.displayMedium,
                    color = White,
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "하나를 선정",
                    style = Typography.titleSmall.copy(fontSize = 20.sp),
                    color = Green01,
                )
                Text(
                    text = "할 수 있어요",
                    style = Typography.displayMedium,
                    color = White,
                )
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
            Text(
                modifier =
                    Modifier
                        .clickable { saveInterest() }
                        .background(color = Green01, shape = RoundedCornerShape(50.dp))
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                text = "이제 관심사를 선정할래요",
                style = Typography.titleSmall,
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.08f))
            Text(
                text = "다음에 할래요",
                style = Typography.headlineMedium.copy(fontSize = 18.sp),
                color = White,
            )
        }
    }
}

@Preview
@Composable
fun InterestSelectDialogPreview() {
    InterestSelectDialog()
}