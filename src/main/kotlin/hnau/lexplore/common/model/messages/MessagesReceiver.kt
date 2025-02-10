package hnau.lexplore.common.model.messages

interface MessagesReceiver<T> {

    fun sendMessage(message: T)
}
