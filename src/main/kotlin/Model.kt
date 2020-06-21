import javafx.collections.FXCollections
import java.io.*


class Model : Serializable {
    var channelName = ""
    var oauthToken = ""
    var followShortcuts = FXCollections.observableArrayList<FollowShortcut>()
    var chatCommandShortcuts = FXCollections.observableArrayList<ChatCommandShortcut>()
    var channelPointsShortcuts = FXCollections.observableArrayList<ChannelPointsShortcut>()
    var bitsShortcuts = FXCollections.observableArrayList<BitsShortcut>()
    var subscriptionShortcuts = FXCollections.observableArrayList<SubscriptionShortcut>()
    var giftSubscriptionShortcuts = FXCollections.observableArrayList<GiftSubscriptionShortcut>()

    companion object {
        private const val configFile = "teksConfig"
        fun load(): Model {
            try {
                ObjectInputStream(FileInputStream(configFile)).use {
                    return it.readObject() as Model
                }
            } catch (e: FileNotFoundException) {
                print("No config file found")
            }

            return Model()
        }
    }

    fun save() {
        // Auto sort lists
        followShortcuts.sortBy { it.shortcutOnEventString }
        chatCommandShortcuts.sortBy { it.command.toLowerCase() }
        channelPointsShortcuts.sortBy { it.title.toLowerCase() }
        bitsShortcuts.sortBy { it.bits }
        subscriptionShortcuts.sortBy { it.months }
        giftSubscriptionShortcuts.sortBy { it.count }

        ObjectOutputStream(FileOutputStream(configFile)).use { it.writeObject(this) }
    }

    private fun writeObject(oos: ObjectOutputStream) {
        oos.writeUTF(channelName)
        oos.writeUTF(oauthToken)
        oos.writeObject(followShortcuts.toList())
        oos.writeObject(chatCommandShortcuts.toList())
        oos.writeObject(channelPointsShortcuts.toList())
        oos.writeObject(bitsShortcuts.toList())
        oos.writeObject(subscriptionShortcuts.toList())
        oos.writeObject(giftSubscriptionShortcuts.toList())
    }

    @Suppress("UNCHECKED_CAST")
    private fun readObject(ois: ObjectInputStream) {
        channelName = ois.readUTF()
        oauthToken = ois.readUTF()

        try {
            followShortcuts = FXCollections.observableArrayList(ois.readObject() as List<FollowShortcut>)
            chatCommandShortcuts = FXCollections.observableArrayList(ois.readObject() as List<ChatCommandShortcut>)
            channelPointsShortcuts = FXCollections.observableArrayList(ois.readObject() as List<ChannelPointsShortcut>)
            bitsShortcuts = FXCollections.observableArrayList(ois.readObject() as List<BitsShortcut>)
            subscriptionShortcuts = FXCollections.observableArrayList(ois.readObject() as List<SubscriptionShortcut>)
            giftSubscriptionShortcuts = FXCollections.observableArrayList(ois.readObject() as List<GiftSubscriptionShortcut>)
        } catch (e: ClassCastException) {
            print("Unable to deserialize shortcuts")
        }
    }
}