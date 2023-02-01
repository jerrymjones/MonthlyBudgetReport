/**
 * An implementation of <code>Printable</code> for printing
 * <code>JTable</code>s.
 * <p>
 * This implementation spreads table rows naturally in sequence across multiple
 * pages, fitting as many rows as possible per page. The distribution of
 * columns, on the other hand, is controlled by a printing mode parameter passed
 * to the constructor. When <code>JTable.PrintMode.NORMAL</code> is used, the
 * implementation handles columns in a similar manner to how it handles rows,
 * spreading them across multiple pages (in an order consistent with the table's
 * <code>ComponentOrientation</code>). When
 * <code>JTable.PrintMode.FIT_WIDTH</code> is given, the implementation scales
 * the output smaller if necessary, to ensure that all columns fit on the page.
 * (Note that width and height are scaled equally, ensuring that the aspect
 * ratio remains the same).
 * <p>
 * The portion of table printed on each page is headed by the appropriate
 * section of the table's <code>JTableHeader</code>.
 * <p>
 * Header and footer text can be added to the output by providing
 * <code>MessageFormat</code> instances to the constructor. The printing code
 * requests Strings from the formats by calling their <code>format</code> method
 * with a single parameter: an <code>Object</code> array containing a single
 * element of type <code>Integer</code>, representing the current page number.
 * <p>
 * There are certain circumstances where this <code>Printable</code> cannot fit
 * items appropriately, resulting in clipped output. These are:
 * <ul>
 * <li>In any mode, when the header or footer text is too wide to fit completely
 * in the printable area. The implementation prints as much of the text as
 * possible starting from the beginning, as determined by the table's
 * <code>ComponentOrientation</code>.
 * <li>In any mode, when a row is too tall to fit in the printable area. The
 * upper most portion of the row is printed and no lower border is shown.
 * <li>In <code>JTable.PrintMode.NORMAL</code> when a column is too wide to fit
 * in the printable area. The center of the column is printed and no left and
 * right borders are shown.
 * </ul>
 * <p>
 * It is entirely valid for a developer to wrap this <code>Printable</code>
 * inside another in order to create complex reports and documents. They may
 * even request that different pages be rendered into different sized printable
 * areas. The implementation was designed to handle this by performing most of
 * its calculations on the fly. However, providing different sizes works best
 * when <code>JTable.PrintMode.FIT_WIDTH</code> is used, or when only the
 * printable width is changed between pages. This is because when it is printing
 * a set of rows in <code>JTable.PrintMode.NORMAL</code> and the implementation
 * determines a need to distribute columns across pages, it assumes that all of
 * those rows will fit on each subsequent page needed to fit the columns.
 * <p>
 * It is the responsibility of the developer to ensure that the table is not
 * modified in any way after this <code>Printable</code> is created (invalid
 * modifications include changes in: size, renderers, or underlying data). The
 * behavior of this <code>Printable</code> is undefined if the table is changed
 * at any time after creation.
 *
 * @author Shannon Hickey
 * @version 1.41 11/17/05
 */
package com.moneydance.modules.features.budgetreport;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.MessageFormat;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

class MyTablePrintable implements Printable {

    /**
     * The table to print.
     */
    private final JTable table;

    /**
     * For quick reference to the table's header.
     */
    private final JTableHeader header;

    /**
     * For quick reference to the table's column model.
     */
    private final TableColumnModel colModel;

    /**
     * To save multiple calculations of total column width.
     */
    private final int totalColWidth;

    /**
     * The printing mode of this printable.
     */
    private final JTable.PrintMode printMode;

    /**
     * Provides the header text for the table.
     */
    private final MessageFormat[] headerFormat;

    /**
     * Provides the footer text for the table.
     */
    private final MessageFormat[] footerFormat;

    /**
     * The most recent page index asked to print.
     */
    private int last = -1;

    /**
     * The next row to print.
     */
    private int row = 0;

    /**
     * The next column to print.
     */
    private int col = 0;

    /**
     * Used to store an area of the table to be printed.
     */
    private final Rectangle clip = new Rectangle(0, 0, 0, 0);

    /**
     * Used to store an area of the table's header to be printed.
     */
    private final Rectangle hclip = new Rectangle(0, 0, 0, 0);

    /**
     * Saves the creation of multiple rectangles.
     */
    private final Rectangle tempRect = new Rectangle(0, 0, 0, 0);

    /**
     * Vertical space to leave between table and header/footer text.
     */
    private static final int H_F_SPACE = 8;

    /**
     * Font size for the header text.
     */
    private static final float HEADER_FONT_SIZE_1 = 10;
    private static final float HEADER_FONT_SIZE_2 = 8;

    /**
     * Font size for the footer text.
     */
    private static final float FOOTER_FONT_SIZE = 8;

    /**
     * The font to use in rendering header text.
     */
    private final Font headerFont1;
    private final Font headerFont2;

    /**
     * The font to use in rendering footer text.
     */
    private final Font footerFont;

    /**
     * Create a new <code>TablePrintable</code> for the given
     * <code>JTable</code>. Header and footer text can be specified using the
     * two <code>MessageFormat</code> parameters. When called upon to provide a
     * String, each format is given the current page number.
     *
     * @param table the table to print
     * @param printMode the printing mode for this printable
     * @param headerFormat a <code>MessageFormat</code> specifying the text to
     * be used in printing a header, or null for none
     * @param footerFormat a <code>MessageFormat</code> specifying the text to
     * be used in printing a footer, or null for none
     * @throws IllegalArgumentException if passed an invalid print mode
     */
    public MyTablePrintable(final JTable table,
            final JTable.PrintMode printMode,
            final MessageFormat[] headerFormat,
            final MessageFormat[] footerFormat) 
        {
        this.table = table;

        this.header = table.getTableHeader();
        this.colModel = table.getColumnModel();
        this.totalColWidth = this.colModel.getTotalColumnWidth();

        if (this.header != null) 
            {
            // the header clip height can be set once since it's unchanging
            this.hclip.height = this.header.getHeight();
            }

        this.printMode = printMode;

        this.headerFormat = headerFormat;
        this.footerFormat = footerFormat;

        // derive the header and footer font from the table's font
        this.headerFont1 = table.getFont().deriveFont(Font.BOLD, MyTablePrintable.HEADER_FONT_SIZE_1);    // First line of the header
        this.headerFont2 = table.getFont().deriveFont(Font.PLAIN, MyTablePrintable.HEADER_FONT_SIZE_2);   // Subsequent lines of the header
        this.footerFont = table.getFont().deriveFont(Font.PLAIN, MyTablePrintable.FOOTER_FONT_SIZE);
        }

    /**
     * Prints the specified page of the table into the given {@link Graphics}
     * context, in the specified format.
     *
     * @param graphics the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex the zero based index of the page to be drawn
     * @return PAGE_EXISTS if the page is rendered successfully, or NO_SUCH_PAGE
     * if a non-existent page index is specified
     * @throws PrinterException if an error causes printing to be aborted
     */
    public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
        // for easy access to these values
        final int imageableWidth = (int) pageFormat.getImageableWidth();
        final int imageableHeight = (int) pageFormat.getImageableHeight();

        if (imageableWidth <= 0)
            throw new PrinterException("Width of printable area is too small.");

        // to pass the page number when formatting the header and footer text
        final Object[] pageNumber = new Object[]{Integer.valueOf(pageIndex + 1)};

        // fetch the formatted header text, if any
        String[] headerText = null;
        if (this.headerFormat != null) 
            {
            headerText = new String[this.headerFormat.length];
            for (int i = 0; i < this.headerFormat.length; i++) 
                headerText[i] = this.headerFormat[i].format(pageNumber);
            }

        // fetch the formatted footer text, if any
        String[] footerText = null;
        if (this.footerFormat != null) 
            {
            footerText = new String[this.footerFormat.length];
            for (int i = 0; i < this.footerFormat.length; i++) 
                footerText[i] = this.footerFormat[i].format(pageNumber);
            }

        // to store the bounds of the header and footer text
        Rectangle2D[] hRect = null;
        Rectangle2D[] fRect = null;

        // the amount of vertical space needed for the header and footer text
        int headerTextSpace = 0;
        int footerTextSpace = 0;

        // the amount of vertical space available for printing the table
        int availableSpace = imageableHeight;

        // if there's header text, find out how much space is needed for it
        // and subtract that from the available space
        if (headerText != null) 
            {
            hRect = new Rectangle2D[headerText.length];
            for (int i = 0; i < headerText.length; i++) 
                {
                // The first line of the header is bold
                if (i == 0)
                    graphics.setFont(this.headerFont1);
                else
                    graphics.setFont(this.headerFont2);
                hRect[i] = graphics.getFontMetrics().getStringBounds(headerText[i], graphics);
                hRect[i] = new Rectangle2D.Double(hRect[i].getX(), Math.abs(hRect[i].getY()), hRect[i].getWidth(), hRect[i].getHeight());
                headerTextSpace += (int) Math.ceil(hRect[i].getHeight());
                }
            
            // Subtract header plus the gap from the available space
            availableSpace -= (headerTextSpace + MyTablePrintable.H_F_SPACE);
            }

        // if there's footer text, find out how much space is needed for it
        // and subtract that from the available space
        if (footerText != null) 
            {
            graphics.setFont(this.footerFont);
            fRect = new Rectangle2D[footerText.length];
            for (int i = 0; i < footerText.length; i++) 
                {
                fRect[i] = graphics.getFontMetrics().getStringBounds(footerText[i], graphics);
                fRect[i] = new Rectangle2D.Double(fRect[i].getX(), Math.abs(fRect[i].getY()), fRect[i].getWidth(), fRect[i].getHeight());
                footerTextSpace += (int) Math.ceil(fRect[i].getHeight());
                }
            
            // Subtract footer plus the gap from the available space
            availableSpace -= (footerTextSpace + MyTablePrintable.H_F_SPACE);
            }

        if (availableSpace <= 0)
            throw new PrinterException("Height of printable area is too small.");

        // depending on the print mode, we may need a scale factor to
        // fit the table's entire width on the page
        double sf = 1.0D;
        if ((this.printMode == JTable.PrintMode.FIT_WIDTH) && (this.totalColWidth > imageableWidth)) 
            {
            // if not, we would have thrown an exception previously
            assert imageableWidth > 0;

            // it must be, according to the if-condition, since imgWidth > 0
            assert this.totalColWidth > 1;

            sf = (double) imageableWidth / (double) this.totalColWidth;
            }

        // dictated by the previous two assertions
        assert sf > 0;

        // This is in a loop for two reasons:
        // First, it allows us to catch up in case we're called starting
        // with a non-zero pageIndex. Second, we know that we can be called
        // for the same page multiple times. The condition of this while
        // loop acts as a check, ensuring that we don't attempt to do the
        // calculations again when we are called subsequent times for the
        // same page.
        while (this.last < pageIndex) {
            // if we are finished all columns in all rows
            if ((this.row >= this.table.getRowCount()) && (this.col == 0))
                return Printable.NO_SUCH_PAGE;

            // rather than multiplying every row and column by the scale factor
            // in findNextClip, just pass a width and height that have already
            // been divided by it
            final int scaledWidth = (int) (imageableWidth / sf);
            final int scaledHeight = (int) ((availableSpace - this.hclip.height) / sf);

            // calculate the area of the table to be printed for this page
            this.findNextClip(scaledWidth, scaledHeight);

            this.last++;
        }

        // Create a copy of the graphics object so we don't affect the one given to us
        final Graphics2D g2d = (Graphics2D) graphics.create();
        
        // translate into the co-ordinate system of the pageFormat
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // to save and store the transform
        AffineTransform oldTrans;

        // if there's footer text, print it at the bottom of the imageable area
        if (footerText != null) 
            {
            oldTrans = g2d.getTransform();

            g2d.translate(0, imageableHeight - footerTextSpace);
            for (int i = 0; i < footerText.length; i++) 
                this.printText(g2d, footerText[i], fRect[i], this.footerFont, i, imageableWidth);

            g2d.setTransform(oldTrans);
            }

        // if there's header text, print it at the top of the imageable area
        // and then translate downwards
        if (headerText != null) 
            {
            for (int i = 0; i < headerText.length; i++) 
                {
                // The first line of the header is bold
                if (i == 0)
                    this.printText(g2d, headerText[i], hRect[i], this.headerFont1, i, imageableWidth);
                else
                    this.printText(g2d, headerText[i], hRect[i], this.headerFont2, i, imageableWidth);
                }

            g2d.translate(0, headerTextSpace + MyTablePrintable.H_F_SPACE);
            }

        // constrain the table output to the available space
        this.tempRect.x = 0;
        this.tempRect.y = 0;
        this.tempRect.width = imageableWidth;
        this.tempRect.height = availableSpace;
        g2d.clip(this.tempRect);

        // if we have a scale factor, scale the graphics object to fit
        // the entire width
        if (sf != 1.0D) 
            g2d.scale(sf, sf);
        else 
            {
            // otherwise, ensure that the current portion of the table is
            // centered horizontally
            final int diff = (imageableWidth - this.clip.width) / 2;
            g2d.translate(diff, 0);
            }

        // store the old transform and clip for later restoration
        oldTrans = g2d.getTransform();
        final Shape oldClip = g2d.getClip();

        // if there's a table header, print the current section and
        // then translate downwards
        if (this.header != null) {
            this.hclip.x = this.clip.x;
            this.hclip.width = this.clip.width;

            g2d.translate(-this.hclip.x, 0);
            g2d.clip(this.hclip);
            this.header.print(g2d);

            // restore the original transform and clip
            g2d.setTransform(oldTrans);
            g2d.setClip(oldClip);

            // translate downwards
            g2d.translate(0, this.hclip.height);
        }

        // print the current section of the table
        g2d.translate(-this.clip.x, -this.clip.y);
        g2d.clip(this.clip);

        // set a property so that BasicTableUI#paint can know JTable printMode
        // is FIT_WIDTH since TablePrintable.printMode is not accessible from BasicTableUI
        if (this.printMode == JTable.PrintMode.FIT_WIDTH)
            this.table.putClientProperty("Table.printMode", JTable.PrintMode.FIT_WIDTH);

        this.table.print(g2d);

        // restore the original transform and clip
        g2d.setTransform(oldTrans);
        g2d.setClip(oldClip);

        // clear the property
        if (this.printMode == JTable.PrintMode.FIT_WIDTH)
            this.table.putClientProperty("Table.printMode", null);

        // dispose the graphics copy
        g2d.dispose();

        return Printable.PAGE_EXISTS;
    }

    /**
     * A helper method that encapsulates common code for rendering the header
     * and footer text.
     *
     * @param g2d the graphics to draw into
     * @param text the text to draw, non null
     * @param rect the bounding rectangle for this text, as calculated at the
     * given font, non null
     * @param font the font to draw the text in, non null
     * @param imgWidth the width of the area to draw into
     */
    private void printText(final Graphics2D g2d,
            final String text,
            final Rectangle2D rect,
            final Font font,
            final int textIndex,
            final int imgWidth) 
        {
        // The X coordinate location to draw the text
        int tx;

        // Set text color and font
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        // if the text is small enough to fit, center it
        if (rect.getWidth() < imgWidth) 
            tx = (int)((imgWidth - rect.getWidth()) / 2);
//            tx = (int) (imgWidth / 2 - g2d.getFontMetrics().getStringBounds(text, g2d).getWidth() / 2);

        // otherwise, if the table is LTR, ensure the left side of
        // the text shows; the right can be clipped
        else if (this.table.getComponentOrientation().isLeftToRight()) 
            tx = 0;
        else // otherwise, ensure the right side of the text shows
            tx = -(int) (Math.ceil(rect.getWidth()) - imgWidth);

        // Calculate the Y coordinate location to draw the text
        final int ty = (int) Math.ceil(Math.abs(rect.getY() + (textIndex * rect.getHeight())));

        // Display the text
        g2d.drawString(text, tx, ty);
    }

    /**
     * Calculate the area of the table to be printed for the next page. This
     * should only be called if there are rows and columns left to print.
     *
     * To avoid an infinite loop in printing, this will always put at least one
     * cell on each page.
     *
     * @param pw the width of the area to print in
     * @param ph the height of the area to print in
     */
    private void findNextClip(final int pw, final int ph) {
        final boolean ltr = this.table.getComponentOrientation().isLeftToRight();

        // if we're ready to start a new set of rows
        if (this.col == 0) {
            if (ltr) 
                {
                // adjust clip to the left of the first column
                this.clip.x = 0;
                } 
            else 
                {
                // adjust clip to the right of the first column
                this.clip.x = this.totalColWidth;
                }

            // adjust clip to the top of the next set of rows
            this.clip.y += this.clip.height;

            // adjust clip width and height to be zero
            this.clip.width = 0;
            this.clip.height = 0;

            // fit as many rows as possible, and at least one
            final int rowCount = this.table.getRowCount();
            int rowHeight = this.table.getRowHeight(this.row);
            do {
                this.clip.height += rowHeight;

                if (++this.row >= rowCount)
                    break;

                rowHeight = this.table.getRowHeight(this.row);
            } while (this.clip.height + rowHeight <= ph);
        }

        // we can short-circuit for JTable.PrintMode.FIT_WIDTH since
        // we'll always fit all columns on the page
        if (this.printMode == JTable.PrintMode.FIT_WIDTH) 
            {
            this.clip.x = 0;
            this.clip.width = this.totalColWidth;
            return;
            }

        if (ltr) 
            {
            // adjust clip to the left of the next set of columns
            this.clip.x += this.clip.width;
            }

        // adjust clip width to be zero
        this.clip.width = 0;

        // fit as many columns as possible, and at least one
        final int colCount = this.table.getColumnCount();
        int colWidth = this.colModel.getColumn(this.col).getWidth();
        do {
            this.clip.width += colWidth;
            if (!ltr)
                this.clip.x -= colWidth;

            if (++this.col >= colCount) 
                {
                // reset col to 0 to indicate we've finished all columns
                this.col = 0;
                break;
                }

            colWidth = this.colModel.getColumn(this.col).getWidth();
        } while (this.clip.width + colWidth <= pw);
    }
} 
