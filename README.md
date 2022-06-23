# ubmp4-speaker-android

## **What is this project about?**

This is a part of a larger project for the UBMP4.1 board. The project is to create a bluetooth music player.

## **How does this app work?**

This app works by first asking the user for bluetooth and location permissions (which are required for BLE). Then, when the scan button is pressed, it scans for the UBMP4 board. Once it attempts to connect and checks the connection state to see if it is safe to discover services. When the services are discovered it checks for read and write permissions before going to the second page of the activity. On the second page, there is a play button, which, when pressed, activates notifications. This causes a rapid onslaught of values to pour in, after which it sends the appropriate data to the board. There is a problem where the board doesn't respond fast enough to catch some of the sent values, so they may need to be resent (which is done automatically). There is also an issue where some data may be lost during transmission, so the board continuously sends the value until it receives a value in order to mitigate this.

After this, notes start to be processed and sent over bluetooth. The pitches of the notes are stored in an array and designated using enums. The pitches *must* be sent 1 byte at a time, as if it is not it may cause an overflow on the board. The rhythms and effects (timbre) are also sent during this time, and they are designated in a seperate array of bytes.

By this point you may be able to tell that the code is incomprehensibly messy, this is due to a variety of factors:

1. The bulk of the research came from [this site](https://punchthrough.com/android-ble-guide/) which happens to be in Kotlin, which I also happen to not know or have the time to learn currently.
2. Most of the android documentation is outdated.
3. I was very limited on time, as this application was created over the span of a week.

Due to those factors I was more focused on functionality rather than making it pretty. I do however plan to improve this application in the future.