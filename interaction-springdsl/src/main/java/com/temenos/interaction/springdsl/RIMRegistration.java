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


import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;


/**
 * Implementations of this interface will provide a mechanism for registering resource interaction models 
 * (RIMs) with a web service container e.g. Apache Wink.
 *
 * @author mlambert
 *
 */
public interface RIMRegistration {

	/**
	 * Registers a resource interaction model (RIM) with a web service container
	 * 
	 * @param resource
	 * 				The resource to register
	 */
	void register(HTTPResourceInteractionModel resource);
}
