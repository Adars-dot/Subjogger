package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SubJoggerDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.SubscriptionRepository
import com.example.ui.SubJoggerApp
import com.example.ui.theme.SubJoggerTheme
import com.example.ui.viewmodel.SubJoggerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = SubJoggerDatabase.getInstance(applicationContext)
        val prefsRepository = UserPreferencesRepository(applicationContext)
        val repository = SubscriptionRepository(database.subscriptionDao(), prefsRepository)
        val factory = SubJoggerViewModel.Factory(repository, applicationContext)

        setContent {
            SubJoggerTheme {
                val vm: SubJoggerViewModel = viewModel(factory = factory)
                SubJoggerApp(viewModel = vm)
            }
        }
    }
}
