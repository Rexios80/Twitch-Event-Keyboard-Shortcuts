import javafx.scene.input.KeyCode
import java.awt.Robot

class KeyStroker {
    private val robot = Robot()

    fun strokeKeys(shortcut: Shortcut) {
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
    }
}