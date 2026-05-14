package com.hulampay.mobile.di

import com.google.gson.Gson
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.api.AuthApiService
import com.hulampay.mobile.data.api.AuthInterceptor
import com.hulampay.mobile.data.api.CampusApiService
import com.hulampay.mobile.data.api.ChatApiService
import com.hulampay.mobile.data.api.ClaimApiService
import com.hulampay.mobile.data.api.ItemApiService
import com.hulampay.mobile.data.api.NotificationApiService
import com.hulampay.mobile.data.api.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/api/" // Standard emulator localhost alias

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = AppGson.instance

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideItemApiService(retrofit: Retrofit): ItemApiService {
        return retrofit.create(ItemApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideClaimApiService(retrofit: Retrofit): ClaimApiService {
        return retrofit.create(ClaimApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCampusApiService(retrofit: Retrofit): CampusApiService {
        return retrofit.create(CampusApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppApi(retrofit: Retrofit): com.hulampay.mobile.data.remote.AppApi {
        return retrofit.create(com.hulampay.mobile.data.remote.AppApi::class.java)
    }
}
