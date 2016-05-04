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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.internal.Payload;

public class AtomLinkHandlerTest {
    
    private static final String TEST_DATA = "/atom_entry_with_link_embedded.txt";
    private static final String TEST_EMPTY_INLINE_LINK_DATA = "/atom_entry_with_embedded_empty_inline_link.txt";

	private Entry testEntry;

	@Before
	public void setUp() {
		testEntry = loadTestEntry(TEST_DATA);
	}

	@Test
	public void testGetTitle() {
		assertEquals(
				"input deal",
				new AtomLinkHandler(testEntry
						.getLink("http://temenostech.temenos.com/rels/input"))
						.getTitle());
	}

	@Test
	public void testGetHref() {
		assertEquals(
				"http://localhost:9089/t24interactiontests-iris/t24interactiontests.svc/errors",
				new AtomLinkHandler(testEntry
						.getLink("http://temenostech.temenos.com/rels/errors"))
						.getHref());
	}

	@Test
	public void testGetRel() {
		assertEquals(
				"http://temenostech.temenos.com/rels/input",
				new AtomLinkHandler(testEntry
						.getLink("http://temenostech.temenos.com/rels/input"))
						.getRel());
	}

	@Test
	public void testGetBaseUrl() {
		assertEquals(
				"http://localhost:9089/t24interactiontests-iris/t24interactiontests.svc/GB0010001/",
				new AtomLinkHandler(testEntry
						.getLink("http://temenostech.temenos.com/rels/hold"))
						.getBaseUri());
	}

	@Test
	public void testGetEmbeddedPayloadWithValidProperties() {
		AtomLinkHandler errorsLinkHandler = new AtomLinkHandler(
				testEntry.getLink("http://temenostech.temenos.com/rels/errors"));
		assertNotNull(errorsLinkHandler);
		// TODO below tests to be mioved to Entity handler test case
		Payload embeddedErrors = errorsLinkHandler.getEmbeddedPayload();
		assertNotNull(embeddedErrors);
		// with no index
		assertEquals("NO SPECIFIED VALUE",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(0)/Code"));
		// with index 0
		assertEquals("NON_FATAL_ERROR",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(0)/Type"));
		// with index 1
		assertEquals("INPUT MISSING",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(1)/Text"));		
		// with index 2
		assertEquals("verCustomer_Input.verCustomer_Input_ShortNameMvGroup.ShortName:en:1",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(2)/Info"));
		// with last index i.e., 4
		assertEquals("verCustomer_Input.Gender:1:1",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(4)/Info"));
		// Invalid indexes
		assertEquals("",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(5)/Info"));
		assertEquals("",
				embeddedErrors.entity().get("Errors_ErrorsMvGroup(200)/Info"));
	}

	@Test
	public void testGetNoEmbeddedPayload() {
		assertNull(new AtomLinkHandler(
				testEntry.getLink("http://temenostech.temenos.com/rels/hold"))
				.getEmbeddedPayload());
	}
    
    @Test
    public void testGetEmbeddedPayloadWithEmptyInlineElement(){
        testEntry = loadTestEntry(TEST_EMPTY_INLINE_LINK_DATA);
        assertNull(new AtomLinkHandler(
            testEntry.getLink("http://temenostech.temenos.com/rels/errors")
        ).getEmbeddedPayload());
    }

	private Entry loadTestEntry(String resource) {
		Entry entry = new Abdera()
				.getParser()
				.<Entry> parse(
						AtomLinkHandler.class
								.getResourceAsStream(resource))
				.getRoot();
		return entry;
	}

}
