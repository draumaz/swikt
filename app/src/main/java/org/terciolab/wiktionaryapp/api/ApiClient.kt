package org.terciolab.wiktionaryapp.api

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

object ApiClient {

    private const val WIKTIONARY_SEARCH_URL = "https://%s.wiktionary.org/w/rest.php/v1/"
    private const val KAIKII_URL = "https://kaikki.org/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(MeaningsJsonlAdapter())
        .build()


    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // User-Agent interceptor following Wikimedia guidelines
    private val userAgentInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "WiktionaryApp/1.0 (https://github.com/Terciocode/WiktionaryTercioApp; sebirocsdev@gmail.com)")
            .build()
        chain.proceed(request)
    }

    // Error handling interceptor for 403 responses
    private val errorHandlingInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        
        if (response.code == 403) {
            Log.e("ApiClient", "403 Forbidden error for URL: ${response.request.url}")
            Log.e("ApiClient", "This may be due to missing or incorrect User-Agent header")
            // Return the response instead of throwing an exception
            // The calling code can handle the empty/error response gracefully
        }
        
        response
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private var wiki : WikiService? = null

    fun getWiki(lang: String): WikiService {
        if (wiki == null) {

            wiki = Retrofit.Builder()
                .client(client)
                .baseUrl(String.format(WIKTIONARY_SEARCH_URL,lang))
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(WikiService::class.java)
        }
        return wiki!!
    }

    val kaikki : KaikkiService by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl(KAIKII_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KaikkiService::class.java)
    }

}
