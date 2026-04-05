// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Thêm dòng này vào (chọn phiên bản mới nhất)
    id("com.google.dagger.hilt.android") version "2.48" apply false
}