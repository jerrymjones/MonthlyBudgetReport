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

import java.io.Serializable;

public class Report implements Serializable {
    /*
	 * Static and transient fields are not stored
	 */
    private static final long serialVersionUID = 1L;

    // true when this is a memorized report
    private transient boolean memorized;

    /*
	 * Fields to be stored
	 */
    private String  reportName;  // This is also the file name for memorized reports
    private String  budgetName;
    private int     period;
    private int     year;
    private int     startMonth;
    private int     endMonth;
    private int     subtotalBy;
    private boolean subtotalParents;
    private boolean categoryCurrency;

    /**
     * Create a new report.
     * 
     * @param reportName - The name of the report, usually the file name as well.
     * @param budgetName - The budget name to use for this report.
     * @param period - The period selected.
     * @param year - The year, if manually entered.
     * @param startMonth - The beginning month, if manually entered.
     * @param endMonth - The ending month, if manually entered.
     * @param subtotalBy - The subtotal by selection.
     * @param subtotalParents - True if parent categories should be totaled and displayed.
     * @param categoryCurrency - True the categories should be displayed in their currency, false for base currency.
     */
    public Report(final String reportName, final String budgetName, final int period, final int year, final int startMonth, final int endMonth, final int subtotalBy, final boolean subtotalParents, final boolean categoryCurrency) {
        this.reportName = reportName;
        this.budgetName = budgetName;
        this.period = period;
        this.year = year;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.subtotalBy = subtotalBy;
        this.subtotalParents = subtotalParents;
        this.categoryCurrency = categoryCurrency;
        this.memorized = false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Report [budgetName=" + this.budgetName + ", period=" + this.period + ", year=" + this.year + ", startMonth="
                + this.startMonth + ", endMonth=" + this.endMonth + ", subtotalBy=" + this.subtotalBy + ", subtotalParents="
                + this.subtotalParents + ", categoryCurrency " + this.categoryCurrency + "]";
    }

    /**
     * @return the serialversionuid 
     */
    public static long getSerialversionuid() {
        return Report.serialVersionUID;
    }
  
    /**
     * Method to get the memorized flag.
     * 
     * @return the memorized flag, true if this is a memorized report, false if this is a default report
     */
    public boolean isMemorized() {
        return this.memorized;
    }

    /**
     * Method to set the report is memorized flag.
     * 
     * @param memorized - Set the memorized flag
     */
    public void setMemorized(boolean memorized) {
        this.memorized = memorized;
    }

    /**
     * @return the reportName
     */
    public String getReportName() {
        return this.reportName;
    }

    /**
     * @param reportName the reportName to set
     */
    public void setReportName(final String reportName) {
        this.reportName = reportName;
    }

    /**
     * @return the budgetName
     */
    public String getBudgetName() {
        return this.budgetName;
    }
    
    /**
     * @param budgetName the budgetName to set
     */
    public void setBudgetName(final String budgetName) {
        this.budgetName = budgetName;
    }
    
    /**
     * @return the period
     */
    public int getPeriod() {
        return this.period;
    }
    
    /**
     * @param period the period to set
     */
    public void setPeriod(final int period) {
        this.period = period;
    }
    
    /**
     * @return the year
     */
    public int getYear() {
        return this.year;
    }
    
    /**
     * @param year the year to set
     */
    public void setYear(final int year) {
        this.year = year;
    }
    
    /**
     * @return the startMonth
     */
    public int getStartMonth() {
        return this.startMonth;
    }
    
    /**
     * @param startMonth the startMonth to set
     */
    public void setStartMonth(final int startMonth) {
        this.startMonth = startMonth;
    }
    
    /**
     * @return the endMonth
     */
    public int getEndMonth() {
        return this.endMonth;
    }
    
    /**
     * @param endMonth the endMonth to set
     */
    public void setEndMonth(final int endMonth) {
        this.endMonth = endMonth;
    }
    
    /**
     * @return the subtotalBy
     */
    public int getSubtotalBy() {
        return this.subtotalBy;
    }
    
    /**
     * @param subtotalBy the subtotalBy to set
     */
    public void setSubtotalBy(final int subtotalBy) {
        this.subtotalBy = subtotalBy;
    }

    /**
     * @return the subtotalParents flag
     */
    public boolean isSubtotalParents() {
        return this.subtotalParents;
    }

    /**
     * @param subtotalParents the subtotalParents to set
     */
    public void setSubtotalParents(final boolean subtotalParents) {
        this.subtotalParents = subtotalParents;
    }
        
    /**
     * @return the isUseCategoryCurrency flag
     */
    public boolean isUseCategoryCurrency() {
        return this.categoryCurrency;
    }

    /**
     * @param subtotalParents the subtotalParents to set
     */
    public void setCategoryCurrency(final boolean categoryCurrency) {
        this.categoryCurrency = categoryCurrency;
    }
}
