Pellet4Android
==============

- Description:
-------------
This project is a Pellet port for running in Android devices. As the port cannot be directly made due to the differences between Pellet Java classes and Android's Dalvik platform, several classes have been rewritten and adapted. This is because of the dependences from, for example, Javax and Xerces packages, which cannot be compiled in Dalvik.

The project has been compiled for Android 4.3, although I am pretty sure it will be easily compiled with any other Android version, since the main problem here was to compile Pellet classes and dependencies in Android.

- Instructions:
---------------
The AdaptUI class is the one that makes public several necessary methods to developers for using Pellet4Android. The class is named AdaptUI due to a personal project, so it is
not very important how to call it (perhaps it should be call Main or something like that... it's up to you). 

If you want to download an example of how Pellet4Android is used, check out this project: https://github.com/edlectrico/adaptui_demo
The AdaptUIDemo Android project should import Pellet4Android as an Android library. Once it's configured in that way, it should work. Please notice that the ontologies I used are physically stored in the /sdcard/ontologies/ folder in my device, and I am using a test.owl main ontology. Check this in your device (use your own ontologies) before running this project, or it won't work.

Every task is performed through a class called OntologyManager, which manages, configures and runs every operation that deals with the ontology. You can load the ontology (from file or from the Web), insert and delete classes, individuals, datatype and object properties, execute rules, etc. Everything you can do in Pellet is available in Pellet4Android (at least this is my intention :p).

Btw, the needed libraries are shown in the following figure: 
![libraries] (https://github.com/edlectrico/Pellet4Android/tree/master/res/drawable-hdpi/libraries.png "Needed libraries")

