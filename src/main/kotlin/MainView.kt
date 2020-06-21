import javafx.beans.binding.When
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
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
import javafx.scene.text.Font
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class TeksApp : App(MainView::class)

class MainView : View() {
    private val controller = MainController()
    private var selectedShortcutProperty = SimpleObjectProperty<MetaShortcut>()

    override val root = vbox {
        style {
            padding = box(20.px)
        }

        // Unfocus textfield on startup
        runLater { requestFocus() }

        hbox {
            form {
                minWidth = 350.0
                disableProperty().bind(When(controller.startedProperty).then(true).otherwise(false))
                fieldset("Authentication", labelPosition = Orientation.VERTICAL) {
                    field("Channel Name") {
                        textfield().bind(controller.channelNameProperty)
                    }
                    field("OAuth Token") {
                        textfield().bind(controller.oauthTokenProperty)
                        button("Get") {
                            action {
                                Desktop.getDesktop().browse(URI.create("https://twitchtokengenerator.com/quick/jkwvSpx2rv"))
                            }
                        }
                    }
                }
            }
            hbox(alignment = Pos.CENTER) {
                paddingLeft = 50.0
                button("Start") {
                    disableProperty().bind(When(controller.startedProperty).then(true).otherwise(false))
                    setPrefSize(200.0, 100.0)
                    font = Font.font(30.0)
                    action { controller.start() }
                }
                label {
                    paddingTop = 10.0
                    bind(controller.errorTextProperty)
                }
            }
            spacer()
            add(ShortcutCreation(controller))
            spacer()
            // MARK: Test events
            form {
                disableProperty().bind(When(controller.startedProperty).then(false).otherwise(true))
                fieldset("Test Events", labelPosition = Orientation.VERTICAL) {
                    val selectedEventType = SimpleObjectProperty<EventType>(EventType.follow)
                    val valueProperty = SimpleStringProperty("")
                    combobox(selectedEventType, EventType.values().toList())
                    field(EventType.follow.fieldText) {
                        textfield().bind(valueProperty)
                        isDisable = true
                        selectedEventType.onChange {
                            text = it?.fieldText
                            isDisable = it == EventType.follow
                        }
                    }
                    button("Send Test Event") {
                        action {
                            controller.sendTestEvent(selectedEventType.value, valueProperty.value)
                        }
                    }
                }
            }
        }
        vbox {
            hbox {
                add(ShortcutsView(FollowShortcut::class.java, controller, "Follow Shortcuts", selection = selectedShortcutProperty))
                add(ShortcutsView(ChannelPointsShortcut::class.java, controller, "Channel Points Shortcuts", true, "Title", selectedShortcutProperty))
                add(ShortcutsView(BitsShortcut::class.java, controller, "Bits Shortcuts", true, "Bits", selectedShortcutProperty))
            }
            hbox {
                add(ShortcutsView(SubscriptionShortcut::class.java, controller, "Subscription Shortcuts", true, "Months", selectedShortcutProperty))
                add(ShortcutsView(GiftSubscriptionShortcut::class.java, controller, "Gift Subscription Shortcuts", true, "Count", selectedShortcutProperty))
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
            button("Delete Selection") {
                action { controller.removeShortcut(selectedShortcutProperty.value) }
            }
        }
    }

    class ShortcutCreation(controller: MainController) : Fragment() {
        val valueProperty = SimpleStringProperty("")
        val shortcutOnEventString = SimpleStringProperty("")
        val waitTimeProperty = SimpleStringProperty("")
        val shortcutAfterWaitString = SimpleStringProperty("")
        val alwaysFireProperty = SimpleBooleanProperty(false)
        val cooldownProperty = SimpleStringProperty("")

        val shortcutOnEvent = Shortcut(mutableListOf(), null)
        val shortcutAfterWait = Shortcut(mutableListOf(), null)

        val selectedEventType = SimpleObjectProperty<EventType>(EventType.follow)

        override val root = form {
            fieldset("Add Shortcuts", labelPosition = Orientation.VERTICAL) {
                hbox(alignment = Pos.BOTTOM_LEFT) {
                    hbox(alignment = Pos.BOTTOM_LEFT) {
                        combobox(selectedEventType, EventType.values().toList())
                        paddingBottom = 5.0
                    }
                    add(betterSpacer(20.0))
                    field(EventType.follow.fieldText) {
                        isDisable = true
                        textfield {
                            prefWidth = 140.0
                            bind(valueProperty)
                        }
                        selectedEventType.onChange {
                            isDisable = it == EventType.follow
                            text = it?.fieldText
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
                        isDisable = true// = hasValue && clazz != ChannelPointsShortcut::class.java
                        paddingBottom = 10
                        bind(alwaysFireProperty)
                        selectedEventType.onChange {
                            isDisable = it == EventType.follow || it == EventType.channelPoints
                        }
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
                    field("Cooldown (Milliseconds)") {
                        textfield {
                            bind(cooldownProperty)
                            prefWidth = 140.0
                        }

                    }
                    add(betterSpacer(20.0))
                    hbox(alignment = Pos.BOTTOM_LEFT) {
                        paddingBottom = 6.0
                        button("Add") {
                            action {
                                val shortcutClass = when (selectedEventType.value) {
                                    EventType.follow -> FollowShortcut::class.java
                                    EventType.channelPoints -> ChannelPointsShortcut::class.java
                                    EventType.bits -> BitsShortcut::class.java
                                    EventType.subscription -> SubscriptionShortcut::class.java
                                    EventType.giftSubscription -> GiftSubscriptionShortcut::class.java
                                    else -> MetaShortcut::class.java
                                }
                                controller.addShortcut(shortcutClass, valueProperty.value, shortcutOnEvent.copy(), waitTimeProperty.value.toLongOrNull(), shortcutAfterWait.copy(), alwaysFireProperty.value, cooldownProperty.value.toLongOrNull())
                            }
                        }
                    }
                }
            }
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

    class ShortcutsView<T : MetaShortcut>(clazz: Class<T>, controller: MainController, title: String, hasValue: Boolean = false, valueLabel: String? = null, selection: SimpleObjectProperty<MetaShortcut>) : Fragment() {
        override val root = form {
            fieldset(title) {
                field {
                    val items = controller.getShortcutsList(clazz) as ObservableList<MetaShortcut>
                    tableview(items) {
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
                        readonlyColumn("Cooldown", MetaShortcut::cooldownString) {
                            isSortable = false
                            isResizable = false
                        }
                        selectionModel.selectedItemProperty().onChange {
                            if (it != null) {
                                selection.value = it
                            }
                        }
                        selection.onChange {
                            if (it != null && !items.contains(it)) {
                                selectionModel.clearSelection()
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