/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2022-2023, Jerry Jones
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
package com.moneydance.modules.features.budgetreport;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
* This class overrides the default cell renderer to display numeric cells in the
* table as currency. Eventually this may need to be updated to handle other 
* currencies.
*
* @author  Jerry Jones
*/
public class CurrencyTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * Default constructor
     */
    public CurrencyTableCellRenderer() {
        this.setHorizontalAlignment(JLabel.RIGHT);
    }

    
    /** 
     * This method overrides the default table cell renderer to add required
     * functionality then it performs the default actions.
     * 
     * @param table - The budget editor table object.
     * @param value - The value to render,
     * @param isSelected - true if the cell is currently selected.
     * @param hasFocus - true if the cell currently has focus.
     * @param row - The table row where this cell resides.
     * @param column - The table column where this cell resides.
     * @return Component
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        Object newValue;

        // If the value is numeric then format it as a currency value
        if (value instanceof Number)
            newValue = NumberFormat.getCurrencyInstance().format(value);
        else
            newValue = value;

        // Add padding to right most column
        if ((newValue instanceof String) && (column == table.getModel().getColumnCount() - 1))
            newValue += "    ";

        // Now call the default renderer with the updated value
        final Component c = super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);

        // Return the component being rendered
        return c;
    }
}
