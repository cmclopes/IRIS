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
import com.temenos.interaction.core.command.ChainingCommandController;
import com.temenos.interaction.core.loader.FileEvent;
import com.temenos.interaction.loader.classloader.CachingParentLastURLClassloaderFactory;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class SpringBasedLoaderActionTest {

    @Test
    public void test() throws IOException, InterruptedException {
        
    	CachingParentLastURLClassloaderFactory classLoaderFactory = new CachingParentLastURLClassloaderFactory();
    	
        FileEvent<File> dirEvent = new FileEvent<File>() {
            @Override
            public File getResource() {
                return new File(System.getProperty("jar.folder"));
            }
        };

        ChainingCommandController chainingCommandController = new ChainingCommandController();
       
        SpringBasedLoaderAction instance = new SpringBasedLoaderAction();
        instance.setParentChainingCommandController(chainingCommandController);
        instance.setClassloaderFactory(classLoaderFactory);
        // Check chainingCommandController and try to find the GETEntities command before the loading
        // This test should be false because the bean doesn't exist
        Assert.assertFalse(chainingCommandController.isValidCommand("GETEntities"));
        instance.execute(dirEvent);
        
        // Check chainingCommandController and try to find the GETEntities command after the loading
        // This test should be true because the bean has been loaded
        Assert.assertTrue(instance.getParentChainingCommandController().isValidCommand("GETEntities"));
    }
}
