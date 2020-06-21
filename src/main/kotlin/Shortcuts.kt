import javafx.scene.input.KeyCode
import java.io.Serializable
import java.text.NumberFormat
import java.util.*

data class Shortcut(val modifiers: MutableList<KeyCode>, var key: KeyCode?) : Serializable {
    fun createShortcutString(): String {
        val keysPressed = mutableListOf<String>()
        if (modifiers.contains(KeyCode.COMMAND)) {
            keysPressed.add("⌘")
        }
        if (modifiers.contains(KeyCode.WINDOWS)) {
            keysPressed.add("⊞")
        }
        if (modifiers.contains(KeyCode.CONTROL)) {
            keysPressed.add("^")
        }
        if (modifiers.contains(KeyCode.ALT)) {
            keysPressed.add("⎇")
        }
        if (modifiers.contains(KeyCode.SHIFT)) {
            keysPressed.add("⇧")
        }
        val keyName = if (key?.isKeypadKey == true) {
            when (key) {
                KeyCode.KP_UP -> "N↑"
                KeyCode.KP_DOWN -> "N↓"
                KeyCode.KP_LEFT -> "N←"
                KeyCode.KP_RIGHT -> "N→"
                else -> "N" + key?.getName()?.split(" ")?.get(1)
            }
        } else {
            when (key) {
                KeyCode.BACK_SPACE -> "⇤"
                KeyCode.ENTER -> "↵"
                KeyCode.TAB -> "⇥"
                KeyCode.UP -> "↑"
                KeyCode.DOWN -> "↓"
                KeyCode.LEFT -> "←"
                KeyCode.RIGHT -> "→"
                else -> {
                    val name = key?.getName() ?: ""
                    if (name.length >= 3) {
                        name.substring(0, 3).toUpperCase()
                    } else {
                        name
                    }
                }
            }
        }
        keysPressed.add(keyName)

        return keysPressed.joinToString(" + ")
    }
}

abstract class MetaShortcut(val shortcutOnEvent: Shortcut, val waitTime: Long?, val shortcutAfterWait: Shortcut?, val alwaysFire: Boolean, val cooldown: Long?) : Serializable {
    val shortcutOnEventString: String get() = shortcutOnEvent.createShortcutString()
    val shortcutAfterWaitString: String get() = shortcutAfterWait?.createShortcutString() ?: ""
    val waitTimeString: String get() = if (waitTime == null) "" else NumberFormat.getNumberInstance(Locale.US).format(waitTime)
    val alwaysFireString: String get() = if (alwaysFire) "✓" else ""
    val cooldownString: String get() = if (cooldown == null) "" else NumberFormat.getNumberInstance(Locale.US).format(cooldown)

    abstract val valueInt: Int
    abstract val valueString: String?

    var lastFireTime: Long = 0
}

class FollowShortcut(shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, cooldown: Long?) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, false, cooldown) {
    override val valueInt: Int get() = -1
    override val valueString: String? get() = null
}

class ChatCommandShortcut(val command: String, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, cooldown: Long?) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, false, cooldown) {
    override val valueInt: Int get() = -1
    override val valueString: String? get() = command
}

class ChannelPointsShortcut(val title: String, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, cooldown: Long?) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, false, cooldown) {
    override val valueInt: Int get() = -1
    override val valueString: String? get() = title
}

class BitsShortcut(val bits: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean, cooldown: Long?) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown) {
    override val valueInt: Int get() = bits
    override val valueString: String? get() = NumberFormat.getNumberInstance(Locale.US).format(bits)
}

class SubscriptionShortcut(val months: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean, cooldown: Long?) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown) {
    override val valueInt: Int get() = months
    override val valueString: String? get() = months.toString()
}

class GiftSubscriptionShortcut(val count: Int, shortcutOnEvent: Shortcut, waitTime: Long?, shortcutAfterWait: Shortcut, alwaysFire: Boolean, cooldown: Long?) : MetaShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire, cooldown) {
    override val valueInt: Int get() = count
    override val valueString: String? get() = count.toString()
}