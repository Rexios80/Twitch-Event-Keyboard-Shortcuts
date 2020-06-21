# Twitch Event Keyboard Shortcuts (TEKS)
Turn Twitch Events into keyboard shortcuts to interface with any program on your system. Make your Twitch interactions more meaningful!

### Supported Events
- Follows
- Chat commands (messages prepended by "!")
- Channel point redemptions
- Bits
- Subscriptions
- Gift subscriptions

### Features
- Fire keyboard shortcuts linked to Twitch Events
- Fire another keyboard shortcut after a specified amount of time (to undo the action of the original shortcut)
- Customizable cooldown period to prevent spam
- Test events locally to make sure your shortcuts behave as intended

### Prerequisites
- Java

### How to use
1. Download the latest release [here](https://github.com/Rexios80/Twitch-Event-Keyboard-Shortcuts/releases)
2. Open the program
3. Enter your channel name
4. Click on the "Get" button next to the "OAuth Token" field
5. After authenticating, enter the "ACCESS TOKEN" into the "OAuth Token" field
6. Click the start button

The application is now listening for events on your Twitch channel! Add as many shortcuts as you like, and create issues for any feedback you may have.

![Demo Image](https://github.com/Rexios80/Twitch-Event-Keyboard-Shortcuts/blob/master/demo.png)

### Useful Information
- On macOS you will need to go into your Security & Privacy settings both to launch the program, and to give Accessibility permission to perform the keyboard shortcuts.
- Shortcuts with a number value associated with them (bits, subs, gift subs) will fire if the event value is greater than the shortcut value and less than the next highest shortcut value.
- If a shortcut is marked with "Always Fire" or "AF", the shortcut will always fire if the given value is met or exceeded. This does not affect the firing of other shortcuts.

### Support Me
This program is free to use, but if you find it useful even a small donation to show your appreciation means a lot.
[StreamElements](https://streamelements.com/rexios85/tip)  
[Patreon](https://www.patreon.com/rexios)
