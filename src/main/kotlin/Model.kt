import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import java.io.*


class Model : Serializable {
    var channelName = ""
    var oauthToken = ""
    var followShortcuts = FXCollections.observableArrayList<FollowShortcut>()
    var channelPointsShortcuts = FXCollections.observableArrayList<ChannelPointsShortcut>()
    var cheerShortcuts = FXCollections.observableArrayList<CheerShortcut>()
    var subscriptionShortcuts = FXCollections.observableArrayList<SubscriptionShortcut>()
    var giftSubscriptionShortcuts = FXCollections.observableArrayList<GiftSubscriptionShortcut>()

    init {
        setupAutoSave()
    }

    fun setupAutoSave() {
        // Save the lists whenever they change
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
                    val model = it.readObject() as Model
                    // Deserialization calls init first so those listeners get overwritten
                    model.setupAutoSave()
                    return model
                }
            } catch (e: FileNotFoundException) {
                print("No config file found")
            }

            return Model()
        }
    }

    fun save() {
        // Auto sort lists
        channelPointsShortcuts.sortBy { it.title }
        cheerShortcuts.sortBy { it.bits }
        subscriptionShortcuts.sortBy { it.months }
        giftSubscriptionShortcuts.sortBy { it.count }

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

    @Suppress("UNCHECKED_CAST")
    private fun readObject(ois: ObjectInputStream) {
        channelName = ois.readUTF()
        oauthToken = ois.readUTF()

        try {
            followShortcuts = FXCollections.observableArrayList(ois.readObject() as List<FollowShortcut>)
            channelPointsShortcuts = FXCollections.observableArrayList(ois.readObject() as List<ChannelPointsShortcut>)
            cheerShortcuts = FXCollections.observableArrayList(ois.readObject() as List<CheerShortcut>)
            subscriptionShortcuts = FXCollections.observableArrayList(ois.readObject() as List<SubscriptionShortcut>)
            giftSubscriptionShortcuts = FXCollections.observableArrayList(ois.readObject() as List<GiftSubscriptionShortcut>)
        } catch (e: ClassCastException) {
            print("Unable to deserialize shortcuts")
        }
    }
}