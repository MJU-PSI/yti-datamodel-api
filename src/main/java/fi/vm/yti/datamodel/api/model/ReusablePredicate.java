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

public class ReusablePredicate extends AbstractPredicate {

    private static final Logger logger = LoggerFactory.getLogger(ReusablePredicate.class.getName());

    public ReusablePredicate(IRI predicateId,
                             GraphManager graphManager) throws IllegalArgumentException {
        super(predicateId, graphManager);
    }

    public ReusablePredicate(Model model,
                             GraphManager graphManager) throws IllegalArgumentException {
        super(model, graphManager);
    }

    public ReusablePredicate(IRI conceptIRI,
                             IRI modelIRI,
                             String predicateLabel,
                             String lang,
                             IRI typeIRI,
                             GraphManager graphManager,
                             TerminologyManager terminologyManager) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        String queryString = "CONSTRUCT  { "
            + "?predicateIRI owl:versionInfo ?draft . "
            + "?predicateIRI a ?type .  "
            + "?predicateIRI rdfs:isDefinedBy ?model . "
            + "?model rdfs:label ?modelLabel . "
            + "?model a ?modelType . "
            + "?predicateIRI rdfs:label ?predicateLabel . "
            + "?predicateIRI rdfs:comment ?comment . "
            + "?predicateIRI dcterms:subject ?concept . "
            + "?concept a skos:Concept . "
            + "?concept skos:prefLabel ?label . "
            + "?concept skos:definition ?comment . "
            + "?concept owl:versionInfo ?status . "
            + "?concept dcterms:modified ?conceptModified . "
            + "?concept skos:inScheme ?scheme . "
            + "?scheme a skos:ConceptScheme . "
            + "?scheme skos:prefLabel ?title . "
            + "} "
            + "WHERE { "
            + "?model a ?modelType . "
            + "?model rdfs:label ?modelLabel . "
            + "?concept a skos:Concept . "
            + "?concept skos:prefLabel ?label . "
            + "?concept skos:inScheme ?scheme . "
            + "?concept dcterms:modified ?conceptModified . "
            + "?concept owl:versionInfo ?status . "
            + "?scheme skos:prefLabel ?title . "
            + "OPTIONAL {"
            + "?concept skos:definition ?comment . } "
            + "}";

        pss.setCommandText(queryString);

        pss.setIri("concept", conceptIRI);
        pss.setIri("model", modelIRI);
        pss.setIri("type", typeIRI);
        pss.setLiteral("draft", "DRAFT");
        pss.setLiteral("predicateLabel", ResourceFactory.createLangLiteral(predicateLabel, lang));
        String predicateName = LDHelper.propertyName(predicateLabel);
        String predicateUri = LDHelper.resourceIRI(modelIRI.toString(), predicateName);
        pss.setIri("predicateIRI", predicateUri);

        this.graph = terminologyManager.constructModelFromTerminologyAPIAndCore(conceptIRI.toString(), modelIRI.toString(), pss.asQuery());
        this.graph.add(ResourceFactory.createResource(predicateUri), DCTerms.created, LDHelper.getDateTimeLiteral());
        this.graph.add(ResourceFactory.createResource(predicateUri), DCTerms.modified, LDHelper.getDateTimeLiteral());

    }

    /**
     * Creates superpredicate from exiting predicate
     *
     * @param oldPredicateIRI IRI of the existing class
     * @param newModelIRI     Model to create the superclass
     * @param graphManager    Graphservice
     */
    public ReusablePredicate(IRI oldPredicateIRI,
                             IRI newModelIRI,
                             Property relatedProperty,
                             GraphManager graphManager) {

        this.graph = graphManager.getCoreGraph(oldPredicateIRI);

        if (this.graph.size() < 1) {
            throw new IllegalArgumentException("No existing predicate found");
        }

        if (!(this.graph.contains(ResourceFactory.createResource(oldPredicateIRI.toString()), RDF.type, OWL.DatatypeProperty) || this.graph.contains(ResourceFactory.createResource(oldPredicateIRI.toString()), RDF.type, OWL.ObjectProperty) || this.graph.contains(ResourceFactory.createResource(oldPredicateIRI.toString()), RDF.type, OWL.AnnotationProperty))) {
            throw new IllegalArgumentException("Expected predicate type");
        }

        Resource relatedPredicate = this.graph.getResource(oldPredicateIRI.toString());
        String superPredicateIRI = newModelIRI + "#" + relatedPredicate.getLocalName();
        logger.debug("Creating new superPredicate: " + superPredicateIRI);
        ResourceUtils.renameResource(relatedPredicate, superPredicateIRI);

        relatedPredicate = this.graph.getResource(superPredicateIRI);
        relatedPredicate.removeAll(OWL.versionInfo);
        relatedPredicate.addLiteral(OWL.versionInfo, "DRAFT");

        Resource oldModel = relatedPredicate.getPropertyResourceValue(RDFS.isDefinedBy);
        oldModel.removeProperties();
        relatedPredicate.removeAll(RDFS.isDefinedBy);
        relatedPredicate.removeAll(relatedProperty);
        relatedPredicate.addProperty(RDFS.isDefinedBy, ResourceFactory.createResource(newModelIRI.toString()));
        relatedPredicate.addProperty(relatedProperty, ResourceFactory.createResource(oldPredicateIRI.toString()));

        LDHelper.rewriteLiteral(this.graph, relatedPredicate, DCTerms.created, LDHelper.getDateTimeLiteral());
        LDHelper.rewriteLiteral(this.graph, relatedPredicate, DCTerms.modified, LDHelper.getDateTimeLiteral());
        relatedPredicate.removeAll(DCTerms.identifier);

        this.graph.add(graphManager.getModelInfo(newModelIRI));

    }

    public ReusablePredicate(IRI modelIRI,
                             String predicateLabel,
                             String lang,
                             IRI typeIRI,
                             GraphManager graphManager) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        String queryString = "CONSTRUCT  { "
            + "?predicateIRI owl:versionInfo ?draft . "
            + "?predicateIRI a ?type .  "
            + "?predicateIRI rdfs:isDefinedBy ?model . "
            + "?model rdfs:label ?modelLabel . "
            + "?model a ?modelType . "
            + "?predicateIRI rdfs:label ?predicateLabel . "
            + "} "
            + "WHERE { GRAPH ?model {"
            + "?model a ?modelType . "
            + "?model rdfs:label ?modelLabel . } "
            + "}";

        pss.setCommandText(queryString);
        pss.setIri("model", modelIRI);
        pss.setIri("type", typeIRI);
        pss.setLiteral("draft", "DRAFT");
        pss.setLiteral("predicateLabel", ResourceFactory.createLangLiteral(predicateLabel, lang));
        String predicateName = LDHelper.propertyName(predicateLabel);
        String predicateUri = LDHelper.resourceIRI(modelIRI.toString(), predicateName);
        pss.setIri("predicateIRI", predicateUri);

        this.graph = graphManager.constructModelFromCoreGraph(pss.toString());
        this.graph.add(ResourceFactory.createResource(predicateUri), DCTerms.created, LDHelper.getDateTimeLiteral());
        this.graph.add(ResourceFactory.createResource(predicateUri), DCTerms.modified, LDHelper.getDateTimeLiteral());

    }

}

