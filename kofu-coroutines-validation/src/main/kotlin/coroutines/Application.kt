package coroutines

import org.springframework.boot.WebApplicationType
import org.springframework.fu.kofu.application

val app = application(WebApplicationType.REACTIVE) {
    beans {
        bean<Handlers>()
    }
    enable(dataConfig)
    enable(webFlux)
}

fun main() {
    app.run()
}
