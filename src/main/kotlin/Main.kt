import javafx.beans.binding.When
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Orientation
import javafx.scene.input.KeyEvent
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class TornadoApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()

    private val bitsProperty = SimpleIntegerProperty(0)

    override val root = vbox {
        style {
            padding = box(20.px)
        }

        hbox {
            form {
                minWidth = 350.0
                fieldset(labelPosition = Orientation.VERTICAL) {
                    disableProperty().bind(When(controller.startedProperty).then(true).otherwise(false))
                    field("Channel Name") {
                        textfield {
                            bind(controller.channelNameProperty)
                        }
                    }
                    field("OAuth Token") {
                        textfield {
                            bind(controller.oauthTokenProperty)
                        }
                        button("Get") {
                            action { Desktop.getDesktop().browse(URI.create("https://twitchapps.com/tmi/")) }
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
                            isSortable = false
                        }
                        readonlyColumn("Event", ConsoleEvent::message) {
                            isSortable = false
                        }
                    }
                }
            }
        }
        hbox {
            // MARK: Follow Shortcuts
            form {
                fieldset("Follow Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("spacer") {
                        isVisible = false
                        textfield {}
                    }
                    field("Shortcut") {
                        textfield {
                            bind(controller.shortcutStringProperty)
                            isEditable = false
                            addEventHandler(KeyEvent.KEY_PRESSED) { controller.handleKeyPress(it) }
                            addEventHandler(KeyEvent.KEY_RELEASED) { controller.handleKeyPress(it) }
                        }
                    }
                    field {
                        button("Add") {
                            action { controller.addFollowShortcut() }
                        }
                    }
                    field {
                        tableview(controller.model.followShortcuts) {
                            smartResize()
                            readonlyColumn("Key Combination", FollowShortcut::shortcutString) {
                                isSortable = false
                            }
                        }
                    }
                }
            }
            // MARK: Cheer Shortcuts
            form {
                fieldset("Cheer Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("Bits") {
                        textfield {
                            bind(bitsProperty)
                        }
                    }
                    field("Shortcut") {
                        textfield {
                            bind(controller.shortcutStringProperty)
                            isEditable = false
                            addEventHandler(KeyEvent.KEY_PRESSED) { controller.handleKeyPress(it) }
                            addEventHandler(KeyEvent.KEY_RELEASED) { controller.handleKeyPress(it) }
                        }
                    }
                    field {
                        button("Add") {
                            action { controller.addFollowShortcut() }
                        }
                    }
                    field {
                        tableview(controller.model.followShortcuts) {
                            smartResize()
                            readonlyColumn("Key Combination", FollowShortcut::shortcutString) {
                                isSortable = false
                            }
                        }
                    }
                }
            }
            // MARK: Follow Shortcuts
            form {
                fieldset("Follow Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("spacer") {
                        isVisible = false
                        textfield {}
                    }
                    field("Shortcut") {
                        textfield {
                            bind(controller.shortcutStringProperty)
                            isEditable = false
                            addEventHandler(KeyEvent.KEY_PRESSED) { controller.handleKeyPress(it) }
                            addEventHandler(KeyEvent.KEY_RELEASED) { controller.handleKeyPress(it) }
                        }
                    }
                    field {
                        button("Add") {
                            action { controller.addFollowShortcut() }
                        }
                    }
                    field {
                        tableview(controller.model.followShortcuts) {
                            smartResize()
                            readonlyColumn("Key Combination", FollowShortcut::shortcutString) {
                                isSortable = false
                            }
                        }
                    }
                }
            }
            // MARK: Follow Shortcuts
            form {
                fieldset("Follow Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("spacer") {
                        isVisible = false
                        textfield {}
                    }
                    field("Shortcut") {
                        textfield {
                            bind(controller.shortcutStringProperty)
                            isEditable = false
                            addEventHandler(KeyEvent.KEY_PRESSED) { controller.handleKeyPress(it) }
                            addEventHandler(KeyEvent.KEY_RELEASED) { controller.handleKeyPress(it) }
                        }
                    }
                    field {
                        button("Add") {
                            action { controller.addFollowShortcut() }
                        }
                    }
                    field {
                        tableview(controller.model.followShortcuts) {
                            smartResize()
                            readonlyColumn("Key Combination", FollowShortcut::shortcutString) {
                                isSortable = false
                            }
                        }
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