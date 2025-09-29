package org.terciolab.wiktionaryapp.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchWord(
    val title: String,
    val key: String
)

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val pages: List<SearchWord>
)

@JsonClass(generateAdapter = true)
data class Sense(
    val glosses: List<String>?,
    val tags: List<String>?
)

@JsonClass(generateAdapter = true)
data class WordMeaning(
    val senses: List<Sense>,
    val pos: String,
    val word: String,
    val lang: String,
    val etymology_text: String?,
    val sounds: List<Sound>?
)

@JsonClass(generateAdapter = true)
data class Sound(
    val ipa: String?,
    val tags: List<String>?,
)