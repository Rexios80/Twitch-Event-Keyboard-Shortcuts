import com.github.philippheuer.credentialmanager.CredentialManagerBuilder
import com.github.philippheuer.credentialmanager.domain.AuthenticationController
import com.github.philippheuer.credentialmanager.domain.IdentityProvider
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.auth.providers.TwitchIdentityProvider
import tornadofx.*

class TornadoApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()
    override val root = group { label("Hello World") }
}

class MainController : Controller() {
    private val credentialManager = CredentialManagerBuilder.builder().build()
    private val twitchClient: TwitchClient? = null
}

class TestAuthenticationController : AuthenticationController() {
    override fun startOAuth2AuthorizationCodeGrantType(
        p0: OAuth2IdentityProvider?,
        p1: String?,
        p2: MutableList<Any>?
    ) {
        TODO("Not yet implemented")
    }

}