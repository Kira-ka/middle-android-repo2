import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import ru.yandex.praktikumchatapp.data.ChatRepository
import ru.yandex.praktikumchatapp.presentation.ChatViewModel
import ru.yandex.praktikumchatapp.presentation.Message

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    private var testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val responses = flowOf("Отлично!")

    private lateinit var viewModel: ChatViewModel
    private val mockRepository: ChatRepository = mock<ChatRepository> {
        on { getReplyMessage() } doReturn responses
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(isWithReplies = false, repository = mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send message should update state with MyMessage`() = runTest {
        val message = "Тестовое сообщение"
        viewModel.state.test {

            awaitItem()

            viewModel.sendMyMessage(message)

            val afterSend = awaitItem()

            assert(
                afterSend.messagesList == listOf(
                    Message.MyMessage(message)
                )
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testReceiveMessage_concurrentMessages() = runTest {
        val messagesToSend = (1..100).map { Message.MyMessage("Message $it") }

        viewModel.state.test {

            coroutineScope {
                val jobs = messagesToSend.map { msg ->
                    launch {
                        viewModel.sendMyMessage(msg.text)
                    }
                }

                jobs.joinAll()
            }

            repeat(messagesToSend.size) { awaitItem() }

            val finalState = awaitItem()
            assert(finalState.messagesList.size == messagesToSend.size) {
                "Ожидалось ${messagesToSend.size} сообщений, " +
                        "получено ${finalState.messagesList.size}"
            }
            assert(finalState.messagesList == messagesToSend) {
                "Список сообщений отличается от ожидаемого"
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
