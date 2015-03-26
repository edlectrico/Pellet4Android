package edu.pellet4android;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import edu.pellet4android.ontmanager.OntologyManager;
import edu.pellet4android.ontmanager.exceptions.OntologyLoadException;

/**
 * 
 * @author edlectrico
 *
 * This class aims to be the entry point for developers
 * to request knowledge from the ontologies they use
 * to dynamically adapt their user interfaces of the
 * corresponding applications.
 *
 */
public class Pellet4Android {
	
	private static OntologyManager ontManager;
	private String namespace;
	
	public Pellet4Android(){
		ontManager = new OntologyManager();
	}
	
	/**
	 * Calling this method ensures a faster adaptation with a bit slower
	 * initialization. The views are managed in the onPostExecute method
	 * from the AsynTask
	 * 
	 * @param namespace
	 * @param views
	 */
	public Pellet4Android(final String namespace, final Collection<View> views){
		ontManager = new OntologyManager();
		
		this.namespace = namespace;
	}
	
	public void loadOntologyFromFile(final String path, final String ontologyFilename){
		new OntologyLoader().execute(path, ontologyFilename);
	}
	
	private class OntologyLoader extends AsyncTask<String, Void, String> {
		//params[0]: android path to the ontology; params[1]: ontology file name
		protected String doInBackground(String... params) {
			try {
				try {
					//if no mapping is performed before, it should be done as shown below:
					//ontManager.setMapping("http://xmlns.com/foaf/0.1/", getExternalDirectory("foaf.rdf"));
					
					File file = new File(params[0] + params[1]);
					FileInputStream fileInputStream = new FileInputStream(file);
					
					ontManager.loadOntologyFromFile(fileInputStream);				
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (OntologyLoadException e) {
				e.printStackTrace();
			} 
			return null;
		}

		protected void onPostExecute(String string) {
			//To avoid errors, it is recommended to use something here to
			//warn us that the ontology has been completely loaded. Otherwise
			//we may be requesting individuals of classes that haven't been
			//loaded yet. I recommend, for example, using a dialog, opening
			//it when the loading process starts, and closing it in this
			//method.

			// ontologyClassA = ontManager.getIndividualOfClass(namespace + clazz.getClass().getSimpleName());
		}
	}
	
	public String getExternalDirectory(final String file){
		return "file:" + Environment.getExternalStorageDirectory().getPath() + "/ontologies/" + file;
	}
	
	public OntologyManager getOntManager(){
		return ontManager;
	}
	
	/**
	 * Alternatively, we can use this method to map other ontologies used
	 * in the main ontology (e.g., swrla.owl, sqwrl.owl, foaf.owl and so forth).
	 * This allows the application to not need Internet to check the namespaces,
	 * being the process executed in local.
	 * 
	 * @param namespace
	 * @param fileLocation
	 */
	public void mapOntology(final String namespace, final String fileLocation){
		ontManager.setMapping(namespace, fileLocation);
	}
	
	/**
	 * 
	 * @param namespace
	 * @param clazz
	 * @return
	 */
	public List<String> getIndividualOfClass(final String namespace, final String clazz){
		return ontManager.getIndividualOfClass(namespace + clazz);
	}
}
