import javafx.scene.input.KeyCode
import java.io.Serializable

open class Shortcut(val modifiers: List<KeyCode>, val key: KeyCode) : Serializable

class FollowShortcut(modifiers: List<KeyCode>, key: KeyCode) : Shortcut(modifiers, key)

class ChannelPointsShortcut(modifiers: List<KeyCode>, key: KeyCode, val title: String) : Shortcut(modifiers, key)

class CheerShortcut(modifiers: List<KeyCode>, key: KeyCode, val bits: Int) : Shortcut(modifiers, key)

class SubscriptionShortcut(modifiers: List<KeyCode>, key: KeyCode, val months: Int) : Shortcut(modifiers, key)

class GiftSubscriptionShortcut(modifiers: List<KeyCode>, key: KeyCode, val count: Int) : Shortcut(modifiers, key)