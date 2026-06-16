package com.example.nutriscan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.local.dao.FoodDiaryDao
import com.example.nutriscan.data.local.dao.UserDao
import com.example.nutriscan.data.local.entity.FoodDiaryEntity
import com.example.nutriscan.data.local.entity.UserEntity
import com.example.nutriscan.data.remote.RetrofitClient
import com.example.nutriscan.data.remote.dto.ProductDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.round

data class DailyCalorieStat(
    val day: Int,
    val calories: Int
)

class NutriViewModel(
    private val userDao: UserDao,
    private val foodDiaryDao: FoodDiaryDao
) : ViewModel() {

    var bmiResult = MutableStateFlow(0.0)
        private set

    var bmiStatus = MutableStateFlow("Chưa thiết lập")
        private set

    var goalCalories = MutableStateFlow(2000)
        private set

    var goalProtein = MutableStateFlow(50.0)
        private set

    var goalCarbs = MutableStateFlow(250.0)
        private set

    var searchUiState = MutableStateFlow<List<ProductDto>>(emptyList())
        private set

    var isLoading = MutableStateFlow(false)
        private set

    var totalCalories = MutableStateFlow(0)
        private set

    var totalProtein = MutableStateFlow(0.0)
        private set

    var totalCarbs = MutableStateFlow(0.0)
        private set

    var todayFoodList = MutableStateFlow<List<FoodDiaryEntity>>(emptyList())
        private set

    var currentUser = MutableStateFlow<UserEntity?>(null)
        private set

    var selectedDate = MutableStateFlow(getTodayDate())
        private set

    var monthlyCalorieStats = MutableStateFlow<List<DailyCalorieStat>>(emptyList())
        private set

    var isDarkMode = MutableStateFlow(false)
        private set

    init {
        loadUserDataFromRoom()
        loadNutritionByDate(selectedDate.value)
        loadMonthlyCalorieStats()
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
        viewModelScope.launch {
            val user = userDao.getUser() ?: UserEntity(name = "", weight = 0.0, height = 0.0, age = 0)
            userDao.insertOrUpdateUser(user.copy(isDarkMode = isDarkMode.value))
        }
    }

    private fun String.removeAccents(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("")
            .replace("đ", "d")
            .replace("Đ", "D")
            .lowercase()
            .trim()
    }

    private fun getCalories(product: ProductDto): Double {
        return product.nutriments?.calories ?: 0.0
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    fun changeSelectedDate(date: String) {
        selectedDate.value = date
        loadNutritionByDate(date)
        loadMonthlyCalorieStats()
    }

    fun goToPreviousDay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            val date = dateFormat.parse(selectedDate.value)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            calendar.time = Date()
        }

        calendar.add(Calendar.DAY_OF_YEAR, -1)

        val newDate = formatDate(calendar.time)
        selectedDate.value = newDate
        loadNutritionByDate(newDate)
        loadMonthlyCalorieStats()
    }

    fun goToNextDay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            val date = dateFormat.parse(selectedDate.value)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            calendar.time = Date()
        }

        calendar.add(Calendar.DAY_OF_YEAR, 1)

        val newDate = formatDate(calendar.time)
        selectedDate.value = newDate
        loadNutritionByDate(newDate)
        loadMonthlyCalorieStats()
    }

    fun goToToday() {
        val today = getTodayDate()
        selectedDate.value = today
        loadNutritionByDate(today)
        loadMonthlyCalorieStats()
    }

    private fun loadUserDataFromRoom() {
        viewModelScope.launch {
            val user = userDao.getUser()

            if (user != null) {
                isDarkMode.value = user.isDarkMode
                
                if (user.weight > 0.0 && user.height > 0.0) {
                    currentUser.value = user

                    calculateBMI(
                        weight = user.weight,
                        height = user.height,
                        saveToRoom = false
                    )
                } else {
                    resetToDefaultProfile()
                }
            } else {
                resetToDefaultProfile()
            }
        }
    }

    private fun resetToDefaultProfile() {
        currentUser.value = null
        bmiResult.value = 0.0
        bmiStatus.value = "Chưa thiết lập"

        goalCalories.value = 2000
        goalProtein.value = 50.0
        goalCarbs.value = 250.0
    }

    fun saveUserProfile(
        name: String,
        weight: Double,
        height: Double,
        age: Int
    ) {
        viewModelScope.launch {
            val user = UserEntity(
                id = 1,
                name = name,
                weight = weight,
                height = height,
                age = age,
                isDarkMode = isDarkMode.value
            )

            userDao.insertOrUpdateUser(user)
            currentUser.value = user

            calculateBMI(
                weight = weight,
                height = height,
                saveToRoom = false
            )
        }
    }

    fun clearUserProfile() {
        viewModelScope.launch {
            val emptyUser = UserEntity(
                id = 1,
                name = "",
                weight = 0.0,
                height = 0.0,
                age = 0,
                isDarkMode = isDarkMode.value
            )

            userDao.insertOrUpdateUser(emptyUser)

            resetToDefaultProfile()

            loadNutritionByDate(selectedDate.value)
            loadMonthlyCalorieStats()
        }
    }

    fun calculateBMI(
        weight: Double,
        height: Double,
        saveToRoom: Boolean = true
    ) {
        if (weight > 0.0 && height > 0.0) {
            val heightInMeters = height / 100.0
            val bmi = weight / (heightInMeters * heightInMeters)

            bmiResult.value = round(bmi * 10) / 10.0

            bmiStatus.value = when {
                bmi < 18.5 -> "Thiếu cân"
                bmi < 25.0 -> "Bình thường"
                bmi < 30.0 -> "Thừa cân"
                else -> "Béo phì"
            }

            val calo = when {
                bmi < 18.5 -> (weight * 35).toInt()
                bmi < 25.0 -> (weight * 30).toInt()
                bmi < 30.0 -> (weight * 27).toInt()
                else -> (weight * 24).toInt()
            }

            goalCalories.value = calo
            goalProtein.value = (calo * 0.30) / 4.0
            goalCarbs.value = (calo * 0.50) / 4.0

            if (saveToRoom) {
                viewModelScope.launch {
                    val user = UserEntity(
                        id = 1,
                        name = currentUser.value?.name ?: "User",
                        weight = weight,
                        height = height,
                        age = currentUser.value?.age ?: 20,
                        isDarkMode = isDarkMode.value
                    )

                    userDao.insertOrUpdateUser(user)
                    currentUser.value = user
                }
            }
        }
    }

    fun loadTodayNutritionFromRoom() {
        loadNutritionByDate(selectedDate.value)
    }

    fun loadNutritionByDate(date: String) {
        viewModelScope.launch {
            val foods = foodDiaryDao.getFoodsByDate(date)

            todayFoodList.value = foods

            var calo = 0
            var protein = 0.0
            var carbs = 0.0

            foods.forEach { food ->
                calo += food.calories.toInt()
                protein += food.protein
                carbs += food.carbs
            }

            totalCalories.value = calo
            totalProtein.value = protein
            totalCarbs.value = carbs
        }
    }

    fun loadMonthlyCalorieStats() {
        viewModelScope.launch {
            val allFoods = foodDiaryDao.getAllFoods()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val selectedCalendar = Calendar.getInstance()

            try {
                val selected = dateFormat.parse(selectedDate.value)
                if (selected != null) {
                    selectedCalendar.time = selected
                }
            } catch (e: Exception) {
                selectedCalendar.time = Date()
            }

            val selectedMonth = selectedCalendar.get(Calendar.MONTH)
            val selectedYear = selectedCalendar.get(Calendar.YEAR)

            val foodsInSelectedMonth = allFoods.filter { food ->
                try {
                    val foodDate = dateFormat.parse(food.date)
                    if (foodDate != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = foodDate

                        calendar.get(Calendar.MONTH) == selectedMonth &&
                                calendar.get(Calendar.YEAR) == selectedYear
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }

            val result = foodsInSelectedMonth
                .groupBy { food ->
                    val foodDate = dateFormat.parse(food.date)
                    val calendar = Calendar.getInstance()
                    calendar.time = foodDate ?: Date()
                    calendar.get(Calendar.DAY_OF_MONTH)
                }
                .map { entry ->
                    DailyCalorieStat(
                        day = entry.key,
                        calories = entry.value.sumOf { it.calories.toInt() }
                    )
                }
                .sortedBy { it.day }

            monthlyCalorieStats.value = result
        }
    }

    fun saveFoodToDiary(
        product: ProductDto,
        gram: Double = 100.0
    ) {
        viewModelScope.launch {
            if (gram <= 0.0) {
                return@launch
            }

            val nutriments = product.nutriments
            val multiplier = gram / 100.0
            val caloriesValue = getCalories(product)

            val foodEntity = FoodDiaryEntity(
                foodName = product.productName ?: "Món ăn",
                mealType = "",
                gram = gram,
                calories = caloriesValue * multiplier,
                protein = (nutriments?.protein ?: 0.0) * multiplier,
                carbs = (nutriments?.carbs ?: 0.0) * multiplier,
                date = selectedDate.value
            )

            foodDiaryDao.insertFood(foodEntity)

            loadNutritionByDate(selectedDate.value)
            loadMonthlyCalorieStats()
        }
    }

    fun updateFoodGram(
        food: FoodDiaryEntity,
        newGram: Double
    ) {
        viewModelScope.launch {
            if (newGram <= 0.0 || food.gram <= 0.0) {
                return@launch
            }

            val caloriesPer100g = food.calories / food.gram * 100.0
            val proteinPer100g = food.protein / food.gram * 100.0
            val carbsPer100g = food.carbs / food.gram * 100.0

            val multiplier = newGram / 100.0

            val updatedFood = food.copy(
                gram = newGram,
                calories = caloriesPer100g * multiplier,
                protein = proteinPer100g * multiplier,
                carbs = carbsPer100g * multiplier
            )

            foodDiaryDao.updateFood(updatedFood)

            loadNutritionByDate(selectedDate.value)
            loadMonthlyCalorieStats()
        }
    }

    fun deleteFoodFromDiary(food: FoodDiaryEntity) {
        viewModelScope.launch {
            foodDiaryDao.deleteFood(food)

            loadNutritionByDate(selectedDate.value)
            loadMonthlyCalorieStats()
        }
    }

    fun updateFoodInDiary(food: FoodDiaryEntity) {
        viewModelScope.launch {
            foodDiaryDao.updateFood(food)

            loadNutritionByDate(selectedDate.value)
            loadMonthlyCalorieStats()
        }
    }

    fun searchFoodFromApi(query: String) {
        val cleanQuery = query.removeAccents()

        if (cleanQuery.isBlank()) {
            searchUiState.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                isLoading.value = true

                val response = RetrofitClient.api.searchFood(query)

                val results = response.products
                    .filter { product ->
                        val productName = product.productName ?: ""
                        val calories = getCalories(product)
                        val cleanProductName = productName.removeAccents()

                        productName.isNotBlank() &&
                                calories > 0.0 &&
                                cleanProductName.contains(cleanQuery)
                    }
                    .sortedWith(
                        compareBy<ProductDto> { product ->
                            val name = product.productName?.removeAccents() ?: ""

                            when {
                                name == cleanQuery -> 0
                                name.startsWith(cleanQuery) -> 1
                                name.contains(cleanQuery) -> 2
                                else -> 3
                            }
                        }.thenBy { product ->
                            product.productName?.length ?: 999
                        }
                    )
                    .distinctBy { product ->
                        product.productName?.removeAccents()?.trim()
                    }
                    .take(20)

                searchUiState.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                searchUiState.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }
}

class NutriViewModelFactory(
    private val userDao: UserDao,
    private val foodDiaryDao: FoodDiaryDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutriViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutriViewModel(
                userDao = userDao,
                foodDiaryDao = foodDiaryDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}