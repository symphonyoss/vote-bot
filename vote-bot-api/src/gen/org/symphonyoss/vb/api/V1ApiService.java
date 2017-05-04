package org.symphonyoss.vb.api;

import org.symphonyoss.vb.api.*;
import org.symphonyoss.vb.api.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.symphonyoss.vb.api.model.ErrorDTO;
import org.symphonyoss.vb.api.model.VoteProposalV1;

import java.util.List;
import org.symphonyoss.vb.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

public abstract class V1ApiService {
    public abstract Response v1ProposalIdGet(String id,SecurityContext securityContext) throws NotFoundException;
}
