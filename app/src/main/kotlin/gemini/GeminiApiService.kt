package com.mindeaseai.gemini

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): Response<GeminiResponse>
}

// Request/Response data classes

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)
