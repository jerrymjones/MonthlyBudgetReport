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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;

import javax.swing.table.TableCellRenderer;

import com.moneydance.apps.md.view.gui.MDColors;

/**
* This class extends EditingTable to provide additional features such as pop-up
* menu support to ease editing of budgets based on a variety of methods, and 
* budget initialization from prior year's actuals or budget.
*
* @author  Jerry Jones
*/
public class Table extends JTable {
	private final MDColors colors;
	private final TableModel model;

	static final float PRINT_FONT_SIZE = 7.0f;

	/**
	* Constructor for our table.
	*
	* @param model - The table model for this table.
	* @param colors - Moneydance color scheme.
	* @param forPrint - true when this table is configured for printing, false for display.
	*/
	public Table(final TableModel model, final MDColors colors, boolean forPrint) {
       	super(model);

		// Save the table model for later use
		this.model = model;

		// Save access to Moneydance colors
        this.colors = colors;
		
		// Is this table for printing or display
		if (forPrint)
			{	// Print
			// Set the font size for printing
			this.setFont(this.getFont().deriveFont(Table.PRINT_FONT_SIZE));
			this.tableHeader.setFont(this.getFont().deriveFont(Table.PRINT_FONT_SIZE));

			// Set default colors for printing
			this.setBackground(Color.WHITE);
			this.tableHeader.setBackground(new Color(215, 215, 215));
			this.tableHeader.setForeground(Color.BLACK);
			
			// Also, this makes the overall table look less squished.
			this.setRowHeight(Math.round(Table.PRINT_FONT_SIZE) + 5);
			}
		else	// Display
			{
			// Some themes have too small a row height so try to fix that here
			// Also, this makes the overall table look less squished.
			this.setRowHeight(this.getFont().getSize() + 15);
			}
	}


	/** 
	 * Prepare the renderer for the cell at the specified row and column.
	 * 
	 * @param renderer - The table cell renderer object.
	 * @param row - The row to reppare.
	 * @param column - The column to prepare.
	 * @return Component - The component being rendered.
	 */
	@Override
	public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) 
	{
		final Component c = super.prepareRenderer(renderer, row, column);

		// Are we displaying or printing the table?
		if (this.isPaintingForPrint())
			{
			c.setBackground(Color.WHITE);
			c.setForeground(Color.BLACK);

			// Alternate row colors in table for readability
			if ((row % 2) != 0) 
				c.setBackground(new Color(235, 235, 235));
			else
				c.setBackground(Color.WHITE);
			}
		else	// Displaying
			{
			// Ensures proper foreground color on all themes
			c.setForeground(this.colors.defaultTextForeground);

			// Alternate row colors in table for readability
			if ((row % 2) != 0) 
				c.setBackground(this.colors.headerBG);
			else
				c.setBackground(this.colors.listBackground);
			}

		// Set text color of totals. Other than the category name in column 0, any other 
		// cell could potentially be a total so we color it specially.
		if  (this.model.getBugetCategoryItem(row).getHasChildren())
			{
			if (row == 0)
				c.setForeground(new Color(33, 144, 255));	// Income-Expense row - Medium blue
			else
				c.setForeground(new Color(0, 204, 204));	// Other rollup rows - Dark Cyan
			}

		// Highlight negative difference values red
		if ((column !=0) && (column % 3 == 0))
			{
			Object value = this.getValueAt(row, column);
			if (value instanceof Number)
				{
				if (((Double)value < 0))
					c.setForeground(Color.RED);
				}
			}

		// Remove border from the cells. The editor will still set the selection border. This makes uneditable cells also appear unselectable.
		((JComponent) c).setBorder(null);

		// Return the component object being rendered
		return c;   
	}
}
