package com.misbah.quickcart.core.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.lifecycle.ViewModel
import dagger.android.support.DaggerAppCompatActivity

/**
 * @author: Mohammad Misbah
 * @since: 15-Dec-2023
 * @sample: Technology Assessment for Sr. Android Role
 * Email Id: mohammadmisbahazmi@gmail.com
 * GitHub: https://github.com/misbahazmi
 * Expertise: Android||Java/Kotlin||Flutter
 */
abstract class BaseActivity<V: ViewModel> : DaggerAppCompatActivity(){

    private lateinit var  viewModel: V
    public abstract fun getViewModel() : V

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        this.viewModel = viewModel
    }

}