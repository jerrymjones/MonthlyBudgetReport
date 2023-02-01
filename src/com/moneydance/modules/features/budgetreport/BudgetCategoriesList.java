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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.infinitekind.moneydance.model.Account;

/**
* Create a list to hold budget category items
* This class creates a list of category items that are budgetable.
* Inactive categories or categories hidden from the home screen
* are not included in the list.
*
* @author  Jerry Jones
*/
public class BudgetCategoriesList {

    // Create an empty LinkedHashMap to hold the data
    private LinkedHashMap<String, BudgetCategoryItem> lhm = null;

    
    // Create an object to track the parent as categories are added to the list
    private final parentTracker tracker = new parentTracker();


    /** 
     * Default constructor for the BudgetCategoriesList.
     */
    public BudgetCategoriesList() {
        // Create a hash map for the categories
        this.lhm = new LinkedHashMap<String, BudgetCategoryItem>();
    }

    
    /** 
     * This method returns the number of categories in the budget categories
     * list.
     * 
     * @return int - Number of items in the list.
     */
    public int getCategoryCount() {
        return this.lhm.size();  
    }

    
    /** 
     * This method returns a set containing all the category items in the list.
     * 
     * @return Set<Entry<String, BudgetCategoryItem>>
     */
    public Set<Map.Entry<String, BudgetCategoryItem>> getCategorySet() {
        return this.lhm.entrySet();
    }

    
    /** 
     * Add a special category to the list - Totals, Income or Expense for example.
     * 
     * <p><b>Note:</b> Special categoties always have children.
     * 
     * @param name - The name of the special category to add.
     * @param type - The type of this category: Account.AccountType.ROOT (Totals),
     * Account.AccountType.Income (Income) or Account.AccountType.EXPENSE (Expenses).
     * @param level - The indent level of this category.
     * @return BudgetCategoryItem - Returns the BudgetCategoryItem object created 
     * for this category.
     */
    public BudgetCategoryItem add(final String name, final Account.AccountType type, final int level) {
        // Create a new budget category item for this category
        final BudgetCategoryItem bcItem = new BudgetCategoryItem(name, type, this.tracker.getParent(level, true), level);
        
        // Put the item in the hash map
        this.lhm.put(name, bcItem);

        // Return the new item to the caller
        return bcItem;
    }

    
    /** 
     * Add a regular Moneydance category to the list.
     * 
     * <p><b>Note:</b> A category in Moneydance is the same thing as an account.
     * 
     * @param acct - The account object of the category to add.
     * @param type - The type of this category: 
     * Account.AccountType.Income (Income) or Account.AccountType.EXPENSE (Expenses).
     * @return BudgetCategoryItem - Returns the BudgetCategoryItem object created 
     * for this category.
     */
    public BudgetCategoryItem add(final Account acct, final Account.AccountType type) {   
        // Get the sub-accounts of this account
        final List <Account> subAccts = acct.getSubAccounts();
        
        // Loop through the sub categories of this category to see if it has any active children
        // You can't simply use acct.getSubAccountCount() as it will also count inactive accounts.
        boolean hasChildren = false;
        for (final Iterator<Account> i = subAccts.iterator(); i.hasNext();) {
            final Account item = i.next();
            if ((!item.getAccountOrParentIsInactive()) && (!item.getHideOnHomePage()))
                {
                // We only have to find one to declare that this category has children
                hasChildren = true;
                break;
                }
        }

        // Get the full account name of the category item
        final String name = acct.getFullAccountName();

        // Get the indent level of this category
        final int indentLevel = BudgetCategoriesList.calcIndentLevel(name);
        
        // Create a new budget category item for this category
        final BudgetCategoryItem bcItem = new BudgetCategoryItem(acct, type, this.tracker.getParent(indentLevel, hasChildren), indentLevel, hasChildren);
        
        // Put the item in the hash map
        this.lhm.put(acct.getFullAccountName(), bcItem);

        // Return the new item to the caller
        return bcItem;
    }

    
    /**
     * This method returns a BudgetCategoryItem for the full account name
     * passed.
     *  
     * @param fullAcctName - The full account name i.e. Auto:Fuel.
     * @return BudgetCategoryItem - The BudgetCategoryItem object corresponding
     * to the full account name. A null return value indicates the item does
     * not exist.
     */
    public BudgetCategoryItem getCategoryItem(final String fullAcctName) {
        return this.lhm.get(fullAcctName);
    }
     
    
    /** 
     * This method returns a BudgetCategoryItem for the index passed.
     * 
     * @param index - The index [0...n] to retrieve.
     * @return BudgetCategoryItem - The BudgetCategoryItem object corresponding
     * to the index provided. Returns null if the index is not valid.
     */
    public BudgetCategoryItem getCategoryItemByIndex(final int index) {
        final Set<String> keys = this.lhm.keySet();
        return this.lhm.get(keys.toArray()[index]);
    }

    
    /** 
     * This method returns the short name for a category based on the full
     * account name passed.
     * 
     * @param fullAcctName - The full account name i.e. Auto:Fuel.
     * @return String - The short name for the category specified
     * by the full account name. Returns null if the full account
     * name is not valid.
     */
    public String getShortName(final String fullAcctName) {
        return this.lhm.get(fullAcctName).getShortName();
    }

    
    /** 
     * This method returns the indent level of the category based on the full
     * account name passed.
     * 
     * @param fullAcctName - The full account name i.e. Auto:Fuel.
     * @return int - The indent level of this category or -1 if the full account
     * name is not found.
     */
    public int getIndent(final String fullAcctName) {
        BudgetCategoryItem item = this.lhm.get(fullAcctName);
        if (item != null)
            return item.getIndentLevel();
        else
            return -1;
    }

    
    /** 
     * This method calculates the indent level given a long category name.
     * 
     * @param longName - The long category name i.e. "Auto:fuel"
     * @return int - The indent level of the category. For "Auto:fuel" 3 would
     * be returned.
     */
    private static int calcIndentLevel(final String longName) {
        // Start at 2 instead of 0 to account for special "Income", "Expense", and "Totals" top level categories
        int level = 2;
        for (int i = 0; i < longName.length(); i++) 
            {
            if (longName.charAt(i) == ':')
                level++;
            }

        return level;
    }


    /**
    * Track parents for items in the category list
    * This class keeps track of the parents at each level in the category
    * list so that children budgets can roll up properly to their parents.
    * 
    * <p><b>Note:</b> Levels are based on the indent level of each category.
    * See calcIndentLevel for more information.
    *
    * @author  Jerry Jones
    */
    public class parentTracker {
        private class level {    
            public int level;    
            public int parent;    

            public level(final int level, final int parent) {    
                this.level = level;    
                this.parent = parent;    
                }    
        }

        // Tracker for the current child level
        private int currentChildLevel;

        // Tracker for the current parent index
        private int currentParentIndex;
        
        // Stack (queue) to hold parent for each indent level
        private final Deque<level> stack;

        /** 
         * Default constructor for the parentTracker.
         */
        public parentTracker() {
            // Initialize the current tracking variables
            this.currentChildLevel = 0;
            this.currentParentIndex = -1;

            // Initialize a stack (queue) to hold parent for each indent level
            this.stack = new ArrayDeque<level>();
        }


        /** 
         * This method returns the parent given the indentLevel passed.
         * 
         * @param indentLevel - The indent level to return the parent for.
         * @param hasChildren - true if this category has children categories,
         * false otherwise.
         * @return int - The parent category index for this indent level.
         */
        public int getParent(final int indentLevel, final boolean hasChildren) {
            int parent;

            // If the indent level of this category is less than the current child level then go back to the parent at this level
            if (indentLevel < this.currentChildLevel)
                {
                // Pop items off the stack until this items level is equal to the one on the stack, 
                // set the currentChildLevel equal to this category’s level and currentParentIndex equal 
                // to the one from the stack.
                while (this.stack.isEmpty() != true) {
                    if (this.stack.peekFirst().level == indentLevel) {
                        // We found our level on the stack, restore the parent at this level
                        this.currentChildLevel = this.stack.peekFirst().level;
                        this.currentParentIndex = this.stack.peekFirst().parent;
                        break;
                        }
                    else
                        // Pop an item off the stack
                        this.stack.removeFirst();
                    }
                }
            
            // The parent to return is the current parent index
            parent = this.currentParentIndex;
            
            // Does this category have children?
            if (hasChildren)
                {
                // Push the currentParentIndex and currentChildLevel on the stack so we can go back to them later
                this.stack.addFirst(new level(this.currentChildLevel, this.currentParentIndex));

                // Set the current parent index to the index of this category when it is added
                this.currentParentIndex = BudgetCategoriesList.this.lhm.size();

                // Set the chidlrens indent level 
                this.currentChildLevel = indentLevel + 1;
                }

            return parent;
        } 
    }
}
