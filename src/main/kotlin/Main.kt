import com.github.philippheuer.credentialmanager.CredentialManagerBuilder
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.auth.providers.TwitchIdentityProvider
import tornadofx.*

class TornadoApp : App(MainView::class)

class MainView : View() {
    override val root = group { label("Hello World") }
}

class MainController : Controller() {
    private val credentialManager = CredentialManagerBuilder.builder().build()
    private val twitchClient = TwitchClientBuilder.builder()
        .withEnableHelix(true)
        .withEnablePubSub(true)
        .withCredentialManager(credentialManager)
        .withEventManager(null)
        .build()

    init {
        credentialManager.registerIdentityProvider(TwitchIdentityProvider())
    }
}