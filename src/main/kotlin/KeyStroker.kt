import javafx.scene.input.KeyCode
import java.awt.Robot

class KeyStroker(private val console: EventConsole) {
    private val robot = Robot()

    fun strokeKeys(shortcut: MetaShortcut) {
        stroke(shortcut.shortcutOnEvent)

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
        robot.keyRelease(keyCode)
        modifierCodes.forEach {
            robot.keyRelease(it)
        }

        console.log("Shortcut Fired: " + shortcut.createShortcutString())
    }
}