package com.misbah.quickcart.ui.main

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.misbah.quickcart.R
import com.misbah.quickcart.core.base.BaseActivity
import com.misbah.quickcart.databinding.ActivityMainBinding
import com.misbah.quickcart.notifications.NotificationReminderReceiver
import com.misbah.quickcart.ui.category.CategoryFragmentDirections
import com.misbah.quickcart.ui.category.CategoryViewModel
import com.misbah.quickcart.ui.orders.OrderListFragmentDirections
import com.misbah.quickcart.ui.products.ProductViewModel
import com.misbah.quickcart.ui.orders.OrderListViewModel
import com.misbah.quickcart.ui.utils.Constants
import com.misbah.quickcart.ui.utils.exhaustive
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


/**
 * @author: Mohammad Misbah
 * @since: 17-DEC-2023
 * @sample: Technology Assessment for Sr. Android Role
 * Email Id: mohammadmisbahazmi@gmail.com
 * GitHub: https://github.com/misbahazmi
 * Expertise: Android||Java/Kotlin||Flutter
 */
class MainActivity : BaseActivity<MainViewModel>() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: CartViewModel by viewModels<CartViewModel>()
    @Inject
    internal lateinit var viewModel: MainViewModel
    override fun getViewModel(): MainViewModel {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.findNavController()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.ordersFragment, R.id.nav_product, R.id.nav_category, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is OrderListViewModel.OrdersEvent.NavigateToAddTaskScreen -> {
                            val action =
                                OrderListFragmentDirections.actionOrdersFragmentToAddEditTaskFragment(
                                    "Place New Order",
                                    null
                                )
                            navController.navigate(action)

                        }
                        is OrderListViewModel.OrdersEvent.NavigateToEditTaskScreen -> {
                            val action =
                                OrderListFragmentDirections.actionOrdersFragmentToAddEditTaskFragment(
                                    "View Order Details",
                                    event.order
                                )
                            navController.navigate(action)
                        }
                        is OrderListViewModel.OrdersEvent.QuitAppPopUp -> {
                            val action =
                                OrderListFragmentDirections.actionGlobalQuitAppDialogFragment()
                            navController.navigate(action)
                        }
                        else ->{}
                    }.exhaustive
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.categoryEvent.collect{ event->
                    when (event) {
                        is CategoryViewModel.CategoryEvent.NavigateToAddCategoryDialog -> {
                            val action = CategoryFragmentDirections.actionAddCategoryDialogFragment()
                            navController.navigate(action)
                        }
                        else ->{}
                    }.exhaustive
                }

            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.productEvent.collect{
                        event->
                    when (event) {
                        is ProductViewModel.ProductEvent.NavigateToAddProductDialog -> {
                            val action = CategoryFragmentDirections.actionAddProductDialogFragment()
                            navController.navigate(action)
                        }
                        else ->{}
                    }.exhaustive
                }
            }
        }

        binding.appBarMain.fab.setOnClickListener {
            val navController = findNavController(this, R.id.nav_host_fragment_content_main)
            when (navController.currentDestination!!.id) {
                R.id.nav_category -> {
                    viewModel.onAddNewCategoryClick()
                }
                R.id.nav_product-> {
                    viewModel.onAddNewProductClick()
                }
                R.id.ordersFragment -> {
                    if(sharedViewModel.shoppingCart.value?.getCartItems()!!.isEmpty()){
                        navController.navigate(R.id.nav_product)
                    } else {
                        viewModel.onAddNewTaskClick()
                    }
                }
                else -> {}
            }
        }
        binding.appBarMain.fab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_anim))

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                exitOnBackPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        Log.i("TAG", "handleOnBackPressed: Exit")
                        exitOnBackPressed()
                    }
                })
        }
        navView.setNavigationItemSelectedListener { menuItem ->
            // close drawer when item is tapped
            drawerLayout.closeDrawers()
            val bundle = bundleOf("title" to "Orders Details", "order" to null)
            when (menuItem.itemId) {
                R.id.addEditTaskFragment -> {
                    navController.navigate(menuItem.itemId, bundle)
                    hideFAB()
                }
                R.id.ordersFragment -> {
                    showFAB()
                    navController.navigate(menuItem.itemId)
                }
                R.id.nav_product -> {
                    showFAB()
                    navController.navigate(menuItem.itemId)
                }
                R.id.nav_category-> {
                    showFAB()
                    navController.navigate(menuItem.itemId)
                }
                else -> {
                    hideFAB()
                    navController.navigate(menuItem.itemId)
                }
            }
            true
        }
        scheduleAllTaskEveryMorningReminder()
        createNotificationChannel()
        requestNotificationPermission()
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun hideFAB(){
        binding.appBarMain.fab.hide()
    }
    fun showFAB(){
        binding.appBarMain.fab.show()
    }

    fun performFABClick(){
        binding.appBarMain.fab.performClick()
    }

    fun updateFABCart(){
        binding.appBarMain.fab.setImageResource(R.drawable.ic_add_shopping_cart)
    }
    fun updateFABCartDetails(){
        binding.appBarMain.fab.setImageResource(R.drawable.ic_cart_details)
    }

    fun updateFABAdd(){
        binding.appBarMain.fab.setImageResource(R.drawable.ic_add)
    }

    fun exitOnBackPressed() {
        if (navController.currentDestination?.id == R.id.ordersFragment)
            viewModel.onBackClickQuitApp()
        else
            navController.popBackStack()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean -> }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }
    private fun scheduleAllTaskEveryMorningReminder() {
        val intentAlarm = Intent(this, NotificationReminderReceiver::class.java)
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        //set the notification to repeat every fifteen minutes
        val alarmStartTime: Calendar = Calendar.getInstance()
        val now: Calendar = Calendar.getInstance()
        alarmStartTime.set(Calendar.HOUR_OF_DAY, 6)
        alarmStartTime.set(Calendar.MINUTE, 0)
        alarmStartTime.set(Calendar.SECOND, 0)
        if (now.after(alarmStartTime)) {
            alarmStartTime.add(Calendar.DATE, 1)
        }
        // set unique id to the pending item, so we can call it when needed
        val pendingIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            1100,
            intentAlarm,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC, alarmStartTime.timeInMillis,AlarmManager.INTERVAL_DAY, pendingIntent
        )
    }
}
const val ADD_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1