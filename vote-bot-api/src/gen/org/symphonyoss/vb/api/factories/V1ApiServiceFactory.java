package org.symphonyoss.vb.api.factories;

import org.symphonyoss.vb.api.V1ApiService;
import org.symphonyoss.vb.api.impl.V1ApiServiceImpl;


public class V1ApiServiceFactory {
    private final static V1ApiService service = new V1ApiServiceImpl();

    public static V1ApiService getV1Api() {
        return service;
    }
}
