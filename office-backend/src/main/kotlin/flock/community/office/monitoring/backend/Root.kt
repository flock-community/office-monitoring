package flock.community.office.monitoring.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class Root {

    @GetMapping
    fun getRoot() = Greeting()

}

class Greeting {
    val greet: String = "Hello World!"
}
