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

import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.Account.AccountType;

/**
* Class for budget category items
* Each budget category is contained within a budget category item including
* special and roll-up totals categories.
*
* <p><b>Note:</b> In Moneydance a category is just another account.
*
* @author  Jerry Jones
*/
final class BudgetCategoryItem {
    // The category item row of the parent item this category rolls up to if any. -1 means no parent.
    private int parentRow = -1;    

    // The account for tis category
    private final Account account;

    // The short name for this category. This is the final name without parents
    // prepended i.e. "Fuel" not "Auto:Fuel"
    private final String shortName;

    // The indent level of this category. Used for indenting the categories when displaying them
    // and for determining the categories parent.
    private final int indentLevel;

    // The type of account. Account.AccountType.ROOT (Totals),
    // Account.AccountType.Income (Income) or Account.AccountType.EXPENSE (Expenses)
    private final Account.AccountType categoryType;
    
    // WHen true, this category has children and no budget values should exist for this category.
    private final boolean hasChildren; 

    // budgetValues [0] is not used, [1...12] each monthly budget, [13] overall budget total for this category
    private final Long budgetValues[] = {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};

	// actualTotals [0] is not used, [1...12] each monthly actual total, [13] overall actual total for this category
    private final Long actualTotals[] = {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};

    /**
     * Constructor to add a normal category as opposed to a special category.
     * 
     * @param acct - The category (account) to add.
     * @param type - The type of account, either
     * Account.AccountType.Income (Income) or Account.AccountType.EXPENSE (Expenses)
     * @param parent - The parent index for this category.
     * @param indent - The indent level for this category.
     * @param hasChildren - true if this category has children, false otherwise.
     */
    BudgetCategoryItem(final Account acct, final Account.AccountType type, final int parent, final int indent, final boolean hasChildren ) {
        // Save the account
        this.account = acct;

        // Save the short category name;
        this.shortName = this.account.getAccountName();

        // Calculate the indent level (Count colons)
        this.indentLevel = indent;

        // Save the Category type
        this.categoryType = type;

        // Save flag indicating if this category has children and thus shouldn't be edited
        this.hasChildren = hasChildren;

        // The parent category this ine rolls up to
        this.parentRow = parent;
    }


    /**
     * Constructor to add a special category (Income-Expense, Income or Expense)
     * 
     * @param name - The name of the special category.
     * @param type - Account.AccountType.ROOT (Totals),
     * Account.AccountType.Income (Income) or Account.AccountType.EXPENSE (Expenses)
     * @param parent - The parent index for this category.
     * @param indent - The indent level for this category.
     */
    BudgetCategoryItem(final String name, final Account.AccountType type, final int parent, final int indent) {
        // Special accounts don't have an account object
        this.account = null;

        // Save the short category name;
        this.shortName = name;

        // The indent level of special accounts is always 0
        this.indentLevel = indent;

        // Save the Category type
        this.categoryType = type;

        // Special accounts always have children
        this.hasChildren = true;

        // The parent category this ine rolls up to
        this.parentRow = parent;
    }

    
    /** 
     * Get the Account object for this category.
     * 
     * @return Account - The account object.
     */
    public Account getAccount() {
        return this.account;
    }

    
    /** 
     * Get the hasChildren flag for this category.
     * 
     * @return boolean - true if the category has children, false otherwise.
     */
    public boolean getHasChildren() {
        return this.hasChildren;
    }

    
    /** 
     * Get the short name of this category.
     * 
     * @return String - The short name of this category.
     */
    public String getShortName() {
        return this.shortName;
    }

    
    /** 
     * Get the indent level of this category.
     * 
     * @return int - The indent level of this category.
     */
    public int getIndentLevel() {
        return this.indentLevel;
    }

    
    /** 
     * Get the account type of this category.
     * 
     * @return AccountType - Account.AccountType.ROOT (Totals),
     * Account.AccountType.Income (Income) or Account.AccountType.EXPENSE (Expenses)
     */
    public Account.AccountType getCategoryType() {
        return this.categoryType;
    }

    
    /** 
     * Get the budget total for this category.
     * 
     * @return Long - The total of category months 1...12.
     */
    public Long getBudgetTotal() {
        return this.budgetValues[13];
    }

    
    /** 
     * Get the budget amount for the month requested.
     * 
     * @param month - The month to return (1...12).
     * @return Long - The budget value for the month requested.
     */
    public Long getBudgetValueForMonth(final int month) {
        return this.budgetValues[month];
    }


    /** 
     * Set the budget amount for the month requested.
     * 
     * @param model - The table model for the table.
     * @param budgetCategoriesList - The budget categories list object.
     * @param month - The month to set (1...12).
     * @param value - The new budget value.
     * @param type - The category type. Account.AccountType.Income (Income)
     * or Account.AccountType.EXPENSE (Expenses)
     */
    public void setBudgetValueForMonth(final TableModel model, final BudgetCategoriesList budgetCategoriesList, final int month, long value, final AccountType type) {
        long difference = 0;

        // Get previous budgetValue
        final Long previousValue = this.budgetValues[month];

        // Calculate the difference for updating the parent
        if ((this.parentRow == 0) && (type == Account.AccountType.EXPENSE))
            // If the next row is the overall totals and this is an expense change 
            // then we need to reverse the calculation
		    difference = previousValue - value;
        else
            // Calculate the difference between the old value and the new one
            difference = value - previousValue;

        // Save the new value
        this.budgetValues[month] = value;

        // Keep track of the total for this budget category
        this.budgetValues[13] = this.budgetValues[13] - previousValue + value;

        // Update parent
        if (this.parentRow != -1)
            {
            final BudgetCategoryItem parentItem = budgetCategoriesList.getCategoryItemByIndex(this.parentRow);
            if (parentItem != null)
                {
                value = parentItem.budgetValues[month] + difference;
                parentItem.setBudgetValueForMonth(model, budgetCategoriesList, month, value, type);

                // Notify all listeners that the value of the cell at [row, column] has been updated.
                if (model != null)
                    {
                    // Tell the model the month value changed
                    model.fireTableCellUpdated(this.parentRow, month);

                    // Tell the model the row total value changed
                    model.fireTableCellUpdated(this.parentRow, 13);
                    }
                }
            else
                System.err.println("Error: Parent item is null in setBudgetValueForMonth.");
            }
        }

    /** 
     * Get the actuals total for this category.
     * 
     * @return Long - The actuals total of category months 1...12.
     */
    public Long getActualTotal() {
        return this.actualTotals[13];
    }

    /** 
     * Set the actuals total for this category.
     * 
     * @param value = The value to set.
     */
    public void setActualTotal(final long value) {
        this.actualTotals[13] = value;
    }
  
    /** 
     * Get the monthly actual amount for the month requested.
     * 
     * @param month - The month to return (1...12).
     * @return Long - The actual value for the month requested.
     */
    public Long getActualTotalForMonth(final int month) {
        return this.actualTotals[month];
    }

    /** 
     * Set the monthly actual amount for the month requested.
     * 
     * @param month - The month to set (1...12).
     * @param value = The value to set.    
     */
    public void setActualTotalForMonth(final int month, final long value) {
        this.actualTotals[month] = value;
    }
   
    
    /** 
     * This method updates the parent actual totals with the specified child
     * item's actual spending.
     * 
     * @param budgetCategoriesList
     * @param item - The original child item that was updated
     */
    public void updateParentActualTotals(final BudgetCategoriesList budgetCategoriesList, final BudgetCategoryItem childItem) {
    // Update parent if it exists
    if (this.parentRow != -1)
        {
        final BudgetCategoryItem parentItem = budgetCategoriesList.getCategoryItemByIndex(this.parentRow);
        if (parentItem != null)
            {
            // Iterate through each month
            for (int month = 1; month <= 12; month++)
                {
                // The root account is the Income-Expense row so this needs to be treated differently
                if (parentItem.categoryType == Account.AccountType.ROOT)
                    {
                    // Income adds to the Income-Expense total
                    if (this.categoryType == Account.AccountType.INCOME)
                        {
                        parentItem.setActualTotalForMonth(month, parentItem.getActualTotalForMonth(month) + childItem.getActualTotalForMonth(month));
                        parentItem.setActualTotal(parentItem.getActualTotal() + childItem.getActualTotalForMonth(month));
                        }
                    else    // Expense subtracts from the Income-Expense total
                        {
                        parentItem.setActualTotalForMonth(month, parentItem.getActualTotalForMonth(month) - childItem.getActualTotalForMonth(month));
                        parentItem.setActualTotal(parentItem.getActualTotal() - childItem.getActualTotalForMonth(month));
                        }
                    }
                else    // Regular rollup - add up expenses or income
                    {
                    parentItem.setActualTotalForMonth(month, parentItem.getActualTotalForMonth(month) + childItem.getActualTotalForMonth(month));
                    parentItem.setActualTotal(parentItem.getActualTotal() + childItem.getActualTotalForMonth(month));
                    }
                }

            // Walk up the parent tree updating all parents
            parentItem.updateParentActualTotals(budgetCategoriesList, childItem);
            }
        else
            System.err.println("Error: Parent item is null in updateParentActualTotals.");
        }
    }
}
