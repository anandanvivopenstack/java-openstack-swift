package ch.iterate.openstack.swift.handler;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.Region;

public class ContainerResponseHandler implements ResponseHandler<List<Container>> {

    private Region region;

    public ContainerResponseHandler(final Region region) {
        this.region = region;
    }

    public List<Container> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            final StrTokenizer tokenize = new StrTokenizer(EntityUtils.toString(response.getEntity()));
            tokenize.setDelimiterString("\n");
            final String[] containers = tokenize.getTokenArray();
            ArrayList<Container> list = new ArrayList<Container>();
            for(String container : containers) {
                list.add(new Container(region, container));
            }
            return list;
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            return new ArrayList<Container>();
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        else {
            throw new GenericException(new Response(response));
        }
    }
}
