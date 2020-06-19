import javafx.scene.input.KeyCode
import java.io.Serializable

data class Shortcut(val modifiers: MutableList<KeyCode>, var key: KeyCode?) : Serializable {
    fun createShortcutString(): String {
        val keysPressed = mutableListOf<String>()
        if (modifiers.contains(KeyCode.COMMAND)) {
            keysPressed.add("Cmd")
        }
        if (modifiers.contains(KeyCode.WINDOWS)) {
            keysPressed.add("Win")
        }
        if (modifiers.contains(KeyCode.CONTROL)) {
            keysPressed.add("Ctrl")
        }
        if (modifiers.contains(KeyCode.ALT)) {
            keysPressed.add("Alt")
        }
        if (modifiers.contains(KeyCode.SHIFT)) {
            keysPressed.add("Shift")
        }
        val keyName = key?.name ?: ""
        keysPressed.add(keyName.split("_").joinToString(" ") { it.toLowerCase().capitalize() })

        return keysPressed.joinToString(" + ")
    }
}

open class MetaShortcut(val shortcutOnEvent: Shortcut, val waitTime: Long?, val shortcutAfterWait: Shortcut, val alwaysFire: Boolean) : Serializable {
    val shortcutOnEventString: String get() = shortcutOnEvent.createShortcutString()
    val shortcutAfterWaitString: String get() = shortcutAfterWait.createShortcutString()
}

class FollowShortcut(shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire)

class ChannelPointsShortcut(val title: String, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire)

class CheerShortcut(val bits: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire)

class SubscriptionShortcut(val months: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire)

class GiftSubscriptionShortcut(val count: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire)