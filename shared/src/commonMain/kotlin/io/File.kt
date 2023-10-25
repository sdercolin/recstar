package io

/**
 * A cross-platform File API abstraction based on the java.io.File API.
 * - On the Desktop or Android, this is a simple wrapper around java.io.File.
 * - On iOS, this is a wrapper around the iOS Foundation API.
 *
 * @param path The path to the file. This can be a relative or absolute path.
 */
expect class File(path: String) {
    /** Returns true if the file exists. */
    fun exists(): Boolean

    /** Returns the absolute path to the file. */
    val absolutePath: String

    /** Returns true if the file is existing and is a file (not a directory). */
    val isFile: Boolean

    /** Returns true if the file is existing and is a directory. */
    val isDirectory: Boolean

    /** Returns all children of this directory. */
    fun listFiles(): List<File>

    /**
     * Creates the directory and all its parent directories if they do not exist yet.
     *
     * @return true if the directory was created, false otherwise.
     */
    fun mkdirs(): Boolean

    /**
     * Deletes the file or directory.
     *
     * @return true if the file or directory was deleted, false otherwise.
     */
    fun delete(): Boolean

    /** Writes the given text to the file using UTF-8. */
    fun writeText(text: String)

    /** Reads the entire file as a String using UTF-8. */
    fun readText(): String

    /**
     * Returns a new file with the given [path] relative to this file, or if [path] is absolute, returns a new file with
     * the absolute path.
     */
    fun resolve(path: String): File
}
