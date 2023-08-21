/*
 * Licensed under the European Union Public Licence (EUPL) V.1.1
 */
package fi.vm.yti.datamodel.api.utils;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.yti.datamodel.api.config.UriProperties;

/**
 * @author jkesanie
 */
@Service
public class Frames {

    public LinkedHashMap<String, Object> jsonLdKeys;
    public LinkedHashMap<String, Object> inScheme;
    public LinkedHashMap<String, Object> isDefinedBy;
    public LinkedHashMap<String, Object> label;
    public LinkedHashMap<String, Object> name;
    public LinkedHashMap<String, Object> title;
    public LinkedHashMap<String, Object> modified;
    public LinkedHashMap<String, Object> created;
    public LinkedHashMap<String, Object> contentModified;
    public LinkedHashMap<String, Object> statusModified;
    public LinkedHashMap<String, Object> contributor;
    public LinkedHashMap<String, Object> isPartOf;
    public LinkedHashMap<String, Object> contributorID;
    public LinkedHashMap<String, Object> isPartOfID;
    public LinkedHashMap<String, Object> comment;
    public LinkedHashMap<String, Object> range;
    public LinkedHashMap<String, Object> prefLabel;
    public LinkedHashMap<String, Object> subject;
    public LinkedHashMap<String, Object> description;
    public LinkedHashMap<String, Object> preferredXMLNamespaceName;
    public LinkedHashMap<String, Object> preferredXMLNamespacePrefix;
    public LinkedHashMap<String, Object> shDescription;
    public LinkedHashMap<String, Object> path;
    public LinkedHashMap<String, Object> property;
    public LinkedHashMap<String, Object> versionInfo;
    public LinkedHashMap<String, Object> useContext;
    public LinkedHashMap<String, Object> coreContext;
    public LinkedHashMap<String, Object> vocabularyContext;
    public LinkedHashMap<String, Object> conceptContext;
    public LinkedHashMap<String, Object> classificationContext;
    public LinkedHashMap<String, Object> organizationContext;
    public LinkedHashMap<String, Object> referenceDataServerContext;
    public LinkedHashMap<String, Object> referenceDataContext;
    public LinkedHashMap<String, Object> referenceDataCodeContext;
    public LinkedHashMap<String, Object> predicateContext;
    public LinkedHashMap<String, Object> propertyContext;
    public LinkedHashMap<String, Object> namespaceContext;
    public LinkedHashMap<String, Object> classContext;
    public LinkedHashMap<String, Object> esClassContext;
    public LinkedHashMap<String, Object> esModelContext;
    public LinkedHashMap<String, Object> esPredicateContext;
    public LinkedHashMap<String, Object> modelContext;
    public LinkedHashMap<String, Object> modelPositionContext;
    public LinkedHashMap<String, Object> conceptFrame;
    public LinkedHashMap<String, Object> esClassFrame;
    public LinkedHashMap<String, Object> esModelFrame;
    public LinkedHashMap<String, Object> esPredicateFrame;
    public LinkedHashMap<String, Object> origClassContext;
    public LinkedHashMap<String, Object> origClassFrame;
    public LinkedHashMap<String, Object> libraryType;
    public LinkedHashMap<String, Object> profileType;
    public LinkedHashMap<String, Object> externalClassType;
    public LinkedHashMap<String, Object> classType;
    public LinkedHashMap<String, Object> shapeType;
    public LinkedHashMap<String, Object> attributeType;
    public LinkedHashMap<String, Object> associationType;
    public LinkedHashMap<String, Object> annotationType;
    public LinkedHashMap<String, Object> languageSet;
    public LinkedHashMap<String, Object> value;


    @Autowired
    public Frames(UriProperties uriProperties) {
        String uriHost = uriProperties.getHost();

        jsonLdKeys = new LinkedHashMap<String, Object>() {
            {
                put("id", "@id");
                put("type", "@type");
            }
        };

        inScheme = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2004/02/skos/core#inScheme");
                put("@type", "@id");
            }

        };

        libraryType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/ws-mmi-dc/terms/MetadataVocabulary");
            }
        };

        profileType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/ws-mmi-dc/terms/DCAP");
            }
        };

        classType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2000/01/rdf-schema#Class");
            }

        };

        externalClassType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://" + uriHost + "/datamodel/ns/iow#ExternalClass");
            }
        };

        shapeType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/ns/shacl#NodeShape");
            }
        };

        attributeType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2002/07/owl#DatatypeProperty");
            }
        };

        associationType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2002/07/owl#ObjectProperty");
            }
        };

        annotationType = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2002/07/owl#AnnotationProperty");
            }
        };

        subject = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/subject");
                put("@type", "@id");
            }

        };

        comment = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2000/01/rdf-schema#comment");
                put("@container", "@language");
            }

        };

        description = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/description");
                put("@container", "@language");
            }

        };

        shDescription = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/ns/shacl#description");
                put("@container", "@language");
            }
        };

        path = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/ns/shacl#path");
                put("@type", "@id");
            }

        };

        property = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/ns/shacl#property");
                put("@type", "@id");
            }
        };

        isDefinedBy = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
                put("@type", "@id");
            }
        };

        label = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2000/01/rdf-schema#label");
                put("@container", "@language");
            }
        };

        title = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/title");
                put("@container", "@language");
            }
        };

        modified = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/modified");
                put("@type", "http://www.w3.org/2001/XMLSchema#dateTime");
            }
        };

        created = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/created");
                put("@type", "http://www.w3.org/2001/XMLSchema#dateTime");
            }
        };

        contentModified = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://" + uriHost + "/datamodel/ns/iow#contentModified");
                put("@type", "http://www.w3.org/2001/XMLSchema#dateTime");
            }
        };

        statusModified = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://" + uriHost + "/datamodel/ns/iow#statusModified");
                put("@type", "http://www.w3.org/2001/XMLSchema#dateTime");
            }
        };


        isPartOf = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/isPartOf");
                put("@type", "@id");
                put("@container", "@set");
            }
        };

        languageSet = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/language");
                put("@container", "@set");
            }
        };

        contributor = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/contributor");
                put("@type", "@id");
                put("@container", "@set");
            }
        };

        isPartOfID = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/isPartOf");
            }
        };

        contributorID = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/dc/terms/contributor");
            }
        };

        useContext = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://" + uriHost + "/datamodel/ns/iow#useContext");
            }
        };

        versionInfo = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2002/07/owl#versionInfo");
            }
        };

        prefLabel = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2004/02/skos/core#prefLabel");
                put("@container", "@language");
            }
        };

        coreContext = new LinkedHashMap<String, Object>() {
            {
                put("comment", comment);
                put("created", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/created");
                        put("@type", "http://www.w3.org/2001/XMLSchema#dateTime");
                    }
                });
                put("definition", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2004/02/skos/core#definition");
                        put("@container", "@language");
                    }
                });
                put("foaf", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://xmlns.com/foaf/0.1/");
                        put("@type", "@id");
                    }
                });
                put("hasPart", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/hasPart");
                        put("@type", "@id");
                    }
                });
                put("contributor", contributor);
                put("homepage", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/homepage");
                    }
                });
                put("identifier", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/identifier");
                    }
                });
                put("imports", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2002/07/owl#imports");
                        put("@type", "@id");
                    }
                });
                put("isDefinedBy", isDefinedBy);
                put("isPartOf", isPartOf);
                put("label", label);
                put("modified", modified);
                put("nodeKind", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#nodeKind");
                        put("@type", "@id");
                    }
                });
                put("prefLabel", prefLabel);
                put("title", title);
                put("prov", "http://www.w3.org/ns/prov#");
                put("versionInfo", versionInfo);
                put("editorialNote", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2004/02/skos/core#editorialNote");
                        put("@container", "@language");
                    }
                });
                put("localName", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#localName");
                    }
                });
            }
        };

        vocabularyContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("description", description);
            }
        };

        conceptContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("inScheme", inScheme);
            }
        };

        classificationContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("id", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#order");
                    }
                });
            }
        };

        organizationContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("description", description);
            }
        };

        referenceDataServerContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("description", description);
            }
        };

        referenceDataContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("creator", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/creator");
                    }
                });
                put("description", description);
            }
        };

        referenceDataCodeContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
            }
        };

        preferredXMLNamespaceName = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/ws-mmi-dc/terms/preferredXMLNamespaceName");
            }
        };

        preferredXMLNamespacePrefix = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://purl.org/ws-mmi-dc/terms/preferredXMLNamespacePrefix");
            }
        };

        range = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/2000/01/rdf-schema#range");
                put("@type", "@id");
            }
        };

        name = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/ns/shacl#name");
                put("@container", "@language");
            }
        };

        value = new LinkedHashMap<String, Object>() {
            {
                put("@id", "http://www.w3.org/ns/shacl#value");
                put("@container", "@language");
            }
        };

        predicateContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                putAll(conceptContext);
                put("range", range);
                put("subPropertyOf", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
                        put("@type", "@id");
                    }
                });
                put("equivalentProperty", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2002/07/owl#equivalentProperty");
                        put("@type", "@id");
                    }
                });
                put("datatype", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#datatype");
                        put("@type", "@id");
                    }
                });
                put("subject", subject);
                put("value", value);
                put("property", property);
                put("path", path);
                put("node", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#node");
                        put("@type", "@id");
                    }
                });
                put("label", label);
            }
        };

        propertyContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                putAll(predicateContext);
                putAll(referenceDataContext);
                put("order", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#order");
                    }
                });
                put("example", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2004/02/skos/core#example");
                    }
                });
                put("defaultValue", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#defaultValue");
                    }
                });
                put("maxCount", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#maxCount");
                    }
                });
                put("minCount", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#minCount");
                    }
                });
                put("maxLength", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#maxLength");
                    }
                });
                put("minLength", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#minLength");
                    }
                });
                put("inValues", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#in");
                        put("@container", "@list");
                    }
                });
                put("hasValue", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#hasValue");
                    }
                });
                put("pattern", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#pattern");
                    }
                });
                put("type", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/type");
                        put("@type", "@id");
                    }
                });
                put("node", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#node");
                        put("@type", "@id");
                    }
                });
                put("path", path);
                put("classIn", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#classIn");
                        put("@type", "@id");
                    }
                });
                put("memberOf", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/dcam/memberOf");
                    }
                });
                put("stem", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#stem");
                        put("@type", "@id");
                    }
                });
                put("languageIn", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#languageIn");
                        put("@container", "@list");
                    }
                });
                put("isResourceIdentifier", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#isResourceIdentifier");
                    }
                });
                put("uniqueLang", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#uniqueLang");
                    }
                });
                put("isXmlWrapper", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#isXmlWrapper");
                    }
                });
                put("isXmlAttribute", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#isXmlAttribute");
                    }
                });
                put("name", name);
                put("description", shDescription);
                put("readOnlyValue", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://schema.org/readonlyValue");
                    }
                });
                put("value", value);
                put("property", property);
            }
        };

        classContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                putAll(propertyContext);
                putAll(conceptContext);
                put("abstract", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#abstract");
                    }
                });
                put("property", property);
                put("targetClass", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#targetClass");
                        put("@type", "@id");
                    }
                });
                put("subClassOf", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2000/01/rdf-schema#subClassOf");
                        put("@type", "@id");
                    }
                });
                put("equivalentClass", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/2002/07/owl#equivalentClass");
                        put("@type", "@id");
                    }
                });
                put("constraint", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#constraint");
                        put("@type", "@id");
                    }
                });
                put("orCond", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#or");
                        put("@container", "@list");
                    }
                });
                put("andCond", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#and");
                        put("@container", "@list");
                    }
                });
                put("notCond", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#not");
                        put("@container", "@list");
                    }
                });
                put("minProperties", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#minProperties");
                    }
                });
                put("maxProperties", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#maxProperties");
                    }
                });
                put("subject", subject);
            }
        };

        namespaceContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("preferredXMLNamespaceName", preferredXMLNamespaceName);
                put("preferredXMLNamespacePrefix", preferredXMLNamespacePrefix);
            }
        };

        modelContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                putAll(namespaceContext);
                putAll(referenceDataContext);
                putAll(vocabularyContext);
                put("rootResource", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://rdfs.org/ns/void#rootResource");
                        put("@type", "@id");
                    }
                });
                put("references", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/references");
                        put("@type", "@id");
                    }
                });
                put("requires", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/requires");
                        put("@type", "@id");
                    }
                });
                put("relations", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/relation");
                        put("@container", "@list");
                    }
                });
                put("codeLists", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#codeLists");
                        put("@type", "@id");
                    }
                });
                put("language", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://purl.org/dc/terms/language");
                        put("@container", "@list");
                    }
                });
            }
        };

        modelPositionContext = new LinkedHashMap<String, Object>() {
            {
                putAll(coreContext);
                put("path", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#path");
                        put("@type", "@id");
                    }
                });

                put("property", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://www.w3.org/ns/shacl#property");
                        put("@type", "@id");
                    }
                });
                put("pointXY", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#pointXY");
                    }
                });
                put("vertexXY", new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://" + uriHost + "/datamodel/ns/iow#vertexXY");
                        put("@container", "@list");
                    }
                });
            }
        };

        conceptFrame = new LinkedHashMap<String, Object>() {
            {
                put("@context", conceptContext);
            }
        };

        esModelContext = new LinkedHashMap<String, Object>() {
            {
                put("profile",profileType);
                put("library",libraryType);
                put("label", label);
                put("comment", comment);
                put("modified", modified);
                put("created", created);
                put("contentModified", contentModified);
                put("statusModified", statusModified);
                put("contributor", contributorID);
                put("isPartOf", isPartOfID);
                put("useContext", useContext);
                put("status", versionInfo);
                put("prefix", preferredXMLNamespacePrefix);
                put("namespace", preferredXMLNamespaceName);
                put("language", languageSet);
                putAll(jsonLdKeys);
            }
        };

        esModelFrame = new LinkedHashMap<String, Object>() {
            {
                put("@context", esModelContext);
                put("contributor", new LinkedHashMap<String, Object>());
                put("isPartOf", new LinkedHashMap<String, Object>());
            }
        };

        esClassContext = new LinkedHashMap<String, Object>() {
            {
                put("externalClass",externalClassType);
                put("class",classType);
                put("shape",shapeType);
                put("label", name);
                put("comment", shDescription);
                put("modified", modified);
                put("created", created);
                put("status", versionInfo);
                put("statusModified", statusModified);
                put("isDefinedBy", isDefinedBy);
                putAll(jsonLdKeys);
            }
        };

        esClassFrame = new LinkedHashMap<String, Object>() {
            {
                put("@context", esClassContext);
                put("isDefinedBy", new LinkedHashMap<String, Object>());
            }
        };

        origClassContext = new LinkedHashMap<String, Object>() {
            {
                put("name", name);
                put("description", shDescription);
                put("modified", modified);
                put("created", created);
                put("status", versionInfo);
                put("isDefinedBy", isDefinedBy);
                putAll(jsonLdKeys);
            }
        };

        origClassFrame = new LinkedHashMap<String, Object>() {
            {
                put("@context", origClassContext);
                put("isDefinedBy", new LinkedHashMap<String, Object>());
            }
        };

        esPredicateContext = new LinkedHashMap<String, Object>() {
            {
                put("attribute",attributeType);
                put("association",associationType);
                put("annotation",annotationType);
                put("label", label);
                put("comment", comment);
                put("modified", modified);
                put("created", created);
                put("range", range);
                put("status", versionInfo);
                put("statusModified", statusModified);
                put("isDefinedBy", isDefinedBy);
                putAll(jsonLdKeys);
            }
        };

        esPredicateFrame = new LinkedHashMap<String, Object>() {
            {
                put("@context", esPredicateContext);
                put("isDefinedBy", new LinkedHashMap<String, Object>());
            }
        };
    }
}
