/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.segments.asah.connector.internal.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import com.liferay.petra.string.StringBundler;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Arques
 */
@Component(immediate = true, service = JSONWebServiceClient.class)
public class JSONWebServiceClientImpl implements JSONWebServiceClient {

	@Override
	public String doDelete(
		String url, Map<String, String> parameters,
		Map<String, String> headers) {

		WebTarget webTarget = _client.target(_baseURI);

		webTarget = webTarget.path(url);

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
		}

		Invocation.Builder builder = webTarget.request(
			MediaType.APPLICATION_JSON_TYPE);

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}

		Response response = builder.delete();

		_validateResponse(response);

		return response.readEntity(String.class);
	}

	@Override
	public String doGet(
		String url, MultivaluedMap<String, Object> parameters,
		Map<String, String> headers) {

		WebTarget webTarget = _client.target(_baseURI);

		webTarget = webTarget.path(url);

		for (MultivaluedMap.Entry<String, List<Object>> entry :
				parameters.entrySet()) {

			for (Object value : entry.getValue()) {
				webTarget = webTarget.queryParam(entry.getKey(), value);
			}
		}

		Invocation.Builder builder = webTarget.request(
			MediaType.APPLICATION_JSON_TYPE);

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}

		Response response = builder.get();

		_validateResponse(response);

		return response.readEntity(String.class);
	}

	@Override
	public <T> T doPost(
		Class<T> clazz, String url, T object, Map<String, String> headers) {

		WebTarget webTarget = _client.target(_baseURI);

		webTarget = webTarget.path(url);

		Invocation.Builder builder = webTarget.request(
			MediaType.APPLICATION_JSON_TYPE);

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}

		Response response = builder.post(
			Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));

		_validateResponse(response);

		return response.readEntity(clazz);
	}

	@Override
	public <T> void doPut(String url, T object, Map<String, String> headers) {
		WebTarget webTarget = _client.target(_baseURI);

		webTarget = webTarget.path(url);

		Invocation.Builder builder = webTarget.request(
			MediaType.APPLICATION_JSON_TYPE);

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}

		Response response = builder.put(
			Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));

		_validateResponse(response);
	}

	@Override
	public String getBaseURI() {
		return _baseURI;
	}

	@Override
	public void setBaseURI(String baseURI) {
		_baseURI = baseURI;
	}

	@Activate
	protected void activate() {
		_client = _clientBuilder.build();

		JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();

		jacksonJsonProvider.configure(
			SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jacksonJsonProvider.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		_client.register(jacksonJsonProvider);
	}

	private void _validateResponse(Response response) {
		int status = response.getStatus();

		if ((status < HttpServletResponse.SC_OK) ||
			(status >= HttpServletResponse.SC_MULTIPLE_CHOICES)) {

			throw new ClientErrorException(
				StringBundler.concat(
					"Unexpected response status ", status,
					" with response message: ",
					response.readEntity(String.class)),
				status);
		}
	}

	private String _baseURI;
	private Client _client;

	@Reference
	private ClientBuilder _clientBuilder;

}