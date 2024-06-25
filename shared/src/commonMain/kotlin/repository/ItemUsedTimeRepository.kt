package repository

import io.File
import model.ListItem
import util.DateTime
import util.Log
import util.parseJson
import util.stringifyJson

interface ItemUsedTimeRepository<T : ListItem<T>> {
    fun loadUsedTimes()

    fun getUsedTime(name: String): Long

    fun saveUsedTime(
        name: String,
        time: Long,
    )

    fun updateUsedTime(name: String)
}

class ItemUsedTimeRepositoryImpl<T : ListItem<T>>(
    private val recordFile: File,
    private val updateItems: ((List<T>) -> List<T>) -> Unit,
) : ItemUsedTimeRepository<T> {
    private val usedTimeMap: MutableMap<String, Long> = mutableMapOf()

    override fun loadUsedTimes() {
        runCatching {
            recordFile.takeIf { it.exists() }?.readText()?.runCatching { parseJson<Map<String, Long>>() }
                ?.getOrNull()
                ?.toMutableMap() ?: mutableMapOf()
        }.onSuccess { map ->
            usedTimeMap.clear()
            usedTimeMap.putAll(map)
        }.onFailure {
            Log.w(it)
        }
    }

    override fun getUsedTime(name: String): Long = usedTimeMap[name] ?: 0L

    override fun saveUsedTime(
        name: String,
        time: Long,
    ) {
        usedTimeMap[name] = time
        runCatching {
            recordFile.writeText(usedTimeMap.stringifyJson())
        }.onFailure {
            Log.w(it)
        }
    }

    override fun updateUsedTime(name: String) {
        updateItems { items ->
            items.map {
                if (it.name == name) {
                    it.usedTimeUpdated(DateTime.getNow())
                } else {
                    it
                }
            }
        }
        saveUsedTime(name, DateTime.getNow())
    }
}
