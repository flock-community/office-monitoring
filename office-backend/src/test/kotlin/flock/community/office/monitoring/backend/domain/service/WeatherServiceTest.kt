package flock.community.office.monitoring.backend.domain.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.api.client.json.Json
import org.json.JSONArray
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import kotlin.test.assertEquals

@SpringBootTest
internal class WeatherServiceTest (@Autowired val testService: WeatherService){

    val mapper = jacksonObjectMapper()
    val expectation = mapper.readValue<WeatherPrediction>("{\n" +
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
            "}", WeatherPrediction::class.java)

    @Test
    fun `test get weather object`(){
        print(expectation)
        assertEquals(expectation, WeatherPrediction::class.java)
    }

    @Test
    fun `test parse weather object`(){
        val result = testService.getPrediction()
        val plaats = result.block()?.coord
        assertEquals(WeatherPrediction::class.java, result::class.java)
    }

}