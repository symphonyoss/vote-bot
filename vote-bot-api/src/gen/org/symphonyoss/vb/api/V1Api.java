package org.symphonyoss.vb.api;

import org.symphonyoss.vb.api.model.*;
import org.symphonyoss.vb.api.V1ApiService;
import org.symphonyoss.vb.api.factories.V1ApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.symphonyoss.vb.api.model.ErrorDTO;
import org.symphonyoss.vb.api.model.VoteProposalV1;

import java.util.List;
import org.symphonyoss.vb.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/v1")

@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the v1 API")

public class V1Api  {
   private final V1ApiService delegate = V1ApiServiceFactory.getV1Api();

    @GET
    @Path("/proposal/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retreive vote proposal by VoteId", notes = "", response = VoteProposalV1.class, tags={ "Proposal", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = VoteProposalV1.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad request.", response = VoteProposalV1.class),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden: Certificate authentication is not allowed for the requested user.", response = VoteProposalV1.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error, see response body for further details.", response = VoteProposalV1.class) })
    public Response v1ProposalIdGet(@ApiParam(value = "",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.v1ProposalIdGet(id,securityContext);
    }
}
