/*
 * Copyright 2021 Keith Gamble
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.bwdesigngroup.neo4j.editors;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


import com.bwdesigngroup.neo4j.components.DatabaseDropdown;
import com.bwdesigngroup.neo4j.resources.Neo4JProperties;
import com.bwdesigngroup.neo4j.scripting.ScriptingFunctions;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.gui.AbstractProfileOptionDropdown;
import com.inductiveautomation.ignition.client.util.gui.AntialiasLabel;
import com.inductiveautomation.ignition.client.util.gui.HeaderLabel;
import com.inductiveautomation.ignition.common.project.resource.ResourceType;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.propertyeditor.AbstractPropertyEditorPanel;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Keith Gamble
 */
public class GeneralPropertyEditor extends AbstractPropertyEditorPanel {

    private final ScriptingFunctions rpc;
    private Neo4JProperties neo4jProps = new Neo4JProperties();
    private final DatabaseDropdown dropdown;

    public GeneralPropertyEditor(DesignerContext context) {
        super(new MigLayout("fill", "[pref!][grow,fill]", "[]15[]"));

        rpc = ModuleRPCFactory.create(
            "com.bwdesigngroup.neo4j.neo4j-driver",
            ScriptingFunctions.class
        );
        dropdown = new DatabaseDropdown(false, rpc);


        add(HeaderLabel.forKey("GeneralPropertyEditor.Database.Header"), "wrap r");

        add(new JLabel(BundleUtil.get().getString("GeneralPropertyEditor.Database.Label")), "");
        add(dropdown, "wrap");
        listenTo(dropdown.getDropdown());


    }

    public List<String> getConnections() {
        return rpc.getConnections();
    }

    @Override
    public Object commit() {
        if ( dropdown.getSelectedIndex() != -1 ) {
            neo4jProps.setDefaultDatabase(dropdown.getSelectedItem().toString());
        } else {
            return null;
        }
        
        return neo4jProps;
    }

    @Override
    public String getCategory() {
        return "Neo4J";
    }

    @Override
    public ResourceType getResourceType() {
        return neo4jProps.getResourceType();
    }

    @Override
    public String getTitleKey() {
        return "GeneralPropertyEditor.General.Title";
    }

    @Override
    public void initProps(Object props) {
        if ( props == null ) {
            dropdown.setSelectedItem(null);
        } else {
            Neo4JProperties updatedProps = (Neo4JProperties) props;
            this.neo4jProps = updatedProps;
    
            dropdown.setSelectedItem(neo4jProps.getDefaultDatabase());
        }
    }
    
}
