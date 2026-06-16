package com.example.nutriscan.ui.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileActionButtons(
    primaryColor: Color,
    warningColor: Color,
    showClearProfile: Boolean,
    isDarkMode: Boolean,
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color,
    onToggleTheme: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenGuidePdf: () -> Unit,
    onClearProfile: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Nhập sửa thông tin"
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            Text(
                text = "Nhập / sửa thông tin",
                fontWeight = FontWeight.Bold

            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackground
            ),
            border = BorderStroke(
                width = 1.dp,
                color = borderColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.WbSunny,
                    contentDescription = "Chế độ giao diện",
                    tint = primaryColor
                )

                Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Chế độ giao diện",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isDarkMode) "Đang bật chế độ tối" else "Đang bật chế độ sáng",
                        color = textSecondary
                    )
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        onToggleTheme()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryColor,
                        uncheckedThumbColor = primaryColor,
                        uncheckedTrackColor = primaryColor.copy(alpha = 0.18f),
                        uncheckedBorderColor = primaryColor.copy(alpha = 0.45f)
                    )
                )
            }
        }

        OutlinedButton(
            onClick = onOpenGuidePdf,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = borderColor
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = cardBackground,
                contentColor = primaryColor
            )
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "Mở hướng dẫn sử dụng"
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            Text(
                text = "Mở hướng dẫn sử dụng PDF",
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showClearProfile) {
            OutlinedButton(
                onClick = onClearProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = warningColor.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = warningColor.copy(alpha = 0.08f),
                    contentColor = warningColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa thông tin cá nhân"
                )

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                Text(
                    text = "Xóa thông tin cá nhân",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}