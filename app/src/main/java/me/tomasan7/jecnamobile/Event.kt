package me.tomasan7.jecnamobile

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandledOrReturnNull(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun handleIfNotHandledYet(block: (T) -> Unit)
    {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            block(content)
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

fun <T> T.asEvent() = Event<T>(this)