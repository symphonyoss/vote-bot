/*
 *  Copyright 2017 The Symphony Software Foundation
 *
 *  Licensed to The Symphony Software Foundation (SSF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.symphonyoss.vb.api;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;

import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.time.LocalDate;
import java.util.List;

@Provider
public class LocalDateProvider extends PerRequestTypeInjectableProvider<QueryParam, LocalDate> {
    private final UriInfo uriInfo;

    public LocalDateProvider(@Context UriInfo uriInfo) {
        super(LocalDate.class);
        this.uriInfo = uriInfo;
    }

    @Override
    public Injectable<LocalDate> getInjectable(final ComponentContext cc, final QueryParam a) {
        return new Injectable<LocalDate>() {
            @Override
            public LocalDate getValue() {
                final List<String> values = uriInfo.getQueryParameters().get(a.value());

                if (values == null || values.isEmpty())
                    return null;
                if (values.size() > 1) {
                    throw new WebApplicationException(Response.status(Status.BAD_REQUEST).
                            entity(a.value() + " cannot contain multiple values").build());
                }

                return LocalDate.parse(values.get(0));
            }
        };
    }
}