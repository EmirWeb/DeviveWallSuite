# AMQP Services

Authors:

[Emir Hasanbegovic](https://github.com/xtreme-emir-hasanbegovic)

[Peter Iskandar](https://github.com/xtreme-peter-iskandar)


This project facilitates all the communication between devices and their server counterparts.


This service is meant to run alone on the device where you wish to connect to the AMQP server (and reconnect).

To point to your server, make sure you modify AmqpConstants.java inside DeviceWallModels to get this working for you.

Also you will need an AMQP server running on a machine to be able to connect and communicate properly.