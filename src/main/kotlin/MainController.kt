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

    val eventConsole = EventConsole()

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
        errorText = ""

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
            eventConsole.log("Error starting: $errorText")
            return
        } catch (e: NullPointerException) {
            errorText = "Channel not found"
            eventConsole.log("Error starting: $errorText")
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
        eventConsole.log("Started!")
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
        model.save()
    }

    fun removeFollowShortcut(shortcut: FollowShortcut?) {
        model.followShortcuts.remove(shortcut)
        model.save()
    }

    fun addChannelPointsShortcut(title: String) {
        if (title.isEmpty()) {
            return
        }
        model.channelPointsShortcuts.add(
            ChannelPointsShortcut(
                savedModifierKeys.toMutableList(),
                nonModifierKeyPressed ?: return,
                title
            )
        )
        model.save()
    }

    fun removeChannelPointsShortcut(shortcut: ChannelPointsShortcut?) {
        model.channelPointsShortcuts.remove(shortcut)
        model.save()
    }

    fun addCheerShortcut(bits: Int) {
        if (bits < 0) {
            return
        }
        model.cheerShortcuts.add(
            CheerShortcut(
                savedModifierKeys.toMutableList(),
                nonModifierKeyPressed ?: return,
                bits
            )
        )
        model.save()
    }

    fun removeCheerShortcut(shortcut: CheerShortcut?) {
        model.cheerShortcuts.remove(shortcut)
        model.save()
    }

    fun addSubscriptionShortcut(months: Int) {
        if (months < 0) {
            return
        }
        model.subscriptionShortcuts.add(
            SubscriptionShortcut(
                savedModifierKeys.toMutableList(),
                nonModifierKeyPressed ?: return,
                months
            )
        )
        model.save()
    }

    fun removeSubscriptionShortcut(shortcut: SubscriptionShortcut?) {
        model.subscriptionShortcuts.remove(shortcut)
        model.save()
    }

    fun addGiftSubscriptionShortcut(count: Int) {
        if (count < 0) {
            return
        }
        model.giftSubscriptionShortcuts.add(
            GiftSubscriptionShortcut(
                savedModifierKeys.toMutableList(),
                nonModifierKeyPressed ?: return,
                count
            )
        )
        model.save()
    }

    fun removeGiftSubscriptionShortcut(shortcut: GiftSubscriptionShortcut?) {
        model.giftSubscriptionShortcuts.remove(shortcut)
        model.save()
    }

    @EventSubscriber
    fun handleFollow(event: FollowEvent) {
        eventConsole.log("Follow Event - User: " + event.user.name)
        model.followShortcuts.forEach {
            keyStroker.strokeKeys(it.modifiers, it.key)
            eventConsole.log("Shortcut fired: " + it.shortcutString)
        }
    }

    @EventSubscriber
    fun handleChannelPointsRedemption(event: ChannelPointsRedemptionEvent) {
        val title = event.redemption.reward.title
        eventConsole.log("Channel Points Redemption Event - User: " + event.redemption.user.displayName + ", Title: $title")
        model.channelPointsShortcuts.filter { it.title == title }.forEach {
            keyStroker.strokeKeys(it.modifiers, it.key)
            eventConsole.log("Shortcut fired: " + it.shortcutString)
        }
    }

    @EventSubscriber
    fun handleCheer(event: CheerEvent) {
        eventConsole.log("Cheer Event - User: " + event.user.name + ", Bits: " + event.bits)
        var previous: Shortcut? = null
        model.cheerShortcuts.forEach {
            if (event.bits < it.bits) {
                keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
                return
            }
            previous = it
        }

        // The value is higher than the last value in the list
        keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
    }

    @EventSubscriber
    fun handleSubscription(event: SubscriptionEvent) {
        if (event.gifted) {
            return // Handle these elsewhere
        }
        eventConsole.log("Subscription Event - User: " + event.user.name + ", Months: " + event.months)
        var previous: Shortcut? = null
        model.subscriptionShortcuts.forEach {
            if (event.months < it.months) {
                keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
                return
            }
            previous = it
        }

        // The value is higher than the last value in the list
        keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
    }

    @EventSubscriber
    fun handleGiftSubscriptions(event: GiftSubscriptionsEvent) {
        eventConsole.log("Gift Subscriptions Event - User: " + event.user.name + ", Count: " + event.count)
        var previous: Shortcut? = null
        model.giftSubscriptionShortcuts.forEach {
            if (event.count < it.count) {
                keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
                return
            }
            previous = it
        }

        // The value is higher than the last value in the list
        keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
    }
}