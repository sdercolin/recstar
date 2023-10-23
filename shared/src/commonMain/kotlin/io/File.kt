package io

expect class File(path: String) {

    fun exists(): Boolean
    val absolutePath: String
    val isFile: Boolean
    val isDirectory: Boolean
    fun listFiles(): List<File>
    fun mkdirs(): Boolean
    fun delete(): Boolean
    fun writeText(text: String)
    fun readText(): String
    fun resolve(path: String): File
}
