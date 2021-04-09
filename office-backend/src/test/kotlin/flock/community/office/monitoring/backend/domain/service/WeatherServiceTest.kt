package flock.community.office.monitoring.backend.domain.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.wykorijnsburger.kminrandom.minRandom
import nl.wykorijnsburger.kminrandom.minRandomCached
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

internal class WeatherServiceTest (){

    val mockedWebclient: WebClient = mock(WebClient::class.java)
    val testService = WeatherService(mockedWebclient)


    val expectation = "{\n" +
            "  \"coord\": {\n" +
            "    \"lon\": 5.1223,\n" +
            "    \"lat\": 52.0927\n" +
            "  },\n" +
            "  \"weather\": [\n" +
            "    {\n" +
            "      \"id\": 804,\n" +
            "      \"main\": \"Clouds\",\n" +
            "      \"description\": \"overcast clouds\",\n" +
            "      \"icon\": \"04d\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"base\": \"stations\",\n" +
            "  \"main\": {\n" +
            "    \"temp\": 282.05,\n" +
            "    \"feels_like\": 279.04,\n" +
            "    \"temp_min\": 281.48,\n" +
            "    \"temp_max\": 282.59,\n" +
            "    \"pressure\": 1022,\n" +
            "    \"humidity\": 57\n" +
            "  },\n" +
            "  \"visibility\": 10000,\n" +
            "  \"wind\": {\n" +
            "    \"speed\": 5.86,\n" +
            "    \"deg\": 245,\n" +
            "    \"gust\": 7.2\n" +
            "  },\n" +
            "  \"clouds\": {\n" +
            "    \"all\": 98\n" +
            "  },\n" +
            "  \"dt\": 1617889783,\n" +
            "  \"sys\": {\n" +
            "    \"type\": 3,\n" +
            "    \"id\": 2012962,\n" +
            "    \"country\": \"NL\",\n" +
            "    \"sunrise\": 1617857870,\n" +
            "    \"sunset\": 1617906264\n" +
            "  },\n" +
            "  \"timezone\": 7200,\n" +
            "  \"id\": 2745912,\n" +
            "  \"name\": \"Utrecht\",\n" +
            "  \"cod\": 200\n" +
            "}"

    @Test
    fun `test get weather object`(){
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
        StepVerifier.create(testService.getPrediction())
            .expectNext(expect)
            .verifyComplete()

        Mockito.verify(xyz).uri(eq("?lat=52.09266175027509&lon=5.122345051397365&appid=&units=metric"))
    }

    @Test
    fun `test parse weather object`(){
        val result = testService.getPrediction()
        val plaats = result.block()?.coord
        assertEquals(WeatherPrediction::class.java, result::class.java)
    }

//    TODO: write more useful tests

}