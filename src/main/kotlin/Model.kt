import com.google.gson.*
import com.google.gson.reflect.TypeToken
import javafx.collections.FXCollections
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type


class Model {
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
                FileReader(configFile).use {
                    return GsonBuilder().registerTypeAdapter(Model::class.java, ModelDeserializer()).create().fromJson(it, Model::class.java)
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

        FileWriter(configFile).use { Gson().toJson(this, it) }
    }

    class ModelDeserializer : JsonDeserializer<Model> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Model {
            val model = Model()
            val jsonObject = json?.asJsonObject ?: return model

            model.channelName = jsonObject.get("channelName").asString
            model.oauthToken = jsonObject.get("oauthToken").asString

            model.followShortcuts = FXCollections.observableArrayList<FollowShortcut>(Gson().fromJson<ArrayList<FollowShortcut>>(jsonObject.get("followShortcuts")))
            model.chatCommandShortcuts = FXCollections.observableArrayList<ChatCommandShortcut>(Gson().fromJson<ArrayList<ChatCommandShortcut>>(jsonObject.get("chatCommandShortcuts")))
            model.channelPointsShortcuts = FXCollections.observableArrayList<ChannelPointsShortcut>(Gson().fromJson<ArrayList<ChannelPointsShortcut>>(jsonObject.get("channelPointsShortcuts")))
            model.bitsShortcuts = FXCollections.observableArrayList<BitsShortcut>(Gson().fromJson<ArrayList<BitsShortcut>>(jsonObject.get("bitsShortcuts")))
            model.subscriptionShortcuts = FXCollections.observableArrayList<SubscriptionShortcut>(Gson().fromJson<ArrayList<SubscriptionShortcut>>(jsonObject.get("subscriptionShortcuts")))
            model.giftSubscriptionShortcuts = FXCollections.observableArrayList<GiftSubscriptionShortcut>(Gson().fromJson<ArrayList<GiftSubscriptionShortcut>>(jsonObject.get("giftSubscriptionShortcuts")))

            return model
        }

        private inline fun <reified T> Gson.fromJson(json: JsonElement): T = fromJson<T>(json, object : TypeToken<T>() {}.type)
    }
}