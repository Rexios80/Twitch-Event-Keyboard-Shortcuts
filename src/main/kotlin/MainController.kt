import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.philippheuer.events4j.simple.domain.EventSubscriber
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.CheerEvent
import com.github.twitch4j.chat.events.channel.FollowEvent
import com.github.twitch4j.chat.events.channel.GiftSubscriptionsEvent
import com.github.twitch4j.chat.events.channel.SubscriptionEvent
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent
import com.netflix.hystrix.exception.HystrixRuntimeException
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import tornadofx.Controller
import tornadofx.getValue
import tornadofx.setValue

class MainController : Controller() {
    val model = Model.load()
    val keyStroker = KeyStroker()
    var twitchClient: TwitchClient? = null

    val channelNameProperty = SimpleStringProperty(model.channelName)
    private var channelName by channelNameProperty

    val oauthTokenProperty = SimpleStringProperty(model.oauthToken)
    private var oauthToken by oauthTokenProperty

    val startedProperty = SimpleBooleanProperty(false)
    private var started by startedProperty

    val errorTextProperty = SimpleStringProperty("")
    private var errorText by errorTextProperty

    val shortcutStringProperty = SimpleStringProperty("")
    private var shortcutString by shortcutStringProperty

    private val modifierKeysPressed = mutableListOf<KeyCode>()
    private val savedModifierKeys = mutableListOf<KeyCode>()
    private var nonModifierKeyPressed: KeyCode? = null

    fun start() {
        val token = oauthToken.removePrefix("oauth:")
        val credential = OAuth2Credential(null, token)
        twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withEnablePubSub(true)
            .withDefaultAuthToken(credential)
            .withClientId(Secret.clientId)
            .withClientSecret(Secret.clientSecret)
            .build()

        val channelId = try {
            val resultList = twitchClient!!.helix.getUsers(token, null, listOf(channelName)).execute()
            resultList.users.find { it.displayName == channelName }!!.id
        } catch (e: HystrixRuntimeException) {
            errorText = "Channel not found"
            return
        } catch (e: NullPointerException) {
            errorText = "Channel not found"
            return
        }

        // Strip "oauth:" from entered string to show the user that it happened
        oauthToken = token

        // Save the channelName and oauthToken
        model.channelName = channelName
        model.oauthToken = token
        model.save()

        twitchClient?.eventManager?.getEventHandler(SimpleEventHandler::class.java)?.registerListener(this)

        twitchClient?.clientHelper?.enableFollowEventListener(channelName)
        twitchClient?.pubSub?.listenForChannelPointsRedemptionEvents(credential, channelId)
        twitchClient?.pubSub?.listenForCheerEvents(credential, channelId)
        twitchClient?.pubSub?.listenForSubscriptionEvents(credential, channelId)

        started = true
    }

    fun stop() {
        twitchClient?.close()
        started = false
    }

    fun handleKeyPress(event: KeyEvent) {
        if (event.eventType == KeyEvent.KEY_PRESSED) {
            if (modifierKeysPressed.isEmpty() && nonModifierKeyPressed != null) {
                // This is a new key combination
                savedModifierKeys.clear()
                nonModifierKeyPressed = null
            }
            if (event.code.isModifierKey) {
                modifierKeysPressed.add(event.code)
            } else {
                // This is the end of a key combination
                nonModifierKeyPressed = event.code
                savedModifierKeys.clear()
                savedModifierKeys.addAll(modifierKeysPressed)
            }

            shortcutString = Shortcut.createShortcutString(modifierKeysPressed, nonModifierKeyPressed)
        } else if (event.eventType == KeyEvent.KEY_RELEASED) {
            if (event.code.isModifierKey) {
                modifierKeysPressed.remove(event.code)

                if (nonModifierKeyPressed == null) {
                    shortcutString = Shortcut.createShortcutString(modifierKeysPressed, nonModifierKeyPressed)
                }
            }
        }
    }

    fun addFollowShortcut() {
        model.followShortcuts.add(FollowShortcut(savedModifierKeys.toMutableList(), nonModifierKeyPressed ?: return))
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
        if (event.gifted) {
            return // Handle these elsewhere
        }
        print("subscription: " + event.user.name + ", months: " + event.months)
    }

    @EventSubscriber
    fun handleGiftSubscriptions(event: GiftSubscriptionsEvent) {
        print("gift: " + event.user.name + ", count: " + event.count)
    }
}