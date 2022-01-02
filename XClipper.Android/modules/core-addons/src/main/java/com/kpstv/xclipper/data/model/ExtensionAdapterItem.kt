package com.kpstv.xclipper.data.model

data class ExtensionAdapterItem(
  override val title: String,
  override val fullDescription: String,
  override val icon: Int,
  override val dominantColor: Int,
  override val sku: String,
  override val smallDescription: String
) : ExtensionData {
  companion object {
    fun ExtensionItem.toAdapterItem() = ExtensionAdapterItem(
      title = title,
      fullDescription = fullDescription,
      icon = icon,
      dominantColor = dominantColor,
      sku = sku,
      smallDescription = smallDescription
    )
  }
}