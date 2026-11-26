package com.GalleryYou.Pixel.ui.theme

enum class ThemeMode(val storageId: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int): ThemeMode = entries.firstOrNull { it.storageId == value } ?: SYSTEM
    }
}
