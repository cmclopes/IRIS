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

import com.temenos.interaction.core.command.annotation.InteractionCommandImpl;
import com.temenos.interaction.loader.classloader.ParentLastURLClassloader;
import com.temenos.annotatedtestclasses.AnnotatedInteractionCmdStubImpl1;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;


/**
 * The tests verifies if the class already existing on the classpath can be reloaded from a JAR coocked up for the person.
 * It also test scanning for annotations in the prescribed group of JARs only.
 * @author andres
 * @author trojan
 */

public class ReflectionsTest {
    
    @Test
    public void testLoadingClassesFromJar() throws MalformedURLException, ClassNotFoundException {
        File jarFile = new File(System.getProperty("jar.location"));
        Assert.assertTrue(jarFile.exists());
        ClassLoader classloader = new ParentLastURLClassloader(new URL[]{jarFile.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
        Class<?> clz = classloader.loadClass("com.temenos.annotatedtestclasses.AnnotatedInteractionCmdStubImpl1");
        Assert.assertEquals("Annotation name was not read as expected", "testName1", clz.getAnnotation(InteractionCommandImpl.class).name());
    }
    
    @Test
    public void testReflectionsOnSpecificPackage() throws MalformedURLException {
        // enforce loading class with current classloader
        AnnotatedInteractionCmdStubImpl1 object = new AnnotatedInteractionCmdStubImpl1();
        
        File jarFile = new File(System.getProperty("jar.location"));
       
        ClassLoader classloader = new ParentLastURLClassloader(new URL[]{jarFile.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
        Reflections r = new Reflections(                
                new ConfigurationBuilder()
            .setUrls(jarFile.toURI().toURL())
            .addClassLoader(classloader)
        );
        
        Set<Class<?>> annotated = r.getTypesAnnotatedWith(InteractionCommandImpl.class);
        
        // we knew 3 classes with given annotation was in a jar we prepared
        Assert.assertEquals("The number of classes detected is different than expected",3, annotated.size());
        for (Class cls : annotated) {
            // for every class chack if it was really loaded with the classloader we wanted
            // AnnotatedClass1 - in case of classloading method being faulty would be from parent!
            Assert.assertEquals("Classloader used to load class different than expected, delegation model failed!", cls.getClassLoader(), classloader);
        }
    }
    
}
