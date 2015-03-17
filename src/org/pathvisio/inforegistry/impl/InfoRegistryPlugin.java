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
package org.pathvisio.inforegistry.impl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.inforegistry.IInfoProvider;
import org.pathvisio.inforegistry.InfoRegistry;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * currently implemented as a plugin (but not shown in plugin manager)
 * creates a side tab and implements the selection listener to update the
 * drop down box with information providers for the selected data node type.
 * 
 * @author mkutmon
 * @author rohansaxena
 *
 */
public class InfoRegistryPlugin extends JPanel implements Plugin, SelectionListener, ApplicationEventListener {

	private InfoRegistry registry;
	private PvDesktop desktop;
	private JPanel sidePanel;
	private JComboBox pluginList;
	private JPanel centerPanel;
	private getInformationWorker giw;
	private Object lastSelected;

	@Override
	public void init(PvDesktop desktop) {
		registry = InfoRegistry.getInfoRegistry();
		this.desktop = desktop;
		
		desktop.getSwingEngine().getEngine().addApplicationEventListener(this);
		VPathway vp = desktop.getSwingEngine().getEngine().getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
		
		addSidePanel();
	}
	
	public InfoRegistryPlugin(){
		super(new BorderLayout());
	}
	
	/**
	 * In this method a new tabbed pane called info is added. 
	 */
	private void addSidePanel() {
		
		//acts like a container
		sidePanel = new JPanel ();
		sidePanel.setLayout(new BorderLayout());
		displayMessage("No pathway element selected.");
		centerPanel = new JPanel();
		
		sidePanel.add(centerPanel,BorderLayout.CENTER);
       
        JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        sidebarTabbedPane.add("Info", sidePanel);
	}

	@Override
	public void done() {		
		desktop.getSideBarTabbedPane().remove(sidePanel);
	}

	/**
	 * updates the dropdown box depending on the pathway element
	 * selected.
	 * Displays warnings in unusual conditions.
	 */
	public void selectionEvent(SelectionEvent e) {
        switch(e.type) {
        case SelectionEvent.OBJECT_ADDED:
        	centerPanel.removeAll();
        	multiSelection(e);
        	break;
        case SelectionEvent.OBJECT_REMOVED:
        	centerPanel.removeAll();
        	multiSelection(e);
        	break;
        case SelectionEvent.SELECTION_CLEARED:
        	centerPanel.removeAll();
        	displayMessage("No node selected.");
        	break;
        }
       
	}
	
	public void applicationEvent(ApplicationEvent e) {
		switch(e.getType()) {
		case VPATHWAY_CREATED:
			((VPathway)e.getSource()).addSelectionListener(this);
			break;
		case VPATHWAY_DISPOSED:
			((VPathway)e.getSource()).removeSelectionListener(this);
			break;
		default:
			break;
		}
	}
	
	/**
	 * First checks if the pathway element is of the type DATANODE. If yes then
	 * updates drop down box depending on the data node type of the selected node.
	 * @param o - currently selected pathway element 
	 */
	private void sidePanelDisplayManager(VPathwayElement o){
        if(o instanceof Graphics) {
            PathwayElement pe = ((Graphics)o).getPathwayElement();
            if(pe.getObjectType() == ObjectType.DATANODE) {   
            	if(isAnnotated(pe)) {
            		List<String> provider = getProvider(pe);
            		if(provider.size() == 0) {
            			displayMessage("<html>No provider available for this data node type.<br/>Check the plugin repository for available plugins.</html>");
            		} else {
            			validElementSelected(pe, provider);
            		}
            	} else{
            		displayMessage("<html>Warning: Data node is not annotated.<br/>Please provide an identifier and database.</html>");
            	}
            }   
        }
	}
	
	private List<String> getProvider(PathwayElement element) {
		Iterator<IInfoProvider> ip = registry.registeredPlugins.iterator();
		List<String> provider = new ArrayList<String>();
		String type = element.getDataNodeType();
		while(ip.hasNext()) {
        	IInfoProvider ipo = ip.next();
        	String name = ipo.getName();
        	boolean add = false;
        	if(type.equals("Protein") && ipo.getDatanodeTypes().contains(DataNodeType.PROTEIN)) {
        		add = true;
        	} else if (type.equals("Rna") && ipo.getDatanodeTypes().contains(DataNodeType.RNA)) {
        		add = true;
    		} else if (type.equals("GeneProduct") && ipo.getDatanodeTypes().contains(DataNodeType.GENEPRODUCT)) {
    			add = true;
     		} else if (type.equals("Metabolite") && ipo.getDatanodeTypes().contains(DataNodeType.METABOLITE)) {
        		add = true;
      		} else if (type.equals("Pathway") && ipo.getDatanodeTypes().contains(DataNodeType.PATHWAY)) {
      			add = true;
      		}
        	if(add) {
        		if(!provider.contains(name)) provider.add(name);
        	}
    	}
		Collections.sort(provider);
		return provider;
	}
	
	private JComboBox fillDropDown(String type, List<String> provider) {
		pluginList = new JComboBox();
    	for(String s : provider) {
    		pluginList.addItem(s);
    	}

    	if(lastSelected != null){
    		pluginList.setSelectedItem(lastSelected);
    	}
    	return pluginList;
	}
	
	private void validElementSelected(final PathwayElement pe, List<String> provider) {
		sidePanel.removeAll();
		CellConstraints cc = new CellConstraints();
	    
		final JComboBox pluginList = fillDropDown(pe.getDataNodeType(), provider);
		
		pluginList.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				lastSelected = pluginList.getSelectedItem();
				
			}
		});
	    FormLayout layout = new FormLayout("5dlu,pref:grow,5dlu", "5dlu,pref,5dlu,pref,5dlu,pref,15dlu");
	    PanelBuilder builder = new PanelBuilder(layout);
	     
	    JLabel label = new JLabel("Please select a provider:");
	    builder.add(label, cc.xy(2,2));
	    builder.add(pluginList, cc.xy(2, 4));
	    
	    JButton goButton = new JButton("Go");
	    JButton cancelButton = new JButton("Cancel");

        goButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e){
        		centerPanel.removeAll();
        		if(pluginList.getItemCount() != 0) {
                    Iterator<IInfoProvider> ip = registry.registeredPlugins.iterator();
                    while(ip.hasNext()) {
                    	IInfoProvider ipo = ip.next();
                    	
                    	if( ipo.getName().equals(pluginList.getSelectedItem().toString())) {
                    		lastSelected = pluginList.getSelectedItem();
                    		if(giw!=null && !giw.isDone()){
                    			giw.cancel(true);
                    		}
                    		sidePanel.add(centerPanel, BorderLayout.CENTER);
                        	giw = new getInformationWorker(ipo, centerPanel, pe.getXref());
                        	giw.execute();
                    	}
                    }
           		} else {
        			displayMessage("No plugin available.");
        		}
        	}}
        );
        
        
        cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
        		if(giw!=null && !giw.isDone()){
        			giw.cancel(true);
        			displayMessage("Query cancelled.");
        		} else {
        			displayMessage("No query in progress.");
        		}
        	}
        });
        
        JPanel panel = new JPanel();
        panel.add(goButton);
        panel.add(cancelButton);
        
        builder.add(panel, cc.xy(2, 6));
        
	    builder.addSeparator("", cc.xyw(2, 7, 2));
	    sidePanel.add(builder.getPanel(), BorderLayout.NORTH);
	    sidePanel.revalidate();
    	sidePanel.repaint();
	}
	
	/**
	 * used to display a message in the info tabbed pane
	 * @param s - Message to be displayed
	 */
	private void displayMessage(String s) {
		sidePanel.removeAll();
		CellConstraints cc = new CellConstraints();
		
	    FormLayout layout = new FormLayout("5dlu,pref:grow,5dlu","25dlu,pref,10dlu,15dlu");
	    PanelBuilder builder = new PanelBuilder(layout);
		
	    JLabel label = new JLabel(s);
	    builder.add(label, cc.xy(2, 2));
	    builder.addSeparator("", cc.xyw(2, 4, 2));

	    sidePanel.add(builder.getPanel(), BorderLayout.NORTH);
       	sidePanel.revalidate();
       	sidePanel.repaint();               
	}
	
	/**
	 * checks the no. of elements selected. If more than one element selected
	 * issues a warning.
	 * @param e - current selection
	 */
	private void multiSelection(SelectionEvent e){
        if(e.selection.size() == 1) {
        	if(registry.registeredPlugins.size() == 0) {
        		displayMessage("No information provider plugins installed.");
        	} else {
        		// create side panel
	        	sidePanelDisplayManager(e.selection.iterator().next());
        	}
        } else if(e.selection.size() == 0) {
        	displayMessage("No pathway element is selected.");
        } else {
        	displayMessage("Warning: multiple elements are selected.");
        }
	}
	
	/**
	 * checks if the element is annotated or not 
	 * @param pe - element to be checked  
	 * @return - returns true if annotated else false
	 */
	private Boolean isAnnotated(PathwayElement pe){
		if(pe.getXref().getDataSource() == null | pe.getXref().getId() == null){
			return false;
		} else {
			return true;	
		}
	}
}