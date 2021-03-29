package flock.community.office.monitoring.backend.domain.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import kotlin.test.assertEquals

@SpringBootTest
internal class WeatherServiceTest (@Autowired val testService: WeatherService){

    val expectation = "{ \"liveweer\": [{\"plaats\": \"Utrecht\", \"temp\": \"13.1\", \"gtemp\": \"8.4\", \"samenv\": \"Licht bewolkt\", \"lv\": \"58\", \"windr\": \"ZZW\", \"windms\": \"7\", \"winds\": \"4\", \"windk\": \"13.6\", \"windkmh\": \"25.2\", \"luchtd\": \"1015.0\", \"ldmmhg\": \"761\", \"dauwp\": \"5\", \"zicht\": \"27\", \"verw\": \"Af en toe zon, vooral in de avond regen. Zaterdag eerst buien, later droger\", \"sup\": \"06:24\", \"sunder\": \"19:05\", \"image\": \"halfbewolkt\", \"d0weer\": \"bewolkt\", \"d0tmax\": \"13\", \"d0tmin\": \"4\", \"d0windk\": \"4\", \"d0windknp\": \"12\", \"d0windms\": \"6\", \"d0windkmh\": \"22\", \"d0windr\": \"ZW\", \"d0neerslag\": \"29\", \"d0zon\": \"22\", \"d1weer\": \"regen\", \"d1tmax\": \"8\", \"d1tmin\": \"4\", \"d1windk\": \"3\", \"d1windknp\": \"10\", \"d1windms\": \"5\", \"d1windkmh\": \"19\", \"d1windr\": \"W\", \"d1neerslag\": \"90\", \"d1zon\": \"30\", \"d2weer\": \"bewolkt\", \"d2tmax\": \"12\", \"d2tmin\": \"4\", \"d2windk\": \"3\", \"d2windknp\": \"10\", \"d2windms\": \"5\", \"d2windkmh\": \"19\", \"d2windr\": \"ZW\", \"d2neerslag\": \"30\", \"d2zon\": \"30\", \"alarm\": \"0\"}]}  "

    @Test
    fun `test get weather object`(){
        assertEquals(testService.getPrediction()?.block()!!::class.java ,WeatherPrediction().javaClass)
    }

    @Test
    fun `test parse weather object`(){
        val result = testService.getPrediction()
        val plaats = result?.block()?.liveweer
        assertEquals(WeatherPrediction().javaClass, result!!.javaClass)
    }

}