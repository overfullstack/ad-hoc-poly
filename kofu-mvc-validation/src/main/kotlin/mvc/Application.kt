package mvc

import org.springframework.fu.kofu.webApplication

val app = webApplication {
    beans {
        bean<Handlers>()
    }
    enable(dataConfig)
    enable(webConfig)
}

fun main() {
    app.run()
}
