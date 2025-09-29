package org.terciolab.wiktionaryapp.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface WikiService {
    @GET("search/title")
    suspend fun suggestWords(@Query("q") query: String, @Query("limit") limit: Int = 10): SearchResponse
}

interface KaikkiService {
    @Streaming
    @GET("{langDictionary}/All%20languages%20combined/meaning/{firstOne}/{firstTwo}/{whole}.jsonl")
    suspend fun getWordMeanings(
        @Path("firstOne") firstOne: String,
        @Path("firstTwo") firstTwo: String,
        @Path("whole") whole: String,
        @Path("langDictionary") langDictionary: String = ""
    ): List<WordMeaning>
}

