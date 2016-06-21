package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
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


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.temenos.interaction.core.resource.ConfigLoader;

/**
 * This class enhances the Spring PropertiesFactoryBean class to provide support for loading IRIS configuration files 
 * from a directory defined as a system property.
 */
public class SpringDSLPropertiesFactoryBean extends PropertiesFactoryBean {
	
	private String filenamePattern;
	private ConfigLoader configLoader = new ConfigLoader();

	/**
	 * @param filenamePattern The file name pattern to use if properties files are being loaded from the file system
	 */
	public SpringDSLPropertiesFactoryBean(String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}
		
	/**
	 * Sets the alternative config loader to use
	 *  
	 * @param configLoader The alternative config loader to use
	 */
	@Autowired(required = false)	
	public void setConfigLoader(ConfigLoader configLoader) {
		this.configLoader = configLoader;
	}

	@Override
	public void setLocations(Resource[] locations) {
		List<Resource> tmpLocations = new ArrayList<Resource>();
		tmpLocations.addAll(Arrays.asList(locations));
		
		String irisResourceDirPath = configLoader.getIrisConfigDirPath();
		
		if(irisResourceDirPath != null) {
			// Try and load the properties from the file system as a resource directory has been specified
			File irisResourceDir = new File(irisResourceDirPath);
			
			if(irisResourceDir.exists() && irisResourceDir.isDirectory()) {
				File[] files = irisResourceDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.matches(filenamePattern);
					}
				});
				
				for(File file: files) {
					// Create a resource for a current file and add it to the collection of properties resources
					tmpLocations.add(new FileSystemResource(file));
				}
			}		
		}
		
		super.setLocations(tmpLocations.toArray(new Resource[0]));		
	}
}