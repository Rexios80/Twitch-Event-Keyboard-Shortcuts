import javafx.beans.binding.When
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.input.KeyEvent
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class TeksApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()

    private val rewardTitleProperty = SimpleStringProperty("")
    private val bitsProperty = SimpleIntegerProperty(0)
    private val monthsProperty = SimpleIntegerProperty(0)
    private val countProperty = SimpleIntegerProperty(0)

    private var selectedFollowShortcut: FollowShortcut? = null
    private var selectedChannelPointsShortcut: ChannelPointsShortcut? = null
    private var selectedCheerShortcut: CheerShortcut? = null
    private var selectedSubscriptionShortcut: SubscriptionShortcut? = null
    private var selectedGiftSubscriptionShortcut: GiftSubscriptionShortcut? = null

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
                            action { Desktop.getDesktop().browse(URI.create("https://twitchtokengenerator.com/quick/dTzb2hfaP5")) }
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
                            minWidth = 75.0
                            action { controller.addFollowShortcut() }
                        }
                        button("Delete") {
                            minWidth = 75.0
                            action { controller.removeFollowShortcut(selectedFollowShortcut) }
                        }
                    }
                    field {
                        tableview(controller.model.followShortcuts) {
                            readonlyColumn("Shortcut", FollowShortcut::shortcutString) {
                                prefWidth = 250.0
                                isSortable = false
                                isResizable = false
                            }
                            selectionModel.selectedItemProperty().onChange {
                                selectedFollowShortcut = it
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
                            minWidth = 75.0
                            action { controller.addChannelPointsShortcut(rewardTitleProperty.value) }
                        }
                        button("Delete") {
                            minWidth = 75.0
                            action { controller.removeChannelPointsShortcut(selectedChannelPointsShortcut) }
                        }
                    }
                    field {
                        tableview(controller.model.channelPointsShortcuts) {
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
                            selectionModel.selectedItemProperty().onChange {
                                selectedChannelPointsShortcut = it
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
                            minWidth = 75.0
                            action { controller.addCheerShortcut(bitsProperty.value) }
                        }
                        button("Delete") {
                            minWidth = 75.0
                            action { controller.removeCheerShortcut(selectedCheerShortcut) }
                        }
                    }
                    field {
                        tableview(controller.model.cheerShortcuts) {
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
                            selectionModel.selectedItemProperty().onChange {
                                selectedCheerShortcut = it
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
                            minWidth = 75.0
                            action { controller.addSubscriptionShortcut(monthsProperty.value) }
                        }
                        button("Delete") {
                            minWidth = 75.0
                            action { controller.removeSubscriptionShortcut(selectedSubscriptionShortcut) }
                        }
                    }
                    field {
                        tableview(controller.model.subscriptionShortcuts) {
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
                            selectionModel.selectedItemProperty().onChange {
                                selectedSubscriptionShortcut = it
                            }
                        }
                    }
                }
            }
            // MARK: Gift Subscription Shortcuts
            form {
                fieldset("Gift Subscription Shortcuts", labelPosition = Orientation.VERTICAL) {
                    field("Count") {
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
                            minWidth = 75.0
                            action { controller.addGiftSubscriptionShortcut(countProperty.value) }
                        }
                        button("Delete") {
                            minWidth = 75.0
                            action { controller.removeGiftSubscriptionShortcut(selectedGiftSubscriptionShortcut) }
                        }
                    }
                    field {
                        tableview(controller.model.giftSubscriptionShortcuts) {
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
                            selectionModel.selectedItemProperty().onChange {
                                selectedGiftSubscriptionShortcut = it
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