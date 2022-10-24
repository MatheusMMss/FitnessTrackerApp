package co.matheusmartins.fitnesstracker

import android.app.Application
import co.matheusmartins.fitnesstracker.model.AppDatabase

class App : Application() {

    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
    }

}