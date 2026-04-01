package com.netsservices.dct.di

import android.app.Application
import com.netsservices.dct.data.remote.ApiServer
import com.netsservices.dct.data.remote.repository.RepositoryImpl
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.ApplicationScope
import com.netsservices.dct.presentation.common.Constants
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.helper.connection.NetworkService
import com.netsservices.dct.presentation.helper.connection.NetworkServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(application: Application): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.url.encodedPath.contains("/auth/login") ||
                    request.url.encodedPath.contains("/auth/register"))
                {
                    return@addInterceptor chain.proceed(request)
                }
                val token = PreferenceManager.getAuthToken(application)
                val lang = runBlocking { LanguagePrefs.getLanguage(application).first() }

                val newRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("x-lang", lang)
                    .build()
                chain.proceed(newRequest)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiServer(retrofit: Retrofit): ApiServer {
        return retrofit.create(ApiServer::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(
        apiServer: ApiServer
    ): Repository {
        return RepositoryImpl(api = apiServer)
    }

    @Provides
    @Singleton
    fun provideNetworkService(application: Application): NetworkService {
        return NetworkServiceImpl(application)
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}