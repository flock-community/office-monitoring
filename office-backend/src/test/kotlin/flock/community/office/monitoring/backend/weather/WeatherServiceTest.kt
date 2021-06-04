package flock.community.office.monitoring.backend.weather

import flock.community.office.monitoring.backend.weather.domain.WeatherPrediction
import flock.community.office.monitoring.backend.weather.service.OpenWeatherMapConfig
import flock.community.office.monitoring.backend.weather.service.WeatherService
import kotlinx.coroutines.runBlocking
import nl.wykorijnsburger.kminrandom.minRandomCached
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.test.assertEquals

internal class WeatherServiceTest (){

    private val apiKey = OpenWeatherMapConfig("fakeKey")
    private val mockedWebclient: WebClient = Mockito.mock(WebClient::class.java)
    private val testService = WeatherService(mockedWebclient, apiKey)

    @Test
    fun `test get weather object`():Unit = runBlocking {
        val expect = WeatherPrediction::class.minRandomCached()

        //Given
        val xyz = Mockito.mock(WebClient.RequestHeadersUriSpec::class.java)
        BDDMockito.given(mockedWebclient.get()).willReturn(xyz)
        val a = Mockito.mock(WebClient.RequestHeadersSpec::class.java)
        BDDMockito.given(xyz.uri(anyString())).willReturn(a)
        val responseSpec = Mockito.mock(WebClient.ResponseSpec::class.java)
        BDDMockito.given(a.retrieve()).willReturn(responseSpec)
        BDDMockito.given(responseSpec.bodyToMono(eq(WeatherPrediction::class.java))).willReturn(Mono.just(expect))

        //When
        assertEquals(expect.coord.lat, testService.getPrediction().coord.lat)

        Mockito.verify(xyz).uri(eq("?lat=52.09266175027509&lon=5.122345051397365&appid=fakeKey&units=metric"))
    }
}
