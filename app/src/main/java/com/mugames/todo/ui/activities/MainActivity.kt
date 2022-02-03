package com.mugames.todo.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialElevationScale
import com.mugames.todo.*
import com.mugames.todo.databinding.ActivityMainBinding
import com.mugames.todo.ui.fragments.*
import com.mugames.todo.ui.viewmodels.TaskViewModel
import com.mugames.todo.ui.viewmodels.TaskViewModelFactory
import kotlinx.coroutines.delay
import java.util.*
import kotlin.NoSuchElementException

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var navController: NavController

    private var searchView: SearchView? = null

    companion object {
        const val TAG = "ToDo"
        const val FRAGMENT_TODO = "com.mugames.todo.ui.activities.FRAGMENT_TODO"
        const val FRAGMENT_MISSING = "com.mugames.todo.ui.activities.FRAGMENT_MISSING"
        const val FRAGMENT_DONE = "com.mugames.todo.ui.activities.FRAGMENT_DONE"
    }


    val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            (application as ToDoApplication).database.getTaskDao()
        )
    }

    private lateinit var binding: ActivityMainBinding

    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToDoApplication.setAppTheme(applicationContext)

        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityTopToolBar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

//        binding.bottomNavView.setupWithNavController(navController) //used to automatically navigate based on  nav menu clicking

        navController = navHostFragment.navController
        binding.run {
            navController.addOnDestinationChangedListener(this@MainActivity)
            bottomNavView.setOnItemSelectedListener { item ->
                val action = when (item.itemId) {
                    R.id.missingFragment -> ToDoFragmentDirections.actionGlobalToDoFragment(
                        FRAGMENT_MISSING
                    )
                    R.id.doneFragment -> ToDoFragmentDirections.actionGlobalToDoFragment(
                        FRAGMENT_DONE
                    )
                    R.id.settingsFragment -> SettingsFragmentDirections.actionGlobalSettingsFragment()
                    else -> ToDoFragmentDirections.actionGlobalToDoFragment(FRAGMENT_TODO)
                }
                navController.popBackStack()
                navController.navigate(action)
                true
            }
        }

        binding.fab.apply {
            setOnClickListener {
                isClickable = false
                openAddFragment()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.global_menu, menu)
        searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val fragment = currentNavigationFragment
                if (fragment is ToDoFragment) fragment.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val fragment = currentNavigationFragment
                if (fragment is ToDoFragment) fragment.filter(newText)
                return false
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_all_records -> viewModel.deleteAllTask()
            R.id.search -> {}
            else -> {
                val currentFragment = currentNavigationFragment
                if (currentFragment is ToDoFragment) {
                    currentFragment.menuAction(item.itemId)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val currentFragment = currentNavigationFragment
        if (!searchView?.isIconified!!) {
            searchView?.isIconified = true
            return
        }
        if ((currentFragment is ToDoFragment && currentFragment.type != FRAGMENT_TODO) || currentFragment is SettingsFragment) {
            navController.popBackStack()
            navController.navigate(
                ToDoFragmentDirections.actionGlobalToDoFragment(
                    FRAGMENT_TODO
                )
            )
            binding.bottomNavView.selectedItemId = R.id.toDoFragment
        } else super.onBackPressed()
    }

    private fun openAddFragment() {
        currentNavigationFragment?.apply {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            }

            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            }
        }
        val action = AddFragmentDirections.actionGlobalAddFragment()
        navController.navigate(action)
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        searchView?.setQuery("", false)
        searchView?.clearFocus()
        searchView?.isIconified = true

        try {
            currentNavigationFragment?.apply {
                exitTransition = MaterialElevationScale(false).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }

                reenterTransition = MaterialElevationScale(true).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }
            }
        } catch (e: NoSuchElementException) {
            e.printStackTrace()//Happens only if backstack is empty ie, on 1st creation
        }
        if (destination.id == R.id.viewTaskFragment) {
            hideTopBar()
            hideBottomBar()
        } else if (destination.id != R.id.addFragment) {
            showTopBar()
            showBottomBar()
            showFab()
        } else {
            hideBottomBar()
        }
        try {
            if (destination.id == R.id.settingsFragment) {
                binding.activityTopToolBar.menu.apply {
                    findItem(R.id.sort_by).isVisible = false
                    findItem(R.id.clear_list).isVisible = false
                    findItem(R.id.search).isVisible = false
                }
            } else {
                binding.activityTopToolBar.menu.apply {
                    findItem(R.id.sort_by).isVisible = true
                    findItem(R.id.clear_list).isVisible = true
                    findItem(R.id.search).isVisible = true
                }

            }
        } catch (e: NullPointerException) {
        }
    }


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ToDoApplication.localeContext(it) })
    }

    private fun hideBottomBar() {
        binding.run {
            bottomBar.performShow()
            bottomBar.performHide()
            bottomBar.animate().setListener(object : AnimatorListenerAdapter() {
                var isCanceled: Boolean = false
                override fun onAnimationEnd(animation: Animator?) {
                    if (isCanceled) return
                    bottomBar.visibility = View.GONE
                    hideFab()
                }


                override fun onAnimationCancel(animation: Animator?) {
                    isCanceled = true
                }
            })
        }
    }

    fun hideTopBar() {
        binding.activityTopBar.setExpanded(false, true)
    }

    private fun hideFab() {
        binding.fab.apply {
            if (isVisible) {
                hide()
                isClickable = false
                visibility = View.INVISIBLE
            }
        }
    }

    private fun showTopBar() {
        binding.activityTopBar.apply {
            setExpanded(true, true)
        }
    }

    private fun showBottomBar() {
        binding.apply {
            if (!bottomBar.isVisible) {
                bottomBar.visibility = View.VISIBLE
                bottomBar.performShow()
                bottomBar.animate().setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        viewModel.snackBarMessage?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fab)
                                .show()
                            viewModel.snackBarMessage = null
                        }
                    }
                })
            }
        }
    }

    private fun showFab() {
        binding.apply {
            if (!fab.isClickable) {
                fab.show()
                fab.isClickable = true
            }
        }
    }
}