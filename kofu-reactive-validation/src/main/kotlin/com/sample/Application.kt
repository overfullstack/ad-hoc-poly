package com.sample

import org.springframework.boot.WebApplicationType
import org.springframework.fu.kofu.application

val app = application(WebApplicationType.REACTIVE) {
    beans {
        bean<UserHandler>()
    }
    enable(dataConfig)
    enable(webFlux)
}

fun main() {
    app.run()
}
