package ru.skillbranch.gameofthrones

import android.app.Application
import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.skillbranch.gameofthrones.data.local.database.AppDatabase
import ru.skillbranch.gameofthrones.data.remote.network.RestService

class App : Application() {
    companion object {
        private var instance:App? = null
        private var database: AppDatabase? = null
        private var restService: RestService? = null

        fun applicationContext() : Context = instance!!.applicationContext

        fun getDatabase() : AppDatabase = if (database == null) {database =
            instance?.let { AppDatabase.invoke(it) }; database!!
        } else database!!

        fun getRestService() : RestService = if (restService == null)
        {val mRetrofit = Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(OkHttpClient())
            .build()
            restService = mRetrofit.create(RestService::class.java)
            restService!!}
        else restService!!
    }


    init {
        instance = this
    }



    override fun onTerminate() {
        database.let{it!!.close()}
        super.onTerminate()
    }
}