// PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2014 BiGCaT Bioinformatics
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//
package org.pathvisio.inforegistry;

import java.util.HashSet;
import java.util.Set;

/**
* 
* This registry class allows plugins to register/unregister
* themselves as additional data provider.
* 
* @author mkutmon
* @author rohansaxena
*/
public class InfoRegistry {

	private static InfoRegistry registry;
	public Set<IInfoProvider> registeredPlugins;
	
	// Registry implements the Singelton design pattern 
	// only one instance of the class is available and 
	// can be retrieved by plugins
	public static InfoRegistry getInfoRegistry() {
		if(registry == null) {
			registry = new InfoRegistry();
		}
		return registry;
	}
	
	/**
	 * Constructor that initializes registeredPlugins to a new
	 * set to contain registered plugins.
	 */
	private InfoRegistry() {

		if(registeredPlugins == null){
			registeredPlugins = new HashSet<IInfoProvider>();
		}
	}
	
	
	/**
	 * To be used by plugins to register themselves with info provider
	 * @param provider - Plugin to be registered
	 */
	public void registerInfoProvider(IInfoProvider provider) {

		if (provider.getName() != null) {
			registeredPlugins.add(provider);
		}
	}
	
	/**
	 * To be used by plugins to unregister themselves with info provider
	 * @param provider - Plugin to be unregistered
	 */
	public void unregisterInfoProvider(IInfoProvider provider) {

		if(registeredPlugins.contains(provider)){
			registeredPlugins.remove(provider);
		}
	}	
}