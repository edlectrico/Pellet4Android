package edu.pellet4android;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLLiteral;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
	private Collection<View> views;
	
	@SuppressWarnings("unused")
	private List<String> buttons, editTexts, textViews;
	
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
		this.views = views;
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
			for (View v : views){
				if (v instanceof Button){
					buttons = ontManager.getIndividualOfClass(namespace + v.getClass().getSimpleName());
				} else if (v instanceof EditText){
					editTexts = ontManager.getIndividualOfClass(namespace + v.getClass().getSimpleName());
				} else if (v instanceof TextView){
					textViews = ontManager.getIndividualOfClass(namespace + v.getClass().getSimpleName());
				}
			}
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
	
	/**
	 * This method requires a previous initialization of the
	 * required views. To do so, the AdaptUI(final String namespace, 
	 * final Collection<View> views) method is given. In the calling
	 * activity the views should be added like this: views.add(button);
	 */
	public Map<String, Integer> adaptLoadedViews(){
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (View v : views){
			System.out.println(v.getClass().getSimpleName());
			map.put("viewBackgroundColor", adaptViewBackgroundColor(this.namespace, v.getClass().getSimpleName()));
			map.put("viewTextColor", adaptViewTextColor(this.namespace, v.getClass().getSimpleName()));
			map.put("viewTextSize", adaptViewTextSize(this.namespace, v.getClass().getSimpleName()));
		}
		
		return map;
	}
	
	public int adaptViewBackgroundColor(final String namespace, final String className) {
		List<String> genericViews = ontManager.getIndividualOfClass(namespace + className);
		
		final Collection<OWLLiteral> backgroundColor = ontManager.getDataTypePropertyValue(genericViews.get(0), namespace + "viewHasColor");
		final int color = Integer.parseInt(((OWLLiteral) backgroundColor.toArray()[0]).getLiteral());
		
		//convert color to RGB
		final int red = Color.red(color);
		final int green = Color.green(color);
		final int blue = Color.blue(color);
		
		return Color.argb(255, red, green, blue);
	}
	
	public int adaptViewTextColor(final String namespace, final String className){
		List<String> genericViews = ontManager.getIndividualOfClass(namespace + className);
		
		final Collection<OWLLiteral> textColor = ontManager.getDataTypePropertyValue(genericViews.get(0), namespace + "viewHasTextColor");
		final int viewTextColor = Integer.parseInt(((OWLLiteral) textColor.toArray()[0]).getLiteral());
		
		//convert color to RGB
		final int red = Color.red(viewTextColor);
		final int green = Color.green(viewTextColor);
		final int blue = Color.blue(viewTextColor);
		
		return Color.argb(255, red, green, blue);
	}
	
	public int adaptViewTextSize(final String namespace, final String className){
		List<String> genericViews = ontManager.getIndividualOfClass(namespace + className);
		
		final Collection<OWLLiteral> textSize = ontManager.getDataTypePropertyValue(genericViews.get(0), namespace + "viewHasTextSize");
		final float viewTextSize = Float.parseFloat(((OWLLiteral) textSize.toArray()[0]).getLiteral());
		
		return (int)viewTextSize;
	}
}
