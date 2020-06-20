import javafx.collections.FXCollections
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsoleEvent(date: LocalDateTime, val message: String) {
    val timeString = DateTimeFormatter.ofPattern("hh:mm:ss a").format(date)
}

class EventConsole {
    val events = FXCollections.observableArrayList<ConsoleEvent>()

    fun log(message: String) {
        events.add(0, ConsoleEvent(LocalDateTime.now(), message))
    }
}