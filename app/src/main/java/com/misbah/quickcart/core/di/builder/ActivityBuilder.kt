package com.misbah.quickcart.core.di.builder


import com.misbah.quickcart.ui.main.MainActivity
import com.misbah.quickcart.ui.main.MainActivityModule
import com.misbah.quickcart.ui.main.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
/**
 * @author: Mohammad Misbah
 * @since:  17-Dec-2023
 * @sample: Technology Assessment for Sr. Android Role
 * Email Id: mohammadmisbahazmi@gmail.com
 * GitHub: https://github.com/misbahazmi
 * Expertise: Android||Java/Kotlin||Flutter
 */
@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeSplashActivity(): SplashActivity
}