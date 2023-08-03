package fi.vm.yti.datamodel.api.model;

import fi.vm.yti.datamodel.api.service.*;
import fi.vm.yti.datamodel.api.utils.LDHelper;

import org.apache.jena.iri.IRI;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.*;

import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.vocabulary.SH;

import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ReusableClass extends AbstractClass {

    private static final Logger logger = LoggerFactory.getLogger(ReusableClass.class.getName());

    public ReusableClass(IRI classId,
                         GraphManager graphManager) throws IllegalArgumentException {
        super(classId, graphManager);
    }

    public ReusableClass(Model model,
                         GraphManager graphManager) throws IllegalArgumentException {
        super(model, graphManager);
    }

    public ReusableClass(IRI conceptIRI,
                         IRI modelIRI,
                         String classLabel,
                         String lang,
                         GraphManager graphManager,
                         TerminologyManager terminologyManager) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        String queryString = "CONSTRUCT  { "
            + "?classIRI owl:versionInfo ?draft . "
            + "?classIRI a rdfs:Class . "
            + "?classIRI rdfs:isDefinedBy ?model . "
            + "?model rdfs:label ?modelLabel . "
            + "?model a ?modelType . "
            + "?classIRI sh:name ?classLabel . "
            + "?classIRI sh:description ?comment . "
            + "?classIRI dcterms:subject ?concept . "
            + "?concept a skos:Concept . "
            + "?concept skos:prefLabel ?label . "
            + "?concept skos:definition ?comment . "
            + "?concept skos:inScheme ?scheme . "
            + "?concept dcterms:modified ?conceptModified . "
            + "?concept owl:versionInfo ?status . "
            + "?scheme a skos:ConceptScheme . "
            + "?scheme skos:prefLabel ?title . "
            + "} WHERE { "
            + "?model a ?modelType . "
            + "?model rdfs:label ?modelLabel . "
            + "?concept a skos:Concept . "
            + "?concept skos:prefLabel ?label . "
            + "?concept owl:versionInfo ?status . "
            + "?concept dcterms:modified ?conceptModified . "
            + "?concept skos:inScheme ?scheme . "
            + "?scheme skos:prefLabel ?title . "
            + "OPTIONAL {"
            + "?concept skos:definition ?comment . } "
            + "}";

        pss.setCommandText(queryString);

        pss.setIri("concept", conceptIRI);
        pss.setIri("model", modelIRI);
        pss.setLiteral("draft", "DRAFT");
        pss.setLiteral("classLabel", ResourceFactory.createLangLiteral(classLabel, lang));
        String resourceName = LDHelper.resourceName(classLabel);
        String resourceURI = LDHelper.resourceIRI(modelIRI.toString(), resourceName);
        pss.setIri("classIRI", resourceURI);


        Literal created = LDHelper.getDateTimeLiteral();

        this.graph = terminologyManager.constructModelFromTerminologyAPIAndCore(conceptIRI.toString(), modelIRI.toString(), pss.asQuery());
        this.graph.add(ResourceFactory.createResource(resourceURI), DCTerms.created, created);
        this.graph.add(ResourceFactory.createResource(resourceURI), DCTerms.modified, created);

    }

    /**
     * Creates superclass from exiting class
     *
     * @param oldClassIRI  IRI of the existing class
     * @param newModelIRI  Model to create the superclass
     * @param graphManager Graphservice
     */
    public ReusableClass(IRI oldClassIRI,
                         IRI newModelIRI,
                         Property classRelation,
                         GraphManager graphManager) {

        this.graph = graphManager.getCoreGraph(oldClassIRI);

        if (this.graph.size() < 1) {
            throw new IllegalArgumentException("No existing class found");
        }

        if (!this.graph.contains(ResourceFactory.createResource(oldClassIRI.toString()), RDF.type, RDFS.Class)) {
            throw new IllegalArgumentException("Expected rdfs:Class type");
        }

        Resource relatedClass = this.graph.getResource(oldClassIRI.toString());
        String superClassIRI = newModelIRI + "#" + relatedClass.getLocalName();

        logger.debug("Creating new superclass: " + superClassIRI);
        ResourceUtils.renameResource(relatedClass, superClassIRI);

        relatedClass = this.graph.getResource(superClassIRI);
        relatedClass.removeAll(OWL.versionInfo);
        relatedClass.addLiteral(OWL.versionInfo, "DRAFT");

        Resource modelResource = relatedClass.getPropertyResourceValue(RDFS.isDefinedBy);
        modelResource.removeProperties();
        relatedClass.removeAll(RDFS.isDefinedBy);
        relatedClass.removeAll(classRelation);
        relatedClass.addProperty(classRelation, ResourceFactory.createResource(oldClassIRI.toString()));
        relatedClass.addProperty(RDFS.isDefinedBy, ResourceFactory.createResource(newModelIRI.toString()));

        LDHelper.rewriteLiteral(this.graph, relatedClass, DCTerms.created, LDHelper.getDateTimeLiteral());
        LDHelper.rewriteLiteral(this.graph, relatedClass, DCTerms.modified, LDHelper.getDateTimeLiteral());
        relatedClass.removeAll(DCTerms.identifier);

        this.graph.add(graphManager.getModelInfo(newModelIRI));

        // Rename property UUIDs
        StmtIterator nodes = relatedClass.listProperties(SH.property);
        List<Statement> propertyShapeList = nodes.toList();
        Iterator<Statement> propertyIter = propertyShapeList.iterator();
        if (Iterables.size((Iterable<?>) propertyIter) > Integer.MAX_VALUE) {
            throw new RuntimeException("Too many items for iteration");
        }

        while (propertyIter.hasNext()) {
            Resource propertyShape = propertyIter.next().getObject().asResource();
            ResourceUtils.renameResource(propertyShape, "urn:uuid:" + UUID.randomUUID().toString());
        }
    }

    public ReusableClass(IRI modelIRI,
                         String classLabel,
                         String lang,
                         GraphManager graphManager) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        String queryString = "CONSTRUCT  { "
            + "?classIRI owl:versionInfo ?draft . "
            + "?classIRI a rdfs:Class . "
            + "?classIRI rdfs:isDefinedBy ?model . "
            + "?model rdfs:label ?modelLabel . "
            + "?model a ?modelType . "
            + "?classIRI sh:name ?classLabel . "
            + "?classIRI sh:description ?comment . "
            + "} WHERE { "
            + "GRAPH ?model {"
            + "?model a ?modelType . "
            + "?model rdfs:label ?modelLabel . "
            + "}}";

        pss.setCommandText(queryString);
        pss.setIri("model", modelIRI);
        pss.setLiteral("draft", "DRAFT");
        pss.setLiteral("classLabel", ResourceFactory.createLangLiteral(classLabel, lang));
        String resourceName = LDHelper.resourceName(classLabel);
        String resourceUri = LDHelper.resourceIRI(modelIRI.toString(), resourceName);
        pss.setIri("classIRI", resourceUri);

        this.graph = graphManager.constructModelFromCoreGraph(pss.toString());

        Literal created = LDHelper.getDateTimeLiteral();

        this.graph.add(ResourceFactory.createResource(resourceUri), DCTerms.created, created);
        this.graph.add(ResourceFactory.createResource(resourceUri), DCTerms.modified, created);

    }

}
