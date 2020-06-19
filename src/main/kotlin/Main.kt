import javafx.beans.binding.When
import javafx.scene.input.KeyEvent
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class TornadoApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()

    override val root = vbox {
        style {
            padding = box(20.px)
        }

        form {
            fieldset {
                field("Channel Name") {
                    textfield().bind(controller.channelNameProperty)
                }
                field("OAuth Token") {
                    textfield().bind(controller.oauthTokenProperty)
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
            form {
                fieldset {
                    field {
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
                            readonlyColumn("Key Combination", FollowShortcut::shortcutString)
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