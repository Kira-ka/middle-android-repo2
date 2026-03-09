package ru.yandex.praktikumchatapp.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class ChatState(
    val messagesList: List<Message> = listOf(),
    val messageText: String = "",
    val isShouldShowKeyboard: Boolean = false,
)
