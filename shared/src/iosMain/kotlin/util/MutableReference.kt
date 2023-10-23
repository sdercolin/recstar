package util

class MutableReference<T> {
    private var value: T? = null

    fun get(): T? = value

    fun set(newValue: T?) {
        value = newValue
    }
}