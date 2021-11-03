package flock.community.office.monitoring.backend.weather

import flock.community.office.monitoring.backend.weather.service.OpenWeatherMapConfig
import flock.community.office.monitoring.backend.weather.service.WeatherClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.web.reactive.function.client.WebClient

@ExperimentalCoroutinesApi
internal class WeatherServiceTest (){

    private val apiKey = OpenWeatherMapConfig("fakeKey", 1.2,2.3)
    private val mockedWebclient: WebClient = mock(WebClient::class.java)
    private val testService = WeatherClient(mockedWebclient, apiKey)

    @Test
    fun bla(){
        val numbers = listOf(5, 2, 10, 4)

        val simpleSum = numbers.reduce { sum, element -> sum + element }
        println(simpleSum)
        val sumDoubled = numbers.scan(0) { sum, element -> sum + element * 2 }
        println(sumDoubled)
    }

//    @Test
//    fun `test get weather object`() = runBlockingTest {
//        val expect = WeatherPrediction::class.minRandomCached()
//
//        //Given
//        val xyz = mock(WebClient.RequestHeadersUriSpec::class.java)
//        given(mockedWebclient.get()).willReturn(xyz)
//        val a = mock(WebClient.RequestHeadersSpec::class.java)
//        given(xyz.uri(any<Function<*>>())).willReturn(a)
////        val responseSpec = mock(WebClient.ResponseSpec::class.java)
////        given(a.awaitExchange(any<suspend (ClientResponse) -> WeatherPrediction>())).willReturn(expect)
////        given(responseSpec.bodyToMono(eq(WeatherPrediction::class.java))).willReturn(Mono.just(expect))
//
//        //When
//        assertEquals(expect.lat, testService.getPrediction().lat)
//
//        Mockito.verify(xyz).uri(eq("?lat=1.2&lon=2.3&appid=fakeKey&units=metric"))
//    }
}
