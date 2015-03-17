// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2014 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.inforegistry;

import java.util.Set	;

import javax.swing.JComponent;

import org.bridgedb.Xref;
import org.pathvisio.core.model.DataNodeType;

/**
 * 
 * Interface to be implemented by plugins that
 * want to register additional information providers
 * 
 * @author mkutmon
 * @author rohansaxena
 */
public interface IInfoProvider {

	/**
	 * method to get the name of the registered plugin
	 * @return name of the registered plugin
	 */
	public String getName();
	
	
	/**
	 * method to get the data node types that the registering plugin 
	 * supports
	 * @return set of data node types supported by the registered 
	 * plugin
	 */
	public Set<DataNodeType> getDatanodeTypes();
	
	
	/**
	 * method to get additional information from the registered plugin
	 * for a particular data node
	 * @param xref - to provide id and datasource to the registered plugin 
	 * @return additional information returned from the registered plugin
	 */
	public JComponent getInformation(Xref xref);
	
}