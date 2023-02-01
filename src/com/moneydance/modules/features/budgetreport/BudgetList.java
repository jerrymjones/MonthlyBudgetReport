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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.infinitekind.moneydance.model.Budget;
import com.infinitekind.moneydance.model.PeriodType;
import com.moneydance.apps.md.controller.FeatureModuleContext;


/**
* Create a list of budgets
* This class creates a list of existing new style monthly budgets.
*
* @author  Jerry Jones
*/
public class BudgetList {
	private final Map<String,Budget> mapBudgets = new HashMap<String,Budget>();

	/** 
     * Constructor for the BudgetList.
	 * 
	 * @param context - The context for this feature module.
     */
	public BudgetList(final FeatureModuleContext context) { 
		// Create the map of budgets by name and object - only load new style monthly budgets
		final List<Budget> listBudgets = context.getCurrentAccountBook().getBudgets().getAllBudgets();
		for (final Budget objBud: listBudgets) {
			if ((objBud.isNewStyle()) && (objBud.getPeriodType() == PeriodType.MONTH))
				this.mapBudgets.put(objBud.getName(),objBud);
		}
	}

	
	/** 
	 * This method returns a sorted array of the budget names found.
	 * 
	 * @return String[] - Budget names array
	 */
	public String [] getBudgetNames () {
		final Set<String> setNames = this.mapBudgets.keySet();
		final String [] arrNames = setNames.toArray(new String[0]);
		Arrays.sort(arrNames);
		return arrNames;
	}

	
	/** 
	 * This method returns a budget object given the name passed to it.
	 * 
	 * @param strName - The budget name to return
	 * @return Budget - The budget object given the name or null if not found.
	 */
	public Budget getBudget(final String strName) {
		return ( this.mapBudgets.get(strName) );
	}

	
	/** 
	 * This method returns a budget key value based on the budget name passed 
	 * to it.
	 * 
	 * @param strName - The budget name to return the key for
	 * @return String - The key for the given budget name or null if the budget
	 * name was not found.
	 */
	public String getBudgetKey (final String strName) {
		final Budget objBud = this.mapBudgets.get(strName);
		if (objBud != null)
			return objBud.getKey();
		else
			return null;
	}
}
