Pellet4Android
==============

- Description:
-------------
This project allows to use the Pellet reasoning engine in Android devices. As the port cannot be directly made due to the differences between Pellet Java classes and Android's Dalvik platform, several classes have been rewritten and adapted. This is because of the dependences from, for example, Javax and Xerces packages, which cannot be compiled in Dalvik.

Besides, I have experienced several difficulties caused by the large amount of methods in the whole project, which triggers several dex errors in Android. I couldn't make it work in the Android Studio <http://developer.android.com/tools/studio/index.html> (even taking into account the Android ProGuard <http://developer.android.com/tools/help/proguard.html> and Android 5.0, which it seems to allow more than 65K methods). Anyway, this project has been developed with Eclipse IDE <https://eclipse.org/downloads/packages/eclipse-ide-java-developers/lunasr2> and it works.

Pellet4android has been compiled for Android 4.3, although I am pretty sure it will be easily compiled with any other Android version, since the main problem here was to compile Pellet classes and dependencies in Android.

- Configuring the project:
--------------------------
First, you can either generate a .jar file of the project or import it as an Android library (Project -> Properties -> Android -> isLibrary (checked) ).

The Pellet4Android class is the one that makes public several necessary methods to developers for using Pellet4Android. 

- Usage example:
----------------
Pellet4Android is very easy to use by following these simple steps:

1) First, declare several String files indicating your ontologies path in the device, namespaces, and ontologies names, as follows:

	private static final String ONTOLOGY_FILE = "test.owl";
	private static final String ONT_PATH = "/sdcard/ontologies/";
	private static final String DEFAULT_NAMESPACE = "http://www.yournamespace/";
	
2) Declare also a instance of the Pellet4Android class:

	private Pellet4Android reasoner;
	
3) Initialize Pellet4Android by using a default constructor:

	reasoner = new Pellet4Android(DEFAULT_NAMESPACE + ONTOLOGY_FILE + "#");
	
3) You can add your local ontologies by mapping them as follows (if you do not want to manage them online):

	reasoner.mapOntology("http://xmlns.com/foaf/0.1/", reasoner.getExternalDirectory("foaf.rdf"));
	reasoner.loadOntologyFromFile(ONT_PATH, ONTOLOGY_FILE);
	
4) And there you are! Now you have your ontologies loaded in your Android project. The set of available methods are in the class OntologyManager, which is accessible by:

	reasoner.getOntologyManager();


Every task is performed through the OntologyManager, which manages, configures and runs every operation that deals with the ontology. You can load the ontology (from file or from the Web), insert and delete classes, individuals, datatype and object properties, execute rules, etc. Everything you can do in Pellet is available in Pellet4Android (at least this is my intention :p).

Btw, the needed libraries are shown in the following figure: 
![libraries] (https://raw.github.com/edlectrico/Pellet4Android/master/res/drawable-hdpi/libraries.png?raw=true "Needed libraries")


Finally, I don't seek any profit from this project, and I hope I did not break any license by using the cited libraries. As shown in the figure, all the libraries have been legally downloaded.

These are, again, the imported and absolutely necessary libraries:
 - jetty.jar
 - jgrapht-jdk1.5.jar
 - owlapi-bin.jar
 - owlapi-distribution-3.4.5.jar
 - xsdlib.jar
 - androjena_0.5.jar
 - aterm-java-1.6.jar
 - icu4j-3.4.5.jar
 - iri-0.8.jar
 - servlet.jar
 - Pellet libraries: (pellet)-dig/cli/core/el/explanation/modularity/owlapiv3/pellint/query/rules.jar

 If you find any problem configuring the project don't hesitate to contact me and I'll try to help you.
 
 
 Enjoy!