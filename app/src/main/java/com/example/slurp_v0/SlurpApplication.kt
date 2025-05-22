package com.example.slurp_v0

import android.app.Application
import com.google.firebase.FirebaseApp

class SlurpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 