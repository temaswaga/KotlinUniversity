package com.example.myapplication.Task12

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.random.Random

class FactsViewModel : ViewModel() {

    private val facts = listOf(
        "Слоны — единственные животные, которые не умеют прыгать.",
        "Сердце синего кита весит около тонны и бьётся всего 6 раз в минуту.",
        "Отпечатки пальцев коалы практически неотличимы от человеческих.",
        "У осьминогов три сердца, а их кровь имеет голубой цвет.",
        "Фламинго могут пить кипящую воду и выживать в щелочных озёрах.",
        "Ленивцы могут задерживать дыхание под водой до 40 минут.",
        "Белые медведи имеют прозрачную шерсть и абсолютно чёрную кожу.",
        "Колибри — единственные птицы на Земле, способные летать задом наперёд.",
        "У тигров полосатая не только шерсть, но и сама кожа.",
        "Жирафы спят всего от 10 минут до 2 часов в сутки.",
        "Муравьи никогда не спят и не имеют лёгких.",
        "Глаз гигантского кальмара может достигать размеров футбольного мяча.",
        "Морские выдры держатся за лапы во сне, чтобы их не унесло течением.",
        "У кошек более 20 различных мышц, управляющих каждым ухом.",
        "Хамелеоны могут двигать глазами независимо друг от друга в двух направлениях."
    )

    private val _factState = MutableStateFlow<String?>("Нажми кнопку, чтобы узнать факт!")
    val factState = _factState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun getRandomFact(): Flow<String> = flow {
        delay(2000L)
        val randomIndex = Random.nextInt(facts.size)
        emit(facts[randomIndex])
    }

    fun loadNewFact() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            getRandomFact().collect { fact ->
                _factState.value = fact
                _isLoading.value = false
            }
        }
    }
}