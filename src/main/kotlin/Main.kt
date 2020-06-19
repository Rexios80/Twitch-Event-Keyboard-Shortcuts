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

        form {
            maxWidth = 350.0
            fieldset(labelPosition = Orientation.VERTICAL) {
                field("Channel Name") {
                    textfield {
                        bind(controller.channelNameProperty)
                        disableProperty().bind(When(controller.startedProperty).then(true).otherwise(false))
                    }
                }
                field("OAuth Token") {
                    textfield {
                        bind(controller.oauthTokenProperty)
                        disableProperty().bind(When(controller.startedProperty).then(true).otherwise(false))
                    }
                    button("Get") {
                        action { Desktop.getDesktop().browse(URI.create("https://twitchapps.com/tmi/")) }
                    }
                }
                field {
                    button {
                        textProperty().bind(When(controller.startedProperty).then("Stop").otherwise("Start"))
                        action {
                            if (controller.startedProperty.value) {
                                controller.stop()
                            } else {
                                controller.start()
                            }
                        }
                    }
                    label().bind(controller.errorTextProperty)
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