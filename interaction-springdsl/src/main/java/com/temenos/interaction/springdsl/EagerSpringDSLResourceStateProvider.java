package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
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

import com.temenos.interaction.core.cache.CacheExtended;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.loader.ResourceStateLoadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static com.temenos.interaction.core.loader.ResourceStateLoadingStrategy.ResourceStateResult;

/**
 * Provider of ResourceState that loads all of them at instantiation time from
 * the PRD files matching the ant style pattern. It allows to plug-in different
 * cache strategies, as well as loading strategies (although the only loading
 * strategy that makes sense here is a Spring one).
 * 
 * The class currently extends SpringDSLResourceStateProvider for code re-usage.
 * This is a temporary solution until major re-factoring is done. 
 * 
 * @author kwieconkowski
 * @author andres
 * @author dgroves
 */
public class EagerSpringDSLResourceStateProvider extends SpringDSLResourceStateProvider {
    private final Logger logger = LoggerFactory.getLogger(EagerSpringDSLResourceStateProvider.class);

    private final CacheExtended<String, ResourceState> cache;
    private final String antStylePattern;
    private Set<String> PRDconfigurationFileSources;
    private ResourceStateLoadingStrategy<String> loadingStrategy;

    public EagerSpringDSLResourceStateProvider(String antStylePattern, ResourceStateLoadingStrategy<String> loadingStrategy, CacheExtended<String, ResourceState> cache) {
        this(antStylePattern, loadingStrategy, cache, null);
    }

    public EagerSpringDSLResourceStateProvider(String antStylePattern, ResourceStateLoadingStrategy<String> loadingStrategy, CacheExtended<String, ResourceState> cache, Properties beanMap) {
        super(beanMap);
        this.antStylePattern = antStylePattern;
        this.loadingStrategy = loadingStrategy;
        this.cache = cache;
        PRDconfigurationFileSources = new LinkedHashSet();
        discoverAllPrdFilesNames();
        loadAllResourceStates();
    }

    public void setLoadingStrategy(ResourceStateLoadingStrategy<String> loadingStrategy) {
        this.loadingStrategy = loadingStrategy;
    }

    @Override
    public ResourceState getResourceState(String resourceStateName) {
        logger.info("Getting resource state name: " + resourceStateName);
        ResourceState resourceState = getResourceStateByNameOrByOldFormatName(resourceStateName);
        if (resourceState == null) {
            logger.error("Could not find resource state name: " + resourceStateName);
        }
        return resourceState;
    }

    private ResourceState getResourceStateByNameOrByOldFormatName(String resourceStateName) {
        ResourceState resourceState = cache.get(resourceStateName);
        return resourceState != null ? resourceState : getResourceStateByOldFormat(resourceStateName);
    }

    private ResourceState getResourceStateByOldFormat(String resourceStateName) {
        ResourceState resourceState = null;
        String oldResourceStateName = resourceStateName;

        oldResourceStateName = substringToFirstLineSymbol(oldResourceStateName);
        resourceState = cache.get(oldResourceStateName);

        if (resourceState == null) {
            oldResourceStateName = replaceLastUnderscoreWithLine(oldResourceStateName);
            resourceState = cache.get(oldResourceStateName);
        }
        return resourceState;
    }

    private String replaceLastUnderscoreWithLine(String resourceStateName) {
        if (!isThereLineSymbol(resourceStateName)) {
            int pos = resourceStateName.lastIndexOf("_");
            if (pos > 0) {
                resourceStateName = String.format("%s-%s", resourceStateName.substring(0, pos), resourceStateName.substring(pos + 1));
            }
        }
        return resourceStateName;
    }

    private boolean isThereLineSymbol(String resourceStateName) {
        return resourceStateName.lastIndexOf("-") >= 0;
    }

    private String substringToFirstLineSymbol(String newResourceStateName) {
        if (newResourceStateName.contains("-")) {
            newResourceStateName = newResourceStateName.substring(0, newResourceStateName.indexOf("-"));
        }
        return newResourceStateName;
    }

    @Override
    public void unload(String resourceStateName) {
        cache.remove(resourceStateName);
    }

    @Override
    public void addState(String stateName, Properties properties) {
        String[] methodAndPath = properties.getProperty(stateName).split(" ");
        String[] methods = methodAndPath[0].split(",");
        String path = methodAndPath[1];
        logger.info(String.format("Attempting to register state: %s, methods: %s, path: %s, using state registeration: %s",
                stateName, methods, path, stateRegisteration != null ? stateRegisteration : "NULL"));

        if (!loadResourceStatesFromPRD(discoverNameOfPrdByUsingResourceStateName(stateName, false))
                && !loadResourceStatesFromPRD(discoverNameOfPrdByUsingResourceStateName(stateName, true))) {
            logger.error("None of discovered PRD configuration xml file names is valid");
            return;
        }
        // populate maps in parent class from properties files
        storeState(stateName, properties.getProperty(stateName));
        stateRegisteration.register(stateName, path, new HashSet<String>(Arrays.asList(methods)));
    }

    @Override
    public boolean isLoaded(String resourceStateName) {
        return (cache.get(resourceStateName) != null);
    }

    /* Reload resource states from prd files (clear old ones from cache before) */
    private synchronized void loadAllResourceStates() {
        cache.removeAll();

        for (String locationOfPRD : PRDconfigurationFileSources) {
            loadResourceStatesFromPRD(locationOfPRD);
        }
    }

    private String discoverNameOfPrdByUsingResourceStateName(String resourceStateName, boolean oldFormat) {
        String pathToPRD = null;
        String newResourceStateName = resourceStateName;

        newResourceStateName = substringToFirstLineSymbol(newResourceStateName);
        if (!oldFormat) {
            return String.format("IRIS-%s-PRD.xml", newResourceStateName);
        }
        pathToPRD = substringToFirstUnderscoreSymbol(newResourceStateName);

        return pathToPRD;
    }

    private String substringToFirstUnderscoreSymbol(String newResourceStateName) {
        int position = newResourceStateName.lastIndexOf("_");
        if (position > 3) {
            return String.format("IRIS-%s-PRD.xml", newResourceStateName.substring(0, position));
        }
        return null;
    }

    private boolean loadResourceStatesFromPRD(String prdName) {
        List<ResourceStateResult> resourceStates = null;
        Map<String, ResourceState> tmp = new HashMap<String, ResourceState>();

        if (prdName == null) {
            return false;
        }
        logger.info("Loading PRD file: " + prdName);
        resourceStates = loadingStrategy.load(prdName);
        if (resourceStates == null) {
            logger.warn("Could not find any resources with name: " + prdName);
            return false;
        }
        for (ResourceStateResult resourceStateResult : resourceStates) {
            tmp.put(resourceStateResult.resourceStateId, resourceStateResult.resourceState);
        }
        cache.putAll(tmp);
        return true;
    }

    private void discoverAllPrdFilesNames() {
        final ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        final Resource[] locationsPRD;
        try {
            String fileName;
            locationsPRD = patternResolver.getResources(antStylePattern);
            if (locationsPRD != null) {
                PRDconfigurationFileSources.clear();
                for (int i = 0; i < locationsPRD.length; i++) {
                    fileName = Paths.get(locationsPRD[i].getURI().getPath().substring(1)).getFileName().toString();
                    PRDconfigurationFileSources.add(fileName);
                    logger.info("Discovered path to PRD file: " + fileName);
                }
            } else {
                logger.warn("There was not found any PRD configuration xml files using given antStylePattern");
            }
        } catch (IOException e) {
            String msg = "IOException while loading PRD configuration xml files";
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }
}
