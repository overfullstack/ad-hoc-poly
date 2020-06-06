package reactive

import org.springframework.fu.kofu.reactiveWebApplication

val app = reactiveWebApplication {
    beans {
        bean<Handlers>()
    }
    enable(dataConfig)
    enable(webFlux)
}

fun main() {
    app.run()
}
