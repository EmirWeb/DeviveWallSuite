# Device Wall Models

Authors:

[Emir Hasanbegovic](https://github.com/xtreme-emir-hasanbegovic)

[Peter Iskandar](https://github.com/xtreme-peter-iskandar)

All the constants and shared models reside within this project.

AMQPConstants.java
Protocol.java
Data.java

These are the files you will need to get comfortable with before adding a project to the device wall.

Each program has its own set of resquests responses and data instances.

Take a look at 

Deserializer and json handlers
com.xtremelabs.devicewall.protocol.identifier 

The data models that are passed between applications 
com.xtremelabs.devicewall.protocol.identifier.data

The Requests objects that are sent by the clients or server clients and received by the server clients
com.xtremelabs.devicewall.protocol.identifier.request

The Response objects that are sent by the server clients and received by the clients
com.xtremelabs.devicewall.protocol.identifier.response