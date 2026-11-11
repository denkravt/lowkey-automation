package com.justan.shared.network

import com.typesafe.config.ConfigFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Two lazy clients: one that follows redirects (default) and one that does not, for backends whose
 * auth flows need the 3xx inspected. Auth headers are redacted before anything is logged.
 */
object RetrofitClient {
    private val config = ConfigFactory.load("backend.conf").getConfig("backend")
    private var token: String? = null
    private val cookieJar = InMemoryCookieJar()

    val isConfigured: Boolean get() = config.getString("baseUrl").isNotBlank()

    private val logging = HttpLoggingInterceptor().apply {
        level = if (config.getBoolean("logBodies")) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("X-Api-Key")
    }

    private fun client(followRedirects: Boolean): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.getInt("connectTimeoutSeconds").toLong(), TimeUnit.SECONDS)
        .readTimeout(config.getInt("readTimeoutSeconds").toLong(), TimeUnit.SECONDS)
        .followRedirects(followRedirects)
        .followSslRedirects(followRedirects)
        .cookieJar(cookieJar)
        .addInterceptor(Interceptor { chain ->
            val builder = chain.request().newBuilder()
            token?.let { builder.header("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        })
        .addInterceptor(logging)
        .build()

    fun setToken(value: String?) {
        token = value
    }

    fun <T> create(service: Class<T>, followRedirects: Boolean = true): T = Retrofit.Builder()
        .baseUrl(config.getString("baseUrl").let { if (it.endsWith("/")) it else "$it/" })
        .client(client(followRedirects))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(service)

    private class InMemoryCookieJar : CookieJar {
        private val store = ConcurrentHashMap<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = store[url.host].orEmpty()
    }
}
