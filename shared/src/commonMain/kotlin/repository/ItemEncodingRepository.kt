package repository

import io.File
import util.Encoding
import util.Log
import util.parseJson
import util.stringifyJson

interface ItemEncodingRepository {
    fun putItemEncoding(
        category: String,
        name: String,
        encoding: Encoding?,
    )

    fun getItemEncoding(
        category: String,
        name: String,
    ): Encoding?
}

class ItemEncodingRepositoryImpl(
    private val categoryMapFileMap: Map<String, File>,
) : ItemEncodingRepository {
    override fun putItemEncoding(
        category: String,
        name: String,
        encoding: Encoding?,
    ) {
        val file = categoryMapFileMap[category]
        if (file == null) {
            Log.e("Unknown category in ItemEncodingRepository: $category")
            return
        }
        val map = getMap(file)
        if (encoding == null) {
            map.remove(name)
        } else {
            map[name] = encoding
        }
        file.writeText(map.stringifyJson())
    }

    override fun getItemEncoding(
        category: String,
        name: String,
    ): Encoding? {
        val file = categoryMapFileMap[category]
        if (file == null) {
            Log.e("Unknown category in ItemEncodingRepository: $category")
            return null
        }
        val map = getMap(file)
        return map[name]
    }

    private fun getMap(file: File): MutableMap<String, Encoding> {
        return if (file.exists()) {
            file.readText().parseJson()
        } else {
            mutableMapOf()
        }
    }
}
