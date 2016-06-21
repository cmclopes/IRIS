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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.temenos.interaction.core.cache.CacheConcurrentImpl;
import com.temenos.interaction.core.cache.CacheExtended;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;
import com.temenos.interaction.core.loader.ResourceStateLoadingStrategy;
import com.temenos.interaction.core.loader.SpringResourceStateLoadingStrategy;

/**
 * @author kwieconkowski
 */
public class TestEagerSpringDSLResourceStateProvider extends TestSpringDSLResourceStateProvider {

    @BeforeClass
    public static void setUpClass() {
        ctx = new ClassPathXmlApplicationContext("classpath:/com/temenos/interaction/springdsl/TestEagerSpringDSLResourceStateProvider-context.xml");     
    }
    
    @Before
    public void setUp() {
        resourceStateProvider = (ResourceStateProvider) ctx.getBean("resourceStateProvider");
    }

    @Test
    public void testGetResourceState() {
        EagerSpringDSLResourceStateProvider springDSLResourceStateProvider = getDefaultClass();
        ResourceState resourceState = springDSLResourceStateProvider.getResourceState("SimpleModel_Home_home");
        assertNotNull(resourceState);
        assertEquals("home", resourceState.getName());
    }

    @Test
    public void testGetResourceState_oldFormat() {
        EagerSpringDSLResourceStateProvider springDSLResourceStateProvider = getDefaultClass();
        springDSLResourceStateProvider.unload("SimpleModel_Home_home");
        ResourceState resourceState = springDSLResourceStateProvider.getResourceState("SimpleModel_Home_home");
        assertNotNull(resourceState);
        assertEquals("home", resourceState.getName());
    }

    @Test
    public void testUnload() {
        EagerSpringDSLResourceStateProvider springDSLResourceStateProvider = getDefaultClass();
        ResourceState resourceState;
        final String beanName = "SimpleModel_Home-home";

        resourceState = springDSLResourceStateProvider.getResourceState(beanName);
        assertNotNull(resourceState);
        springDSLResourceStateProvider.unload(beanName);
        resourceState = springDSLResourceStateProvider.getResourceState(beanName);
        assertNull(resourceState);
    }

    @Test
    public void testAddState() {
        EagerSpringDSLResourceStateProvider springDSLResourceStateProvider = getDefaultClass();
        springDSLResourceStateProvider.stateRegisteration = mock(StateRegisteration.class);

        final String resourceStateName = "SimpleModel_Home_home-ServiceDocument";
        Properties properties = new Properties();
        properties.setProperty(resourceStateName, "GET /");

        springDSLResourceStateProvider.addState(resourceStateName, properties);
    }

    @Test
    public void testIsLoaded() {
        EagerSpringDSLResourceStateProvider springDSLResourceStateProvider = getDefaultClass();
       assertTrue(springDSLResourceStateProvider.isLoaded("SimpleModel_Home_home"));
    }

    @Test
    public void testGetResourceStatesByPath() {
        Properties properties = new Properties();
        properties.put("SimpleModel_Home_home", "GET /test");
        ResourceStateProvider rsp = getDefaultClass(properties);
        Map<String, Set<String>> statesByPath = rsp.getResourceStatesByPath();
        assertEquals(1, statesByPath.size());
        assertEquals(1, statesByPath.get("/test").size());
        assertEquals("SimpleModel_Home_home", statesByPath.get("/test").toArray()[0]);
    }

    private EagerSpringDSLResourceStateProvider getDefaultClass() {
        return getDefaultClass(null);
    }

    private EagerSpringDSLResourceStateProvider getDefaultClass(Properties properties) {
        return getDefaultClass("classpath*:/**/IRIS-*-PRD.xml", new SpringResourceStateLoadingStrategy(), new CacheConcurrentImpl(), properties);
    }

    private EagerSpringDSLResourceStateProvider getDefaultClass(String antStylePattern, ResourceStateLoadingStrategy<String> loadingStrategy, CacheExtended<String, ResourceState> cache, Properties properties) {
        return new EagerSpringDSLResourceStateProvider(antStylePattern, loadingStrategy, cache, properties);
    }
}
