package com.kpstv.xclipper.data.model

data class ExtensionItem(
  override val title: String,
  override val fullDescription: String,
  override val icon: Int,
  override val dominantColor: Int,
  override val sku: String,
  override val smallDescription: String
) : ExtensionData