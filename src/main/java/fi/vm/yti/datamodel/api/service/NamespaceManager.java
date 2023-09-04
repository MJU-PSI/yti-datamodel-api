/*
 * Licensed under the European Union Public Licence (EUPL) V.1.1
 */
package fi.vm.yti.datamodel.api.service;

import fi.vm.yti.datamodel.api.utils.LDHelper;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public final class NamespaceManager {

    private static Logger logger = LoggerFactory.getLogger(NamespaceManager.class);

    private final EndpointServices endpointServices;
    private final JenaClient jenaClient;

    private static final List<String> ACCEPT_TYPES = List.of("application/rdf+xml;q=1.0", "text/turtle", "application/n-triples", "application/ld+json", "text/trig", "application/n-quads", "application/trix+xml", "application/rdf+thrift", "application/rdf+protobuf");

    @Autowired
    NamespaceManager(EndpointServices endpointServices,
                     JenaClient jenaClient) {
        this.endpointServices = endpointServices;
        this.jenaClient = jenaClient;
    }

    /**
     * Creates model of the namespaces and tries to resolve namespaces to import service
     *
     * @return model of the namespaces
     */
    public Model getDefaultNamespaceModelAndResolve() {

        Model nsModel = ModelFactory.createDefaultModel();

        Property preferredXMLNamespaceName = ResourceFactory.createProperty("http://purl.org/ws-mmi-dc/terms/preferredXMLNamespaceName");
        Property preferredXMLNamespacePrefix = ResourceFactory.createProperty("http://purl.org/ws-mmi-dc/terms/preferredXMLNamespacePrefix");
        RDFNode nsTypeStandard = ResourceFactory.createResource("http://purl.org/dc/terms/Standard");

        Iterator i = LDHelper.PREFIX_MAP.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry ns = (Map.Entry) i.next();
            String prefix = ns.getKey().toString();
            String namespace = ns.getValue().toString();

            if (LDHelper.isPrefixResolvable(prefix)) {
                // TODO: Check & optimize resolving
                resolveNamespace(namespace, null, false);
            }

            Resource nsResource = nsModel.createResource(namespace);
            nsModel.addLiteral(nsResource, preferredXMLNamespaceName, nsModel.createLiteral(namespace));
            nsModel.addLiteral(nsResource, preferredXMLNamespacePrefix, nsModel.createLiteral(prefix));
            nsModel.addLiteral(nsResource, RDFS.label, nsModel.createLiteral(prefix, "en"));
            nsModel.add(nsResource, RDF.type, nsTypeStandard);
        }

        return nsModel;

    }

    /**
     * Tries to resolve default namespaces and saves to the fixed namespace graph
     */
    public void resolveDefaultNamespaceToTheCore() {
        jenaClient.putModelToCore("urn:csc:iow:namespaces", getDefaultNamespaceModelAndResolve());
    }

    /**
     * Creates namespace model from default prefix-map
     *
     * @return
     */
    public Model getDefaultNamespaceModel() {

        Model nsModel = ModelFactory.createDefaultModel();

        Property preferredXMLNamespaceName = ResourceFactory.createProperty("http://purl.org/ws-mmi-dc/terms/preferredXMLNamespaceName");
        Property preferredXMLNamespacePrefix = ResourceFactory.createProperty("http://purl.org/ws-mmi-dc/terms/preferredXMLNamespacePrefix");
        RDFNode nsTypeStandard = ResourceFactory.createResource("http://purl.org/dc/terms/Standard");

        Iterator i = LDHelper.PREFIX_MAP.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry ns = (Map.Entry) i.next();
            String prefix = ns.getKey().toString();
            String namespace = ns.getValue().toString();

            Resource nsResource = nsModel.createResource(namespace);
            nsModel.addLiteral(nsResource, preferredXMLNamespaceName, nsModel.createLiteral(namespace));
            nsModel.addLiteral(nsResource, preferredXMLNamespacePrefix, nsModel.createLiteral(prefix));
            nsModel.addLiteral(nsResource, RDFS.label, nsModel.createLiteral(prefix, "en"));
            nsModel.add(nsResource, RDF.type, nsTypeStandard);
        }

        return nsModel;

    }

    /**
     * Adds default namespaces to the fixed namespace graph
     */
    public void addDefaultNamespacesToCore() {
        jenaClient.putModelToCore("urn:csc:iow:namespaces", getDefaultNamespaceModel());
    }

    /**
     * Returns true if schema is already stored
     *
     * @param namespace namespace of the schema
     * @return boolean
     */
    public boolean isSchemaInStore(String namespace) {
        return jenaClient.containsSchemaModel(namespace);
    }

    /**
     * Saves model to import service
     *
     * @param namespace namespace of the schema
     * @param model     schema as jena model
     */
    public void putSchemaToStore(String namespace,
                                 Model model) {
        jenaClient.putToImports(namespace, model);
    }

    /**
     * Returns namespaces from the graph
     *
     * @param graph Graph of the model
     * @return Returns prefix-map
     */
    public Map<String, String> getCoreNamespaceMap(String graph) {

        Model model = jenaClient.getModelFromCore(graph);

        if (model == null) {
            return null;
        }

        return model.getNsPrefixMap();

    }

    /**
     * Queries and returns all prefixes and namespaces used by models
     *
     * @return Prefix map
     */
    public Map<String, String> getCoreNamespaceMap() {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        String selectResources
            = "SELECT ?namespace ?prefix WHERE { "
            + "GRAPH ?graph { "
            + " ?graph a ?type  "
            + " VALUES ?type { owl:Ontology dcap:DCAP }"
            + " ?graph dcap:preferredXMLNamespaceName ?namespace . "
            + " ?graph dcap:preferredXMLNamespacePrefix ?prefix . "
            + "}}";

        pss.setNsPrefixes(LDHelper.PREFIX_MAP);
        pss.setCommandText(selectResources);

        Map namespaceMap = new HashMap<String, String>();

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                namespaceMap.put(soln.getLiteral("prefix").toString(), soln.getLiteral("namespace").toString());
            }
        }

        return namespaceMap;

    }

    @Deprecated
    public String getExternalPredicateType(IRI predicate) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        String selectResources
            = "SELECT ?type WHERE { "
            + "{ ?predicate a ?type . "
            + " VALUES ?type { owl:DatatypeProperty owl:ObjectProperty } "
            + "} UNION {"
            /* IF Predicate Type is rdf:Property and range is rdfs:Literal = DatatypeProperty */
            + "?predicate a rdf:Property . "
            + "?predicate rdfs:range rdfs:Literal ."
            + "BIND(owl:DatatypeProperty as ?type) "
            + "FILTER NOT EXISTS { ?predicate a ?multiType . VALUES ?multiType { owl:DatatypeProperty owl:ObjectProperty } }"
            + "} UNION {"
            /* IF Predicate Type is rdf:Property and range is rdfs:Resource then property is object property */
            + "?predicate a rdf:Property . "
            + "?predicate rdfs:range rdfs:Resource ."
            + "BIND(owl:ObjectProperty as ?type) "
            + "FILTER NOT EXISTS { ?predicate a ?multiType . VALUES ?multiType { owl:DatatypeProperty owl:ObjectProperty } }"
            + "}UNION {"
            /* IF Predicate Type is rdf:Property and range is resource that is class or thing */
            + "?predicate a rdf:Property . "
            + "FILTER NOT EXISTS { ?predicate a ?multiType . VALUES ?multiType { owl:DatatypeProperty owl:ObjectProperty } }"
            + "?predicate rdfs:range ?rangeClass . "
            + "?rangeClass a ?rangeClassType . "
            + "VALUES ?rangeClassType { skos:Concept owl:Thing rdfs:Class }"
            + "BIND(owl:ObjectProperty as ?type) "
            + "} UNION {"
            /* IF Predicate type cannot be guessed */
            + "?predicate a rdf:Property . "
            + "BIND(rdf:Property as ?type)"
            + "FILTER NOT EXISTS { ?predicate a ?multiType . VALUES ?multiType { owl:DatatypeProperty owl:ObjectProperty } }"
            + "FILTER NOT EXISTS { ?predicate rdfs:range rdfs:Literal . }"
            + "FILTER NOT EXISTS { ?predicate rdfs:range rdfs:Resource . }"
            + "FILTER NOT EXISTS { ?predicate rdfs:range ?rangeClass . ?rangeClass a ?rangeClassType . }"
            + "} "
            + "}";

        pss.setNsPrefixes(LDHelper.PREFIX_MAP);
        pss.setCommandText(selectResources);
        pss.setIri("predicate", predicate);

        String type = null;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getImportsSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                if (soln.contains("type")) {
                    Resource resType = soln.getResource("type");
                    type = resType.getURI();
                }
            }

        }
        return type;

    }
    
    public boolean resolveNamespace(String namespace,
                                    String alternativeURL,
                                    boolean force) {

        if(!namespace.startsWith("http") && (alternativeURL==null || alternativeURL.isEmpty() || !alternativeURL.startsWith("http"))) {
            return false;
        }

        try { // Unexpected exception

            IRI namespaceIRI = null;
            IRI alternativeIRI = null;

            try {
                IRIFactory iri = IRIFactory.iriImplementation();
                namespaceIRI = iri.construct(namespace);

                if (alternativeURL != null) {
                    alternativeIRI = iri.construct(alternativeURL);
                }

            } catch (IRIException e) {
                logger.warn("Namespace is invalid IRI!");
                return false;
            }

            if (isSchemaInStore(namespace) && !force) {
                logger.info("Schema found in store: " + namespace);
                return true;
            } else {
                logger.info("Trying to connect to: " + namespace);
                Model model = ModelFactory.createDefaultModel();

                URL url;

                try {
                    if (alternativeIRI != null) {
                        url = new URL(alternativeURL);
                    } else {
                        url = new URL(namespace);
                    }
                } catch (MalformedURLException e) {
                    logger.warn("Malformed Namespace URL: " + namespace);
                    return false;
                }

                if (!("https".equals(url.getProtocol()) || "http".equals(url.getProtocol()))) {
                    logger.warn("Namespace NOT http or https: " + namespace);
                    return false;
                }

                return resolveNamespace(namespace);
            }

        } catch (Exception ex) {
            logger.warn("Error in loading the " + namespace);
            logger.warn(ex.getMessage(), ex);
            return false;
        }
    }


    public boolean resolveNamespace(String namespace){
        logger.info("Resolving namespace: {}", namespace);
        var model = ModelFactory.createDefaultModel();
        try{
            RDFParser.create()
                    .source(namespace)
                    .lang(Lang.RDFXML)
                    .httpAccept(String.join(", ", ACCEPT_TYPES))
                    .parse(model);

            logger.info("Model-size is: " + model.size());

            if(model.size() > 0){
                putSchemaToStore(namespace, model);

                return true;
            }
        } catch (RiotException ex){
            logger.warn("Namespace: {}, not resolvable: {}", namespace, ex.getMessage());
            return false;
        } catch (HttpException ex){
            logger.warn("Namespace not resolvable due to HTTP error, Status code: {}", ex.getStatusCode());
            return false;
        }
        return false;
    }

}
