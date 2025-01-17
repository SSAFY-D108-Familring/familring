package com.familring.presentation.component.textfield

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familring.presentation.theme.Black
import com.familring.presentation.theme.Gray03
import com.familring.presentation.theme.Gray04
import com.familring.presentation.theme.Typography
import org.apache.commons.lang3.StringUtils.isBlank

@Composable
fun GrayBackgroundTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    hint: String = "여기에 작성해 주세요",
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Gray04)
                .padding(16.dp),
    ) {
        BasicTextField(
            modifier =
                Modifier.verticalScroll(
                    state = scrollState,
                ),
            state = textFieldState,
            textStyle =
                Typography.bodyMedium.copy(
                    fontSize = 20.sp,
                    color = Black,
                ),
            decorator = { innerTextField ->
                if (textFieldState.text.isBlank()) {
                    Text(
                        modifier = Modifier.fillMaxSize(),
                        text = hint,
                        style =
                            Typography.bodyMedium.copy(
                                fontSize = 20.sp,
                                color = Gray03,
                            ),
                    )
                }
                innerTextField()
            },
        )
    }
}
