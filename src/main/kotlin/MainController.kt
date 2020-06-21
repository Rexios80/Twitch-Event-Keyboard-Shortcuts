import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.philippheuer.events4j.simple.domain.EventSubscriber
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.FollowEvent
import com.github.twitch4j.chat.events.channel.GiftSubscriptionsEvent
import com.github.twitch4j.common.events.domain.EventChannel
import com.github.twitch4j.common.events.domain.EventUser
import com.github.twitch4j.pubsub.domain.*
import com.github.twitch4j.pubsub.enums.SubscriptionType
import com.github.twitch4j.pubsub.events.ChannelBitsEvent
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent
import com.netflix.hystrix.exception.HystrixRuntimeException
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.Controller
import tornadofx.getValue
import tornadofx.observableListOf
import tornadofx.setValue
import java.util.*

class MainController : Controller() {
    private val model = Model.load()
    private var twitchClient: TwitchClient? = null
    val eventConsole = EventConsole()
    private val keyStroker = KeyStroker(eventConsole)

    var testChannel: EventChannel? = null
    val testUser = EventUser("TEST", "TEST")

    val channelNameProperty = SimpleStringProperty(model.channelName)
    private var channelName by channelNameProperty

    val oauthTokenProperty = SimpleStringProperty(model.oauthToken)
    private var oauthToken by oauthTokenProperty

    val startedProperty = SimpleBooleanProperty(false)
    private var started by startedProperty

    val errorTextProperty = SimpleStringProperty("")
    private var errorText by errorTextProperty

    fun start() {
        errorText = ""

        val credential = OAuth2Credential(null, oauthToken)
        twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withEnablePubSub(true)
            .withEnableChat(true)
            .withChatAccount(credential)
            .withDefaultAuthToken(credential)
            .build()

        val channelId = try {
            val resultList = twitchClient!!.helix.getUsers(oauthToken, null, listOf(channelName)).execute()
            resultList.users.find { it.displayName == channelName }!!.id
        } catch (e: HystrixRuntimeException) {
            errorText = "Invalid OAuth token"
            eventConsole.log("Error starting: $errorText")
            return
        } catch (e: NullPointerException) {
            errorText = "Channel not found"
            eventConsole.log("Error starting: $errorText")
            return
        }

        // Create EventChannel for test events
        testChannel = EventChannel(channelId, channelName)

        // Save the channelName and oauthToken
        model.channelName = channelName
        model.oauthToken = oauthToken
        model.save()

        twitchClient?.eventManager?.getEventHandler(SimpleEventHandler::class.java)?.registerListener(this)

        twitchClient?.clientHelper?.enableFollowEventListener(channelName)
        twitchClient?.pubSub?.listenForChannelPointsRedemptionEvents(credential, channelId)
        twitchClient?.pubSub?.listenForCheerEvents(credential, channelId)
        twitchClient?.pubSub?.listenForSubscriptionEvents(credential, channelId)
        twitchClient?.chat?.joinChannel(channelName)

        started = true
        eventConsole.log("Started!")
    }

    fun <T : MetaShortcut> getShortcutsList(clazz: Class<T>): ObservableList<T> {
        return when (clazz) {
            FollowShortcut::class.java -> model.followShortcuts as ObservableList<T>
            ChannelPointsShortcut::class.java -> model.channelPointsShortcuts as ObservableList<T>
            BitsShortcut::class.java -> model.bitsShortcuts as ObservableList<T>
            SubscriptionShortcut::class.java -> model.subscriptionShortcuts as ObservableList<T>
            GiftSubscriptionShortcut::class.java -> model.giftSubscriptionShortcuts as ObservableList<T>
            else -> observableListOf() // Should never get here
        }
    }

    fun <T : MetaShortcut> addShortcut(clazz: Class<T>, value: String, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean, cooldown: Long?) {
        if (shortcutOnEvent.key == null) return
        if (waitTime != null && shortcutAfterWait.key == null) return

        when (clazz) {
            FollowShortcut::class.java -> model.followShortcuts.add(FollowShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown))
            ChannelPointsShortcut::class.java -> {
                if (value.isEmpty()) return
                model.channelPointsShortcuts.add(ChannelPointsShortcut(value, shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown))
            }
            BitsShortcut::class.java -> {
                val bits = value.toIntOrNull() ?: return
                model.bitsShortcuts.add(BitsShortcut(bits, shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown))
            }
            SubscriptionShortcut::class.java -> {
                val months = value.toIntOrNull() ?: return
                model.subscriptionShortcuts.add(SubscriptionShortcut(months, shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown))
            }
            GiftSubscriptionShortcut::class.java -> {
                val count = value.toIntOrNull() ?: return
                model.giftSubscriptionShortcuts.add(GiftSubscriptionShortcut(count, shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown))
            }
        }

        model.save()
    }

    fun removeShortcut(shortcut: MetaShortcut?) {
        when (shortcut) {
            is FollowShortcut -> model.followShortcuts.remove(shortcut)
            is ChannelPointsShortcut -> model.channelPointsShortcuts.remove(shortcut)
            is BitsShortcut -> model.bitsShortcuts.remove(shortcut)
            is SubscriptionShortcut -> model.subscriptionShortcuts.remove(shortcut)
            is GiftSubscriptionShortcut -> model.giftSubscriptionShortcuts.remove(shortcut)
        }
    }

    @EventSubscriber
    fun handleFollow(event: FollowEvent) {
        eventConsole.log("Follow Event - User: " + event.user.name)
        model.followShortcuts.forEach {
            keyStroker.strokeKeys(it)
        }
    }

    @EventSubscriber
    fun handleChannelPointsRedemption(event: ChannelPointsRedemptionEvent) {
        val title = event.redemption.reward.title
        eventConsole.log("Channel Points Redemption Event - User: " + event.redemption.user.displayName + ", Title: $title")
        model.channelPointsShortcuts.filter { it.title == title }.forEach {
            keyStroker.strokeKeys(it)
        }
    }

    @EventSubscriber
    fun handleBits(event: ChannelBitsEvent) {
        eventConsole.log("Bits Event - User: " + event.data.userName + ", Bits: " + event.data.bitsUsed)
        fireIntValueShortcuts(event.data.bitsUsed, model.bitsShortcuts)
    }

    @EventSubscriber
    fun handleSubscription(event: ChannelSubscribeEvent) {
        if (event.data.context == SubscriptionType.SUB_GIFT || event.data.context == SubscriptionType.ANON_SUB_GIFT) {
            return // Handle these elsewhere
        }
        eventConsole.log("Subscription Event - User: " + event.data.userName + ", Months: " + event.data.cumulativeMonths)
        fireIntValueShortcuts(event.data.cumulativeMonths, model.subscriptionShortcuts)
    }

    @EventSubscriber
    fun handleGiftSubscriptions(event: GiftSubscriptionsEvent) {
        eventConsole.log("Gift Subscription Event - User: " + event.user.name + ", Count: " + event.count)
        fireIntValueShortcuts(event.count, model.giftSubscriptionShortcuts)
    }

    private fun fireIntValueShortcuts(eventValue: Int, shortcuts: List<MetaShortcut>) {
        var previous: MetaShortcut? = null
        val fired = mutableListOf<MetaShortcut>()
        shortcuts.forEach {
            if (eventValue < it.valueInt) {
                if (!fired.contains(previous)) {
                    keyStroker.strokeKeys(previous ?: return)
                }
                return
            }
            if (it.alwaysFire) {
                keyStroker.strokeKeys(it)
                fired.add(it)
            }
            previous = it
        }

        // The value is greater than or equal to the last value in the list
        if (!fired.contains(previous)) {
            keyStroker.strokeKeys(previous ?: return)
        }
    }

    fun sendTestEvent(type: EventType, value: String) {
        val testEvent = when (type) {
            EventType.follow -> FollowEvent(testChannel, testUser)
            EventType.channelPoints -> {
                val redemption = ChannelPointsRedemption()
                redemption.reward = ChannelPointsReward()
                redemption.reward.title = value
                redemption.user = ChannelPointsUser()
                redemption.user.displayName = testUser.name
                ChannelPointsRedemptionEvent(Calendar.getInstance(), redemption)
            }
            EventType.bits -> {
                val data = ChannelBitsData()
                data.userName = testUser.name
                data.bitsUsed = value.toIntOrNull() ?: return
                ChannelBitsEvent(data)
            }
            EventType.subscription -> {
                val data = SubscriptionData()
                data.userName = testUser.name
                data.cumulativeMonths = value.toIntOrNull() ?: return
                ChannelSubscribeEvent(data)
            }
            EventType.giftSubscription -> GiftSubscriptionsEvent(testChannel, testUser, "", value.toIntOrNull() ?: return, -1)
        }

        twitchClient?.eventManager?.publish(testEvent)
    }
}

enum class EventType(val eventName: String) {
    follow("Follow"),
    channelPoints("Channel Points"),
    bits("Bits"),
    subscription("Subscription"),
    giftSubscription("Gift Subscription");

    override fun toString(): String {
        return eventName
    }
}