/*
 * Copyright 2021 Keith Gamble
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.bwdesigngroup.neo4j.designer.editors;

import javax.swing.JLabel;

import com.bwdesigngroup.neo4j.common.projectresources.Neo4JProperties;
import com.bwdesigngroup.neo4j.designer.components.DatabaseDropdown;
import com.inductiveautomation.ignition.client.util.gui.HeaderLabel;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.project.resource.ResourceType;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.propertyeditor.AbstractPropertyEditorPanel;

import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Keith Gamble
 */
public class GeneralPropertyEditor extends AbstractPropertyEditorPanel {

    private final DatabaseDropdown dropdown;

    public GeneralPropertyEditor(DesignerContext context) {
        super(new MigLayout("", "[pref!][grow,fill]", "[]15[]"));
        add(HeaderLabel.forKey("GeneralPropertyEditor.Database.Header"), "wrap r");
        add(new JLabel(BundleUtil.get().getString("GeneralPropertyEditor.Database.Label")), "");

        dropdown = new DatabaseDropdown(false);
        add(dropdown, "wrap");
        listenTo(dropdown.getDropdown());
    }

    @Override
    public Object commit() {
        Neo4JProperties neo4jProps = new Neo4JProperties();
       try {
            neo4jProps.setDefaultDatabase(dropdown.getSelectedItem().toString());
        } catch (Exception e)  { }
        return neo4jProps;
    }

    @Override
    public String getCategory() {
        return "Neo4J";
    }

    @Override
    public ResourceType getResourceType() {
        return Neo4JProperties.RESOURCE_TYPE;
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
            dropdown.setSelectedItem(((Neo4JProperties) props).getDefaultDatabase());
        }
    }
    
}
