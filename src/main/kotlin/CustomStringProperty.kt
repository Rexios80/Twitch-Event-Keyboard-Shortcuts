import javafx.beans.property.SimpleStringProperty

class CustomStringProperty(val function: () -> String) : SimpleStringProperty() {
    override fun get(): String {
        return function()
    }
}