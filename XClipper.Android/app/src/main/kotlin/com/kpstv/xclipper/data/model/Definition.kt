package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType

@AutoGenerateListConverter(using = ConverterType.GSON)
@Entity(tableName = "table_define")
data class Definition(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val word: String?,
    val define: String?
) {
    companion object {
        fun from(word: String?, define: String?): Definition {
            return Definition(word = word, define = define)
        }
        fun returnNull() = Definition(word = null, define = null)
    }
}