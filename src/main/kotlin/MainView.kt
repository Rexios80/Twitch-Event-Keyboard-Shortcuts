import javafx.beans.binding.When
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
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
        }
        vbox {
            hbox {
                add(ShortcutsView(FollowShortcut::class.java, controller, "Follow Shortcuts"))
                add(ShortcutsView(ChannelPointsShortcut::class.java, controller, "Channel Points Shortcuts", true, "Title"))
                add(ShortcutsView(BitsShortcut::class.java, controller, "Bits Shortcuts", true, "Bits"))
            }
            hbox {
                add(ShortcutsView(SubscriptionShortcut::class.java, controller, "Subscription Shortcuts", true, "Months"))
                add(ShortcutsView(GiftSubscriptionShortcut::class.java, controller, "Gift Subscription Shortcuts", true, "Count"))
                form {
                    fieldset("Event Console") {
                        tableview(controller.eventConsole.events) {
                            prefWidth = 480.0
                            prefHeight = 320.0
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
        }
    }

    class ShortcutsView<T : MetaShortcut>(clazz: Class<T>, controller: MainController, title: String, hasValue: Boolean = false, valueLabel: String? = null) : Fragment() {
        val valueProperty = SimpleStringProperty("")
        val shortcutOnEventString = SimpleStringProperty("")
        val waitTimeProperty = SimpleStringProperty("")
        val shortcutAfterWaitString = SimpleStringProperty("")
        val alwaysFireProperty = SimpleBooleanProperty(false)

        val shortcutOnEvent = Shortcut(mutableListOf(), null)
        val shortcutAfterWait = Shortcut(mutableListOf(), null)

        var selectedShortcut: MetaShortcut? = null

        override val root = form {
            fieldset(title, labelPosition = Orientation.VERTICAL) {
                hbox(alignment = Pos.BOTTOM_LEFT) {
                    field(valueLabel ?: "spacer") {
                        isVisible = hasValue
                        textfield {
                            prefWidth = 140.0
                            bind(valueProperty)
                        }
                    }
                    add(betterSpacer(20.0))
                    field("Shortcut On Event") {
                        textfield {
                            prefWidth = 140.0
                            bind(shortcutOnEventString)
                            isEditable = false
                            addEventHandler(KeyEvent.KEY_PRESSED) { handleKeyPress(it, shortcutOnEvent, shortcutOnEventString) }
                            addEventHandler(KeyEvent.KEY_RELEASED) { handleKeyPress(it, shortcutOnEvent, shortcutOnEventString) }
                        }
                    }
                    add(betterSpacer(20.0))
                    checkbox("Always fire") {
                        isVisible = hasValue && clazz != ChannelPointsShortcut::class.java
                        paddingBottom = 10
                        bind(alwaysFireProperty)
                    }
                }
                hbox {
                    field("Wait Time (Milliseconds)") {
                        textfield {
                            prefWidth = 140.0
                            bind(waitTimeProperty)
                        }
                    }
                    add(betterSpacer(20.0))
                    field("Shortcut After Wait") {
                        textfield {
                            prefWidth = 140.0
                            bind(shortcutAfterWaitString)
                            isEditable = false
                            addEventHandler(KeyEvent.KEY_PRESSED) { handleKeyPress(it, shortcutAfterWait, shortcutAfterWaitString) }
                            addEventHandler(KeyEvent.KEY_RELEASED) { handleKeyPress(it, shortcutAfterWait, shortcutAfterWaitString) }
                        }
                    }
                    add(betterSpacer(20.0))
                    hbox(alignment = Pos.BOTTOM_LEFT) {
                        paddingBottom = 5
                        button("Add") {
                            minWidth = 75.0
                            action { controller.addShortcut(clazz, valueProperty.value, shortcutOnEvent.copy(), waitTimeProperty.value.toLongOrNull(), shortcutAfterWait.copy(), alwaysFireProperty.value) }
                        }
                        add(betterSpacer(10.0))
                        button("Delete") {
                            minWidth = 75.0
                            action { controller.removeShortcut(selectedShortcut) }
                        }
                    }
                }
                field {
                    tableview(controller.getShortcutsList(clazz) as ObservableList<MetaShortcut>) {
                        prefHeight = 200.0
                        if (hasValue) {
                            readonlyColumn(valueLabel ?: "", MetaShortcut::valueString) {
                                prefWidth = 75.0
                                isSortable = false
                                isResizable = false
                            }
                            if (clazz != ChannelPointsShortcut::class.java) {
                                readonlyColumn("AF", MetaShortcut::alwaysFireString) {
                                    prefWidth = 30.0
                                    isSortable = false
                                    isResizable = false
                                }
                            }
                        }
                        readonlyColumn("Shortcut On Event", MetaShortcut::shortcutOnEventString) {
                            prefWidth = 140.0
                            isSortable = false
                            isResizable = false
                        }
                        readonlyColumn("Wait Time", MetaShortcut::waitTimeString) {
                            isSortable = false
                            isResizable = false
                        }
                        readonlyColumn("Shortcut After Wait", MetaShortcut::shortcutAfterWaitString) {
                            prefWidth = 140.0
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

        private fun betterSpacer(width: Double? = null, height: Double? = null): Node {
            val spacer = Region()

            if (width == null && height == null) {
                // Make it always grow or shrink according to the available space
                VBox.setVgrow(spacer, Priority.ALWAYS)
                HBox.setHgrow(spacer, Priority.ALWAYS)
            } else {
                spacer.prefWidth = width ?: 0.0
                spacer.prefHeight = height ?: 0.0
            }
            return spacer
        }

        private fun handleKeyPress(event: KeyEvent, shortcut: Shortcut, property: SimpleStringProperty) {
            if (event.eventType == KeyEvent.KEY_PRESSED) {
                if (shortcut.key != null) {
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