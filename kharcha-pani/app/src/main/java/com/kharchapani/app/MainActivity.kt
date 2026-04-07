package com.kharchapani.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kharchapani.app.data.local.KharchaDatabase
import com.kharchapani.app.data.repo.KharchaRepository
import com.kharchapani.app.ui.HomeViewModel
import com.kharchapani.app.ui.KharchaPaniApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = KharchaDatabase.getInstance(applicationContext)
        val repository = KharchaRepository(db.kharchaDao())

        setContent {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
            MaterialTheme {
                Surface {
                    KharchaPaniApp(viewModel = vm)
                }
            }
        }
    }
}
