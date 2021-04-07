/*
 * Copyright 2021 Keith Gamble
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.bwdesigngroup.neo4j.components;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

import com.bwdesigngroup.neo4j.scripting.ScriptingFunctions;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.gui.AbstractProfileOptionDropdown;
import com.inductiveautomation.ignition.common.BundleUtil;

/**
 *
 * @author Keith Gamble
 */
public class DatabaseDropdown extends AbstractProfileOptionDropdown {

    private ScriptingFunctions rpc;

    public DatabaseDropdown(boolean initialize, ScriptingFunctions rpc) {
        super(initialize);
        this.rpc = rpc;
        this.allowNone = true;

        BundleUtil propertiesBundle = BundleUtil.get();

        this.setNoneOptionText(propertiesBundle.getString("GeneralPropertyEditor.Dropdown.Empty"));
        this.setNoSelectionText(propertiesBundle.getString("GeneralPropertyEditor.Dropdown.Empty"));
        this.setToolTipText(propertiesBundle.getString("GeneralPropertyEditor.Dropdown.Tooltip"));
        this.refresh();
    }

    @Override
    protected List<String> getOptions() throws Exception {
        return getConnections();
    }

    private List<String> getConnections() {
        return rpc.getConnections();
    }

    public void setOptions() {
        this.dropdown.setModel(new DefaultComboBoxModel<String>(getConnections().toArray(new String[0])));
    }

    public int getSelectedIndex() {
        return this.dropdown.getSelectedIndex();
    }
    
    public void setSelectedIndex(int index) {
        this.dropdown.setSelectedIndex(index);
    }
}
