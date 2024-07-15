package com.team2.chitchat.ui.chat

import androidx.lifecycle.viewModelScope
import com.team2.chitchat.data.domain.model.chats.GetChatModel
import com.team2.chitchat.data.domain.model.messages.GetMessagesModel
import com.team2.chitchat.data.mapper.chats.GetChatMapper
import com.team2.chitchat.data.mapper.messages.MessagesMapper
import com.team2.chitchat.data.repository.remote.request.messages.NewMessageRequest
import com.team2.chitchat.data.repository.remote.response.BaseResponse
import com.team2.chitchat.data.usecase.local.GetChatUseCase
import com.team2.chitchat.data.usecase.local.GetMessagesForChatUseCase
import com.team2.chitchat.data.usecase.remote.PostNewMessageUseCase
import com.team2.chitchat.hilt.SimpleApplication
import com.team2.chitchat.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesForChatUseCase: GetMessagesForChatUseCase,
    private val messagesMapper: MessagesMapper,
    private val simpleApplication: SimpleApplication,
    private val postNewMessageUseCase: PostNewMessageUseCase,
    private val getChatUseCase: GetChatUseCase,
    private val getChatMapper: GetChatMapper
) :
    BaseViewModel() {
    private val messagesMutableStateFlow: MutableStateFlow<List<GetMessagesModel>> =
        MutableStateFlow(
            emptyList()
        )
    val messagesStateFlow: StateFlow<List<GetMessagesModel>> = messagesMutableStateFlow
    private val chatMutableStateFlow: MutableStateFlow<GetChatModel> =
        MutableStateFlow(GetChatModel("", "", "", false))
    val chatStateFlow: StateFlow<GetChatModel> = chatMutableStateFlow

    fun getMessagesForChat(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getMessagesForChatUseCase(chatId).collect {
                when (it) {
                    is BaseResponse.Error -> {
                        errorMutableSharedFlow.emit(it.error)
                    }

                    is BaseResponse.Success -> {
                        val messages = messagesMapper.getMessages(it.data)
                        messagesMutableStateFlow.value = messages
                    }
                }
            }
        }
    }

    fun getChat(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getChatUseCase(chatId).collect {
                when (it) {
                    is BaseResponse.Error -> {
                        errorMutableSharedFlow.emit(it.error)
                    }

                    is BaseResponse.Success -> {
                        val chat = getChatMapper.getChat(it.data)
                        chatMutableStateFlow.value = chat
                    }
                }
            }
        }
    }

    fun postNewMessage(message: String, chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMessage = NewMessageRequest(chatId, simpleApplication.getUserID(), message)
            postNewMessageUseCase(newMessage).collect {
                when (it) {
                    is BaseResponse.Error -> {
                        errorMutableSharedFlow.emit(it.error)
                    }

                    is BaseResponse.Success -> {
                        getMessagesForChat(chatId)
                    }
                }
            }
        }
    }
}