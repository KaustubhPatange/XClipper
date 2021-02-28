package com.kpstv.xclipper.data.provider

import android.content.Context
import android.net.Uri
import com.kpstv.xclipper.data.db.MainDatabase
import com.kpstv.xclipper.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.nio.charset.Charset
import javax.inject.Inject

class BackupProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MainDatabase
) : BackupProvider {

    override suspend fun backup(toUri: Uri): Boolean {
        val clipTags = database.clipTagDao().getAllData()
        val clipUrls = database.clipUrlDao().getAllData()
        val clipDefines = database.clipDefineDao().getAllWords()
        val clipData = database.clipDataDao().getAllData() ?: return false

        val obj = JSONObject().apply {
            put(database::clipTagDao::name.get(), TagListConverter.toStringFromTag(clipTags))
            put(database::clipUrlDao::name.get(), UrlInfoListConverter.toStringFromUrlInfo(clipUrls))
            put(database::clipDefineDao::name.get(), DefinitionListConverter.toStringFromDefinition(clipDefines))
            put(database::clipDataDao::name.get(), ClipListConverter.toStringFromClip(clipData))
        }

        context.contentResolver.openOutputStream(toUri)?.apply {
            write(obj.toString().toByteArray())
            close()
        }

        return true
    }

    override suspend fun restore(fromUri: Uri): Boolean {
        try {
            val stream = context.contentResolver.openInputStream(fromUri) ?: return false
            val json = stream.readBytes().toString(charset = Charset.defaultCharset())
            stream.close()

            JSONObject(json).apply {
                val clipTags: List<Tag> = TagListConverter.fromStringToTag(getString(database::clipTagDao::name.get()))
                val clipUrls: List<UrlInfo> = UrlInfoListConverter.fromStringToUrlInfo(getString(database::clipUrlDao::name.get()))
                val clipDefines: List<Definition> = DefinitionListConverter.fromStringToDefinition(getString(database::clipDefineDao::name.get()))
                val clipData: List<Clip> = ClipListConverter.fromStringToClip(getString(database::clipDataDao::name.get()))

                database.clipTagDao().deleteAll()
                database.clipTagDao().insert(clipTags)

                database.clipUrlDao().deleteAll()
                database.clipUrlDao().insert(clipUrls)

                database.clipDefineDao().deleteAll()
                database.clipDefineDao().insert(clipDefines)

                database.clipDataDao().deleteAll()
                database.clipDataDao().insert(clipData)
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}