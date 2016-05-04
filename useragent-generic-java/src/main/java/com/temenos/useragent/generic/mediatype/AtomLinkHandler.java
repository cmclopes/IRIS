package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static com.temenos.useragent.generic.mediatype.AtomUtil.NS_ATOM;
import static com.temenos.useragent.generic.mediatype.AtomUtil.NS_ODATA_METADATA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Link;

import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.context.ContextFactory;
import com.temenos.useragent.generic.http.DefaultHttpClientHelper;
import com.temenos.useragent.generic.internal.DefaultPayloadWrapper;
import com.temenos.useragent.generic.internal.Payload;
import com.temenos.useragent.generic.internal.PayloadHandlerFactory;
import com.temenos.useragent.generic.internal.PayloadWrapper;

/**
 * Handler for links in Atom models.
 * 
 * @author ssethupathi
 *
 */
public class AtomLinkHandler {

	private Link abderaLink;
	private Payload embeddedPayload;

	public AtomLinkHandler(Link abderaLink) {
		this.abderaLink = abderaLink;
	}

	public String getTitle() {
		return abderaLink.getTitle();
	}

	public String getHref() {
		return abderaLink.getAttributeValue("href");
	}

	public String getRel() {
		return AtomUtil.extractRel(abderaLink.getAttributeValue("rel"));
	}

	public String getDescription() {
		return AtomUtil.extractDescription(abderaLink.getAttributeValue("rel"));
	}

	public String getBaseUri() {
		try {
			return abderaLink.getBaseUri().toURL().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public Payload getEmbeddedPayload() {
		if (embeddedPayload == null) {
			buildEmbeddedPayload();
		}
		return embeddedPayload;
	}

	private void buildEmbeddedPayload() {
		Element inlineElement = abderaLink.getFirstChild(new QName(
				NS_ODATA_METADATA, "inline"));
		if (inlineElement == null || inlineElement.getFirstChild() == null) {
			embeddedPayload = null; // TODO null payload
			return;
		}
		Element feedElement = inlineElement.getFirstChild(new QName(NS_ATOM,
				"feed"));
		String content = "";
		if (feedElement != null) {
			content = getContent(feedElement);
		} else {
			Element entryElement = inlineElement.getFirstChild(new QName(
					NS_ATOM, "entry"));
			if (entryElement != null) {
				content = getContent(entryElement);
			}
		}
		PayloadHandlerFactory<? extends PayloadHandler> factory = ContextFactory
				.get()
				.getContext()
				.entityHandlersRegistry()
				.getPayloadHandlerFactory(
						DefaultHttpClientHelper.removeParameter(abderaLink
								.getAttributeValue("type")));
		PayloadHandler handler = factory.createHandler(content);
		PayloadWrapper wrapper = new DefaultPayloadWrapper();
		wrapper.setHandler(handler);
		embeddedPayload = wrapper;
	}

	private String getContent(Element element) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			element.writeTo(baos);
			return baos.toString("UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
