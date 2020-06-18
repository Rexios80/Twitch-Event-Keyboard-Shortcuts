import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.philippheuer.events4j.simple.domain.EventSubscriber
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent
import okhttp3.OkHttpClient
import tornadofx.*


class TornadoApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()
    override val root = group { label("Hello World") }
}

class MainController : Controller() {
    private val accessToken = AccessToken(Secret.clientId, Secret.secret, OkHttpClient())
    private val credential: OAuth2Credential get() = OAuth2Credential(null, accessToken.accessToken)
    private val twitchClient = TwitchClientBuilder.builder()
        .withEnableHelix(true)
        .withEnablePubSub(true)
        .withDefaultAuthToken(credential)
        .withClientId(Secret.clientId)
        .withClientSecret(Secret.secret)
        .build()

    init {
        twitchClient.clientHelper.enableFollowEventListener(Secret.channel)
        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java).registerListener(TeksEventHandler())

        val channelName = Secret.channel
        val resultList = twitchClient.helix.getUsers(accessToken.accessToken, null, listOf(channelName)).execute()
        val channelId = resultList.users.find { it.displayName == channelName }?.id

        twitchClient.pubSub.listenForChannelPointsRedemptionEvents(credential, channelId)
    }
}

class TeksEventHandler {
    @EventSubscriber
    fun handleChannelPointsRedemption(event: ChannelPointsRedemptionEvent) {
        println("event: " + event.redemption.reward.title)
    }
}