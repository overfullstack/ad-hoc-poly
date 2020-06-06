package mvc

import org.springframework.fu.kofu.application

val app = application {
    beans {
        bean<Handlers>()
    }
    enable(dataConfig)
    enable(webConfig)
}

fun main() {
    app.run()
}
