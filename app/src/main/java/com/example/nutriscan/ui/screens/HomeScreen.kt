package com.example.nutriscan.ui.screens
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.nutriscan.data.remote.dto.ProductDto
import com.example.nutriscan.ui.components.BottomNavigationBar
import com.example.nutriscan.ui.components.home.CalorieSummaryCard
import com.example.nutriscan.ui.components.home.FoodResultItem
import com.example.nutriscan.ui.components.home.FoodSearchBox
import com.example.nutriscan.ui.components.home.HomeHeader
import com.example.nutriscan.ui.components.home.MacroBox
import com.example.nutriscan.ui.components.home.NutritionSetupCard
import com.example.nutriscan.ui.theme.getNutriAppColors
import com.example.nutriscan.ui.viewmodel.NutriViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: NutriViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedProduct by remember { mutableStateOf<ProductDto?>(null) }
    var gramInput by remember { mutableStateOf("100") }
    var gramErrorMessage by remember { mutableStateOf("") }

    val foodList by viewModel.searchUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val totalCalories by viewModel.totalCalories.collectAsState()
    val totalProtein by viewModel.totalProtein.collectAsState()
    val totalCarbs by viewModel.totalCarbs.collectAsState()

    val goalCalories by viewModel.goalCalories.collectAsState()
    val goalProtein by viewModel.goalProtein.collectAsState()
    val goalCarbs by viewModel.goalCarbs.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val appColors = getNutriAppColors(isDarkMode)

    val primaryColor = appColors.primary
    val warningColor = appColors.warning
    val backgroundColor = appColors.background
    val cardBackground = appColors.card

    val gradientPrimary = Brush.horizontalGradient(
        colors = listOf(
            appColors.primary.copy(alpha = 0.75f),
            appColors.primary
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = "home_screen"
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                HomeHeader(
                    navController = navController,
                    currentUser = currentUser,
                    gradientPrimary = gradientPrimary,
                    textPrimary = appColors.textPrimary,
                    textSecondary = appColors.textSecondary
                )
            }

            item {
                NutritionSetupCard(
                    primaryColor = primaryColor,
                    cardBackground = cardBackground,
                    textPrimary = appColors.textPrimary,
                    textSecondary = appColors.textSecondary,
                    onClick = {
                        navController.navigate("profile_screen")
                    }
                )
            }

            item {
                HomeDateSelector(
                    selectedDate = selectedDate,
                    primaryColor = primaryColor,
                    cardBackground = cardBackground,
                    textPrimary = appColors.textPrimary,
                    textSecondary = appColors.textSecondary,
                    borderColor = appColors.border,
                    onPreviousDay = {
                        viewModel.goToPreviousDay()
                    },
                    onNextDay = {
                        viewModel.goToNextDay()
                    },
                    onDateSelected = { date ->
                        viewModel.changeSelectedDate(date)
                    }
                )
            }

            item {
                CalorieSummaryCard(
                    selectedDate = selectedDate,
                    totalCalories = totalCalories,
                    goalCalories = goalCalories,
                    primaryColor = primaryColor,
                    warningColor = warningColor,
                    cardBackground = cardBackground,
                    textPrimary = appColors.textPrimary,
                    textSecondary = appColors.textSecondary,
                    progressBackground = appColors.border
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroBox(
                        modifier = Modifier.weight(1f),
                        label = "Đạm",
                        value = totalProtein,
                        goal = if (goalProtein > 0.0) goalProtein else 50.0,
                        color = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFFF5252),
                        trackColor = if (isDarkMode) Color(0xFF3A1F1F) else Color(0xFFFFEBEE),
                        icon = "🥩",
                        cardBackground = cardBackground,
                        textPrimary = appColors.textPrimary,
                        textSecondary = appColors.textSecondary
                    )

                    MacroBox(
                        modifier = Modifier.weight(1f),
                        label = "Tinh bột",
                        value = totalCarbs,
                        goal = if (goalCarbs > 0.0) goalCarbs else 250.0,
                        color = appColors.blue,
                        trackColor = if (isDarkMode) Color(0xFF1E2A3A) else Color(0xFFE3F2FD),
                        icon = "🌾",
                        cardBackground = cardBackground,
                        textPrimary = appColors.textPrimary,
                        textSecondary = appColors.textSecondary
                    )
                }
            }

            item {
                FoodSearchBox(
                    searchQuery = searchQuery,
                    primaryColor = primaryColor,
                    cardBackground = cardBackground,
                    textPrimary = appColors.textPrimary,
                    textSecondary = appColors.textSecondary,
                    borderColor = appColors.border,
                    onQueryChange = { query ->

                        searchQuery = query

                        if (isInternetAvailable(context)) {

                            viewModel.searchFoodFromApi(query)

                        } else {

                            Toast.makeText(
                                context,
                                "Không có kết nối Internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            items(foodList) { product ->
                FoodResultItem(
                    product = product,
                    selectedDate = selectedDate,
                    primaryColor = primaryColor,
                    cardBackground = cardBackground,
                    textPrimary = appColors.textPrimary,
                    textSecondary = appColors.textSecondary,
                    onAddClick = { selectedFood ->
                        viewModel.saveFoodToDiary(
                            product = selectedFood,
                            gram = 100.0
                        )
                    },
                    onItemClick = { selectedFood ->
                        selectedProduct = selectedFood
                        gramInput = "100"
                        gramErrorMessage = ""
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    if (selectedProduct != null) {
        AlertDialog(
            onDismissRequest = {
                selectedProduct = null
                gramErrorMessage = ""
            },
            title = {
                Text(
                    text = "Nhập khối lượng",
                    color = appColors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bạn ăn bao nhiêu gram?",
                        color = appColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = gramInput,
                        onValueChange = { value ->
                            gramInput = value.filter { char ->
                                char.isDigit() || char == '.'
                            }
                            gramErrorMessage = ""
                        },
                        label = {
                            Text(text = "Khối lượng (gram)")
                        },
                        singleLine = true
                    )

                    if (gramErrorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = gramErrorMessage,
                            color = warningColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val gram = gramInput.trim().toDoubleOrNull()

                        if (gram == null || gram <= 0.0) {
                            gramErrorMessage = "Bạn phải nhập số lớn hơn 0"
                            return@TextButton
                        }

                        selectedProduct?.let { product ->
                            viewModel.saveFoodToDiary(
                                product = product,
                                gram = gram
                            )
                        }

                        selectedProduct = null
                        gramErrorMessage = ""
                    }
                ) {
                    Text(
                        text = "Lưu",
                        color = primaryColor
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedProduct = null
                        gramErrorMessage = ""
                    }
                ) {
                    Text(
                        text = "Hủy",
                        color = appColors.textSecondary
                    )
                }
            },
            containerColor = cardBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDateSelector(
    selectedDate: String,
    primaryColor: Color,
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onPreviousDay,
                modifier = Modifier.size(44.dp)
            ) {
                Text(
                    text = "‹",
                    color = primaryColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDatePicker = true
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ngày lưu món ăn",
                    color = textSecondary,
                    fontSize = 12.sp
                )

                Text(
                    text = selectedDate,
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onNextDay,
                modifier = Modifier.size(44.dp)
            ) {
                Text(
                    text = "›",
                    color = primaryColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateToMillis(selectedDate)
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis

                        if (millis != null) {
                            val date = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date(millis))

                            onDateSelected(date)
                        }

                        showDatePicker = false
                    }
                ) {
                    Text(
                        text = "Chọn",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text(
                        text = "Hủy",
                        color = textSecondary
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

private fun selectedDateToMillis(date: String): Long? {
    return try {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).parse(date)?.time
    } catch (e: Exception) {
        null
    }
}
private fun isInternetAvailable(
    context: Context
): Boolean {

    val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val network =
        connectivityManager.activeNetwork
            ?: return false

    val capabilities =
        connectivityManager.getNetworkCapabilities(network)
            ?: return false

    return capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}