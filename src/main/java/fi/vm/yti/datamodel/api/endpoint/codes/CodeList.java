/*
 * Licensed under the European Union Public Licence (EUPL) V.1.1
 */
package fi.vm.yti.datamodel.api.endpoint.codes;

import fi.vm.yti.datamodel.api.config.ApplicationProperties;
import fi.vm.yti.datamodel.api.config.UriProperties;
import fi.vm.yti.datamodel.api.model.LocalCodeServer;
import fi.vm.yti.datamodel.api.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component
@Path("v1/codeList")
@Tag(name = "Codes")
public class CodeList {

    private final EndpointServices endpointServices;
    private final ApplicationProperties applicationProperties;
    private final JerseyResponseManager jerseyResponseManager;
    private final CodeSchemeManager codeSchemeManager;
    private final UriProperties uriProperties;

    @Autowired
    CodeList(EndpointServices endpointServices,
             ApplicationProperties applicationProperties,
             CodeSchemeManager codeSchemeManager,
             JerseyResponseManager jerseyResponseManager,
             UriProperties uriProperties) {
        this.endpointServices = endpointServices;
        this.applicationProperties = applicationProperties;
        this.jerseyResponseManager = jerseyResponseManager;
        this.codeSchemeManager = codeSchemeManager;
        this.uriProperties = uriProperties;
    }

    @GET
    @Produces("application/ld+json")
    @Operation(description = "Get list of codelists from code server")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Codelists"),
        @ApiResponse(responseCode = "406", description = "Term not defined"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getCodeList(
        @Parameter(description = "Codeserver uri", required = true) @QueryParam("uri") String uri) {

        if(uri==null || uri.isEmpty()) {
            return jerseyResponseManager.invalidParameter();
        }

        if (uri.startsWith("http://local_code_server")) {
            LocalCodeServer localCodeServer = new LocalCodeServer("http://local_code_server", applicationProperties.getDefaultLocalCodeServerAPI(), endpointServices, codeSchemeManager, uriProperties);
            localCodeServer.updateCodeSchemeList();
        } else {
            return jerseyResponseManager.invalidParameter();
        }

        Model codeListModel = codeSchemeManager.getSchemeGraph(uri);

        if (codeListModel == null) {
            codeListModel = ModelFactory.createDefaultModel();
        }

        return jerseyResponseManager.okModel(codeListModel);
    }
}
