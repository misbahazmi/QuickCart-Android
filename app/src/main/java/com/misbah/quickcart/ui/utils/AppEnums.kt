package com.nytimes.utils

/**
 * @author: MOHAMMAD MISBAH
 * @since: 15-Dec-2023
 * @sample: Technology Assessment for Android Role
 * Email Id: mohammadmisbahazmi@gmail.com
 * GitHub: https://github.com/misbahazmi
 * Expertise: Android||Java||Flutter
 */
object AppEnums {

    enum class DeviceType{
        ANDROID,
        IOS
    }
    enum class TasksPriority(val value: Int) {
        Normal(0), Low(1), Medium(2), High(3)
    }

    enum class ProductCategory(val value: Int) {
        All(0), New(1), Personal(2), Wishlist(3), Shopping(4)
    }
}