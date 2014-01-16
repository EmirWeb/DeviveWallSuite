# Device Wall Prototype

Authors:

[Emir Hasanbegovic](https://github.com/xtreme-emir-hasanbegovic)

[Peter Iskandar](https://github.com/xtreme-peter-iskandar)

This application acts as the remote for the wall.

Use this application to start the AMQP Service on the device and send/receive commands that will launch the rest of the applications.

This also contains the counterpart to the DeviceWallIdentifier.java application. When each DeviceWallService connects to AMQP it contacts the DeviceWallIndentifier to get a unique ID. If you launch the Identifier application it will show you the id of the device on a white background. We take a picture of the devices in this state to be able take a picture and feed it into the Find Squares application which will figure out where each device is and what their ID is. This is the data that drives the Image application.