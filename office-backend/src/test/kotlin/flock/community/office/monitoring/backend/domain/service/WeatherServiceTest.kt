package flock.community.office.monitoring.backend.domain.service

import kotlinx.coroutines.runBlocking
import nl.wykorijnsburger.kminrandom.minRandomCached
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.test.assertEquals

internal class WeatherServiceTest (){

    private val apiKey = OpenWeatherMapConfig("fakeKey")
    private val mockedWebclient: WebClient = mock(WebClient::class.java)
    private val testService = WeatherService(mockedWebclient, apiKey)

    @Test
    fun `test get weather object`():Unit = runBlocking {
        val expect = WeatherPrediction::class.minRandomCached()

        //Given
        val xyz = mock(WebClient.RequestHeadersUriSpec::class.java)
        given(mockedWebclient.get()).willReturn(xyz)
        val a = mock(WebClient.RequestHeadersSpec::class.java)
        given(xyz.uri(anyString())).willReturn(a)
        val responseSpec = mock(WebClient.ResponseSpec::class.java)
        given(a.retrieve()).willReturn(responseSpec)
        given(responseSpec.bodyToMono(eq(WeatherPrediction::class.java))).willReturn(Mono.just(expect))

        //When
        assertEquals(expect.coord.lat, testService.getPrediction().coord.lat)

        Mockito.verify(xyz).uri(eq("?lat=52.09266175027509&lon=5.122345051397365&appid=fakeKey&units=metric"))
    }
}

