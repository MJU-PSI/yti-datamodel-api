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

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Component
@Path("v1/codeValues")
@Tag(name = "Codes")
public class Codes {

    private final EndpointServices endpointServices;
    private final JerseyResponseManager jerseyResponseManager;
    private final ApplicationProperties applicationProperties;
    private final CodeSchemeManager codeSchemeManager;
    private final UriProperties uriProperties;

    @Autowired
    Codes(EndpointServices endpointServices,
          JerseyResponseManager jerseyResponseManager,
          ApplicationProperties applicationProperties,
          CodeSchemeManager codeSchemeManager,
          UriProperties uriProperties) {
        this.endpointServices = endpointServices;
        this.jerseyResponseManager = jerseyResponseManager;
        this.applicationProperties = applicationProperties;
        this.codeSchemeManager = codeSchemeManager;
        this.uriProperties = uriProperties;
    }

    @GET
    @Produces("application/ld+json")
    @Operation(description = "Get code values with id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "codes"),
        @ApiResponse(responseCode = "406", description = "code not defined"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getCodes(
        @Parameter(description = "uri", required = true)
        @QueryParam("uri") String uri,
        @Parameter(description = "forced update")
        @QueryParam("force") boolean force) {
        if (uri.startsWith(this.uriProperties.getScheme() + "://" + this.uriProperties.getHost())) {
            LocalCodeServer codeServer = new LocalCodeServer("http://local_code_server", applicationProperties.getDefaultLocalCodeServerAPI(), endpointServices, codeSchemeManager, uriProperties);
            codeServer.updateCodes(uri, force);
        } else {
            return jerseyResponseManager.invalidParameter();
        }

        Model codeModel = codeSchemeManager.getSchemeGraph(uri);

        // If codeValues are empty for example are codes are DRAFT but scheme is VALID
        if (codeModel == null) {
            codeModel = ModelFactory.createDefaultModel();
        }

        return jerseyResponseManager.okModel(codeModel);

    }

    @PUT
    @Operation(description = "Get code values with id", tags = { "Codes" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "codes"),
        @ApiResponse(responseCode = "406", description = "code not defined"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Response updateCodes(
        @Parameter(description = "uri", required = true)
        @QueryParam("uri") String uri) {

        ResponseBuilder rb;

        if (uri.startsWith(this.uriProperties.getScheme() + "://" + this.uriProperties.getHost())) {
            LocalCodeServer codeServer = new LocalCodeServer("http://local_code_server", applicationProperties.getDefaultLocalCodeServerAPI(), endpointServices, codeSchemeManager, uriProperties);
        }  else {
            return jerseyResponseManager.invalidParameter();
        }

        return jerseyResponseManager.ok();
    }
}
