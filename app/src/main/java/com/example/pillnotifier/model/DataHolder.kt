package com.example.pillnotifier.model;

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.HashMap;

object DataHolder {
    private var data = HashMap<String, String>()
    fun getData(key: String): String? {
        return data[key]
    }
    fun setData(key: String, value: String) {
        data[key] = value
    }
    fun removeData(key: String) {
        data.remove(key)
    }
    private const val dataHolderCacheFile = "dataHolderCacheFile"
    fun cache(context: Context) {
        ObjectOutputStream(
            context.openFileOutput(
                dataHolderCacheFile,
                Context.MODE_PRIVATE
            )
        ).writeObject(data)
    }
    fun uploadFromCache(context: Context) {
        if (context.fileList().contains(dataHolderCacheFile)) {
            data =
                ObjectInputStream(context.openFileInput(dataHolderCacheFile)).readObject() as HashMap<String, String>
        }
    }
}
