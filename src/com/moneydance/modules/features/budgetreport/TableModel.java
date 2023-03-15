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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.Account.AccountType;
import com.infinitekind.moneydance.model.AccountUtil;
import com.infinitekind.moneydance.model.BudgetItem;
import com.infinitekind.moneydance.model.BudgetItemList;
import com.infinitekind.moneydance.model.BudgetPeriod;
import com.infinitekind.moneydance.model.CurrencyType;
import com.infinitekind.moneydance.model.CurrencyUtil;
import com.infinitekind.moneydance.model.PeriodType;
import com.infinitekind.util.DateUtil;
import com.moneydance.apps.md.controller.FeatureModuleContext;

/**
* This class implements the table model for the budget editor table. The 
* table model supplies the data for the table.
*
* @author  Jerry Jones
*/
public class TableModel extends AbstractTableModel  {
    // The column names for the table
    private final String[] columnNames = {"Category","Budget","Actual","Difference"};
       
    // Main budget editor window
    BudgetReportWindow window;

    // The context of the extension
    private final FeatureModuleContext context;

    // The current data file
    private final AccountBook book;

    // Budget item list
    private BudgetItemList budgetItemList;

    // Budget Categories List
    private BudgetCategoriesList budgetCategoriesList;

    // The decimal separator character
    private char separator;
    
    public TableModel(final BudgetReportWindow window, final FeatureModuleContext context) {
        // Save main window for later
        this.window = window;
        
        // Save context for later
        this.context = context;

        // Save the account book for later
        this.book = context.getCurrentAccountBook();

        // Get the decimal separator for this locale
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        this.separator = symbols.getDecimalSeparator();

        // Load the category and budget data from Moneydance
        this.LoadData();
    }

    /**
     * Method to load the data for the table.
     */
    public void LoadData() {
        // Get the start month and number of months from the current report
        final Report currentReport = this.window.getCurrentReport();
        final int startMonth = currentReport.getStartMonth();
        final int months = (currentReport.getEndMonth() -  currentReport.getStartMonth()) + 1;

        // Get the budget item list
        final MyBudgetList budgetList = new MyBudgetList(this.context);
        if (budgetList.getBudgetCount() == 0)
            {
            // Display an error message - No budgets exist!
            JOptionPane.showMessageDialog( this.window,
            "No monthly style budgets have been created.  Use 'Tools:Budget Manager' to create a monthly budget before using this extension.",
            "Error (Monthly Budget Report)",
            JOptionPane.ERROR_MESSAGE);
            
            return;
            }

        this.budgetItemList = budgetList.getBudget(currentReport.getBudgetName()).getItemList();

        // Create a new Budget Categories list
        this.budgetCategoriesList = new BudgetCategoriesList(this.book);

        // Create a special category for the Income - Expenses total row
        this.budgetCategoriesList.add(Constants.UUID_OVERALL, "Income-Expenses", Account.AccountType.ROOT, 0);

        // Add a special category to the table for "Income"
        this.budgetCategoriesList.add(Constants.UUID_INCOME, "Income", Account.AccountType.INCOME, 1);

        // Iterate through the accounts to find all active Income categories
        // Note that accounts and categories are the same, they are all Accounts. 
        for (final Iterator<Account> iter = AccountUtil.getAccountIterator(this.context.getCurrentAccountBook()); iter.hasNext(); ) 
            {
            // Get the account 
            final Account acct = iter.next();

            // Go add category if it's the right type and if it's an income category
            this.addIf(acct, Account.AccountType.INCOME, startMonth, months);
            }

        // Add a special category to the table for "Expenses"
        this.budgetCategoriesList.add(Constants.UUID_EXPENSE, "Expenses", Account.AccountType.EXPENSE, 1);

        // Iterate through the accounts to find all active Expense categories
        for (final Iterator<Account> iter = AccountUtil.getAccountIterator(this.context.getCurrentAccountBook()); iter.hasNext(); ) 
            {
            // Get the account 
            final Account acct = iter.next();

            // Go add category if it's the right type and if it's an expense category
            this.addIf(acct, Account.AccountType.EXPENSE, startMonth, months);
            }

        // Update the report header
        this.window.updateHeader();

        // Update the table
        this.fireTableDataChanged();
    }


    /**
     * This method adds an account (category) to the budget category list if
     * it meets the right criteria - it must be active and not hidden as well
     * as being the proper type.
     * 
     * @param acct - The account to add 
     * @param type - The account type we're looking for,
     * @param startMonth - The starting month to retrieve,
     * @param months - The number of months to retrieve.
     */
    private void addIf(final Account acct, final AccountType type, final int startMonth, final int months) 
    {
    // Get the type of this account
    final AccountType acctType = acct.getAccountType();

    // Is the account type that we're looking for?    
    if (acctType == type)
        {
        // Is the account active
        if ((!acct.getAccountOrParentIsInactive()) && (!acct.getHideOnHomePage()))
            {
            // Add this category
            final BudgetCategoryItem item = this.budgetCategoriesList.add(acct);
            if (item == null)
                return;

            // If this is not a roll-up category then we need to get the current budget values for this category
            if (!item.hasChildren())
                {
                for (int month = startMonth; month < (startMonth + months); month++)
                    {
                    // Find existing budget values for each month
                    final BudgetItem i = this.budgetItemList.getBudgetItemForCategory(acct, new BudgetPeriod(DateUtil.getDate(this.getBudgetYear(), month, 1), PeriodType.MONTH));
                    if (i != null)
                        item.setBudgetValueForMonth(this.window.getModel(), this.budgetCategoriesList, month, i.getAmount(), acctType);
                    }
                }

            // Retrieve the actual totals for this account
            new TransactionTotals(item, this.context, acct, this.getBudgetYear(), startMonth, months);

            // Update the parent actual totals
            item.updateParentActualTotals(this.budgetCategoriesList, item);
            }
        }
    }
    
    
    /** 
     * Method to return a BudgetCategoryItem object given the row from the table.
     * 
     * @param row - The row from the table which is the index into the category items.
     * @return BudgetCategoryItem - The selected BudgetCategoryItem or null if there isn't one.
     */
    public BudgetCategoryItem getBudgetCategoryItem(final int row) {
        return this.budgetCategoriesList.getCategoryItemByIndex(row);
    }

    
    /** 
     * Method to get the BudgetCategoryList object.
     * 
     * @return BudgetCategoriesLis - The budgetCategoryList object requested.
     */
    public BudgetCategoriesList getBudgetCategoriesList() {
        return this.budgetCategoriesList;
    }

    
    /** 
     * Method to get the budget year.
     * 
     * @return int - The budget year.
     */
    public int getBudgetYear() {
        return this.window.getCurrentReport().getYear();
    }

    
    /** 
     * Method to get the number of columns in the table.
     * 
     * @return int - Number of columns.
     */
    @Override
    public int getColumnCount() {
        // Get the current report
        final Report currentReport = this.window.getCurrentReport();

        // Now determine the column count for the selected subtotal type
        if (currentReport.getSubtotalBy() == Constants.SUBTOTAL_NONE)
            return this.columnNames.length;
        else if (currentReport.getSubtotalBy() == Constants.SUBTOTAL_MONTH)
            return ((((currentReport.getEndMonth() + 1) - currentReport.getStartMonth()) * 3) + 1 + 3); // The last + 3 adds the grand totals
        else
            return 0; // Invalid subtotal selection (Shouldn't get here)
    }

    
    /** 
     * Method to get the number of rows (number of categories).
     * 
     * @return int - Number of rows.
     */
    @Override
    public int getRowCount() {
        return this.budgetCategoriesList.getCategoryCount();
    }

    
    /** 
     * Method to get the column name.
     * 
     * @param column - The column index [0...36].
     * @return String - The column name.
     */
    @Override
    public String getColumnName(final int column) {
        if (column == 0)
            return this.columnNames[0];
        else
            {
            // Add month if subtotal by month
            final Report currentReport = this.window.getCurrentReport();
            if ((currentReport.getSubtotalBy() == Constants.SUBTOTAL_MONTH) && ((column - 1) % 3 == 0))
                {
                // Use "Total" for last column
                if (column >= this.getColumnCount() - 3)
                    return this.columnNames[1 + ((column - 1) % 3)]+": "+Constants.shortMonths[12];
                // Otherwise short month name
                else
                    return this.columnNames[1 + ((column - 1) % 3)]+": "+Constants.shortMonths[(column - 1) / 3];
                }
            else
                return this.columnNames[1 + ((column - 1) % 3)];
            }
    }

    
    /** 
     * Method to get the value at a specific row and column.
     * 
     * @param row - The row in the table.
     * @param column - The column in the table.
     * @return Object - The value at the specified row and column.
     */
    @Override
    // Returns a Double for editable cells - be careful when saving
    public Object getValueAt(final int row, final int column) {
        // Get the current report
        final Report currentReport = this.window.getCurrentReport();

        // Get the category item
        final BudgetCategoryItem item = this.budgetCategoriesList.getCategoryItemByIndex(row);
        if (item != null)
            {
            // Get the selected currency type
            CurrencyType toType;
            if (currentReport.isUseCategoryCurrency())
                toType = item.getCurrencyType();                    // Category currency
            else
                toType = this.book.getCurrencies().getBaseType();   // Base currency

            // Category names
            if (column == 0)    // Category name
                {
                // Display the category indented per the indent level
                return (item.getIndentLevel() == 0) ? 
                    "    "+item.getShortName() : 
                    String.format("    %1$" + item.getIndentLevel() * 6 + "s%2$s", "", item.getShortName());
                }
            else
                {
                // If the row is a parent row and 
                if ((item.hasChildren() == true) && (!currentReport.isSubtotalParents()))
                    return (new String(""));
            
                // Budget values and totals
                else if ((column - 1) % 3 == 0)   // Budget
                    {                
                    if ((currentReport.getSubtotalBy() == Constants.SUBTOTAL_NONE) || (column > (((currentReport.getEndMonth() + 1) - currentReport.getStartMonth()) * 3)))
                        return (toType.formatFancy(CurrencyUtil.convertValue(item.getBudgetTotal(), item.getCurrencyType(), toType), this.separator));
                    else
                        return (toType.formatFancy(CurrencyUtil.convertValue(item.getBudgetValueForMonth(currentReport.getStartMonth() + ((column - 1) / 3)), item.getCurrencyType(), toType), this.separator));
                    }
                else if ((column - 1) % 3 == 1)   // Actuals
                    {
                    if ((currentReport.getSubtotalBy() == Constants.SUBTOTAL_NONE) || (column > (((currentReport.getEndMonth() + 1) - currentReport.getStartMonth()) * 3)))
                        return (toType.formatFancy(CurrencyUtil.convertValue(item.getActualTotal(), item.getCurrencyType(), toType), this.separator));
                    else
                        return (toType.formatFancy(CurrencyUtil.convertValue(item.getActualTotalForMonth(currentReport.getStartMonth() + ((column - 1) / 3)), item.getCurrencyType(), toType), this.separator));
                    }
                else if ((column - 1) % 3 == 2)   // Difference
                    {
                    if ((item.getCategoryType() == Account.AccountType.ROOT) || (item.getCategoryType() == Account.AccountType.INCOME))
                        {
                        if ((currentReport.getSubtotalBy() == Constants.SUBTOTAL_NONE) || (column > (((currentReport.getEndMonth() + 1) - currentReport.getStartMonth()) * 3)))    
                            return (toType.formatFancy(CurrencyUtil.convertValue(item.getActualTotal() - item.getBudgetTotal(), item.getCurrencyType(), toType), this.separator));
                        else
                            return (toType.formatFancy(CurrencyUtil.convertValue(item.getActualTotalForMonth(currentReport.getStartMonth() + ((column - 1) / 3)) - item.getBudgetValueForMonth(currentReport.getStartMonth()  + ((column - 1) / 3)), item.getCurrencyType(), toType), this.separator));
                       }
                    else
                        {
                        if ((currentReport.getSubtotalBy() == Constants.SUBTOTAL_NONE) || (column > (((currentReport.getEndMonth() + 1) - currentReport.getStartMonth()) * 3)))
                            return (toType.formatFancy(CurrencyUtil.convertValue(item.getBudgetTotal() - item.getActualTotal(), item.getCurrencyType(), toType), this.separator));    
                        else
                            return (toType.formatFancy(CurrencyUtil.convertValue(item.getBudgetValueForMonth(currentReport.getStartMonth() + ((column - 1) / 3)) - item.getActualTotalForMonth(currentReport.getStartMonth()  + ((column - 1) / 3)), item.getCurrencyType(), toType), this.separator));
                        }
                    }
                else
                    {
                    System.err.println("ERROR: The column is out of range in getValueAt.");
                    return null;
                    }
                }
            }
        else
            {
            System.err.println("ERROR: Item is null in getValueAt.");
            return null;
            }
    }

    
    /** 
     * Method to get the class for the requested column.
     * 
     * @param column - The column in the table.
     * @return Class<?> - The class for the column.
     */
    @Override
    public Class<?> getColumnClass(final int column) {
        return this.getValueAt(0, column).getClass();
    }
}
    