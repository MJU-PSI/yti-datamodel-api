/*
 * Licensed under the European Union Public Licence (EUPL) V.1.1
 */
package fi.vm.yti.datamodel.api.service;

import fi.vm.yti.datamodel.api.utils.LDHelper;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.resultset.ResultSetPeekable;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

import org.apache.jena.util.SplitIRI;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaWriter {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaWriter.class.getName());

    private final EndpointServices endpointServices;
    private final JsonWriterFactory jsonWriterFactory;
    private final GraphManager graphManager;

    JsonSchemaWriter(EndpointServices endpointServices,
                     JsonWriterFactory jsonWriterFactory,
                     GraphManager graphManager) {
        this.endpointServices = endpointServices;
        this.jsonWriterFactory = jsonWriterFactory;
        this.graphManager = graphManager;
    }

    private static final Map<String, String> DATATYPE_MAP =
        Collections.unmodifiableMap(new HashMap<>() {{
            put("http://www.w3.org/2001/XMLSchema#int", "integer");
            put("http://www.w3.org/2001/XMLSchema#integer", "integer");
            put("http://www.w3.org/2001/XMLSchema#long", "integer");
            put("http://www.w3.org/2001/XMLSchema#float", "number");
            put("http://www.w3.org/2001/XMLSchema#double", "number");
            put("http://www.w3.org/2001/XMLSchema#decimal", "number");
            put("http://www.w3.org/2001/XMLSchema#boolean", "boolean");
            put("http://www.w3.org/2001/XMLSchema#date", "string");
            put("http://www.w3.org/2001/XMLSchema#dateTime", "string");
            put("http://www.w3.org/2001/XMLSchema#time", "string");
            put("http://www.w3.org/2001/XMLSchema#gYear", "string");
            put("http://www.w3.org/2001/XMLSchema#gMonth", "string");
            put("http://www.w3.org/2001/XMLSchema#gDay", "string");
            put("http://www.w3.org/2001/XMLSchema#string", "string");
            put("http://www.w3.org/2001/XMLSchema#anyURI", "string");
            put("http://www.w3.org/2001/XMLSchema#hexBinary", "string");
            put("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString", "langString");
            put("http://www.w3.org/2000/01/rdf-schema#Literal", "string");
        }});

    private static final Map<String, String> FORMAT_MAP =
        Collections.unmodifiableMap(new HashMap<>() {{
            put("http://www.w3.org/2001/XMLSchema#dateTime", "date-time");
            put("http://www.w3.org/2001/XMLSchema#date", "date");
            put("http://www.w3.org/2001/XMLSchema#time", "time");
            put("http://www.w3.org/2001/XMLSchema#anyURI", "uri");
        }});

    public String newResourceSchema(String classID,
                                    String lang) {

        JsonArrayBuilder required = Json.createArrayBuilder();
        JsonObjectBuilder schema = Json.createObjectBuilder();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        String selectClass =
            "SELECT ?type ?label ?description ?minProperties ?maxProperties "
                + "WHERE { "
                + "GRAPH ?resourceID { "
                + "?resourceID a ?type . "
                + "OPTIONAL { ?resourceID sh:name ?label . "
                + "FILTER (langMatches(lang(?label),?lang)) }"
                + "OPTIONAL { ?resourceId iow:minProperties ?minProperties . }"
                + "OPTIONAL { ?resourceId iow:maxProperties ?maxProperties . }"
                + "OPTIONAL { ?resourceID sh:description ?description . "
                + "FILTER (langMatches(lang(?description),?lang))"
                + "}"
                + "} "
                + "} ";

        pss.setIri("resourceID", classID);
        if (lang != null) pss.setLiteral("lang", lang);
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        pss.setCommandText(selectClass);

        boolean classMetadata = false;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();

            if (!results.hasNext()) return null;

            int i = 0;
            while (results.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = results.nextSolution();

                if (soln.contains("description")) {
                    String description = soln.getLiteral("description").getString();
                    schema.add("description", description);
                }

                if (soln.contains("minProperties")) {
                    schema.add("minProperties", soln.getLiteral("minProperties").getInt());
                }

                if (soln.contains("maxProperties")) {
                    schema.add("maxProperties", soln.getLiteral("maxProperties").getInt());
                }

                schema.add("id", classID + ".jschema");

                if (soln.contains("label")) {
                    String title = soln.getLiteral("label").getString();
                    schema.add("title", title);
                }

                schema.add("@id", classID);

                String sType = soln.getResource("type").getLocalName();

                if (sType.equals("Class") || sType.equals("Shape") || sType.equals("NodeShape")) {
                    classMetadata = true;
                }

            }

        }

        JsonObjectBuilder properties = Json.createObjectBuilder();

        if (classMetadata) {

            String selectResources =
                "SELECT ?predicate ?id ?property ?propertyDeactivated ?valueList ?schemeList ?predicateName ?label ?datatype ?shapeRef ?min ?max ?minLength ?maxLength ?pattern ?idBoolean "
                    + "WHERE { "
                    + "GRAPH ?resourceID {"
                    + "?resourceID sh:property ?property . "
                    + "?property sh:path ?predicate . "
                    + "OPTIONAL { ?property sh:deactivated ?propertyDeactivated . }"
                    + "OPTIONAL { ?property iow:localName ?id . }"
                    + "OPTIONAL { ?property ?nameProperty ?label . "
                    + "VALUES ?nameProperty { sh:name rdfs:label }"
                    + "FILTER (langMatches(lang(?label),?lang)) }"
                    + "OPTIONAL { ?property ?commentProperty ?description . "
                    + "VALUES ?commentProperty { sh:description rdfs:comment }"
                    + "FILTER (langMatches(lang(?description),?lang))"
                    + "}"
                    + "OPTIONAL { ?property sh:datatype ?datatype . }"
                    + "OPTIONAL { ?property sh:node ?shapeRef . }"
                    + "OPTIONAL { ?property sh:minCount ?min . }"
                    + "OPTIONAL { ?property sh:maxCount ?max . }"
                    + "OPTIONAL { ?property sh:pattern ?pattern . }"
                    + "OPTIONAL { ?property sh:minLength ?minLength . }"
                    + "OPTIONAL { ?property sh:maxLength ?maxLength . }"
                    + "OPTIONAL { ?property sh:in ?valueList . } "
                    + "OPTIONAL { ?property dcam:memberOf ?schemeList . } "
                    + "OPTIONAL { ?property iow:isResourceIdentifier ?idBoolean . }"
                    + "BIND(afn:localname(?predicate) as ?predicateName)"
                    + "}"
                    + "}";

            pss.setCommandText(selectResources);
            pss.setIri("resourceID", classID);
            if (lang != null) pss.setLiteral("lang", lang);

            try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

                ResultSet results = qexec.execSelect();

                if (results.hasNext()) {

                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        //String predicateID = soln.getResource("predicate").getString();
                        String predicateName = soln.getLiteral("predicateName").getString();

                        if (soln.contains("id")) {
                            predicateName = soln.getLiteral("id").getString();
                        }

                        JsonObjectBuilder predicate = Json.createObjectBuilder();

                        if (soln.contains("label")) {
                            String title = soln.getLiteral("label").getString();
                            predicate.add("title", title);
                        }

                        if (soln.contains("min")) {
                            int min = soln.getLiteral("min").getInt();
                            if (min > 0) {
                                required.add(predicateName);
                            }
                        }

                        if (soln.contains("description")) {
                            String description = soln.getLiteral("description").getString();
                            predicate.add("description", description);
                        }

                        if (soln.contains("predicate")) {
                            String predicateID = soln.getResource("predicate").toString();
                            predicate.add("@id", predicateID);
                        }

                        if (soln.contains("valueList")) {
                            JsonArray valueList = getValueList(classID, soln.getResource("property").toString());
                            if (valueList != null) {
                                predicate.add("enum", valueList);
                            }
                        } else if (soln.contains("schemeList")) {
                            JsonArray schemeList = getSchemeValueList(soln.getResource("schemeList").toString());
                            if (schemeList != null) {
                                predicate.add("enum", schemeList);
                            }
                        }

                        if (soln.contains("datatype")) {
                            String datatype = soln.getResource("datatype").toString();

                            if (soln.contains("idBoolean")) {
                                Boolean isId = soln.getLiteral("idBoolean").getBoolean();
                                if (isId) {
                                    predicate.add("@type", "@id");
                                } else predicate.add("@type", datatype);
                            } else {
                                predicate.add("@type", datatype);
                            }

                            String jsonDatatype = DATATYPE_MAP.get(datatype);

                            if (soln.contains("min") && soln.getLiteral("min").getInt() > 0) {
                                predicate.add("minItems", soln.getLiteral("min").getInt());
                            }

                            if (soln.contains("max") && soln.getLiteral("max").getInt() <= 1) {

                                predicate.add("maxItems", 1);

                                if (jsonDatatype != null) {

                                    if (jsonDatatype.equals("langString")) {
                                        predicate.add("type", "object");
                                        predicate.add("$ref", "#/definitions/langString");
                                    } else
                                        predicate.add("type", jsonDatatype);
                                }

                            } else {

                                if (soln.contains("max") && soln.getLiteral("max").getInt() > 1) {
                                    predicate.add("maxItems", soln.getLiteral("max").getInt());
                                }

                                predicate.add("type", "array");

                                if (jsonDatatype != null) {

                                    JsonObjectBuilder typeObject = Json.createObjectBuilder();

                                    if (jsonDatatype.equals("langString")) {
                                        typeObject.add("type", "object");
                                        typeObject.add("$ref", "#/definitions/langString");
                                    } else {
                                        typeObject.add("type", jsonDatatype);
                                    }

                                    predicate.add("items", typeObject.build());

                                }

                            }

                            if (soln.contains("maxLength")) {
                                predicate.add("maxLength", soln.getLiteral("maxLength").getInt());
                            }

                            if (soln.contains("minLength")) {
                                predicate.add("minLength", soln.getLiteral("minLength").getInt());
                            }

                            if (soln.contains("pattern")) {
                                predicate.add("pattern", soln.getLiteral("pattern").getString());
                            }

                            if (FORMAT_MAP.containsKey(datatype)) {
                                predicate.add("format", FORMAT_MAP.get(datatype));
                            }
                        } else {
                            if (soln.contains("shapeRef")) {
                                String shapeRef = soln.getResource("shapeRef").toString();
                                predicate.add("@type", "@id");
                                if (!soln.contains("max") || soln.getLiteral("max").getInt() > 1) {
                                    if (soln.contains("min")) predicate.add("minItems", soln.getLiteral("min").getInt());
                                    if (soln.contains("max")) predicate.add("maxItems", soln.getLiteral("max").getInt());
                                    predicate.add("type", "array");
                                    predicate.add("items", Json.createObjectBuilder().add("type", "object").add("$ref", shapeRef + ".jschema").build());
                                } else {
                                    predicate.add("type", "object");
                                    predicate.add("$ref", shapeRef + ".jschema");
                                }
                            }
                        }

                        if (!soln.contains("propertyDeactivated") || (soln.contains("propertyDeactivated") && !soln.getLiteral("propertyDeactivated").getBoolean())) {
                            properties.add(predicateName, predicate.build());
                        }
                    }
                }
            }

            return createDefaultSchema(schema, properties, required);

        } else {
            /* Return dummy schema if resource is not a class */
            return createDummySchema(schema, properties, required);
        }

    }

    public String jsonObjectToPrettyString(JsonObject object) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = jsonWriterFactory.createWriter(stringWriter);
        writer.writeObject(object);
        writer.close();
        return stringWriter.getBuffer().toString();
    }

    public JsonArray getSchemeValueList(String schemeID) {
        JsonArrayBuilder builder = Json.createArrayBuilder();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        String selectList =
            "SELECT ?value "
                + "WHERE { "
                + "GRAPH ?scheme { "
                + "?code dcterms:identifier ?value . "
                + "} "
                + "} ORDER BY ?value";

        pss.setIri("scheme", schemeID);
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);
        pss.setCommandText(selectList);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getSchemesSparqlAddress(), pss.toString())) {

            ResultSet results = qexec.execSelect();

            if (!results.hasNext()) return null;

            int i = 0;
            while (results.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = results.next();
                if (soln.contains("value")) {
                    builder.add(soln.getLiteral("value").getString());
                }
            }

        }

        return builder.build();
    }

    public JsonArray getValueList(String classID,
                                  String propertyID) {
        JsonArrayBuilder builder = Json.createArrayBuilder();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        String selectList =
            "SELECT ?value "
                + "WHERE { "
                + "GRAPH ?resource { "
                + "?resource sh:property ?property . "
                + "?property sh:in/rdf:rest*/rdf:first ?value"
                + "} "
                + "} ";

        pss.setIri("resource", classID);
        pss.setIri("property", propertyID);
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);
        pss.setCommandText(selectList);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.toString())) {

            ResultSet results = qexec.execSelect();

            if (!results.hasNext()) return null;

            int i = 0;
            while (results.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = results.next();
                if (soln.contains("value")) {
                    builder.add(soln.getLiteral("value").getString());
                }
            }
        }

        return builder.build();
    }


    /*
    Ways to describe codelists, by "type"-list.

        {
        type:[
        {enum:["22PC"], description:"a description for the first enum"},
        {enum:["42GP"], description:"a description for the second enum"},
        {enum:["45GP"], description:"a description for the third enum"},
        {enum:["45UP"], description:"a description for the fourth enum"},
        {enum:["22GP"], description:"a description for the fifth enum"}
        ]
        }

     or by using custom parameters:

        enum:[1,2,3],
        options:[{value:1,descrtiption:"this is one"},{value:2,description:"this is two"}],


    */

    public String getModelRoot(String graph) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        String selectResources =
            "SELECT ?root WHERE {"
                + "GRAPH ?graph { ?graph void:rootResource ?root . }"
                + "}";

        pss.setNsPrefixes(LDHelper.PREFIX_MAP);
        pss.setCommandText(selectResources);
        pss.setIri("graph", graph);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();

            if (!results.hasNext()) return null;
            else {
                QuerySolution soln = results.next();
                if (soln.contains("root")) {
                    return soln.getResource("root").toString();
                } else return null;
            }
        }
    }

    public JsonObjectBuilder getClassDefinitions(String modelID,
                                                 String lang) {

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        String selectResources =
            "SELECT ?resource ?targetClass ?className ?localClassName ?classTitle ?classDeactivated ?classDescription ?minProperties ?maxProperties ?property ?propertyDeactivated ?valueList ?schemeList ?predicate ?id ?title ?description ?predicateName ?datatype ?shapeRef ?shapeRefName ?min ?max ?minLength ?maxLength ?pattern ?idBoolean ?example "
                + "WHERE { "
                + "GRAPH ?modelPartGraph {"
                + "?model dcterms:hasPart ?resource . "
                + "}"
                + "GRAPH ?resource {"
                + "?resource a ?resourceType . "
                + "VALUES ?resourceType { rdfs:Class sh:Shape sh:NodeShape }"
                + "OPTIONAL { ?resource iow:localName ?localClassName . } "
                + "OPTIONAL { ?resource sh:name ?classTitle . "
                + "FILTER (langMatches(lang(?classTitle),?lang)) }"
                + "OPTIONAL { ?resource sh:deactivated ?classDeactivated . }"
                + "OPTIONAL { ?resource iow:minProperties ?minProperties . }"
                + "OPTIONAL { ?resource iow:maxProperties ?maxProperties . }"
                + "OPTIONAL { ?resource sh:targetClass ?targetClass . }"
                + "OPTIONAL { ?resource sh:description ?classDescription . "
                + "FILTER (langMatches(lang(?classDescription),?lang))"
                + "}"
                + "BIND(afn:localname(?resource) as ?className)"
                + "OPTIONAL {"
                + "?resource sh:property ?property . "
                + "?property sh:order ?index . "
                + "?property sh:path ?predicate . "
                + "OPTIONAL { ?property iow:localName ?id . }"
                + "OPTIONAL {?property sh:name ?title . "
                + "FILTER (langMatches(lang(?title),?lang))}"
                + "OPTIONAL { ?property sh:description ?description . "
                + "FILTER (langMatches(lang(?description),?lang))"
                + "}"
                + "OPTIONAL { ?property sh:deactivated ?propertyDeactivated . }"
                + "OPTIONAL { ?property sh:datatype ?datatype . }"
                + "OPTIONAL { ?property sh:node ?shapeRef . BIND(afn:localname(?shapeRef) as ?shapeRefName) }"
                + "OPTIONAL { ?property sh:maxCount ?max . }"
                + "OPTIONAL { ?property sh:minCount ?min . }"
                + "OPTIONAL { ?property sh:pattern ?pattern . }"
                + "OPTIONAL { ?property sh:minLength ?minLength . }"
                + "OPTIONAL { ?property sh:maxLength ?maxLength . }"
                + "OPTIONAL { ?property skos:example ?example . }"
                + "OPTIONAL { ?property sh:in ?valueList . } "
                + "OPTIONAL { ?property dcam:memberOf ?schemeList . } "
                + "OPTIONAL { ?property iow:isResourceIdentifier ?idBoolean . }"
                + "BIND(afn:localname(?predicate) as ?predicateName)"
                + "}"
                + "}"
                + "}"
                + "ORDER BY ?resource ?index ?property";

        pss.setIri("modelPartGraph", modelID + "#HasPartGraph");

        if (lang != null) {
            pss.setLiteral("lang", lang);
        }

        pss.setCommandText(selectResources);
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();
            ResultSetPeekable pResults = ResultSetFactory.makePeekable(results);

            if (!pResults.hasNext()) {
                return null;
            }

            JsonObjectBuilder definitions = Json.createObjectBuilder();
            JsonObjectBuilder properties = Json.createObjectBuilder();

            JsonObjectBuilder predicate = Json.createObjectBuilder();

            HashSet<String> exampleSet = new HashSet<>();
            HashSet<String> requiredPredicates = new HashSet<>();

            JsonArrayBuilder exampleList = Json.createArrayBuilder();
            JsonObjectBuilder typeObject = Json.createObjectBuilder();

            boolean arrayType = false;

            int pIndex = 1;
            String predicateName = null;
            String predicateID = null;
            String className;

            int i = 0;
            while (pResults.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = pResults.nextSolution();

                if (!soln.contains("className")) {
                    return null;
                }

                String localClassName = soln.contains("localClassName") ? soln.getLiteral("localClassName").getString() : null;

                if (!soln.contains("classDeactivated") || (soln.contains("classDeactivated") && !soln.getLiteral("classDeactivated").getBoolean())) {

                    className = soln.getLiteral("className").getString();

                    if (soln.contains("property") && (!soln.contains("propertyDeactivated") || (soln.contains("propertyDeactivated") && !soln.getLiteral("propertyDeactivated").getBoolean()))) {

                        /* First run per predicate */

                        if (pIndex == 1) {

                            predicateID = soln.getResource("predicate").toString();

                            predicate.add("@id", predicateID);

                            predicateName = soln.getLiteral("predicateName").getString();

                            if (soln.contains("id")) {
                                predicateName = soln.getLiteral("id").getString();
                            }

                            if (soln.contains("title")) {
                                String title = soln.getLiteral("title").getString();
                                predicate.add("title", title);
                            }

                            if (soln.contains("min")) {
                                int min = soln.getLiteral("min").getInt();
                                if (min > 0) {
                                    requiredPredicates.add(predicateName);
                                }
                            }

                            if (soln.contains("description")) {
                                String description = soln.getLiteral("description").getString();
                                predicate.add("description", description);
                            }

                            if (soln.contains("valueList")) {
                                JsonArray valueList = getValueList(soln.getResource("resource").toString(), soln.getResource("property").toString());
                                if (valueList != null) {
                                    predicate.add("enum", valueList);
                                }
                            } else if (soln.contains("schemeList")) {
                                JsonArray schemeList = getSchemeValueList(soln.getResource("schemeList").toString());
                                if (schemeList != null) {
                                    predicate.add("enum", schemeList);
                                }
                            }

                            if (soln.contains("datatype")) {

                                String datatype = soln.getResource("datatype").toString();

                                if (soln.contains("idBoolean")) {
                                    Boolean isId = soln.getLiteral("idBoolean").getBoolean();
                                    if (isId) {
                                        predicate.add("@type", "@id");
                                    } else predicate.add("@type", datatype);
                                } else {
                                    predicate.add("@type", datatype);
                                }

                                String jsonDatatype = DATATYPE_MAP.get(datatype);

                                if (soln.contains("maxLength")) {
                                    predicate.add("maxLength", soln.getLiteral("maxLength").getInt());
                                }

                                if (soln.contains("minLength")) {
                                    predicate.add("minLength", soln.getLiteral("minLength").getInt());
                                }

                                if (soln.contains("pattern")) {
                                    predicate.add("pattern", soln.getLiteral("pattern").getString());
                                }

                                if (soln.contains("max") && soln.getLiteral("max").getInt() <= 1) {

                                    // predicate.add("maxItems",1);

                                    if (jsonDatatype != null) {
                                        if (jsonDatatype.equals("langString")) {
                                            predicate.add("type", "object");
                                            predicate.add("$ref", "#/definitions/langString");
                                        } else {
                                            predicate.add("type", jsonDatatype);
                                        }
                                    }

                                } else {

                                    if (soln.contains("max") && soln.getLiteral("max").getInt() > 1) {
                                        predicate.add("maxItems", soln.getLiteral("max").getInt());
                                    }

                                    if (soln.contains("min") && soln.getLiteral("min").getInt() > 0) {
                                        predicate.add("minItems", soln.getLiteral("min").getInt());
                                    }

                                    predicate.add("type", "array");

                                    arrayType = true;

                                    if (jsonDatatype != null) {

                                        if (jsonDatatype.equals("langString")) {
                                            typeObject.add("type", "object");
                                            typeObject.add("$ref", "#/definitions/langString");
                                        } else {
                                            typeObject.add("type", jsonDatatype);
                                        }

                                    }

                                }

                                if (FORMAT_MAP.containsKey(datatype)) {
                                    predicate.add("format", FORMAT_MAP.get(datatype));
                                }

                            } else {
                                if (soln.contains("shapeRefName")) {

                                    predicate.add("@type", "@id");

                                    String shapeRefName = soln.getLiteral("shapeRefName").getString();

                                    if (!soln.contains("max") || soln.getLiteral("max").getInt() > 1) {
                                        if (soln.contains("min")) {
                                            predicate.add("minItems", soln.getLiteral("min").getInt());
                                        }
                                        if (soln.contains("max")) {
                                            predicate.add("maxItems", soln.getLiteral("max").getInt());

                                        }
                                        predicate.add("type", "array");

                                        predicate.add("items", Json.createObjectBuilder().add("type", "object").add("$ref", "#/definitions/" + shapeRefName).build());
                                    } else {
                                        predicate.add("type", "object");
                                        predicate.add("$ref", "#/definitions/" + shapeRefName);
                                    }
                                }
                            }

                        }

                        /* Every run per predicate*/

                        if (soln.contains("example")) {
                            String example = soln.getLiteral("example").getString();
                            exampleSet.add(example);
                        }

                        if (pResults.hasNext() && className.equals(pResults.peek().getLiteral("className").getString()) && (pResults.peek().contains("predicate") && predicateID.equals(pResults.peek().getResource("predicate").toString()))) {

                            pIndex += 1;

                        } else {

                            /* Last run per class */

                            if (!exampleSet.isEmpty()) {

                                Iterator<String> iter = exampleSet.iterator();

                                while (iter.hasNext()) {
                                    String ex = iter.next();
                                    exampleList.add(ex);
                                }

                                predicate.add("example", exampleList.build());

                            }

                            if (arrayType) {
                                predicate.add("items", typeObject.build());
                            }

                            properties.add(predicateName, predicate.build());

                            predicate = Json.createObjectBuilder();
                            typeObject = Json.createObjectBuilder();
                            arrayType = false;
                            pIndex = 1;
                            exampleSet = new HashSet<>();
                            exampleList = Json.createArrayBuilder();
                        }
                    }

                    /* If not build props and requires */
                    if (!pResults.hasNext() || !className.equals(pResults.peek().getLiteral("className").getString())) {
                        predicate = Json.createObjectBuilder();
                        JsonObjectBuilder classDefinition = Json.createObjectBuilder();

                        if (soln.contains("classTitle")) {
                            classDefinition.add("title", soln.getLiteral("classTitle").getString());
                        }
                        classDefinition.add("type", "object");
                        if (soln.contains("targetClass")) {
                            classDefinition.add("@id", soln.getResource("targetClass").toString());
                        } else {
                            classDefinition.add("@id", soln.getResource("resource").toString());
                        }
                        if (soln.contains("classDescription")) {
                            classDefinition.add("description", soln.getLiteral("classDescription").getString());
                        }
                        if (soln.contains("minProperties")) {
                            classDefinition.add("minProperties", soln.getLiteral("minProperties").getInt());
                        }

                        if (soln.contains("maxProperties")) {
                            classDefinition.add("maxProperties", soln.getLiteral("maxProperties").getInt());
                        }

                        JsonObject classProps = properties.build();
                        if (!classProps.isEmpty()) classDefinition.add("properties", classProps);

                        JsonArrayBuilder required = Json.createArrayBuilder();

                        Iterator<String> ri = requiredPredicates.iterator();

                        while (ri.hasNext()) {
                            String ex = ri.next();
                            required.add(ex);
                        }

                        JsonArray reqArray = required.build();

                        if (!reqArray.isEmpty()) {
                            classDefinition.add("required", reqArray);
                        }

                        definitions.add(localClassName != null && localClassName.length() > 0 ? LDHelper.removeInvalidCharacters(localClassName) : className, classDefinition.build());

                        properties = Json.createObjectBuilder();
                        requiredPredicates = new HashSet<>();
                    }
                }
            }

            return definitions;

        }
    }

    public String newModelSchema(String modelID,
                                 String lang) {

        JsonObjectBuilder schema = Json.createObjectBuilder();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        String selectClass =
            "SELECT ?label ?description "
                + "WHERE { "
                + "GRAPH ?modelID { "
                + "?modelID rdfs:label ?label . "
                + "FILTER (langMatches(lang(?label),?lang))"
                + "OPTIONAL { ?modelID rdfs:comment ?description . "
                + "FILTER (langMatches(lang(?description),?lang))"
                + "}"
                + "} "
                + "} ";

        pss.setIri("modelID", modelID);
        if (lang != null) pss.setLiteral("lang", lang);
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        pss.setCommandText(selectClass);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.toString())) {

            ResultSet results = qexec.execSelect();

            if (!results.hasNext()) {
                logger.debug("No results from model: " + modelID);
                return null;
            }

            int i = 0;
            while (results.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = results.nextSolution();
                String title = soln.getLiteral("label").getString();

                logger.info("Building JSON Schema from " + title);

                if (soln.contains("description")) {
                    String description = soln.getLiteral("description").getString();
                    schema.add("description", description);
                }

                if (!modelID.endsWith("/") || !modelID.endsWith("#"))
                    schema.add("@id", modelID + "#");
                else
                    schema.add("@id", modelID);

                schema.add("title", title);

                Date modified = graphManager.modelContentModified(modelID);
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

                if (modified != null) {
                    String dateModified = format.format(modified);
                    schema.add("modified", dateModified);
                }

            }

            JsonObjectBuilder definitions = getClassDefinitions(modelID, lang);

            String modelRoot = getModelRoot(modelID);

            if (modelRoot != null) {
                JsonObjectBuilder modelProperties = Json.createObjectBuilder();
                modelProperties.add("$ref", "#/definitions/" + SplitIRI.localname(modelRoot));
                return createModelSchemaWithRoot(schema, modelProperties, definitions);
            }

            return createDefaultModelSchema(schema, definitions);
        }
    }

    public JsonObject getLangStringObject() {

        /*
         Regexp for validating language codes ?
         For example:
         "langString":{
            "type":"object",
            "patternProperties":{"^[a-z]{2,3}(?:-[A-Z]{2,3}(?:-[a-zA-Z]{4})?)?$":{"type":"string"}},
            "additionalProperties":false

        }*/

        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObjectBuilder add = Json.createObjectBuilder();
        builder.add("type", "object");
        builder.add("title", "Multilingual string");
        builder.add("description", "Object type for localized strings");
        builder.add("additionalProperties", add.add("type", "string").build());
        return builder.build();
    }

    public String newMultilingualModelSchema(String modelID) {

        JsonObjectBuilder schema = Json.createObjectBuilder();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        String selectClass =
            "SELECT ?lang ?title ?description "
                + "WHERE { "
                + "GRAPH ?modelID { "
                + "?modelID rdfs:label ?title . "
                + "BIND(lang(?title) as ?lang)"
                + "OPTIONAL { ?modelID rdfs:comment ?description . "
                + "FILTER(lang(?description)=lang(?title))"
                + "}"
                + "} "
                + "}";

        pss.setIri("modelID", modelID);
        pss.setNsPrefixes(LDHelper.PREFIX_MAP);

        pss.setCommandText(selectClass);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();

            if (!results.hasNext()) return null;

            JsonObjectBuilder titleObject = Json.createObjectBuilder();
            JsonObjectBuilder descriptionObject = null;

            int i = 0;
            while (results.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = results.nextSolution();
                String lang = soln.getLiteral("lang").getString();
                String title = soln.getLiteral("title").getString();

                logger.info("Building JSON Schema from " + title);

                titleObject.add(lang, title);
                if (soln.contains("description")) {
                    if (descriptionObject == null) descriptionObject = Json.createObjectBuilder();
                    String description = soln.getLiteral("description").getString();
                    descriptionObject.add(lang, description);
                }
            }

            schema.add("id", modelID + ".jschema");
            schema.add("title", titleObject);

            if (descriptionObject != null)
                schema.add("description", descriptionObject);

        }

        String selectResources =
            "SELECT ?resource ?property ?propertyDeactivated ?lang ?className ?localClassName ?classTitle ?classDeactivated ?classDescription ?predicate ?predicateName ?datatype ?shapeRef ?pattern ?shapeRefName ?minLength ?maxLength ?min ?max ?propertyLabel ?propertyDescription ?idBoolean "
                + "WHERE { "
                + "GRAPH ?modelPartGraph {"
                + "?model dcterms:hasPart ?resource . "
                + "}"
                + "GRAPH ?resource {"
                + "?resource sh:name ?classTitle . "
                + "BIND(lang(?classTitle) as ?lang)"
                + "OPTIONAL { ?resource sh:deactivated ?classDeactivated . }"
                + "OPTIONAL { ?resource iow:localName ?localClassName . } "
                + "OPTIONAL { ?resource sh:description ?classDescription . "
                + "FILTER(?lang=lang(?classDescription))"
                + "}"
                + "BIND(afn:localname(?resource) as ?className)"
                + "OPTIONAL { "
                + "?resource sh:property ?property . "
                + "?property sh:path ?predicate . "
                + "?property sh:name ?propertyLabel . "
                + "FILTER(?lang=lang(?propertyLabel))"
                + "OPTIONAL { ?property sh:description ?propertyDescription . "
                + "FILTER(?lang=lang(?propertyDescription))"
                + "}"
                + "OPTIONAL { ?property sh:datatype ?datatype . }"
                + "OPTIONAL { ?property sh:deactivated ?propertyDeactivated . }"
                + "OPTIONAL { ?property sh:node ?shapeRef . BIND(afn:localname(?shapeRef) as ?shapeRefName) }"
                + "OPTIONAL { ?property sh:minCount ?min . }"
                + "OPTIONAL { ?property sh:maxCount ?max . }"
                + "OPTIONAL { ?property sh:pattern ?pattern . }"
                + "OPTIONAL { ?property sh:minLenght ?minLength . }"
                + "OPTIONAL { ?property sh:maxLength ?maxLength . }"
                + "OPTIONAL { ?property iow:isResourceIdentifier ?idBoolean . }"
                + "BIND(afn:localname(?predicate) as ?predicateName)"
                + "}"
                + "}"
                + "} GROUP BY ?resource ?property ?propertyDeactivated ?lang ?className ?localClassName ?classTitle ?classDeactivated ?classDescription ?predicate ?predicateName ?datatype ?shapeRef ?shapeRefName ?min ?max ?minLength ?maxLength ?propertyLabel ?propertyDescription ?idBoolean ?pattern "
                + "ORDER BY ?resource ?property ?lang";

        pss.setIri("modelPartGraph", modelID + "#HasPartGraph");
        pss.setCommandText(selectResources);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointServices.getCoreSparqlAddress(), pss.asQuery())) {

            ResultSet results = qexec.execSelect();
            ResultSetPeekable pResults = ResultSetFactory.makePeekable(results);

            if (!pResults.hasNext()) {
                return null;
            }

            JsonObjectBuilder definitions = Json.createObjectBuilder();

            definitions.add("langString", getLangStringObject());

            JsonObjectBuilder properties = Json.createObjectBuilder();
            JsonArrayBuilder required = Json.createArrayBuilder();

            String propertyID;
            JsonObjectBuilder classTitleObject = Json.createObjectBuilder();
            JsonObjectBuilder classDescriptionObject = Json.createObjectBuilder();
            JsonObjectBuilder propertyTitleObject = Json.createObjectBuilder();
            JsonObjectBuilder propertyDescriptionObject = Json.createObjectBuilder();

            int i = 0;
            while (pResults.hasNext()) {
                if (++i == Integer.MAX_VALUE) {
                    throw new RuntimeException("Too many items for iteration");
                }
                QuerySolution soln = pResults.nextSolution();

                if (!soln.contains("className")) return null;

                if (!soln.contains("classDeactivated") || (soln.contains("classDeactivated") && !soln.getLiteral("classDeactivated").getBoolean())) {

                    String className = soln.getLiteral("className").getString();
                    String localClassName = soln.contains("localClassName") ? soln.getLiteral("localClassName").getString() : null;

                    if (soln.contains("property") && (!soln.contains("propertyDeactivated") || (soln.contains("propertyDeactivated") && !soln.getLiteral("propertyDeactivated").getBoolean()))) {

                        propertyID = soln.getResource("property").toString();
                        String lang = soln.getLiteral("lang").getString();
                        String predicateName = soln.getLiteral("predicateName").getString();
                        String title = soln.getLiteral("propertyLabel").getString();
                        String classTitle = soln.getLiteral("classTitle").getString();

                        String propertyDescription = null;
                        String classDescription = null;

                        if (soln.contains("propertyDescription")) {
                            propertyDescription = soln.getLiteral("propertyDescription").getString();
                        }

                        if (soln.contains("classDescription")) {
                            classDescription = soln.getLiteral("classDescription").getString();
                        }

                        JsonObjectBuilder propertyBuilder = Json.createObjectBuilder();

                        /* Build multilingual objects */

                        propertyTitleObject.add(lang, title);

                        if (propertyDescription != null)
                            propertyDescriptionObject.add(lang, propertyDescription);

                        classTitleObject.add(lang, classTitle);

                        if (classDescription != null)
                            classDescriptionObject.add(lang, classDescription);

                        /* If property is iterated the last time build the rest */
                        if (pResults.hasNext() && pResults.peek().contains("property") && !propertyID.equals(pResults.peek().getResource("property").toString()) || !pResults.hasNext()) {

                            propertyBuilder.add("title", propertyTitleObject.build());

                            JsonObject propertyDescriptionJSON = propertyDescriptionObject.build();
                            if (!propertyDescriptionJSON.isEmpty()) {
                                propertyBuilder.add("description", propertyDescriptionJSON);
                            }

                            if (soln.contains("min")) {
                                int min = soln.getLiteral("min").getInt();
                                if (min > 0) {
                                    required.add(predicateName);
                                }
                            }

                            if (soln.contains("datatype")) {

                                String datatype = soln.getResource("datatype").toString();

                                if (soln.contains("idBoolean")) {
                                    Boolean isId = soln.getLiteral("idBoolean").getBoolean();
                                    if (isId) {
                                        propertyBuilder.add("@type", "@id");
                                    } else propertyBuilder.add("@type", datatype);
                                } else {
                                    propertyBuilder.add("@type", datatype);
                                }

                                String jsonDatatype = DATATYPE_MAP.get(datatype);

                                if (soln.contains("maxLength")) {
                                    propertyBuilder.add("maxLength", soln.getLiteral("maxLength").getInt());
                                }

                                if (soln.contains("minLength")) {
                                    propertyBuilder.add("minLength", soln.getLiteral("minLength").getInt());
                                }

                                if (soln.contains("pattern")) {
                                    propertyBuilder.add("pattern", soln.getLiteral("pattern").getString());
                                }

                                if (soln.contains("max") && soln.getLiteral("max").getInt() <= 1) {

                                    // propertyBuilder.add("maxItems",1);

                                    if (jsonDatatype != null) {
                                        if (jsonDatatype.equals("langString")) {
                                            propertyBuilder.add("type", "object");
                                            propertyBuilder.add("$ref", "#/definitions/langString");
                                        } else
                                            propertyBuilder.add("type", jsonDatatype);
                                    }

                                } else {

                                    if (soln.contains("max") && soln.getLiteral("max").getInt() > 1) {
                                        propertyBuilder.add("maxItems", soln.getLiteral("max").getInt());
                                    }

                                    if (soln.contains("min") && soln.getLiteral("min").getInt() > 0) {
                                        propertyBuilder.add("minItems", soln.getLiteral("min").getInt());
                                    }

                                    propertyBuilder.add("type", "array");

                                    if (jsonDatatype != null) {

                                        JsonObjectBuilder typeObject = Json.createObjectBuilder();

                                        if (jsonDatatype.equals("langString")) {
                                            typeObject.add("type", "object");
                                            typeObject.add("$ref", "#/definitions/langString");
                                        } else {
                                            typeObject.add("type", jsonDatatype);
                                        }

                                        propertyBuilder.add("items", typeObject.build());
                                    }

                                }

                                if (FORMAT_MAP.containsKey(datatype)) {
                                    propertyBuilder.add("format", FORMAT_MAP.get(datatype));
                                }

                            } else {
                                if (soln.contains("shapeRefName")) {

                                    propertyBuilder.add("@type", "@id");

                                    String shapeRefName = soln.getLiteral("shapeRefName").getString();

                                    if (!soln.contains("max") || soln.getLiteral("max").getInt() > 1) {
                                        if (soln.contains("min")) {
                                            propertyBuilder.add("minItems", soln.getLiteral("min").getInt());
                                        }
                                        if (soln.contains("max")) {
                                            propertyBuilder.add("maxItems", soln.getLiteral("max").getInt());
                                        }
                                        propertyBuilder.add("type", "array");
                                        propertyBuilder.add("items", Json.createObjectBuilder().add("type", "object").add("$ref", "#/definitions/" + shapeRefName).build());
                                    } else {
                                        propertyBuilder.add("type", "object");
                                        propertyBuilder.add("$ref", "#/definitions/" + shapeRefName);
                                    }
                                }
                            }

                            properties.add(predicateName, propertyBuilder.build());

                            propertyTitleObject = Json.createObjectBuilder();
                            propertyDescriptionObject = Json.createObjectBuilder();
                            propertyBuilder = Json.createObjectBuilder();

                        }
                    }

                    /* IF the class is iterated last time */

                    /* If not build props and requires */
                    if (!pResults.hasNext() || !className.equals(pResults.peek().getLiteral("className").toString())) {
                        JsonObjectBuilder classDefinition = Json.createObjectBuilder();
                        classDefinition.add("title", classTitleObject.build());
                        classDefinition.add("type", "object");
                        JsonObject classDescriptionJSON = classDescriptionObject.build();
                        if (!classDescriptionJSON.isEmpty()) {
                            classDefinition.add("description", classDescriptionJSON);
                        }
                        JsonObject classProps = properties.build();
                        if (!classProps.isEmpty()) classDefinition.add("properties", classProps);
                        JsonArray reqArray = required.build();
                        if (!reqArray.isEmpty()) classDefinition.add("required", reqArray);
                        definitions.add(localClassName != null && localClassName.length() > 0 ? LDHelper.removeInvalidCharacters(localClassName) : className, classDefinition.build());
                        properties = Json.createObjectBuilder();
                        required = Json.createArrayBuilder();

                        classTitleObject = Json.createObjectBuilder();
                        classDescriptionObject = Json.createObjectBuilder();

                    }
                }

            }

            return createV5ModelSchema(schema, definitions);
        }
    }

    private String createV5ModelSchema(JsonObjectBuilder schema,
                                       JsonObjectBuilder definitions) {

        schema.add("$schema", "http://tietomallit.suomi.fi/api/draft05jsonld.json");

        schema.add("type", "object");

        if (definitions != null) {
            definitions.add("langString", getLangStringObject());
            schema.add("definitions", definitions.build());
        }

        return jsonObjectToPrettyString(schema.build());
    }

    private String createDefaultModelSchema(JsonObjectBuilder schema,
                                            JsonObjectBuilder definitions) {

        schema.add("$schema", "http://json-schema.org/draft-04/schema#");

        schema.add("type", "object");

        if (definitions != null) {
            definitions.add("langString", getLangStringObject());
            schema.add("definitions", definitions.build());
        }

        return jsonObjectToPrettyString(schema.build());
    }

    private String createModelSchemaWithRoot(JsonObjectBuilder schema,
                                             JsonObjectBuilder properties,
                                             JsonObjectBuilder definitions) {

        schema.add("$schema", "http://json-schema.org/draft-04/schema#");

        schema.add("type", "object");
        schema.add("allOf", Json.createArrayBuilder().add(properties.build()).build());

        if (definitions != null) {
            definitions.add("langString", getLangStringObject());
            schema.add("definitions", definitions.build());
        }

        return jsonObjectToPrettyString(schema.build());
    }

    private String createDummySchema(JsonObjectBuilder schema,
                                     JsonObjectBuilder properties,
                                     JsonArrayBuilder required) {

        /* TODO: Create basic dummy schema without properties */

        schema.add("$schema", "http://json-schema.org/draft-04/schema#");

        schema.add("properties", properties.build());
        JsonArray reqArray = required.build();
        if (!reqArray.isEmpty()) {
            schema.add("required", reqArray);
        }

        return jsonObjectToPrettyString(schema.build());
    }

    private String createDefaultSchema(JsonObjectBuilder schema,
                                       JsonObjectBuilder properties,
                                       JsonArrayBuilder required) {

        schema.add("$schema", "http://json-schema.org/draft-04/schema#");

        schema.add("type", "object");
        JsonObject classProps = properties.build();
        if (!classProps.isEmpty()) schema.add("properties", classProps);
        JsonArray reqArray = required.build();
        if (!reqArray.isEmpty()) {
            schema.add("required", reqArray);
        }

        return jsonObjectToPrettyString(schema.build());
    }
}
