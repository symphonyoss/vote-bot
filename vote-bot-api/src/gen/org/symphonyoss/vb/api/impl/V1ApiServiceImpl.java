package org.symphonyoss.vb.api.impl;

import org.symphonyoss.vb.api.*;
import org.symphonyoss.vb.api.model.*;

import org.symphonyoss.vb.api.model.ErrorDTO;
import org.symphonyoss.vb.api.model.VoteProposalV1;

import java.util.List;
import org.symphonyoss.vb.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

public class V1ApiServiceImpl extends V1ApiService {
    @Override
    public Response v1ProposalIdGet(String id, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
