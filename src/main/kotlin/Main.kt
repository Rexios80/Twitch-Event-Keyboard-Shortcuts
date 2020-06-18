import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.philippheuer.events4j.simple.domain.EventSubscriber
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.CheerEvent
import com.github.twitch4j.chat.events.channel.FollowEvent
import com.github.twitch4j.chat.events.channel.GiftSubscriptionsEvent
import com.github.twitch4j.chat.events.channel.SubscriptionEvent
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent
import com.netflix.hystrix.exception.HystrixRuntimeException
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.input.KeyEvent
import okhttp3.OkHttpClient
import tornadofx.*
import java.lang.NullPointerException


class TornadoApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()
    override val root = vbox {
        style {
            padding = box(20.px)
        }
        keyboard {
            addEventHandler(KeyEvent.KEY_PRESSED) { println(it.code) }
        }
        hbox {
            style {
                alignment = Pos.CENTER
                setSpacing(20.0)
            }
            label("Channel Name")
            textfield().bind(controller.channelNameProperty)
            button("Start") {
                action { controller.start() }
            }
            label().bind(controller.errorTextProperty)
        }
    }

    override fun onDock() {
        title = "Twitch Event Keyboard Shortcuts"
        currentStage?.isResizable = false
        super.onDock()
    }
}

class MainController : Controller() {
    private val accessToken = AccessToken(Secret.clientId, Secret.clientSecret, OkHttpClient())
    private val credential: OAuth2Credential get() = OAuth2Credential(null, accessToken.accessToken)
    private val twitchClient = TwitchClientBuilder.builder()
        .withEnableHelix(true)
        .withEnablePubSub(true)
        .withDefaultAuthToken(credential)
        .withClientId(Secret.clientId)
        .withClientSecret(Secret.clientSecret)
        .build()

    private val model = Model.load()

    val channelNameProperty = SimpleStringProperty(model.channelName)
    private var channelName by channelNameProperty

    val errorTextProperty = SimpleStringProperty("")
    private var errorText by errorTextProperty

    fun start() {
        val channelId = try {
            val resultList = twitchClient.helix.getUsers(accessToken.accessToken, null, listOf(channelName)).execute()
            resultList.users.find { it.displayName == channelName }!!.id
        } catch(e: HystrixRuntimeException) {
            errorText = "Channel not found"
            return
        } catch(e: NullPointerException) {
            errorText = "Channel not found"
            return
        }

        // Update the saved channelName
        model.channelName = channelName
        model.save()

        errorText = channelId

        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java).registerListener(this)

        twitchClient.clientHelper.enableFollowEventListener(channelName)
        twitchClient.pubSub.listenForChannelPointsRedemptionEvents(credential, channelId)
        twitchClient.pubSub.listenForCheerEvents(credential, channelId)
        twitchClient.pubSub.listenForSubscriptionEvents(credential, channelId)
    }

    @EventSubscriber
    fun handleFollow(event: FollowEvent) {
        print("follow: " + event.user.name)
    }

    @EventSubscriber
    fun handleChannelPointsRedemption(event: ChannelPointsRedemptionEvent) {
        println("channelPoints: " + event.redemption.reward.title)
    }

    @EventSubscriber
    fun handleCheer(event: CheerEvent) {
        print("cheer: " + event.user.name + ", bits: " + event.bits)
    }

    @EventSubscriber
    fun handleSubscription(event: SubscriptionEvent) {
        if(event.gifted) {
            return // Handle these elsewhere
        }
        print("subscription: " + event.user.name + ", months: " + event.months)
    }

    @EventSubscriber
    fun handleGiftSubscriptions(event: GiftSubscriptionsEvent) {
        print("gift: " + event.user.name + ", count: " + event.count)
    }
}