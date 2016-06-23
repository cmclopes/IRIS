package com.temenos.interaction.loader.detector;

/*
 * #%L
 * interaction-dynamic-loader
 * %%
 * Copyright (C) 2012 - 2015 Temenos Holdings N.V.
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

import com.temenos.interaction.core.command.AnnotationBasedCommandController;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.loader.classloader.ParentLastURLClassloader;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author hmanchala
 */
public class AnnotationBasedCommandControllerTest {

    @Test
    public void testExistenceOfAnnotatedCommandInTheController() throws MalformedURLException {

        System.out.println("jar.location is "+System.getProperty("jar.location"));
        File jarFile = new File(System.getProperty("jar.location"));
        ClassLoader classloader = new ParentLastURLClassloader(new URL[]{jarFile.toURI().toURL()}, Thread.currentThread().getContextClassLoader());

        AnnotationBasedCommandController controller = new AnnotationBasedCommandController();
        controller.setClassloader(classloader);
        controller.setJarsToScan(Collections.singleton(jarFile.toURI().toURL()));
        Assert.assertNotNull("Not found a commad that shopuld be there",controller.fetchCommand("testName1"));  // this one was annotated, and is an InteractionCommand - should be there
        Assert.assertNull("Found a class that is annotated, but should be excluded / not an InteractionCommand",controller.fetchCommand("testName3")); // this one was annotated, but is not InteractionCommand - filtered out silently
    }

    @Test
    public void testNumberOfCommandsDetectedByAnnotations() throws MalformedURLException {
        System.out.println("jar.location is "+System.getProperty("jar.location"));
        File jarFile = new File(System.getProperty("jar.location"));
        ClassLoader classloader = new ParentLastURLClassloader(new URL[]{jarFile.toURI().toURL()}, Thread.currentThread().getContextClassLoader());

        CacheExposingAnnotationBasedCommandController controller = new CacheExposingAnnotationBasedCommandController();
        controller.setClassloader(classloader);
        controller.setJarsToScan(Collections.singleton(jarFile.toURI().toURL()));

        Assert.assertNotNull(controller.fetchCommand("testName1")); // ensure cache is initialized

        Assert.assertEquals("The JAR was prepared with 2 annotated InteractionCommands, however different number was detected",2, controller.getCachedCommands().entrySet().size());
    }

    public static class CacheExposingAnnotationBasedCommandController extends AnnotationBasedCommandController {

        public Map<String, InteractionCommand> getCachedCommands() {
            return cache;
        }
    }
}
