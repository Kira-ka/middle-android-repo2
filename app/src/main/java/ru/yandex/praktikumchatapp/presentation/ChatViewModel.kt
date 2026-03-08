package ru.yandex.praktikumchatapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.praktikumchatapp.data.ChatRepository

class ChatViewModel(
    val isWithReplies: Boolean = true,
    val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ChatState>(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isWithReplies) {
                repository.getReplyMessage()
                    .collect { response ->
                        updateState { idle(response) }
                    }
            }
        }
    }

    fun sendMyMessage(messageText: String) {
        updateState { sendMyMessage(messageText) }
    }

    fun updateMyMessage(messageText: String) {
        updateState { updateMyMessage(messageText) }
    }

    private fun updateState(modifier: ChatState.() -> ChatState) {
        _state.update { it.modifier() }
    }

    private fun ChatState.idle(response: String): ChatState = copy(
        messagesList = messagesList + Message.OtherMessage(response),
        isShouldShowKeyboard = messagesList.firstOrNull() == null
    )

    private fun ChatState.sendMyMessage(messageText: String): ChatState = copy(
        messagesList = messagesList + Message.MyMessage(messageText)
    )

    private fun ChatState.updateMyMessage(messageText: String): ChatState = copy(
        messageText = messageText
    )
}

