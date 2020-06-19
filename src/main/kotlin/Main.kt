import javafx.beans.binding.When
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.input.KeyEvent
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class TornadoApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()

    private val rewardTitleProperty = SimpleStringProperty("")
    private val bitsProperty = SimpleIntegerProperty(0)
    private val monthsProperty = SimpleIntegerProperty(0)
    private val countProperty = SimpleIntegerProperty(0)

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
                        textfield().bind(controller.channelNameProperty)
                    }
                    field("OAuth Token") {
                        textfield().bind(controller.oauthTokenProperty)
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
                        textfield()
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
                            readonlyColumn("Shortcut", FollowShortcut::shortcutString) {
                                prefWidth = 250.0
                                isSortable = false
                                isResizable = false
                            }
                        }
                    }
                }
            }
            // MARK: Channel Points Shortcuts
            form {
                fieldset("Channel Points Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("Title") {
                        textfield().bind(rewardTitleProperty)
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
                            action { controller.addChannelPointsShortcut(rewardTitleProperty.value) }
                        }
                    }
                    field {
                        tableview(controller.model.channelPointsShortcuts) {
                            smartResize()
                            readonlyColumn("Title", ChannelPointsShortcut::title) {
                                prefWidth = 80.0
                                isResizable = false
                                isResizable = false
                            }
                            readonlyColumn("Shortcut", ChannelPointsShortcut::shortcutString) {
                                prefWidth = 250.0
                                isSortable = false
                                isResizable = false
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
                            action { controller.addCheerShortcut(bitsProperty.value) }
                        }
                    }
                    field {
                        tableview(controller.model.cheerShortcuts) {
                            smartResize()
                            readonlyColumn("Bits", CheerShortcut::bits) {
                                prefWidth = 80.0
                                isSortable = false
                                isResizable = false
                            }
                            readonlyColumn("Shortcut", CheerShortcut::shortcutString) {
                                prefWidth = 250.0
                                isSortable = false
                                isResizable = false
                            }
                        }
                    }
                }
            }
            // MARK: Subscription Shortcuts
            form {
                fieldset("Subscription Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("Months") {
                        textfield().bind(monthsProperty)
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
                            action { controller.addSubscriptionShortcut(monthsProperty.value) }
                        }
                    }
                    field {
                        tableview(controller.model.subscriptionShortcuts) {
                            smartResize()
                            readonlyColumn("Months", SubscriptionShortcut::months) {
                                prefWidth = 80.0
                                isSortable = false
                                isResizable = false
                            }
                            readonlyColumn("Shortcut", SubscriptionShortcut::shortcutString) {
                                prefWidth = 250.0
                                isSortable = false
                                isResizable = false
                            }
                        }
                    }
                }
            }
            // MARK: Gift Subscription Shortcuts
            form {
                fieldset("Gift Subscription Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("spacer") {
                        textfield().bind(countProperty)
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
                            action { controller.addGiftSubscriptionShortcut(countProperty.value) }
                        }
                    }
                    field {
                        tableview(controller.model.giftSubscriptionShortcuts) {
                            smartResize()
                            readonlyColumn("Count", GiftSubscriptionShortcut::count) {
                                prefWidth = 80.0
                                isSortable = false
                                isResizable = false
                            }
                            readonlyColumn("Shortcut", GiftSubscriptionShortcut::shortcutString) {
                                prefWidth = 250.0
                                isSortable = false
                                isResizable = false
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