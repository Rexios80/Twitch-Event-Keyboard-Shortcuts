import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import tornadofx.asObservable
import tornadofx.observableListOf
import java.io.*
import java.util.*


class Model : Serializable {
    var channelName = ""
    var oauthToken = ""
    var followShortcuts = observableListOf<FollowShortcut>()
    var channelPointsShortcuts = observableListOf<ChannelPointsShortcut>()
    var cheerShortcuts = observableListOf<CheerShortcut>()
    var subscriptionShortcuts = observableListOf<SubscriptionShortcut>()
    var giftSubscriptionShortcuts = observableListOf<GiftSubscriptionShortcut>()

    init {
        // Save the lists whenever they change (autosave)
        followShortcuts.addListener(ListChangeListener { save() })
        channelPointsShortcuts.addListener(ListChangeListener { save() })
        cheerShortcuts.addListener(ListChangeListener { save() })
        subscriptionShortcuts.addListener(ListChangeListener { save() })
        giftSubscriptionShortcuts.addListener(ListChangeListener { save() })
    }

    companion object {
        private const val configFile = "teksConfig"
        fun load(): Model {
            try {
                ObjectInputStream(FileInputStream(configFile)).use {
                    val model = it.readObject()
                    return model as Model
                }
            } catch(e: FileNotFoundException) {
                print("No config file found")
            }

            return Model()
        }
    }

    fun save() {
        ObjectOutputStream(FileOutputStream(configFile)).use { it.writeObject(this) }
    }

    private fun writeObject(oos: ObjectOutputStream) {
        oos.writeUTF(channelName)
        oos.writeUTF(oauthToken)
        oos.writeObject(followShortcuts.toList())
        oos.writeObject(channelPointsShortcuts.toList())
        oos.writeObject(cheerShortcuts.toList())
        oos.writeObject(subscriptionShortcuts.toList())
        oos.writeObject(giftSubscriptionShortcuts.toList())
    }

    private fun readObject(ois: ObjectInputStream) {
        channelName = ois.readUTF()
        oauthToken = ois.readUTF()

        try {
            followShortcuts = (ois.readObject() as List<FollowShortcut>).asObservable()
            channelPointsShortcuts = (ois.readObject() as List<ChannelPointsShortcut>).asObservable()
            cheerShortcuts = (ois.readObject() as List<CheerShortcut>).asObservable()
            subscriptionShortcuts = (ois.readObject() as List<SubscriptionShortcut>).asObservable()
            giftSubscriptionShortcuts = (ois.readObject() as List<GiftSubscriptionShortcut>).asObservable()
        } catch(e: ClassCastException) {
            print("Unable to deserialize shortcuts")
        }
    }
}