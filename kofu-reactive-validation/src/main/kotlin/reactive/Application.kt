package reactive

import org.springframework.fu.kofu.application

val app = application {
    beans {
        bean<Handlers>()
    }
    enable(dataConfig)
    enable(webFlux)
}

fun main() {
    app.run()
}
