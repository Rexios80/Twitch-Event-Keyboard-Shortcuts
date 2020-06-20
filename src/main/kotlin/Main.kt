import javafx.beans.binding.When
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.input.KeyEvent
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class TeksApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()

    override val root = vbox {
        style {
            padding = box(20.px)
        }

        // Unfocus textfield on startup
        runLater { requestFocus() }

        hbox {
            form {
                minWidth = 350.0
                fieldset("Authentication", labelPosition = Orientation.VERTICAL) {
                    disableProperty().bind(When(controller.startedProperty).then(true).otherwise(false))
                    field("Channel Name") {
                        textfield().bind(controller.channelNameProperty)
                    }
                    field("OAuth Token") {
                        textfield().bind(controller.oauthTokenProperty)
                        button("Get") {
                            action {
                                Desktop.getDesktop()
                                    .browse(URI.create("https://twitchtokengenerator.com/quick/dTzb2hfaP5"))
                            }
                        }
                    }
                    field {
                        button("Start") {
                            action { controller.start() }
                        }
                        label().bind(controller.errorTextProperty)
                    }
                }
            }
            form {
                fitToParentWidth()
                fieldset("Event Console") {
                    tableview(controller.eventConsole.events) {
                        smartResize()
                        readonlyColumn("Time", ConsoleEvent::timeString) {
                            minWidth = 100.0
                            isSortable = false
                            isResizable = false
                        }
                        readonlyColumn("Event", ConsoleEvent::message) {
                            isSortable = false
                            isResizable = false
                        }
                    }
                }
            }
        }
        hbox {
            add(ShortcutsView(FollowShortcut::class.java, controller, "Follow Shortcuts"))
            add(ShortcutsView(ChannelPointsShortcut::class.java, controller, "Channel Points Shortcuts", true, "Title"))
        }
    }

    class ShortcutsView<T : MetaShortcut>(clazz: Class<T>, controller: MainController, title: String, hasValue: Boolean = false, valueLabel: String? = null) : Fragment() {
        val valueProperty = SimpleStringProperty("")
        val shortcutOnEventString = SimpleStringProperty("")
        val waitTimeProperty = SimpleLongProperty()
        val shortcutAfterWaitString = SimpleStringProperty("")
        val alwaysFireProperty = SimpleBooleanProperty(false)

        val shortcutOnEvent = Shortcut(mutableListOf(), null)
        val shortcutAfterWait = Shortcut(mutableListOf(), null)

        var selectedShortcut: MetaShortcut? = null

        override val root = form {
            fieldset(title, labelPosition = Orientation.VERTICAL) {
                field(valueLabel ?: "spacer") {
                    if (!hasValue) {
                        isVisible = false
                    }
                    textfield().bind(valueProperty)
                }
                field("Shortcut On Event") {
                    textfield {
                        bind(shortcutOnEventString)
                        isEditable = false
                        addEventHandler(KeyEvent.KEY_PRESSED) { handleKeyPress(it, shortcutOnEvent, shortcutOnEventString) }
                        addEventHandler(KeyEvent.KEY_RELEASED) { handleKeyPress(it, shortcutOnEvent, shortcutOnEventString) }
                    }
                }
                field("Wait Time (Milliseconds)") {
                    textfield().bind(waitTimeProperty)
                }
                field("Shortcut After Wait") {
                    textfield {
                        bind(shortcutAfterWaitString)
                        isEditable = false
                        addEventHandler(KeyEvent.KEY_PRESSED) { handleKeyPress(it, shortcutAfterWait, shortcutAfterWaitString) }
                        addEventHandler(KeyEvent.KEY_RELEASED) { handleKeyPress(it, shortcutAfterWait, shortcutAfterWaitString) }
                    }
                }
                field {
                    checkbox("Always fire").bind(alwaysFireProperty)
                }
                field {
                    button("Add") {
                        minWidth = 75.0
                        // TODO: Clone shortcuts to prevent unintended editing
                        action { controller.addShortcut(clazz, valueProperty.value, shortcutOnEvent.copy(), waitTimeProperty.value, shortcutAfterWait.copy(), alwaysFireProperty.value) }
                    }
                    button("Delete") {
                        minWidth = 75.0
//                        action { controller.removeFollowShortcut(selectedFollowShortcut) }
                    }
                }
                field {
                    tableview(controller.getShortcutsList<T>() as ObservableList<MetaShortcut>) {
                        if (hasValue) {
                            readonlyColumn(valueLabel ?: "", MetaShortcut::valueString) {
                                prefWidth = 75.0
                                isSortable = false
                                isResizable = false
                            }
                        }
                        readonlyColumn("Shortcut On Event", MetaShortcut::shortcutOnEventString) {
                            prefWidth = 250.0
                            isSortable = false
                            isResizable = false
                        }
                        selectionModel.selectedItemProperty().onChange {
                            selectedShortcut = it
                        }
                    }
                }
            }
        }

        private fun handleKeyPress(event: KeyEvent, shortcut: Shortcut, property: SimpleStringProperty) {
            if (event.eventType == KeyEvent.KEY_PRESSED) {
                if (shortcut.modifiers.isEmpty() && shortcut.key != null) {
                    // This is a new key combination
                    shortcut.modifiers.clear()
                    shortcut.key = null
                }
                if (event.code.isModifierKey) {
                    shortcut.modifiers.add(event.code)
                } else {
                    // This is the end of a key combination
                    shortcut.key = event.code
                }

                property.value = shortcut.createShortcutString()
            } else if (event.eventType == KeyEvent.KEY_RELEASED) {
                if (event.code.isModifierKey) {
                    if (shortcut.key == null) {
                        shortcut.modifiers.remove(event.code)
                        property.value = shortcut.createShortcutString()
                    }
                }
            }
        }
    }

    override fun onDock() {
        title = "Twitch Event Keyboard Shortcuts"
        currentStage?.isResizable = false
        super.onDock()
    }
}