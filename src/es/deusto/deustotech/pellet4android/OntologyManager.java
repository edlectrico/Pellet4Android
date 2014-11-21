package es.deusto.deustotech.pellet4android;

/*******************************************************************************      
 *      Licensed to the Apache Software Foundation (ASF) under one
 *      or more contributor license agreements.  See the NOTICE file
 *      distributed with this work for additional information
 *      regarding copyright ownership.  The ASF licenses this file
 *      to you under the Apache License, Version 2.0 (the
 *      "License"); you may not use this file except in compliance
 *      with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing,
 *      software distributed under the License is distributed on an
 *      "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *      KIND, either express or implied.  See the License for the
 *      specific language governing permissions and limitations
 *      under the License.
 *******************************************************************************/

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplNoCompression;
import android.util.Log;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import es.deusto.deustotech.pellet4android.exceptions.OntologyCreationException;
import es.deusto.deustotech.pellet4android.exceptions.OntologyLoadException;
import es.deusto.deustotech.pellet4android.exceptions.OntologySavingException;

public class OntologyManager {

	private transient OWLOntology ontology = null;
	private transient OWLReasoner reasoner = null;
	private final transient OWLOntologyManager manager = OWLManager
			.createOWLOntologyManager();

	public OntologyManager() {
	}

	/**
	 * Load an ontology from an URI
	 * 
	 * @param ontUri
	 *            the uri of the ontology
	 */
	public void loadOntology(final String ontUri) throws OntologyLoadException {
		// To avoid this solution
		// http://clarkparsia.com/pellet/faq/owlapi-sparql/ ,
		// I have decided to try SPARQL-DL API
		// http://www.derivo.de/en/resources/reasoner/sparql-dl-api/
		try {
			ontology = getManager().loadOntology(IRI.create(ontUri));
		} catch (OWLOntologyCreationException e) {
			throw new OntologyLoadException(e.getMessage());
		}

		manager.setOntologyDocumentIRI(ontology, IRI.create(ontUri));
		createReasonerAndEngine();

	}
	
	public void removeOntology(){
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>(manager.getOntologies());
		
		for (OWLOntology ont : ontologies){
			manager.removeOntology(ont);
		}
	}

	private OWLReasoner processReasonerProperties() {
		OWLReasoner reasoner;
		final OWLReasonerFactory reasonerFactory = PelletReasonerFactory
				.getInstance();
		reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		return reasoner;
	}

	/**
	 * Initialize an SPARQL-DL engine
	 */
	private void createReasonerAndEngine() {
		setReasoner(processReasonerProperties());
	}

	/**
	 * Load an ontology from a file
	 * 
	 * @param filePath
	 *            file path
	 * 
	 */
	public void loadOntologyFromFile(final InputStream filePath)
			throws OntologyLoadException {

		try {
			ontology = getManager().loadOntologyFromOntologyDocument(filePath);
		} catch (OWLOntologyCreationException e) {
			throw new OntologyLoadException(e.getMessage());
		}

		createReasonerAndEngine();
	}

	/**
	 * Get the value of a datatype property.
	 * 
	 * @param individual
	 *            individual signature
	 * @param property
	 *            property signature
	 * @return values
	 */
	public Collection<OWLLiteral> getDataTypePropertyValue(
			final String individual, final String property) {
		final List<OWLLiteral> dataPropertyValues = new ArrayList<OWLLiteral>();
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		if (ontology
				.containsIndividualInSignature(IRI.create(individual), true)
				&& ontology.containsDataPropertyInSignature(
						IRI.create(property), true)) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			OWLDataProperty op = factory.getOWLDataProperty(IRI
					.create(property));
			dataPropertyValues.addAll(getReasoner().getDataPropertyValues(ind,
					op));
		}
		return dataPropertyValues;
	}
	
	public void saveOntologyAs(final String file) throws OntologySavingException {
        try {
            manager.saveOntology(getOntology(), new FileOutputStream(file));
        } catch (OWLOntologyStorageException e) {
            Log.e("Error saving ontology in " + file, e.getMessage());
            throw new OntologySavingException(e.getMessage(), e.getCause());
        } catch (FileNotFoundException e) {
        	Log.e("Error saving ontology in " + file, e.getMessage());
            throw new OntologySavingException(e.getMessage(), e.getCause());
        }
    }

	/**
	 * @return the ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * It sets an ontology as the default ontology and initialize the reasoner
	 * and the SPARQL-DL engine.
	 * 
	 * @param ont
	 *            ontology
	 */
	public void setOntology(OWLOntology ont) {
		ontology = ont;
		createReasonerAndEngine();
	}

	/**
	 * Get the value of a datatype for an individual
	 * 
	 * @param individual
	 *            individual
	 * @param property
	 *            property signature
	 * @return datatype value
	 */
	public List<String> getPropertyValue(final String individual,
			final String property) {
		List<String> result = new ArrayList<String>();
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();

		boolean isObjProp = ontology.containsObjectPropertyInSignature(
				IRI.create(property), true);
		boolean isDatatype = ontology.containsDataPropertyInSignature(
				IRI.create(property), true);
		boolean individualExists = ontology.containsIndividualInSignature(
				IRI.create(individual), true);
		if (isObjProp && individualExists) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			OWLObjectProperty obp = factory.getOWLObjectProperty(IRI
					.create(property));
			NodeSet<OWLNamedIndividual> values = getReasoner()
					.getObjectPropertyValues(ind, obp);
			List<OWLNamedIndividual> nodes = new ArrayList<OWLNamedIndividual>(
					values.getFlattened());
			for (OWLNamedIndividual n : nodes) {
				result.add(n.getIRI().toURI().toString());
			}
		} else if (isDatatype && individualExists) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			OWLDataProperty op = factory.getOWLDataProperty(IRI
					.create(property));
			Set<OWLLiteral> values = getReasoner().getDataPropertyValues(ind,
					op);
			for (OWLLiteral l : values) {
				result.add(l.getLiteral());
			}
		}
		return result;
	}

	public Set<OWLAnnotationAssertionAxiom> getAnnotationValue(
			String turambarOntologyAnnotation) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		final OWLAnnotationProperty dcProperty = factory
				.getOWLAnnotationProperty(IRI
						.create(turambarOntologyAnnotation));
		final Set<OWLAxiom> axioms = dcProperty.getReferencingAxioms(
				getOntology(), true);
		final Set<OWLAnnotationAssertionAxiom> values = new HashSet<OWLAnnotationAssertionAxiom>();
		for (OWLAxiom a : axioms) {
			if (a.getAxiomType().equals(AxiomType.ANNOTATION_ASSERTION)) {
				final OWLAnnotationAssertionAxiom annotationAssertion = (OWLAnnotationAssertionAxiom) a;

				values.add(annotationAssertion);
			}
		}
		return values;
	}

	/**
	 * @return the reasoner
	 */
	public OWLReasoner getReasoner() {
		return reasoner;
	}

	/**
	 * @param re
	 *            the reasoner to set
	 */
	void setReasoner(final OWLReasoner re) {
		this.reasoner = re;

	}

	/**
	 * Get the value of an object property
	 * 
	 * @param subj
	 *            individual
	 * @param property
	 *            property signature
	 * @return object property value
	 */

	public List<OWLNamedIndividual> getObjectProperty(final String subj,
			final String property) {

		final List<OWLNamedIndividual> result = new ArrayList<OWLNamedIndividual>();

		final OWLDataFactory factory = this.ontology.getOWLOntologyManager()
				.getOWLDataFactory();

		final OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(subj));

		final OWLObjectProperty obp = factory.getOWLObjectProperty(IRI
				.create(property));

		final NodeSet<OWLNamedIndividual> values = getReasoner()
				.getObjectPropertyValues(ind, obp);

		final List<OWLNamedIndividual> nodes = new ArrayList<OWLNamedIndividual>(
				values.getFlattened());

		for (OWLNamedIndividual n : nodes) {
			result.add(n);
		}
		return result;
	}

	/**
	 * Create an individual of a given type
	 * 
	 * @param individual
	 *            the individual signature
	 * @param type
	 *            individual type
	 */

	public void createIndividual(final String individual, final String type) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();

		OWLClass clazz = factory.getOWLClass(IRI.create(type));

		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(
				clazz, ind);
		final List<OWLOntologyChange> owlOntologyChanges = ontology
				.getOWLOntologyManager().addAxiom(ontology, assertion);
		manager.applyChanges(owlOntologyChanges);
		reasoner.flush();
	}

	/**
	 * Add a new class membership to an individual
	 * 
	 * @param individual
	 *            the individual signature
	 * @param type
	 *            individual type
	 */

	public void addIndividualMembership(final String individual,
			final String type) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();

		OWLClass clazz = factory.getOWLClass(IRI.create(type));

		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(
				clazz, ind);
		final List<OWLOntologyChange> owlOntologyChanges = ontology
				.getOWLOntologyManager().addAxiom(ontology, assertion);
		manager.applyChanges(owlOntologyChanges);
		reasoner.flush();
	}

	/**
	 * Remove class membership from an individual
	 * 
	 * @param individual
	 *            the individual signature
	 * @param type
	 *            individual type
	 */
	//
	public void removeIndividualMembership(final String individual,
			final String type) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();

		OWLClass clazz = factory.getOWLClass(IRI.create(type));

		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(
				clazz, ind);
		final List<OWLOntologyChange> owlOntologyChanges = ontology
				.getOWLOntologyManager().removeAxiom(ontology, assertion);
		manager.applyChanges(owlOntologyChanges);
		reasoner.flush();
	}

	/**
	 * This method adds a object property to an OWL individual. If the property
	 * does not exist, it is created. If the individual does not exist, it is
	 * created. If the other individual (individual represented by value) does
	 * not exist, it is created. If the object property is functional, then it
	 * is created.
	 * 
	 * @param individual
	 *            individual
	 * @param property
	 *            property
	 * @param value
	 *            the other individual
	 */

	public void addObjectPropertyValue(String individual, String property,
			String value) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI
				.create(value));
		// removeObjectPropertyValuesOfFunctionalProperties(ind,
		// objectProperty);
		if (objectProperty.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);
		final OWLAxiom axiom = addObjectProperty(ind, property, value);

		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();
	}

	/**
	 * Add an object property value to an individual
	 * 
	 * @param ind
	 *            individual signature
	 * @param property
	 *            property signature
	 * @param value
	 *            value
	 * @return created OWLAxiom
	 */
	private OWLAxiom addObjectProperty(final OWLNamedIndividual ind,
			final String property, final String value) {
		assert ontology.containsObjectPropertyInSignature(IRI.create(property),
				true);
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLObjectProperty objProp = factory.getOWLObjectProperty(IRI
				.create(property));
		OWLNamedIndividual objPropIn = factory.getOWLNamedIndividual(IRI
				.create(value));

		if (objProp.isFunctional(ontology.getOWLOntologyManager()
				.getOntologies())) {

			NodeSet<OWLNamedIndividual> values = reasoner
					.getObjectPropertyValues(ind, objProp);
			List<Node<OWLNamedIndividual>> list = new ArrayList<Node<OWLNamedIndividual>>(
					values.getNodes());
			if (list.size() == 1) {

				Node<OWLNamedIndividual> node = list.get(0);
				OWLObjectPropertyAssertionAxiom toRemove = factory
						.getOWLObjectPropertyAssertionAxiom(objProp, ind,
								node.getRepresentativeElement());

				RemoveAxiom rAxiom = new RemoveAxiom(ontology, toRemove);
				ontology.getOWLOntologyManager().applyChange(rAxiom);
				reasoner.flush();
			}
		}

		return factory.getOWLObjectPropertyAssertionAxiom(objProp, ind,
				objPropIn);
	}

	/**
	 * Set the following assertion DatatypePropertyAssertion( property
	 * individual value ) If the properties or/and individual do not exist, they
	 * will be created If the property is functional, previous values are
	 * deleted.
	 * 
	 * @param individual
	 *            owl named individual
	 * @param property
	 *            datatype property IRI
	 * @param value
	 *            datatype values as integer
	 */

	public void addDataTypePropertyValue(String individual, String property,
			int value) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLDataPropertyAssertionAxiom axiom = null;
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
				.create(property));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		if (dataProp.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);
		axiom = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, value);
		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();

	}

	/**
	 * Set the following assertion DatatypePropertyAssertion( property
	 * individual value ) If the properties or/and individual do not exist, they
	 * will be created If the property is functional, previous values are
	 * deleted.
	 * 
	 * @param individual
	 *            owl named individual
	 * @param property
	 *            datatype property IRI
	 * @param value
	 *            datatype values
	 */

	public void addDataTypePropertyValue(String individual, String property,
			float value) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLDataPropertyAssertionAxiom axiom = null;
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
				.create(property));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		if (dataProp.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);
		axiom = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, value);
		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();

	}

	/**
	 * Set the following assertion DatatypePropertyAssertion( property
	 * individual value ) If the properties or/and individual do not exist, they
	 * will be created If the property is functional, previous values are
	 * deleted.
	 * 
	 * @param individual
	 *            owl named individual
	 * @param property
	 *            datatype property IRI
	 * @param value
	 *            datatype values
	 */

	public void addDataTypePropertyValue(String individual, String property,
			boolean value) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLDataPropertyAssertionAxiom axiom = null;
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
				.create(property));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		if (dataProp.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);

		axiom = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, value);
		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();

	}

	/**
	 * Set the following assertion DatatypePropertyAssertion( property
	 * individual value ) If the properties or/and individual do not exist, they
	 * will be created If the property is functional, previous values are
	 * deleted.
	 * 
	 * @param individual
	 *            owl named individual
	 * @param property
	 *            datatype property IRI
	 * @param value
	 *            datatype values
	 */

	public void addDataTypePropertyValue(String individual, String property,
			double value) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLDataPropertyAssertionAxiom axiom = null;
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
				.create(property));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		if (dataProp.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);

		axiom = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, value);
		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();

	}

	/**
	 * Set the following assertion DatatypePropertyAssertion( property
	 * individual value ) If the properties or/and individual do not exist, they
	 * will be created If the property is functional, previous values are
	 * deleted.
	 * 
	 * @param individual
	 *            owl named individual
	 * @param property
	 *            datatype property IRI
	 * @param value
	 *            datatype values
	 */

	public void addDataTypePropertyValue(String individual, String property,
			String value) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLDataPropertyAssertionAxiom axiom = null;
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
				.create(property));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		if (dataProp.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);

		axiom = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, value);
		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();

	}

	/**
	 * Set the following assertion DatatypePropertyAssertion( property
	 * individual value ) If the properties or/and individual do not exist, they
	 * will be created If the property is functional, previous values are
	 * deleted.
	 * 
	 * @param individual
	 *            owl named individual
	 * @param property
	 *            datatype property IRI
	 * @param value
	 *            datatype values
	 */

	public void addDataTypePropertyValue(String individual, String property,
			String value, String type) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		OWLDataPropertyAssertionAxiom axiom = null;
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
				.create(property));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		if (dataProp.isFunctional(ontology))
			deleteAllValuesOfProperty(individual, property);

//		OWLLiteral literal = new OWLLiteralImpl(factory, value,
//				factory.getOWLDatatype(IRI.create(type)));
		
		OWLLiteral literal  = new OWLLiteralImplNoCompression(value, "", factory.getOWLDatatype(IRI.create(type)));
		
		axiom = factory
				.getOWLDataPropertyAssertionAxiom(dataProp, ind, literal);
		ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
		reasoner.flush();

	}

	/**
	 * This method sets an alternative URI for an ontology.
	 * 
	 * @param ontURl
	 *            original URI
	 * @param ontFile
	 *            alternative URI
	 */

	public void setMapping(final String ontURl, final String ontFile) {
		IRI ontologyUri = IRI.create(ontURl);
		IRI ontologyMapped = IRI.create(ontFile);

		OWLOntologyIRIMapper iriMapper = new SimpleIRIMapper(ontologyUri,
				ontologyMapped);
		getManager().addIRIMapper(iriMapper);
	}

	/**
	 * Given an individual returns its classes
	 * 
	 * @param i
	 *            individual signature
	 * @return classes which is member
	 */

	public Set<String> getIndividualClasses(String i) {
		Set<String> response = new HashSet<String>();
		OWLDataFactory factory = getManager().getOWLDataFactory();
		if (ontology.containsIndividualInSignature(IRI.create(i), true)) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(i));

			NodeSet<OWLClass> nodes = reasoner.getTypes(ind, false);

			Set<OWLClass> types = nodes.getFlattened();

			for (OWLClass c : types) {
				response.add(c.getIRI().toString());
			}
		}

		return response;
	}

	public void createOntology(String url) throws OntologyCreationException,
			OWLOntologyCreationException {
		if (ontology == null) {
			ontology = manager.createOntology(IRI.create(url));
			createReasonerAndEngine();
		}
	}

	/**
	 * Check if a property is functional. This method does not check if the
	 * property is a valid property.
	 * 
	 * @param property
	 *            the property
	 * @return if it is a functional property, it returns true. Otherwise it
	 *         returns false.
	 */

	public boolean isFunctionalProperty(final String property) {
		boolean isObjProp = ontology.containsObjectPropertyInSignature(
				IRI.create(property), true);
		boolean isDatatype = ontology.containsDataPropertyInSignature(
				IRI.create(property), true);

		assert (isObjProp || isDatatype);
		boolean result;
		if (isObjProp) {
			final OWLObjectProperty owlObjectProperty = ontology
					.getOWLOntologyManager().getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(property));
			result = owlObjectProperty.isFunctional(manager.getOntologies());
		} else {
			final OWLDataProperty owlDataProperty = ontology
					.getOWLOntologyManager().getOWLDataFactory()
					.getOWLDataProperty(IRI.create(property));
			result = owlDataProperty.isFunctional(manager.getOntologies());
		}
		return result;
	}

	/**
	 * Returns individuals of a class.
	 * 
	 * @param clazz
	 *            the class
	 * @return members of class setConsistencyChecking
	 */

	public List<String> getIndividualOfClass(String clazz) {
		List<String> membersOfClass = new ArrayList<String>();
		OWLDataFactory factory = getManager().getOWLDataFactory();
		if (ontology.containsClassInSignature(IRI.create(clazz), true)) {
			OWLClass owlClazz = factory.getOWLClass(IRI.create(clazz));

			final NodeSet<OWLNamedIndividual> instances = reasoner
					.getInstances(owlClazz, true);
			final Set<OWLNamedIndividual> individuals = instances
					.getFlattened();
			for (OWLNamedIndividual t : individuals) {
				membersOfClass.add(t.getIRI().toString());
			}
		}
		return membersOfClass;
	}

	/**
	 * Remove all properties related to an individual
	 * 
	 * @param individual
	 *            individual
	 * @param property
	 *            property
	 */

	public void deleteAllValuesOfProperty(final String individual,
			final String property) {
		OWLDataFactory factory = getManager().getOWLDataFactory();

		boolean existInd = ontology.containsIndividualInSignature(
				IRI.create(individual), true);
		boolean isObjProp = ontology.containsObjectPropertyInSignature(
				IRI.create(property), true);
		boolean isDatatype = ontology.containsDataPropertyInSignature(
				IRI.create(property), true);

		if (isObjProp && existInd) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			OWLObjectProperty objProp = factory.getOWLObjectProperty(IRI
					.create(property));
			NodeSet<OWLNamedIndividual> values = reasoner
					.getObjectPropertyValues(ind, objProp);
			List<Node<OWLNamedIndividual>> list = new ArrayList<Node<OWLNamedIndividual>>(
					values.getNodes());
			if (list.size() >= 1) {

				for (Node<OWLNamedIndividual> node : list) {

					OWLObjectPropertyAssertionAxiom toRemove = factory
							.getOWLObjectPropertyAssertionAxiom(objProp, ind,
									node.getRepresentativeElement());
					removeAxiom(toRemove);

				}
			}
		} else if (isDatatype && existInd) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
					.create(property));

			Set<OWLLiteral> values = reasoner.getDataPropertyValues(ind,
					dataProp);

			List<OWLLiteral> list = new ArrayList<OWLLiteral>(values);
			if (list.size() >= 1) {
				for (OWLLiteral l : list) {
					OWLDataPropertyAssertionAxiom toRemoveAxiom = factory
							.getOWLDataPropertyAssertionAxiom(dataProp, ind, l);
					removeAxiom(toRemoveAxiom);

				}

			}
		}
	}

	public void deletePropertyValue(String individual, String property,
			String value) {
		OWLDataFactory factory = getManager().getOWLDataFactory();

		boolean existInd = ontology.containsIndividualInSignature(
				IRI.create(individual), true);

		boolean isObjProp = ontology.containsObjectPropertyInSignature(
				IRI.create(property), true);
		boolean isDatatype = ontology.containsDataPropertyInSignature(
				IRI.create(property), true);

		if (isObjProp && existInd) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			OWLObjectProperty objProp = factory.getOWLObjectProperty(IRI
					.create(property));
			if (ontology.containsIndividualInSignature(IRI.create(value))) {

				OWLNamedIndividual objVal = factory.getOWLNamedIndividual(IRI
						.create(value));

				OWLObjectPropertyAssertionAxiom toRemove = factory
						.getOWLObjectPropertyAssertionAxiom(objProp, ind,
								objVal);

				removeAxiom(toRemove);
			}
		} else if (isDatatype && existInd) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(individual));
			if (ontology.containsDataPropertyInSignature(IRI.create(property),
					true)) {
				OWLDataProperty dataProp = factory.getOWLDataProperty(IRI
						.create(property));
				Set<OWLLiteral> values = reasoner.getDataPropertyValues(ind,
						dataProp);

				List<OWLLiteral> list = new ArrayList<OWLLiteral>(values);

				for (OWLLiteral l : list) {
					if (l.getLiteral().equals(value)) {

						OWLDataPropertyAssertionAxiom toRemoveAxiom = factory
								.getOWLDataPropertyAssertionAxiom(dataProp,
										ind, l);
						removeAxiom(toRemoveAxiom);
					}
				}
			}

		}

	}

	private void removeAxiom(final OWLAxiom axiom) {
		RemoveAxiom rAxiom = new RemoveAxiom(ontology, axiom);
		getManager().applyChange(rAxiom);
		reasoner.flush();
	}

	public void deleteIndividual(String individual) {
		OWLDataFactory factory = getManager().getOWLDataFactory();
		OWLEntityRemover remover = new OWLEntityRemover(manager,
				Collections.singleton(ontology));
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
				.create(individual));
		ind.accept(remover);
		manager.applyChanges(remover.getChanges());
		reasoner.flush();
		remover.reset();
	}

	public void createAnnotation(String subj, String annotation, String value) {
		final OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		final OWLAnnotationProperty owlAnnotationProperty = factory
				.getOWLAnnotationProperty(IRI.create(annotation));

		final OWLAnnotationValue supportOntologyAnnValue = factory
				.getOWLLiteral(value, OWL2Datatype.RDF_PLAIN_LITERAL);

		final OWLAnnotationAssertionAxiom owlAnnotationAssertionAxiom = factory
				.getOWLAnnotationAssertionAxiom(owlAnnotationProperty,
						IRI.create(subj), supportOntologyAnnValue);

		manager.addAxiom(getOntology(), owlAnnotationAssertionAxiom);
	}

	public void addAnnotationToOntology(String annotation, String value) {

		OWLDataFactory factory = manager.getOWLDataFactory();

		final OWLAnnotationProperty owlAnnotationProperty = factory
				.getOWLAnnotationProperty(IRI.create(annotation));

		final OWLAnnotationValue supportOntologyAnnValue = factory
				.getOWLLiteral(value, OWL2Datatype.RDF_PLAIN_LITERAL);

		final OWLAnnotation owlAnnotation = factory.getOWLAnnotation(
				owlAnnotationProperty, supportOntologyAnnValue);

		manager.applyChange(new AddOntologyAnnotation(getOntology(),
				owlAnnotation));
	}

	OWLOntologyManager getManager() {
		return manager;
	}

	/**
	 * Get the prefix of an ontology given its abbreviation.
	 * 
	 * @param abbreviation
	 *            the abbreviation without :
	 * @return the prefix url
	 */

	public String getPrefixByAbbreviation(String abbreviation) {
		final PrefixOWLOntologyFormat prefixFormat = getOntology()
				.getOWLOntologyManager().getOntologyFormat(getOntology())
				.asPrefixOWLOntologyFormat();
		return prefixFormat.getPrefix(abbreviation + ":");
	}

	public boolean containsIndividual(String subj) {
		return ontology.containsIndividualInSignature(IRI.create(subj), true);
	}

	public Set<OWLAnnotationValue> getOntologyAnnotationValue(
			String turambarOntologyAnnotation) {
		Set<OWLAnnotationValue> response = new HashSet<OWLAnnotationValue>();
		if (ontology.containsAnnotationPropertyInSignature(
				IRI.create(turambarOntologyAnnotation), true)) {
			final Set<OWLAnnotation> annotations = ontology.getAnnotations();
			OWLDataFactory factory = ontology.getOWLOntologyManager()
					.getOWLDataFactory();
			final OWLAnnotationProperty dcProperty = factory
					.getOWLAnnotationProperty(IRI
							.create(turambarOntologyAnnotation));
			for (OWLAnnotation a : annotations) {
				if (a.getProperty().equals(dcProperty)) {
					response.add(a.getValue());
				}
			}
		}
		return response;
	}

	/**
	 * 
	 * @return A Collection containing the names of the classes in the ontology
	 */
	public Collection<String> getClassList() {

		Collection<String> classes = new ArrayList<String>();

		// it should be true to include the classes imported from other
		// ontologies.
		Set<OWLClass> classesInSignature = ontology.getClassesInSignature(true);
		for (OWLClass c : classesInSignature) {
			classes.add(c.getIRI().toString());
		}

		return classes;
	}

	public Collection<String> getSubclassesOfClass(String c) {

		final OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		final OWLClass owlClass = factory.getOWLClass(IRI.create(c));
		final Set<OWLOntology> ontologySet = new HashSet<OWLOntology>();
		ontologySet.add(ontology);
		ontologySet.addAll(ontology.getImportsClosure());
		final Set<OWLClassExpression> subClasses = owlClass
				.getSubClasses(ontologySet);
		final Set<String> response = new HashSet<String>();
		for (OWLClassExpression expr : subClasses) {
			final OWLClass asOWLClass = expr.asOWLClass();
			response.add(asOWLClass.getIRI().toString());
		}
		return response;
	}

	private Collection<Collection<String>> getEquivalentClassByClassExpresionType(
			final String c, ClassExpressionType type) {

		final OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		final OWLClass owlClass = factory.getOWLClass(IRI.create(c));
		final Set<OWLOntology> ontologySet = new HashSet<OWLOntology>();
		ontologySet.add(ontology);
		ontologySet.addAll(ontology.getImportsClosure());

		final Set<OWLClassExpression> equivalentClasses = owlClass
				.getEquivalentClasses(ontologySet);
		return getClassExpressionOfType(equivalentClasses, type);
	}

	public Collection<Collection<String>> getClassIntersectionOf(String c) {
		return getEquivalentClassByClassExpresionType(c,
				ClassExpressionType.OBJECT_INTERSECTION_OF);
	}

	public Collection<Collection<String>> getClassUnionOf(String c) {
		return getEquivalentClassByClassExpresionType(c,
				ClassExpressionType.OBJECT_UNION_OF);
	}

	public Collection<Collection<String>> getClassComplementOf(String c) {
		return getEquivalentClassByClassExpresionType(c,
				ClassExpressionType.OBJECT_COMPLEMENT_OF);
	}

	public Collection<Collection<String>> getClassEquivalentClass(String c) {
		return getEquivalentClassByClassExpresionType(c,
				ClassExpressionType.OWL_CLASS);
	}

	public Collection<Collection<String>> getClassDisjointWith(String c) {

		final OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();
		final OWLClass owlClass = factory.getOWLClass(IRI.create(c));
		final Set<OWLOntology> ontologySet = new HashSet<OWLOntology>();
		ontologySet.add(ontology);
		ontologySet.addAll(ontology.getImportsClosure());

		final Set<OWLClassExpression> disjointClasses = owlClass
				.getDisjointClasses(ontologySet);

		return getClassExpressionOfType(disjointClasses,
				ClassExpressionType.OWL_CLASS);
	}

	public String whichOntologyContainsAxiom(OWLAxiom axiom) {
		String response = null;
		for (OWLOntology ont : manager.getOntologies()) {
			if (ont.containsAxiom(axiom)) {
				response = ont.getOntologyID().getOntologyIRI().toString();
				break;
			}
		}
		return response;
	}

	public Map<String, Set<OWLAnnotationValue>> getOntologyAnnotationsOrderedByOntology(
			String annotation) {
		Map<String, Set<OWLAnnotationValue>> annotationValuesByOntologyIri = new HashMap<String, Set<OWLAnnotationValue>>();
		final OWLDataFactory factory = manager.getOWLDataFactory();
		final OWLAnnotationProperty dcProperty = factory
				.getOWLAnnotationProperty(IRI.create(annotation));
		for (OWLOntology ontology : manager.getOntologies()) {

			Set<OWLAnnotationValue> annotationValues = new HashSet<OWLAnnotationValue>();
			for (OWLAnnotation owlAnnotation : ontology.getAnnotations()) {
				if (owlAnnotation.getProperty().equals(dcProperty)) {
					annotationValues.add(owlAnnotation.getValue());
				}
			}

			if (!annotationValues.isEmpty()) {
				final OWLOntologyID ontologyID = ontology.getOntologyID();

				annotationValuesByOntologyIri.put(ontologyID.getOntologyIRI()
						.toString(), annotationValues);
			}

		}
		return annotationValuesByOntologyIri;
	}

	private Collection<Collection<String>> getClassExpressionOfType(
			Collection<OWLClassExpression> classExpressions,
			ClassExpressionType type) {
		Collection<Collection<String>> collection = new HashSet<Collection<String>>();
		for (OWLClassExpression expression : classExpressions) {
			final ClassExpressionType classExpressionType = expression
					.getClassExpressionType();
			if (type.equals(classExpressionType)) {
				final Set<OWLClass> equivalentClassesList = expression
						.getClassesInSignature();
				List<String> classAsStringList = new ArrayList<String>();
				for (OWLClass clz : equivalentClassesList) {
					classAsStringList.add(clz.getIRI().toString());
				}
				collection.add(classAsStringList);
			}
		}
		return collection;
	}

	public void addOntologyToMerge(String ontologyUri)
			throws OWLOntologyCreationException {
		manager.loadOntology(IRI.create(ontologyUri));
	}

	public void merge() throws OWLOntologyCreationException {
		OWLOntologyMerger merger = new OWLOntologyMerger(manager);
		ontology = merger.createMergedOntology(manager,
				IRI.create("http://www.talisman.org/temp.owl"));
	}

	public String toString() {

		// noinspection ObjectToString
		return "TraditionalOntologyImpl{" + "ontology=" + ontology
				+ ", reasoner=" + reasoner + ", manager=" + manager + '}';
	}

}