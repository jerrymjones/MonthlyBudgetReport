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

public abstract class Constants {
    /*
     * Report Period constants
    */
    public static final String[] periods            = {"Automatic", "This Year", "Last Year", "This Month", "Last Month", "Custom"};
    public static final int PERIOD_AUTOMATIC        = 0;
    public static final int PERIOD_THIS_YEAR        = 1;
    public static final int PERIOD_LAST_YEAR        = 2;
    public static final int PERIOD_THIS_MONTH       = 3;
    public static final int PERIOD_LAST_MONTH       = 4;
    public static final int PERIOD_CUSTOM           = 5;

    /*
     * Report subtotal constants
     */
    public static final String[] subtotal = {"None", "Subtotal by Month"};
    public static final int SUBTOTAL_NONE           = 0;
    public static final int SUBTOTAL_MONTH          = 1;
    
    /*
     * Report months list
     */
    // Note that months using the below array will be indexes from 0...11 NOT 1...12
    public static final String[] months             = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    // Short month names for subtotal by month column headers
    public static final String[] shortMonths        = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec", "Total"};

    /*
     * Configuration file constants
     */
    public static String CONFIG_FILE                = "Monthly Budget.mbcfg";
    public static String DEFAULT_REPORT             = "default_report";
    public static String UNSAVED_REPORT             = "<Report Not Memorized>";

    /*
     * Table column widths
     */
    public static final int CATEGORY_WIDTH          = 400;
    public static final int VALUE_WIDTH             = 200;

    public static final int PRINT_CATEGORY_WIDTH    = 240;
    public static final int PRINT_VALUE_WIDTH       = 60; 
}
