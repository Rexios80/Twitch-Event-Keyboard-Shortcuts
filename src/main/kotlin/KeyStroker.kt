import javafx.scene.input.KeyCode
import java.awt.Robot
import java.lang.System.currentTimeMillis

class KeyStroker(private val console: EventConsole) {
    private val robot = Robot()

    fun strokeKeys(shortcut: MetaShortcut) {
        val now = currentTimeMillis()
        if (now - shortcut.lastFireTime <= shortcut.cooldown ?: 0) {
            console.log("Attempted to fire shortcut within cooldown period")
            return
        }
        shortcut.lastFireTime = now

        Thread {
            stroke(shortcut.shortcutOnEvent)
        }.start()

        if (shortcut.waitTime != null) {
            Thread {
                console.log("Waiting " + shortcut.waitTimeString + "ms")
                Thread.sleep(shortcut.waitTime)
                stroke(shortcut.shortcutAfterWait)
            }.start()
        }
    }

    private fun stroke(shortcut: Shortcut?) {
        shortcut ?: return

        val field = KeyCode::class.java.getDeclaredField("code")
        field.isAccessible = true

        val modifierCodes = shortcut.modifiers.map { field.getInt(it) }
        val keyCode = field.getInt(shortcut.key)

        modifierCodes.forEach {
            robot.keyPress(it)
        }
        robot.keyPress(keyCode)
        Thread.sleep(250)
        robot.keyRelease(keyCode)
        modifierCodes.forEach {
            robot.keyRelease(it)
        }

        console.log("Shortcut Fired: " + shortcut.createShortcutString())
    }
}