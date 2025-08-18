package com.mindeaseai.gemini

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {
    @Provides
    @Singleton
    fun provideGeminiApiService(): GeminiApiService {
        val apiKey = com.mindeaseai.BuildConfig.GEMINI_API_KEY
    // Do not crash the app; requests will fail with 401 and the UI can surface a helpful error
        val interceptor = okhttp3.Interceptor { chain ->
            val original = chain.request()
            val originalUrl = original.url
            val url = originalUrl.newBuilder()
                .addQueryParameter("key", apiKey)
                .build()
            val requestBuilder = original.newBuilder().url(url)
            chain.proceed(requestBuilder.build())
        }
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GeminiApiService::class.java)
    }
}
