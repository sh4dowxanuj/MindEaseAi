package com.mindeaseai.gemini

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

private class FakeApiServiceSuccess: GeminiApiService {
    override suspend fun generateContent(request: GeminiRequest): Response<GeminiResponse> {
        val resp = GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = Content(
                        parts = listOf(Part(text = "Hello from AI"))
                    )
                )
            )
        )
        return Response.success(resp)
    }
}

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GeminiViewModelTest {
    @Test
    fun blank_message_sets_error() = runTest {
    val vm = GeminiViewModel(apiService = FakeApiServiceSuccess())
        vm.sendMessage("")
        assertEquals("Please enter a message.", vm.error.first())
    }

    @Test
    fun success_appends_message_pair() = runTest {
    val vm = GeminiViewModel(apiService = FakeApiServiceSuccess())
        vm.sendMessage("Hi")
    // Advance coroutines launched in viewModelScope
    runCurrent()
    val messages = vm.messages.first()
        assertEquals(1, messages.size)
        assertEquals("Hi", messages[0].first)
        // The formatted response appends a reminder suffix
        val ai = messages[0].second
        assert(ai.startsWith("Hello from AI"))
    }
}
