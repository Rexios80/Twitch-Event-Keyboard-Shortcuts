import javafx.scene.input.KeyCode
import java.awt.Robot

class KeyStroker {
    private val robot = Robot()

    fun strokeKeys(modifiers: List<KeyCode>, key: KeyCode) {
        val field = KeyCode::class.java.getDeclaredField("code")
        field.isAccessible = true

        val modifierCodes = modifiers.map { field.getInt(it) }
        val keyCode = field.getInt(key)

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