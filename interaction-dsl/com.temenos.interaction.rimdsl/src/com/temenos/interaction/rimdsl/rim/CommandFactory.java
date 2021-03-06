package com.temenos.interaction.rimdsl.rim;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

/**
 * Constructs commands and add to the list of commands.
 *
 * @author aphethean
 *
 */
public class CommandFactory {
	
	Map<String, Command> commandMap = new HashMap<String, Command>();
	EList<Command> modelReferences;
	
	public CommandFactory(EList<Command> modelReferences) {
		this.modelReferences = modelReferences;
	}
	
	public ResourceCommand createResourceCommand(String commandName, Map<String,String> properties) {
		// a resource command references a command
		ResourceCommand resourceCommand = RimFactory.eINSTANCE.createResourceCommand();
		if (commandMap.get(commandName) == null) {
			Command command = RimFactory.eINSTANCE.createCommand();
			command.setName(commandName);
			// command.getProperties();
			commandMap.put(commandName, command);
			modelReferences.add(command);
		}
		resourceCommand.setCommand(commandMap.get(commandName));
		// add any resource specific command properties
		if (properties != null) {
			for (String key : properties.keySet()) {
				CommandProperty commandProperty = RimFactory.eINSTANCE.createCommandProperty();
				commandProperty.setName(key);
				commandProperty.setValue(properties.get(key));
				resourceCommand.getProperties().add(commandProperty);
			}
		}
		return resourceCommand;
	}

}
