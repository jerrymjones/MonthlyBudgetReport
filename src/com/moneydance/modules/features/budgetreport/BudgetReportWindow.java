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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.Budget;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.view.gui.MDColors;
import com.moneydance.awt.AwtUtil;
import com.moneydance.awt.GridC;

/**
* Create the main window of the Budget Report extension.
* This class creates the main window of the Budget Report including the controls
* and table used to enter budget data. 
*
* @author  Jerry Jones
*/
public class BudgetReportWindow extends JFrame implements ComponentListener
{
  // Display Constants
  private static final int TOP_HEIGHT = 150;
 
  // Calculated frame width and height
  private int frameWidth;
  private int frameHeight;

  // Flag used to return error status back to Main
  public boolean bError = false;

  // Storage for the Main extension object
  private Main extension;

  // Extension context
  private FeatureModuleContext context;

  // The budget selection control
  private JComboBox<String> budgetSelector;

  // List of available monthly budgets that can be edited
  private MyBudgetList budgetList;
  
  // Budget map
  private final Map<String,Budget> mapBudgets = new HashMap<String,Budget>();

  // Storage for the table used to edit budget data
  private Table table;
  private TableModel tableModel = null;

  // Panels used to display information
  private JPanel topLtPanel;
  private JPanel topCtrPanel;
  private JPanel topRtPanel;
  private JPanel reportPanel;

  // Clickable User Guide link
  private JLabel helpLink;

  // The export button
  private JButton exportButton;

  // Controls needing access from outside doEdit
  private final JComboBox<String> startSelector = new JComboBox<String>(Constants.months);
  private final JComboBox<String> endSelector = new JComboBox<String>(Constants.months);
  private final JSpinner yearSelector = new JSpinner();

  // Report header
  private JLabel dateRange = null;

  // List of memorized report names
  private JComboBox<String> reportSelector = null;
  
  // The currently loaded report
  private Report currentReport = null;

  // The Moneydance colors for the current Moneydance theme
  private MDColors colors;

  /** 
   * Default constructor for the BudgetEditorWindow.
   */
  public BudgetReportWindow(final Main extension) 
  {
    // Title for main window
    super("Monthly Budget Report");

    // Save the extension for later
    this.extension = extension;

    // Get FeatureModule context
    this.context = extension.getUnprotectedContext();

    // Get the colors for the current Moneydance theme
    this.colors = com.moneydance.apps.md.view.gui.MDColors.getSingleton();

    // Get the list of available budgets
    this.budgetList = new MyBudgetList(this.context);

    // No Monthly budgets are available, so exit
    if (this.budgetList.getBudgetCount() == 0)
      {
      // Display an error message - No budgets exist!
      JOptionPane.showMessageDialog( this,
      "No monthly style budgets have been created.  Use 'Tools:Budget Manager' to create a monthly budget before using this extension.",
      "Error (Monthly Budget Report)",
      JOptionPane.ERROR_MESSAGE);
      
      // Tell main that window initialization failed
      this.bError = true;

      // Exit from the constructor
      return;
      }

    // Load the default report
    if (!this.loadReport(null))
      {
      this.bError = true;
      return;
      }

    /*
    * Configure the frame for the report screen
    */

    // Set the frame size based on the screen size of the primary display
    this.setFrameSize();

    // Set what to do on close
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.enableEvents(WindowEvent.WINDOW_CLOSING);

    // Center the frame on the screen
    AwtUtil.centerWindow(this);

    /*
    * Add the Top Panel - Configuration Options
    */
    final JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBackground(this.colors.headerBG);
    this.add( topPanel, BorderLayout.NORTH );

    /*
    ** Top right
    */
    // Create a panel in the upper right corner of the window
    this.topRtPanel = new JPanel(new GridBagLayout());
    this.topRtPanel.setBackground(this.colors.headerBG);
    topPanel.add( this.topRtPanel, BorderLayout.EAST);

      /*
      ** Edit Button
      */
      final JButton editButton = new JButton("Edit");
      editButton.setToolTipText("Change the report parameters");
      this.topRtPanel.add(editButton,GridC.getc(0,0).insets(10,5,10,5));

      // Create an action listener to dispatch the action when this button is clicked
      editButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          BudgetReportWindow.this.doEdit(true);
        }
      });

      /*
      ** Print Button
      */
      final JButton printButton = new JButton("Print");
      printButton.setToolTipText("Print the current report");
      this.topRtPanel.add(printButton,GridC.getc(1,0).insets(10,5,10,5));

      // Create an action listener to dispatch the action when this button is clicked
      printButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          BudgetReportWindow.this.doPrint();
        }
      });

      /*
      ** Memorize Button
      */
      final JButton memorizeButton = new JButton("Memorize");
      memorizeButton.setToolTipText("Memorize the current report");
      this.topRtPanel.add(memorizeButton,GridC.getc(2,0).insets(10,5,10,5));

      // Create an action listener to dispatch the action when this button is clicked
      memorizeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          BudgetReportWindow.this.doMemorize();
        }
      });

      /*
      ** Export Button
      */
      this.exportButton = new JButton("Export");
      this.exportButton.setToolTipText("Export the report data in various formats");
      this.topRtPanel.add(this.exportButton,GridC.getc(3,0).insets(10,5,10,15));

      // Create an action listener to dispatch the action when this button is clicked
      this.exportButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          BudgetReportWindow.this.doExport((Component)e.getSource());
        }
      });

    /*
    ** Top left panel
    */
    // Create a panel in the upper left corner of the window
    this.topLtPanel = new JPanel(new GridBagLayout());
    this.topLtPanel.setBackground(this.colors.headerBG);
    topPanel.add( this.topLtPanel, BorderLayout.WEST);

    // Add a clickable text link to request help
    this.helpLink = new JLabel("User Guide", JLabel.LEFT);
    this.helpLink.setForeground(new Color(33, 144, 255));

    // Set the preferred size of this item so that the center panel actually is
    // centered on the frame.
    final Dimension d = this.topRtPanel.getPreferredSize();
    d.setSize(d.width - 30, d.height);
    this.helpLink.setPreferredSize(d);
    
    // Add the help link
    this.topLtPanel.add(this.helpLink, GridC.getc(0, 0).insets(10, 15, 10, 15));

    // Create an action listener to dispatch the action when this label is clicked
    this.helpLink.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        BudgetReportWindow.this.showHelp();
      }
    });

    /*
    ** Create a center panel at the top to select the report to display.
    */
    this.topCtrPanel = new JPanel(new GridBagLayout());
    this.topCtrPanel.setBackground(this.colors.headerBG);
    topPanel.add( this.topCtrPanel, BorderLayout.CENTER );

    // Configure the report selector
    this.reportSelector = new JComboBox<String>();
    this.reportSelector.setEditable(false);
    this.reportSelector.setToolTipText("Select the memorized report to display");
    
    // Get a current list of report names
    this.updateReportSelector();
        
    // Add the report selector
    this.topCtrPanel.add(this.reportSelector, GridC.getc(0, 0).insets(10, 0, 10, 0));

    // Create an action listener to dispatch the action when this control is changed
    this.reportSelector.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        // Load the report unless the selector is set to "<Unsaved Report>"
        if (!BudgetReportWindow.this.reportSelector.getSelectedItem().toString().equals(Constants.UNSAVED_REPORT))
          {
          // Load the selected report
          BudgetReportWindow.this.loadReport(BudgetReportWindow.this.reportSelector.getSelectedItem().toString());
          
          // Reload the table
          BudgetReportWindow.this.tableModel.LoadData();
          
          // Update the report header
          BudgetReportWindow.this.updateHeader();

          // Update the memorized report list after loading a report in case
          // there was an <Unsaved Report> selector in the list before this
          // report was selected.
          BudgetReportWindow.this.updateReportSelector();
          }
        else
          {
          // Set the report name
          BudgetReportWindow.this.currentReport.setReportName(Constants.UNSAVED_REPORT);

          // Update the report header
          BudgetReportWindow.this.updateHeader();
          }
          
        }
      });


    /*
    * Add the Budget Report Table Panel
    */
    this.reportPanel = new JPanel(new BorderLayout());
    this.reportPanel.setPreferredSize(new Dimension( this.frameWidth, this.frameHeight - BudgetReportWindow.TOP_HEIGHT ));
    this.reportPanel.setForeground(this.colors.defaultTextForeground);
    this.reportPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    this.add( this.reportPanel, BorderLayout.CENTER ); 

    // Add the report header label
    this.dateRange = new JLabel();
    this.dateRange.setHorizontalAlignment(JLabel.CENTER);
    this.dateRange.setBorder(new EmptyBorder(0, 0, 15, 0));
    this.reportPanel.add(this.dateRange, BorderLayout.NORTH);

    // Create a table to use to display the budget values
    this.table = new Table(this.tableModel = new TableModel(this, this.context), this.colors, false);

    // Do not allow selection of an entire row
    this.table.setRowSelectionAllowed(false);

    // Do not allow columns to be reordered by dragging them
    this.table.getTableHeader().setReorderingAllowed(false);

    // Do not allow the JTable to do automatic resizing
    this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // Set the minimum width of the columns as well as the cell renderer
    this.forceTableStructureChange(false); 

    //Create the scroll pane and add the table to it.
    final JScrollPane scrollPane = new JScrollPane(this.table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    //Add the scroll pane to this panel.
    this.reportPanel.add(scrollPane, BorderLayout.CENTER);

    // Add a component listener so we get resize events
    this.reportPanel.addComponentListener(this);
  }

  /**
   * Method to update the report selector with memorized report names.
   */
  private void updateReportSelector() {
      // Create a default array in case no reports are found
      String[] reportNames = {Constants.UNSAVED_REPORT};

      // Find all reports that have been memorized
      final File rootFolder = this.context.getCurrentAccountBook().getRootFolder();
      final String[] filenames = rootFolder.list((f,name)->name.endsWith(".mbrpt"));

      // Did we find any reports?
      if (filenames.length != 0)
        {
        // Strip off the file extension from the file names to get the report names
        final List<String> v = new ArrayList<>();
        for (int i = 0; i < filenames.length; i++) 
          v.add(filenames[i].replace(".mbrpt", ""));
        reportNames = v.toArray(new String[v.size()]);
        Arrays.sort(reportNames);
        }

      // Update the report selector
      if (this.reportSelector != null)
        {
        this.reportSelector.setModel(new DefaultComboBoxModel<String>(reportNames));
        this.reportSelector.setSelectedItem(this.currentReport.getReportName()); 
        }
    } 

  /** 
   * Method called to update the report header
   */
  public void updateHeader() {
    this.dateRange.setText(Constants.months[this.currentReport.getStartMonth() - 1]+" "+this.currentReport.getYear()+" - "
        +Constants.months[this.currentReport.getEndMonth() - 1]+" "+this.currentReport.getYear());
  }

  /** 
   * Processes events on this window.
   * 
   * @param e - The event that sent us here.
   */
  public final void processEvent(final AWTEvent e) 
  {
    if(e.getID() == WindowEvent.WINDOW_CLOSING) 
      this.extension.closeConsole();
    super.processEvent(e);
  }


  /** 
   * Load a report 
   * 
   * @param name - The name of the report to load or null if loading the default report
   * @return boolean - true if successful, false otherwise
   */
  public boolean loadReport(String name) {
    // Get current time
    final Calendar now = Calendar.getInstance();
    final int thisYear = now.get(Calendar.YEAR);
    final int thisMonth = now.get(Calendar.MONTH) + 1;  // Calendar months are 0...11 and we want 1...12
    
    // If a name is not given then load the default report
    if (name == null)
      {
      // Get the default report name
      final Account rootAccount = this.context.getCurrentAccountBook().getRootAccount();
      name = rootAccount.getPreference(Constants.DEFAULT_REPORT, null);

      // If the name is still null there is no default so we need to create one 
      if (name == null)
        {
        // Create a default report
        this.currentReport = new Report(Constants.UNSAVED_REPORT, "Budget", Constants.PERIOD_AUTOMATIC, thisYear, 1, thisMonth, Constants.SUBTOTAL_NONE, true);

        // Allow the user to edit the default report
        this.doEdit(false);

        // Successfully created a default report
        return (true);
        }
      }

    // Get the file path to load the report from
    final File rootFolder = this.context.getCurrentAccountBook().getRootFolder();
    final String filePath = rootFolder.getAbsolutePath()+File.separator+name+".mbrpt";

    // Load the report
    try 
      {
      // Open the input streams
      final FileInputStream fileIn = new FileInputStream(filePath);
      final ObjectInputStream ois = new ObjectInputStream(fileIn);

      // Only allow our Report objects
      final ObjectInputFilter filter = ObjectInputFilter.Config.createFilter("com.moneydance.modules.features.budgetreport.Report;!*");
      ois.setObjectInputFilter(filter);

      // Read in the report
      this.currentReport = (Report) ois.readObject();

      // Set the report loaded from file flag (meaning it's memorized)
      this.currentReport.setMemorized(true);

      // Close the streams
      ois.close();
      fileIn.close();
      } 
    catch (final IOException i) 
      {
      if (i instanceof FileNotFoundException)
        {
        // The report specified wasn't found
        JOptionPane.showMessageDialog( this,
        "The selected report ("+name+") does not exist.",
        "Report Not Found Error",
        JOptionPane.ERROR_MESSAGE);

        // Exit with failure status
        return false;
        }
      else
        {
        // For debugging
        i.printStackTrace();

        // IO Error trying to load the report
        JOptionPane.showMessageDialog( this,
        "IO Error trying to load the report ("+name+"). "+i.toString(),
        "File IO Error",
        JOptionPane.ERROR_MESSAGE);

        // Exit with failure status
        return false;
        }
      }
    catch (final ClassNotFoundException c) 
      {
      // For debugging
      c.printStackTrace();

      // Invalid report - class not found
      JOptionPane.showMessageDialog( this,
      "Invalid report ("+name+") - class not found. "+c.toString(),
      "Invalid Report Error",
      JOptionPane.ERROR_MESSAGE);

      // Exit with failure status
      return false;
      }

    // Verify the budget name exists
    if (this.budgetList.getBudget(this.currentReport.getBudgetName()) == null)
      {
      // This report is no longer valid since the budget cannot be found so we will delete it
      JOptionPane.showMessageDialog( this,
      "The selected report is invalid because the budget used by it ("+this.currentReport.getBudgetName()+") does not exist. This report will be deleted.",
      "Error",
      JOptionPane.ERROR_MESSAGE);

      // Delete the report
      final File rptFile = new File(filePath);
      if (rptFile.exists())
        rptFile.delete();

      // If this was the default report then set the default report to null so we don't try to use it again
      final Account rootAccount = this.context.getCurrentAccountBook().getRootAccount();
      if (name.equals(rootAccount.getPreference(Constants.DEFAULT_REPORT, null)))
        rootAccount.setPreference(Constants.DEFAULT_REPORT, null);

      // We don't have a current report
      this.currentReport = null;
      return false;  
      }

    // Depending on period, load the year, startMonth, and endMonth fields based on current time
    switch (this.currentReport.getPeriod()) 
      {
      case Constants.PERIOD_AUTOMATIC:
        this.currentReport.setYear(thisYear);
        this.currentReport.setStartMonth(1);
        this.currentReport.setEndMonth(thisMonth);
        break;

      case Constants.PERIOD_THIS_YEAR:
        this.currentReport.setYear(thisYear);
        this.currentReport.setStartMonth(1);
        this.currentReport.setEndMonth(12);
        break;
      
      case Constants.PERIOD_LAST_YEAR:
        this.currentReport.setYear(thisYear - 1);
        this.currentReport.setStartMonth(1);
        this.currentReport.setEndMonth(12);
        break;

      case Constants.PERIOD_THIS_MONTH:
        this.currentReport.setYear(thisYear);
        this.currentReport.setStartMonth(thisMonth);
        this.currentReport.setEndMonth(thisMonth);
        break;    

      case Constants.PERIOD_LAST_MONTH:
        if (now.get(Calendar.MONTH) == 0)
          {
          this.currentReport.setYear(thisYear - 1);
          this.currentReport.setStartMonth(12);
          this.currentReport.setEndMonth(12);
          }
        else
          {
          this.currentReport.setYear(thisYear);
          this.currentReport.setStartMonth(thisMonth - 1);
          this.currentReport.setEndMonth(thisMonth - 1);
          }
        break;    
        
      // The year, startMonth and endMonth are already in the report data  
      case Constants.PERIOD_CUSTOM:
      default:
        break;
      }

    // Update the table structure
    this.forceTableStructureChange(true);
     
    // Successfully loaded the report
    return true;
  }  

  
  /** 
   * Force a table structure change when the number of months in a report changes or
   * when subtotal by month changes.
   * 
   * @param informTableModel - True when the table model should be informed of the change.
   */
  private void forceTableStructureChange(final Boolean informTableModel) {
    // Adjust the column widths
    this.resizeColumns(informTableModel);
    
    // Force a resize of the main window
    this.pack();
  }

  private void resizeColumns(final Boolean informTableModel) {
    // If the table model is defined then force a structure change
    if (this.tableModel != null)
      {
      // If requested, tell the table model that the structure changed   
      if (informTableModel) 
        this.tableModel.fireTableStructureChanged();

      // Get the column count
      final int colCount = this.tableModel.getColumnCount();

      // Calculate the column adjustment needed to fill the viewport width if any
      int colAdj = 0;
      final int extraSpace = this.getWidth() - 55 - Constants.CATEGORY_WIDTH - (Constants.VALUE_WIDTH * (colCount - 1));
      if (extraSpace > 0)
        colAdj = extraSpace / colCount;

      // Get the column model
      final TableColumnModel colModel = this.table.getColumnModel();

      // Set column renderer and column sizes
      for (int i = 0; i < colCount; i++ ) 
        {
        // Get the TableColumn object for each column
        final TableColumn colSelect = colModel.getColumn(i);

        // Is this column 0 (Category)?
        if (i == 0)
          colSelect.setPreferredWidth(Constants.CATEGORY_WIDTH + colAdj);
        else
          {
          // Set the cell renderer for the numeric cells
          colSelect.setCellRenderer(new CurrencyTableCellRenderer());

          // If this is a table subtotaled by month then set the minimum width of the column
          colSelect.setPreferredWidth(Constants.VALUE_WIDTH + colAdj);
          }
        }
      }
    }

  /**
   * @return the currentReport
   */
  public Report getCurrentReport() {
    return this.currentReport;
  }

  /** 
   * Get a budget key given the budget name.
   * 
   * @param strName - The name of the budget to retrieve the key for.
   * @return String - The key for the budget name or null if the name wasn't found.
   */
	public String getBudgetKey (final String strName) {
		final Budget objBud = this.mapBudgets.get(strName);
		if (objBud != null)
			return objBud.getKey();
		else
			return null;
	}

  
  /** 
   * Get the table model for the table.
   * 
   * @return BudgetEditorTableModel - The table model.
   */
  public TableModel getModel() {
    return this.tableModel;
  }

  /**
   * Set the frame size based on the width and height of the display.
   */
  private void setFrameSize() {
    final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    this.frameWidth  = ( gd.getDisplayMode().getWidth() * 85 ) / 100;
    this.frameHeight = ( gd.getDisplayMode().getHeight() * 85 ) / 100;
    this.setSize( this.frameWidth, this.frameHeight );
  }
        
  /** 
   * Set the custom control state based on the information passed)
   * 
   * @param year - The year to pre-select
   * @param start - The start month to pre-select
   * @param end - The end month to preselect
   * @param enableCustom - When true, enables controls for custom report settings
   */
  private void setControls(final int year, final int start, final int end, final boolean enableCustom) {
    BudgetReportWindow.this.yearSelector.setValue(year);
    BudgetReportWindow.this.yearSelector.setEnabled(enableCustom);
    BudgetReportWindow.this.startSelector.setSelectedIndex(start);
    BudgetReportWindow.this.startSelector.setEnabled(enableCustom);
    BudgetReportWindow.this.endSelector.setSelectedIndex(end);
    BudgetReportWindow.this.endSelector.setEnabled(enableCustom); 
  }

  
  /** 
   * Method to handle Edit button clicks.
   * 
   * @param allowCancel - When true, allows cancelling of the edit.
   */
  private void doEdit(final boolean allowCancel) {
    // Create and configure a dialog to edit the report parameters
    final JDialog dialog = new JDialog ();
    if (allowCancel)
      dialog.setTitle("Edit Budget Report Parameters");
    else
      dialog.setTitle("Create Default Budget Report");
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setMaximumSize(new Dimension(400, 480));
    dialog.setResizable(false);
    dialog.setLayout(new GridBagLayout());
    
    // Do not allow close if not allowing the user to cancel
    if (!allowCancel)
      dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    else
      dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

    // Center the frame on the screen
    AwtUtil.centerWindow(dialog);
   
    /*
    ** Budget selector - Get a list of monthly style budgets to select from
    */
    final String strNames[] = this.budgetList.getBudgetNames();

    // If there are no budgets then we have to inform the user then exit
    if (strNames.length < 1)
      {
      // Display an error message - No budgets exist!
      JOptionPane.showMessageDialog( this,
      "No Budgets have been created.  Use 'Tools:Budget Manager' to create a monthly budget before using this extension.",
      "Error",
      JOptionPane.ERROR_MESSAGE);

      // Exit
      return;
      }

    // Budget Selector label
    final JLabel budgetLabel = new JLabel("Budget:");
    budgetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dialog.add(budgetLabel,GridC.getc(0, 0).insets(10, 10, 5, 15).east());

    // Create the selector
    this.budgetSelector = new JComboBox<String>(strNames);
    this.budgetSelector.setSelectedItem(this.currentReport.getBudgetName());    
    this.budgetSelector.setToolTipText("Select the budget to report");  
    dialog.add(this.budgetSelector, GridC.getc(1, 0).insets(10, 0, 5, 15).fillx());

    /*
    ** Report year selector
    **
    ** Note: Must be defined before the periodSelector so yearSelector is defined.
    */
    final JLabel yearLabel = new JLabel("Report year:");
    yearLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dialog.add(yearLabel,GridC.getc(0, 2).insets(5, 10, 5, 15).east());

    // Get the dates to use for the spinner model
    final int initial = this.currentReport.getYear();
    final int earliest  = initial - 50;
    final int latest  = initial + 1;

    // Create the spinner model and use it with the year selector
    this.yearSelector.setModel(new SpinnerNumberModel(
      initial,   //initial value  
      earliest,  //minimum value  
      latest,    //maximum value  
      1));  // Step size

    // Create a new number editor with no group separator (,)
    final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this.yearSelector,"#");
   
    // Set the new editor on the JSpinner
    this.yearSelector.setEditor(editor);
  
    // Get the text field so we can configure it
    final JFormattedTextField textField = editor.getTextField();
    
    // Left align the year
    textField.setHorizontalAlignment(JTextField.LEFT);
    
    // Disable manual entry of data
    textField.setEditable(false);

    // Set the background the same as the other controls
    this.yearSelector.setBackground(this.budgetSelector.getBackground());

    // Begin disabled unless the period is set to "Selected Period"
    this.yearSelector.setEnabled(false);
    
    // Add the selector
    dialog.add(this.yearSelector, GridC.getc(1, 2).insets(5, 0, 5, 15).fillx());

    /*
    ** Period selector
    */
    final JLabel periodLabel = new JLabel("Period:");
    periodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dialog.add(periodLabel,GridC.getc(0, 1).insets(5, 10, 5, 15).east());

    // Create the selector
    final JComboBox<String> periodSelector = new JComboBox<String>(Constants.periods);
    periodSelector.setSelectedIndex(this.currentReport.getPeriod());
    periodSelector.setToolTipText("Select the period to report");
    dialog.add(periodSelector, GridC.getc(1, 1).insets(5, 0, 5, 15).fillx());

    // Create an action listener to dispatch perform the action when this control is changed
    periodSelector.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) 
        {
        // Get time reference to use for the selections
        final Calendar now = Calendar.getInstance();
        final int thisYear = now.get(Calendar.YEAR);
        final int thisMonth = now.get(Calendar.MONTH);

        // Now update the fields based on the period selected
        switch (periodSelector.getSelectedIndex()) 
          {
          case Constants.PERIOD_AUTOMATIC:
            BudgetReportWindow.this.setControls(thisYear, 0, thisMonth, false);           
            break;

          case Constants.PERIOD_THIS_YEAR:
            BudgetReportWindow.this.setControls(thisYear, 0, 11, false);
            break;
          
          case Constants.PERIOD_LAST_YEAR:
            BudgetReportWindow.this.setControls(thisYear - 1, 0, 11, false);
            break;
    
          case Constants.PERIOD_THIS_MONTH:
            BudgetReportWindow.this.setControls(thisYear, thisMonth, thisMonth, false);
            break;    
     
          case Constants.PERIOD_LAST_MONTH:
            if (thisMonth == 0)
              BudgetReportWindow.this.setControls(thisYear - 1, 11, 11, false);
            else
              BudgetReportWindow.this.setControls(thisYear, thisMonth - 1, thisMonth - 1, false);
            break;    
                      
          case Constants.PERIOD_CUSTOM:
          default:
            BudgetReportWindow.this.setControls(thisYear, 0, 11, true);
            break;
          }
        }
      }); 
   
    /*
    ** Start month selector
    */
    final JLabel startLabel = new JLabel("Start month:");
    startLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dialog.add(startLabel,GridC.getc(0, 3).insets(5, 10, 5, 15).east());

    // Create the selector
    this.startSelector.setSelectedIndex(this.currentReport.getStartMonth() - 1);
    this.startSelector.setToolTipText("Select the starting month to report");
    this.startSelector.setEnabled(false);
    dialog.add(this.startSelector, GridC.getc(1, 3).insets(5, 0, 5, 15).fillx());
    
    // Create an action listener to dispatch perform the action when this control is changed
    this.startSelector.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) 
        {
        // Keep the selected end month in bounds
        if (BudgetReportWindow.this.endSelector.getSelectedIndex() < BudgetReportWindow.this.startSelector.getSelectedIndex())
          BudgetReportWindow.this.endSelector.setSelectedIndex(BudgetReportWindow.this.startSelector.getSelectedIndex());
        }
      });
    
    /*
    ** End month selector
    */
    final JLabel endLabel = new JLabel("End month:");
    endLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dialog.add(endLabel,GridC.getc(0, 4).insets(5, 10, 5, 15).east());

    // Create the selector - Select the current month by default
    this.endSelector.setSelectedIndex(this.currentReport.getEndMonth() - 1);  
    this.endSelector.setEnabled(false);
    this.endSelector.setToolTipText("Select the last month to report");
    dialog.add(this.endSelector, GridC.getc(1, 4).insets(5, 0, 5, 15).fillx());

    // Create an action listener to dispatch perform the action when this control is changed
    this.endSelector.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) 
        {
        // Keep the selected end month in bounds
        if (BudgetReportWindow.this.endSelector.getSelectedIndex() < BudgetReportWindow.this.startSelector.getSelectedIndex())
          BudgetReportWindow.this.endSelector.setSelectedIndex(BudgetReportWindow.this.startSelector.getSelectedIndex());
        }
      });
  
    /*
    ** Enable the custom selectors if the period is custom
    */
    if (this.currentReport.getPeriod() == Constants.PERIOD_CUSTOM)
      {
      BudgetReportWindow.this.yearSelector.setEnabled(true);
      BudgetReportWindow.this.startSelector.setEnabled(true);
      BudgetReportWindow.this.endSelector.setEnabled(true);
      }
    
    /*
    ** Subtotal selector
    */
    final JLabel subtotalLabel = new JLabel("Subtotal:");
    subtotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dialog.add(subtotalLabel,GridC.getc(0, 5).insets(5, 10, 5, 15).east());

    // Create the selector
    final JComboBox<String> subtotalSelector = new JComboBox<String>(Constants.subtotal);
    subtotalSelector.setSelectedIndex(this.currentReport.getSubtotalBy());
    subtotalSelector.setToolTipText("Subtotal mode");
    dialog.add(subtotalSelector, GridC.getc(1, 5).insets(5, 0, 5, 15).fillx());

    /*
    ** Include non-zero categories not included in budget
    */
    // This is not really needed

    /*
    ** Include items with zero actual and budgeted amounts
    */
    // Not really that useful for me

    /*
    ** Subtotal category roll-ups check box
    */
    final JCheckBox rollup = new JCheckBox("Subtotal rollups");
    rollup.setSelected(this.currentReport.isSubtotalParents());
    rollup.setToolTipText("Select to enable rollup totals");
    dialog.add(rollup,GridC.getc(1, 6).insets(5, 0, 5, 0).fillx());

    /*
    * Add the Bottom Panel so we can center the action Buttons
    */  
    final JPanel bottomPanel = new JPanel(new GridBagLayout());
    dialog.add(bottomPanel,GridC.getc(0, 7).colspan(2));

      /*
      ** OK Button
      */
      final JButton okButton = new JButton("OK");
      okButton.setToolTipText("Set the budget parameters and exit");
      bottomPanel.add(okButton,GridC.getc(0,0).insets(15,15,15,15)); 

      // Create an action listener to dispatch the action when this button is clicked
      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            // Save edits to the current report
            BudgetReportWindow.this.currentReport.setBudgetName((String)BudgetReportWindow.this.budgetSelector.getSelectedItem());
            BudgetReportWindow.this.currentReport.setPeriod(periodSelector.getSelectedIndex());
            BudgetReportWindow.this.currentReport.setYear((int)BudgetReportWindow.this.yearSelector.getValue());
            BudgetReportWindow.this.currentReport.setStartMonth(BudgetReportWindow.this.startSelector.getSelectedIndex() + 1);
            BudgetReportWindow.this.currentReport.setEndMonth(BudgetReportWindow.this.endSelector.getSelectedIndex() + 1);
            BudgetReportWindow.this.currentReport.setSubtotalBy(subtotalSelector.getSelectedIndex());
            BudgetReportWindow.this.currentReport.setSubtotalParents(rollup.isSelected());

            // Update the report with the changes
            if (BudgetReportWindow.this.tableModel != null)
              BudgetReportWindow.this.tableModel.LoadData();

            // Add an Unsaved Report item in the report selector and then select it  
            if (BudgetReportWindow.this.reportSelector != null)
              {
              BudgetReportWindow.this.reportSelector.insertItemAt(Constants.UNSAVED_REPORT, 0);
              BudgetReportWindow.this.reportSelector.setSelectedIndex(0);
              }

            // Set the report name
            if (BudgetReportWindow.this.currentReport != null)
              {
              BudgetReportWindow.this.currentReport.setReportName(Constants.UNSAVED_REPORT);
              BudgetReportWindow.this.currentReport.setMemorized(false);
              }

            // Hide the frame
            dialog.setVisible(false);

            // The table structure may have changed so force an update
            BudgetReportWindow.this.forceTableStructureChange(true);
        }
      });

      /*
      ** Cancel Button
      */
      if (allowCancel)
        {
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel budget editing and exit");
        bottomPanel.add(cancelButton,GridC.getc(1,0).insets(15,15,15,15));
        
        // Create an action listener to dispatch the action when this button is clicked
        cancelButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            // Hide the frame
            dialog.setVisible(false);
          }
        });
      }

    // Update window size
    dialog.pack();
        
    // Center the frame on the screen
    AwtUtil.centerWindow(dialog);

    // Show the frame
    dialog.setVisible(true);
  }

  /**
   * Method to print the budget report.
   */
  private void doPrint() {
    // Get a PrinterJob.
    // Do this before anything with side-effects since it may throw a
    // security exception - in which case we don't want to do anything else.
    final PrinterJob job = PrinterJob.getPrinterJob();

    // Header
    final MessageFormat[] header = {
      // Report name
      new MessageFormat(this.currentReport.getReportName()),
      
      // Report dates
      new MessageFormat(Constants.months[this.currentReport.getStartMonth() - 1]+" "+this.currentReport.getYear()+" - "
        +Constants.months[this.currentReport.getEndMonth() - 1]+" "+this.currentReport.getYear())
      };
      
    // Footer
    final MessageFormat[] footer = {
      // Page number
      new MessageFormat("Page {0}")
      };

    // Create a table to use for printing the budget values
    final Table printTable = new Table(this.tableModel, this.colors, true);

    // Get the column count
    final int colCount = printTable.getColumnCount();

    // Get the column model
    final TableColumnModel colModel = printTable.getColumnModel();

    // Set the column renderer and column sizes
    for (int i = 0; i < colCount; i++ ) 
      {
      // Get the TableColumn object for each column
      final TableColumn colSelect = colModel.getColumn(i);

      // Is this column 0 (Category)?
      if (i == 0)
        {
        // Set the minimum width of the category column
        colSelect.setPreferredWidth(Constants.PRINT_CATEGORY_WIDTH);
        colSelect.setMinWidth(Constants.PRINT_CATEGORY_WIDTH);
        }
      else
        {
        // Set the minimum width of the category column
        colSelect.setPreferredWidth(Constants.PRINT_VALUE_WIDTH); 
        colSelect.setMinWidth(Constants.PRINT_VALUE_WIDTH); 

        // Set the cell renderer for the numeric cells
        colSelect.setCellRenderer(new CurrencyTableCellRenderer());
        }
      }

    // Set the size of the table so the entire table will print
    // printTable.setSize(printTable.getPreferredSize()); results in several of
    // the right most columns not displaying.
    printTable.setSize(4000, 8000);

    // Get the table header object
    final JTableHeader tableHeader = printTable.getTableHeader();

    // This code gets the table header to print but by default the default height 
    // is taller than I prefer so I reduce it by one third.
    final Dimension d = tableHeader.getPreferredSize();
    d.setSize(d.getWidth(),(d.getHeight()/3)+4);
    tableHeader.setSize(d);

    //Remove border from the header
    final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setForeground(Color.BLACK);
    renderer.setBackground(Color.LIGHT_GRAY);
    renderer.setHorizontalAlignment(JLabel.CENTER);
    tableHeader.setDefaultRenderer(renderer);

    // Print request attributes
    final PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

    // Print via a custom printable method so we can generate proper headers/footers
    job.setPrintable(new MyTablePrintable(printTable, JTable.PrintMode.NORMAL, header, footer));
    // job.setPrintable(new MyTablePrintable(printTable, JTable.PrintMode.FIT_WIDTH, header, footer));

    // Print the report
    if (job.printDialog(attr)) 
      {
      try 
        {
        job.print(attr);
        } 
      catch (final PrinterException e) 
        { 
        e.printStackTrace();

        JOptionPane.showMessageDialog( this,
        "The print job did not complete successfully!",
        "Print Error",
        JOptionPane.ERROR_MESSAGE);
        }
      }
  }


  /**
   * Method to handle the Memorize button press.
   */
  private void doMemorize() {
    // Create and configure a dialog to name and memorize a report
    final JDialog dialog = new JDialog ();
    dialog.setTitle("Memorize Budget Report");
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setMaximumSize(new Dimension(550, 240));
    dialog.setResizable(false);
    dialog.setLayout(new GridBagLayout());

    // Setup the prompt
    final JLabel prompt = new JLabel ("Enter the name for the report:");
    prompt.setPreferredSize(new Dimension(500, 35));
    dialog.add(prompt, GridC.getc(0, 0).insets(5, 10, 5, 10));
    
    // Add a text field to enter the report name
    final JTextField name = new JTextField();
    name.setPreferredSize(new Dimension(500, 35));
    dialog.add(name, GridC.getc(0, 1).insets(5, 10, 5, 10));

    // If this is already a memorized report then default the report name to
    // the existing report name.
    if (this.currentReport.isMemorized())
      name.setText(this.currentReport.getReportName());

    // Create a bottom panel for the buttons
    final JPanel bottomPanel = new JPanel(new GridBagLayout());
    dialog.add(bottomPanel, GridC.getc(0, 2).insets(5, 10, 5, 10));

    // Should we enable the Forget button?
    if (this.currentReport.isMemorized())
      {
      // Button to forget this memorized report
      final JButton forgetButton = new JButton("Forget");
      forgetButton.setToolTipText("Forget this memorized report");
      bottomPanel.add(forgetButton, GridC.getc(0, 0).insets(15, 10, 15, 35));

      // Create an action listener to dispatch the action when this button is clicked
      forgetButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          // Find out if this is the default report
          final Account rootAccount = BudgetReportWindow.this.context.getCurrentAccountBook().getRootAccount();
          final boolean isDefault = BudgetReportWindow.this.currentReport.getReportName().equals(rootAccount.getPreference(Constants.DEFAULT_REPORT, null));

          // Prompt "Are you sure?"
          final String message;
          if (isDefault)
            message = "Are you sure you want to forget the default report?";
          else
            message = "Are you sure you want to forget this report?";
          final int response = JOptionPane.showConfirmDialog( BudgetReportWindow.this,
          message,
          "Forget Report",
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (response == 1)  // No
            return;

          // If this was the default report remove it from the configuration settings
          if (isDefault)
            rootAccount.setPreference(Constants.DEFAULT_REPORT, null);

          // Get the file path to load the report from
          final File rootFolder = BudgetReportWindow.this.context.getCurrentAccountBook().getRootFolder();
          final String filePath = rootFolder.getAbsolutePath()+File.separator+BudgetReportWindow.this.currentReport.getReportName()+".mbrpt";

          // Delete the named file  
          final File rptFile = new File(filePath);
          if (rptFile.exists())
            rptFile.delete();

          // Update the memorized report list after deleting this one
          BudgetReportWindow.this.updateReportSelector();

          // Add an Unsaved Report item in the report selector and then select it
          BudgetReportWindow.this.reportSelector.insertItemAt(Constants.UNSAVED_REPORT, 0);
          BudgetReportWindow.this.reportSelector.setSelectedIndex(0);

          // Set the report name
          BudgetReportWindow.this.currentReport.setReportName(Constants.UNSAVED_REPORT);
          BudgetReportWindow.this.currentReport.setMemorized(false);

          // Update the report header
          BudgetReportWindow.this.updateHeader();

          // Close the dialog and exit
          dialog.setVisible(false);
          }
        });
      }

    // Button to set this report as the default report to run
    final JButton defaultButton = new JButton("Default");
    defaultButton.setToolTipText("Memorize the report and set as the default report");
    bottomPanel.add(defaultButton, GridC.getc(1, 0).insets(15, 10, 15, 35));

    // Create an action listener to dispatch the action when this button is clicked
    defaultButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (BudgetReportWindow.this.memorizeReport(name.getText(), true))
          dialog.setVisible(false);
        }
      });

    // Button to memorize the report but not make it the default report to run
    final JButton okButton = new JButton("OK");
    okButton.setToolTipText("Memorize the report");
    bottomPanel.add(okButton, GridC.getc(2, 0).insets(15, 10, 15, 10));

    // Create an action listener to dispatch the action when this button is clicked
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (BudgetReportWindow.this.memorizeReport(name.getText(), false))
          dialog.setVisible(false);
        }
      });

    // Cancel memorization
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.setToolTipText("Do not memorize the report");
    bottomPanel.add(cancelButton, GridC.getc(3, 0).insets(15, 10, 15, 10));

    // Create an action listener to dispatch the action when this button is clicked
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        dialog.setVisible(false);
        }
      });

    // Update window size
    dialog.pack();

    // Center the frame on the screen
    AwtUtil.centerWindow(dialog);

    // Show the dialog and wait for input
    dialog.setVisible(true);
  }
  
  /** 
   * This method determines if the path passed to it is a valid file path for
   * the operating system.
   * 
   * @param path - The path to check
   * @return boolean - Returns true if the path is valid, false otherwise.
   */
  public static boolean isValidFilePath(final String path) {
    final File f = new File(path);
    try 
      {
      f.getCanonicalPath();
      return true;
      }
    catch (final IOException e) 
      {
      return false;
      }
    }

  /** 
   * This method memorizes a report for future use optionally making it the default
   * report that gets loaded every time Monthly Budget Report is run.
   * 
   * @param reportName - The name of the report which will also be the file name
   * @param setDefault - true when this report should be set as the default report
   * @return boolean - true if successful, false otherwise - filename is invalid
   */
   private boolean memorizeReport(final String reportName, final boolean setDefault) { 
    // Get the file path to store this report to
    final File rootFolder = this.context.getCurrentAccountBook().getRootFolder();
    final String filePath = rootFolder.getAbsolutePath()+File.separator+reportName+".mbrpt";

    // Validate the report name
    if ((reportName == null) || (reportName.isBlank()) || reportName.contains("/") 
      || (reportName.equals(Constants.UNSAVED_REPORT))
      || (!BudgetReportWindow.isValidFilePath(filePath)))
      {
      JOptionPane.showMessageDialog( this,
      "The report name is invalid. Please try again.",
      "Error",
      JOptionPane.ERROR_MESSAGE);

      // Allow the user to try again
      return(false);
      }
  
    // Find out if this report was renamed 
    boolean isRenamed = false;
    if (!BudgetReportWindow.this.currentReport.getReportName().equals(Constants.UNSAVED_REPORT))
      isRenamed = !reportName.equals(BudgetReportWindow.this.currentReport.getReportName());
  
    // If renamed then prompt the user to confirm the rename
    if (isRenamed)
      {
      final int response = JOptionPane.showConfirmDialog( this,
      "The report has been renamed. Do you want to continue?",
      "Report Renamed",
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (response == 1)  // No, allow retry
        return (false);
      }

    // Prompt before overwriting an existing report if the file exists
    if (new File(filePath).exists())
      {
      final int response = JOptionPane.showConfirmDialog( this,
      "The selected report already exists. Do you want to overwrite it?",
      "Report Exists",
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (response == 1)  // No, allow retry
        return (false);
      }

    // Save the report name in the report
    final String oldName = this.currentReport.getReportName();
    this.currentReport.setReportName(reportName);

    // Save the report
    try 
      {
      final FileOutputStream file = new FileOutputStream(filePath);
      final ObjectOutputStream oos = new ObjectOutputStream(file);
      oos.writeObject(this.currentReport);
      oos.flush();
      oos.close();
      file.close();
      }
    catch(final IOException i)
      {
      // For debug
      i.printStackTrace();

      // Tell user we failed to save the report
      JOptionPane.showMessageDialog( this,
      "Error saving the report: "+i.toString(),
      "Error",
      JOptionPane.ERROR_MESSAGE);

      // Restore the old report name
      this.currentReport.setReportName(oldName);

      // Failed, allow retry
      return (false);
      }

    // Get the root account
    final Account rootAccount = this.context.getCurrentAccountBook().getRootAccount();

    // If this is a rename, delete the old report. If the old report was the default report then reset the default report to none
    if (isRenamed)
      {
      // Delete the original report
      final String oldReportPath = rootFolder.getAbsolutePath()+File.separator+oldName+".mbrpt";
      final File rptFile = new File(oldReportPath);
      if (rptFile.exists())
        rptFile.delete();

      // If we're renaming the default report then remove the old default name from the configuration file
      if (reportName.equals(rootAccount.getPreference(Constants.DEFAULT_REPORT, null)))
        rootAccount.setPreference(Constants.DEFAULT_REPORT, null);
      }

    // The current report is memorized  
    this.currentReport.setMemorized(true);

    // Update the report header with the new report name
    BudgetReportWindow.this.updateHeader();

    // Update the memorized report list after adding a new one
    this.updateReportSelector();

    // If requested, set the default report to this report
    if (setDefault)
      rootAccount.setPreference(Constants.DEFAULT_REPORT, reportName);

    // Success
    return (true);
  }

  
  /**
   * Method to copy a report to the clipboard.
   *  
   * @param cellBreak - The cell break string
   * @param lineBreak - The line break string
   */
  private void copyToClipboard(final String cellBreak, final String lineBreak) {
    // Create a transferrable of the table data to copy to the clipboard
    final StringSelection sel  = new StringSelection(this.exportTable(cellBreak, lineBreak)); 
  
    // Get an instance of the system clipboard
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); 

    // Copy the report to the clipboard
    clipboard.setContents(sel, sel); 
  }

  
  /** 
   * Method to export a report to a file.
   * 
   * @param extension - The file extension for the created file.
   * @param cellBreak - The cell break string
   * @param lineBreak - The line break string
   * @param excelFormat - True when exporting in excel format, false otherwise
   */
  private void exportToFile(final String extension, final String cellBreak, final String lineBreak, final boolean excelFormat) {
    String filePath;
    File file;

    // Select the location and name for file
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Specify a location and file name to export the report to");
    fileChooser.setSelectedFile(new File(this.currentReport.getReportName()+extension));
    final int userSelection = fileChooser.showSaveDialog(this);
   
    // Did the user select or cancel?
    if (userSelection == JFileChooser.APPROVE_OPTION) 
      {
      file = fileChooser.getSelectedFile();
      filePath = file.getAbsolutePath();
      }
    else  // The user cancelled the operation
      return;

    // Prompt before overwriting an existing file
    if (file.exists())
      {
      final int response = JOptionPane.showConfirmDialog( this,
      "The selected file already exists. Do you want to overwrite it?",
      "File Exists",
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (response == 1)  // No, allow retry
        return;
      }

    //Save the file
    try
      {
      // Open a new file writer
      final FileWriter fileWriter = new FileWriter(filePath);
      
      // If excelFormat then prepend the UTF-8 Byte Order Mark
      if (excelFormat)
        fileWriter.append("\ufeff");

      // Append the report to the file
      fileWriter.append(this.exportTable(cellBreak, lineBreak));

      // Flush and close the file
      fileWriter.flush();
      fileWriter.close();
      }
    catch(final IOException i)
      {
      // For debug
      i.printStackTrace();

      // Tell user we failed to export the report
      JOptionPane.showMessageDialog( this,
      "Error exporting the report: "+i.toString(),
      "Error",
      JOptionPane.ERROR_MESSAGE);
      }
  }

  
  /** 
   * Method to create a string to export a report in different formats.
   * 
   * @param cellBreak - The cell break string
   * @param lineBreak - The line break string
   * @return String - The returned string for export
   */
  private String exportTable(final String cellBreak, final String lineBreak) {
    // Create a string buffer to hold the data to export
    final StringBuffer dataString = new StringBuffer(); 

    // Get the row and column count of the table
    final int numRows = this.table.getRowCount(); 
    final int numCols = this.table.getColumnCount(); 


    // Add the report name
    dataString.append(this.currentReport.getReportName());
    dataString.append(lineBreak);

    // Add the report dates
    dataString.append(Constants.months[this.currentReport.getStartMonth() - 1]+" "+this.currentReport.getYear()+" - "
      +Constants.months[this.currentReport.getEndMonth() - 1]+" "+this.currentReport.getYear());
    dataString.append(lineBreak);
    dataString.append(lineBreak);

    // Add the table header
    for (int column = 0; column < numCols; column++) 
      {
      dataString.append(this.table.getColumnName(column));
      if (column < numCols - 1) 
        dataString.append(cellBreak); // Insert a break for this cell
      else
        dataString.append(lineBreak); // Insert a break for this line
      }

    // Add the table data
    for (int row = 0; row < numRows; row++) 
      { 
      for (int column = 0; column < numCols; column++) 
        { 
        dataString.append(this.table.getValueAt(row, column)); 
        if (column < numCols - 1) 
          dataString.append(cellBreak); // Insert a break for this cell
        else
          dataString.append(lineBreak); // Insert a break for this line
        } 
      } 

    return (dataString.toString());
  }


  /**
   * Method to export a report in HTML format.
   */
  private void exportToHTML() {
    String filePath;
    File file;

    // Select the location and name for file
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Specify a location and file name to export the report to");
    fileChooser.setSelectedFile(new File(this.currentReport.getReportName()+".html"));
    final int userSelection = fileChooser.showSaveDialog(this);
   
    // Did the user select or cancel?
    if (userSelection == JFileChooser.APPROVE_OPTION) 
      {
      file = fileChooser.getSelectedFile();
      filePath = file.getAbsolutePath();
      }
    else  // The user cancelled the operation
      return;

    // Prompt before overwriting an existing file
    if (file.exists())
      {
      final int response = JOptionPane.showConfirmDialog( this,
      "The selected file already exists. Do you want to overwrite it?",
      "File Exists",
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (response == 1)  // No, allow retry
        return;
      }

    //Save the file
    try
      {
      // Open a new file writer
      final FileWriter fileWriter = new FileWriter(filePath);

      // Get the row and column count of the table
      final int numRows = this.table.getRowCount(); 
      final int numCols = this.table.getColumnCount(); 

      // Head of file
      fileWriter.append("<!DOCTYPE HTML>\n");
      fileWriter.append("<html>\n");
      fileWriter.append("<head>\n");
      fileWriter.append("<title>Moneydance Monthly Budget Report</title>\n");
      
      // Styles
      fileWriter.append("<style type='text/css'>\n"+
        "body {\n"+
        " background-color: white;\n"+
        " padding: 0pt 0pt;\n"+
        " border: 0;\n"+
        " font-family: sans-serif;\n"+
        " font-size: 18pt;\n"+
        " margin: 0px;\n"+
        " }\n"+
        "table {\n"+
        " width: 100%;\n"+
        " cellspacing: 0;\n"+ 
        " padding: 0px 0px;\n"+
        " border: 0;\n"+
        " border-collapse: collapse;\n"+
        " margin: 0px;\n"+
        " }\n"+
        "tr:nth-child(even){\n"+
        " background-color: #f2f2f2;\n"+
        " }\n"+
        "tr {\n"+
        " padding: 0px 0px;\n"+
        " border: 0;\n"+
        " margin: 0px;\n"+
        " }\n"+
        "th {\n"+
        " padding: 4px 8px;\n"+
        " border: 0;\n"+
        " position: sticky;\n"+
        " top: 0;\n"+
        " margin: 0px;\n"+
        " font-size: larger;\n"+
        " }\n"+
        "td {\n"+
        " padding: 4px 8px;\n"+
        " border: 0;\n"+
        " margin: 0px;\n"+
        " }\n"+
        "h1,h2 {\n"+
        " text-align: center;\n"+
        " }\n"+
        ".red {\n"+
        " color: red;\n"+
        " }\n"+
        ".black {\n"+
        " color: black;\n"+
        " }\n"+
        ".bold {\n"+
        " font-weight: bold;\n"+
        " }\n"+
        ".normal {\n"+
        " font-weight: normal;\n"+
        " }\n"+
        ".left {\n"+
        " text-align: left;\n"+
        " }\n"+
        ".right {\n"+
        " text-align: right;\n"+
        " }\n"+
        "</style>\n");

      // Start of body
      fileWriter.append("</head>\n<body>\n");
      
      // Add the report name
      fileWriter.append("<h1>"+this.currentReport.getReportName()+"</h1>\n");
 
      // Add the report dates
      fileWriter.append("<h2>"+Constants.months[this.currentReport.getStartMonth() - 1]+"&nbsp;"+this.currentReport.getYear()+"&nbsp;-&nbsp;"
        +Constants.months[this.currentReport.getEndMonth() - 1]+"&nbsp;"+this.currentReport.getYear()+"</h2>\n");

      // End header
      fileWriter.append("<br/>\n");

      // Start the table
      fileWriter.append("<table>\n");

      // Add the table header
      fileWriter.append("<tr>\n");
      for (int column = 0; column < numCols; column++) 
        fileWriter.append("<th>"+this.table.getColumnName(column)+"</th>\n");
      fileWriter.append("</tr>\n");

      // Get the budget categories List
      final BudgetCategoriesList budgetCategoriesList = this.tableModel.getBudgetCategoriesList();

      // Add the table data
      for (int row = 0; row < numRows; row++) 
        { 
        for (int column = 0; column < numCols; column++) 
          { 
          // Font weight for this row
          String weight;

          // Rows with children are roll-up rows and will be bolded
          if (budgetCategoriesList.getCategoryItemByIndex(row).getHasChildren())
            weight="bold";
          else
            weight="normal";

          // Display the cell data
          if (column == 0)
            fileWriter.append("<tr><td class='left black "+weight+"'>");  
          else
            {
            // Highlight negative numbers in red
            if (this.table.getValueAt(row, column) instanceof Number)
              {
              if ((double)this.table.getValueAt(row, column) >= 0.0)
                fileWriter.append("<td class='right black "+weight+"'>"); 
              else
                fileWriter.append("<td class='right red "+weight+"'>");
              }
            }
          
          // Finish the cell
          if (column == 0)
            fileWriter.append(this.table.getValueAt(row, column).toString().replaceAll(" ", "&nbsp;")+"</td>\n");
          else
            {
            if (this.table.getValueAt(row, column) instanceof Number)
              fileWriter.append(NumberFormat.getCurrencyInstance().format((double)this.table.getValueAt(row, column))+"</td>\n");
            else
              fileWriter.append("<td></td>\n");
            }
          } 
        
        // Finish the table row
        fileWriter.append("</tr>\n");
        }

      // Close the table and the page
      fileWriter.append("</table>\n</body>\n</html>\n");

      // Flush and close the file
      fileWriter.flush();
      fileWriter.close();
      }
    catch(final IOException i)
      {
      // For debug
      i.printStackTrace();

      // Tell user we failed to export the report
      JOptionPane.showMessageDialog( this,
      "Error exporting the report: "+i.toString(),
      "Error",
      JOptionPane.ERROR_MESSAGE);
      }
  }

  /**
   * This class extends JMenuItem adding an ID 
   *
   * @author  Jerry Jones
   */
  public class BudgetReportMenuItem extends JMenuItem
  {
    // Identifier for this menu item
    int id;

    /**
     * Constructor method used to create the top bar menu items.
     * 
     * @param menu - The pop-up menu to add the item to.
     * @param id - The identifier that will be used to determine what item was selected.
     * @param text - The text of the pop-up menu item.
     * @param listener - The action listener for this item.
     */
    public BudgetReportMenuItem(final JPopupMenu menu, final int id, final String text, final ActionListener listener) 
    {
        // Call the super class constructor
        super();

        // Save the identifier for this menu item
        this.id = id;

        // Set the text to display for this menu item
        this.setText(text);

        // Add an action listener for the menu item
        this.addActionListener(listener);

        // Add the popup menu item to the menu
        menu.add(this);
    }

    /**
     * Method to return the ID of a menu item
     * 
     * @return the id
     */
    public int getId() {
        return this.id;
    }
  }

  /**
	 * Create the action listener to receive menu item events.
	 */
  ActionListener popListener = new ActionListener () {
		@Override
		public void actionPerformed(final ActionEvent event) {
      final int id = ((BudgetReportMenuItem) event.getSource()).getId();

      switch(id) {
        // 	Copy the report to the clipboard in tab delimited format
				case Constants.EXPORT_CLIPBOARD:
          BudgetReportWindow.this.copyToClipboard("\t", "\n");
          break;

        // Copy the report to the clipboard in CSV format
        case Constants.EXPORT_CLIPBOARD_CSV:
          BudgetReportWindow.this.copyToClipboard(",", "\n");
          break;

        // Save the report in tab delimited format
        case Constants.EXPORT_TAB:
          BudgetReportWindow.this.exportToFile(".txt", "\t", "\n", false);
          break;

        // Save the report in CSV format
        case Constants.EXPORT_CSV:
          BudgetReportWindow.this.exportToFile(".csv", ",", "\n", false);
          break;

        // Save the report in CSV format for Excel
        case Constants.EXPORT_EXCEL:
          BudgetReportWindow.this.exportToFile(".csv", ",", "\n", true);
          break;
        
        // Save the report as HTML
        case Constants.EXPORT_HTML:
          BudgetReportWindow.this.exportToHTML();
          break;
      }
    }
	};

  /**
   * Method to export the data in various formats
   */
  private void doExport(final Component c) {
		// Create new popup menu
		final JPopupMenu popMenu = new JPopupMenu();

    // Add the popup menu items
    for (int i = 0; i < Constants.exportItems.length; i++)
      new BudgetReportMenuItem(popMenu, i, Constants.exportItems[i], this.popListener);  
   
    // Show the menu under the button
    popMenu.show(this, this.getWidth() - 15 - (int)popMenu.getPreferredSize().getWidth(), 4 + this.getInsets().top + c.getY() + c.getHeight());
  }

  /**
   * Action method called when the User Guide label is clicked. This method
   * displays a brief help message for the extension.
   */
  private void showHelp() 
  {
    final String url = "https://github.com/jerrymjones/MonthlyBudgetReport/wiki";
    final String myOS = System.getProperty("os.name").toLowerCase();
    try 
      {
      if (myOS.contains("windows"))
        { // Windows
        if (Desktop.isDesktopSupported())
          {
          final Desktop desktop = Desktop.getDesktop();
          desktop.browse(new URI(url));
          }
        else
          this.showHelpFailed(url); // Copy URL to clipboard
        } 
      else 
        { // Not-windows
          final Runtime runtime = Runtime.getRuntime();
          if (myOS.contains("mac")) // Apple
            runtime.exec("open " + url);
          else if (myOS.contains("nix") || myOS.contains("nux")) // Linux
            runtime.exec("xdg-open " + url);
          else 
            this.showHelpFailed(url); // Copy URL to clipboard
        }
      }
    catch(IOException | URISyntaxException e) 
      {
        this.showHelpFailed(url); // Copy URL to clipboard
      }
  }

  /**
   * This method is called when we could not open a browser window to show help.
   * We'll tell the user and copy the URL to the clipboard so it can be manually
   * opened.
   */
  private void showHelpFailed(final String url)
  {
    // Create a transferrable of the url to copy to the clipboard
    final StringSelection sel  = new StringSelection(url); 
  
    // Get an instance of the system clipboard
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); 

    // Copy the url to the clipboard
    clipboard.setContents(sel, sel); 

    // Now tell the user what happened
    JOptionPane.showMessageDialog(this,
    "I could not open the help URL on this system. The URL was copied to the clipboard for you to manually open it.",
    "Browser Open Failed",
    JOptionPane.INFORMATION_MESSAGE);
  }

  /*
   * ComponentListener events
   */
  @Override
  public void componentResized(final ComponentEvent e) 
  {
    // Resize the table columns
    this.resizeColumns(true);     
  }

  @Override
  public void componentMoved(final ComponentEvent e) {}

  @Override
  public void componentShown(final ComponentEvent e) {} 

  @Override
  public void componentHidden(final ComponentEvent e) {}
}