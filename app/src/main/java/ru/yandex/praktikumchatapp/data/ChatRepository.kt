package ru.yandex.praktikumchatapp.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import java.lang.Exception

class ChatRepository(
    private val api: ChatApi = ChatApi()
) {

    fun getReplyMessage(): Flow<String> {
        var delay = DELAY_START
        return api.getReply().retryWhen { cause, attempt ->
            if (cause is Exception) {
                delay(delay)
                delay = if (attempt < 2) delay * delay else DELAY_START
            }
            true
        }
    }

    companion object {
        private const val DELAY_START = 10L
    }
}
