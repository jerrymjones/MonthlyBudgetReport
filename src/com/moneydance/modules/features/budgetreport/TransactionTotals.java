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

import com.infinitekind.moneydance.model.AbstractTxn;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.Txn;
import com.infinitekind.moneydance.model.TxnSearch;
import com.infinitekind.moneydance.model.TxnSet;
import com.infinitekind.util.DateUtil;
import com.moneydance.apps.md.controller.FeatureModuleContext;

public class TransactionTotals implements TxnSearch{
	// Transaction set for this account
	private final TxnSet txnSet;

	// The account object these totals are for
	private final Account acct;

	// The start date of the transactions to total
	private final int startDate;

	// The end date of the transactions to total (endDate is not included in the totals)
	private int endDate;

	/**
	 * Construct an TransactionTotals object to return actual spending totals by 
	 * month for a given account (category) given a start date and the 
	 * number of months to return.
	 * 
	 * @param context - The feature module (extension) context.
	 * @param acct - The account (category) to total.
	 * @param budgetYear - The year we are editing (YYYY).
	 * @param startMonth - The starting month to total (1...12).
	 * @param months - The number of months to total.
	 */
	public TransactionTotals(final BudgetCategoryItem item, final FeatureModuleContext context, final Account acct, final int budgetYear, final int startMonth, final int months) {
		// Save the account for later
		this.acct = acct;

		// Get the start date
		this.startDate = DateUtil.getDate(budgetYear, startMonth, 1);

		// Calculate the end date
		if ((startMonth + months) > 12)
			this.endDate = DateUtil.getDate(budgetYear + 1, 1, 1);
		else
			this.endDate = DateUtil.getDate(budgetYear, startMonth + months, 1);

		// Get a txnSet for the category specified
		this.txnSet = context.getCurrentAccountBook().getTransactionSet().getTransactions(this);
		// Calculate totals by month
		for (final AbstractTxn txnLine : this.txnSet) 
			{
			final int month = (txnLine.getDateInt() / 100) - (budgetYear * 100);
			if ((month > 0) && (month <= 12))
				{
				// Update the monthly total and grand total of all months requested
				if (acct.getAccountType() == Account.AccountType.INCOME)
					{
					item.setActualTotal(item.getActualTotal() - txnLine.getValue());
					item.setActualTotalForMonth(month, item.getActualTotalForMonth(month) - txnLine.getValue());
					}
				else
					{
					item.setActualTotal(item.getActualTotal() + txnLine.getValue());
					item.setActualTotalForMonth(month, item.getActualTotalForMonth(month) + txnLine.getValue());
					}
				}
			else
				System.err.println("ERROR: Calculated month was out of range - month: "+month);			
			}
	}

	
	/** 
	 * Override for TxnSearch (see com.infinitekind.moneydance.model.TxnSearch)
	 * 
	 * @param transaction - The transaction to compare.
	 * @return boolean - true if the account matches the criteria, false otherwise.
	 */
	@Override
	public boolean matches(final Txn transaction) {
		if (transaction.getAccount() == this.acct && transaction.getDateInt() >= this.startDate && transaction.getDateInt() < this.endDate)
			return true;
		else
			return false;
	}

	
	/** 
	 * Override for TxnSearch (see com.infinitekind.moneydance.model.TxnSearch)
	 * 
	 * @return boolean - Returns true if matches all, false otherwise.
	 */
	@Override
	public boolean matchesAll() {
		return false;
	}
}
