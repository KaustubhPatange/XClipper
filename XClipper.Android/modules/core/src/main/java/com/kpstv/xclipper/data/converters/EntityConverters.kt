package com.kpstv.xclipper.data.converters

import com.google.gson.reflect.TypeToken
import com.kpstv.xclipper.data.model.*
import com.kpstv.xclipper.extensions.GsonConverter
import com.kpstv.xclipper.extensions.GsonListConverter

object ClipConverter : GsonConverter<Clip>(type = Clip::class)
object ClipListConverter : GsonListConverter<Clip>(typeToken = object : TypeToken<List<Clip>>() {})
object DeviceListConverter : GsonListConverter<Device>(typeToken = object : TypeToken<List<Device>>() {})
object UserEntityConverter : GsonConverter<UserEntity>(type = UserEntity::class)
object TagConverter : GsonConverter<Tag>(type = Tag::class)
object TagListConverter : GsonListConverter<Tag>(typeToken = object : TypeToken<List<Tag>>() {})
object UrlInfoListConverter : GsonListConverter<UrlInfo>(typeToken = object : TypeToken<List<UrlInfo>>() {})