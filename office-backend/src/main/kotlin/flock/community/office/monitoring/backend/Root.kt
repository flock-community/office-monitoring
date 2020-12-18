package flock.community.office.monitoring.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class Root {

    @GetMapping
    fun getGreeting(@RequestParam name: String?) = Greeting(name)
}

data class Greeting constructor(
        private val name: String?,
) {
    val greeting: String get() = if (name.isNullOrBlank()) "H🤦‍♀️llo  World!" else "Hello $name!"
}
