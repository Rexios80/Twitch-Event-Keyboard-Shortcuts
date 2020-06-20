import javafx.scene.input.KeyCode
import java.io.Serializable
import java.text.NumberFormat
import java.util.*

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

abstract class MetaShortcut(val shortcutOnEvent: Shortcut, val waitTime: Long?, val shortcutAfterWait: Shortcut?, val alwaysFire: Boolean) : Serializable {
    val shortcutOnEventString: String get() = shortcutOnEvent.createShortcutString()
    val shortcutAfterWaitString: String get() = shortcutAfterWait?.createShortcutString() ?: ""
    val waitTimeString: String get() = if (waitTime == null) "" else NumberFormat.getNumberInstance(Locale.US).format(waitTime)
    val alwaysFireString: String get() = if (alwaysFire) "✓" else ""

    abstract val valueString: String?
}

class FollowShortcut(shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire) {
    override val valueString: String? get() = null
}

class ChannelPointsShortcut(val title: String, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire) {
    override val valueString: String? get() = title
}

class BitsShortcut(val bits: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire) {
    override val valueString: String? get() = NumberFormat.getNumberInstance(Locale.US).format(bits)
}

class SubscriptionShortcut(val months: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire) {
    override val valueString: String? get() = months.toString()
}

class GiftSubscriptionShortcut(val count: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire) {
    override val valueString: String? get() = count.toString()
}