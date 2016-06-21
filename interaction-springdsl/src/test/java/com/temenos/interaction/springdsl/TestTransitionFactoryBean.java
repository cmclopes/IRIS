package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
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

import org.junit.Test;

import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.Transition;

/**
 * The Class TestTransitionFactoryBean.
 */
public class TestTransitionFactoryBean {

	/**
	 * Test construct.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testConstruct() throws Exception {
		ResourceState mockTargetResoruce = new ResourceState("entity", "name", null, "/test");

		Transition fromBuilder = new Transition.Builder().target(mockTargetResoruce).build();
		TransitionFactoryBean factoryBean = new TransitionFactoryBean();
		factoryBean.setTarget(mockTargetResoruce);
		Transition fromFactory = factoryBean.getObject();
		assertEquals(fromBuilder, fromFactory);
	}

}
