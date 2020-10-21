package sample;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;

import java.io.IOException;

public class MyEventHandler implements IEventHandler {

    private final String title;
    private final String water;

    public MyEventHandler(String title, String water) {
        this.title = title;
        this.water = water;
    }

    public void handleEvent(Event event) {
        // Chinese Style
        Style chinese = new Style();
        PdfFont fontChinese = null;
        try {
            fontChinese = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        chinese.setFont(fontChinese);

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        int pageNumber = pdfDoc.getPageNumber(page);
        Rectangle pageSize = page.getPageSize();
        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

        // change page size
        float margin = 52;
        Rectangle mediaBox = page.getMediaBox();
        Rectangle newMediaBox = new Rectangle(
                mediaBox.getLeft() - margin, mediaBox.getBottom() - margin,
                mediaBox.getWidth() + margin * 2, mediaBox.getHeight() + margin * 2);
        page.setMediaBox(newMediaBox);
        // add border
        PdfCanvas over = new PdfCanvas(page);
        over.setStrokeColor(ColorConstants.RED);
        over.rectangle(mediaBox.getLeft(), mediaBox.getBottom(),
                mediaBox.getWidth(), mediaBox.getHeight());
        over.stroke();
        // change rotation of the even pages
        if (pageNumber % 2 == 0) {
            page.setRotation(180);
        }

        //Set background
        Color limeColor = new DeviceCmyk(0.208f, 0, 0.584f, 0);
        Color blueColor = new DeviceCmyk(0.445f, 0.0546f, 0, 0.0667f);
        pdfCanvas.saveState()
                .setFillColor(pageNumber % 2 == 1 ? limeColor : blueColor)
                .rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight())
                .fill().restoreState();

        //Add header and footer
        /* pdfCanvas.beginText()
                .setFontAndSize(helvetica, 10)
                .moveText(pageSize.getWidth() / 2 - 60, pageSize.getTop() - 20)
                .showText("爱丽家居")
                .moveText(60, -pageSize.getTop() + 30)
                .showText(String.valueOf(pageNumber))
                .endText();*/

        //Draw header text
        pdfCanvas.beginText().setFontAndSize(
                fontChinese, 10)
                .moveText(pageSize.getWidth() / 2 - 24, pageSize.getHeight() - 15)
                .showText(title)
                .endText();
        //Draw footer line
        pdfCanvas.setStrokeColor(ColorConstants.BLACK)
                .setLineWidth(.1f)
                .moveTo(pageSize.getWidth() / 2 - 30, 25)
                .lineTo(pageSize.getWidth() / 2 + 30, 25).stroke();
        //Draw page number
        pdfCanvas.beginText().setFontAndSize(
                fontChinese, 10)
                .moveText(pageSize.getWidth() / 2 - 7, 10)
                .showText(String.valueOf(pageNumber))
                .showText("  /  ")
                .showText(String.valueOf(pdfDoc.getNumberOfPages()))
                .endText();

        //Add watermark
        PdfFont helveticaBold = null;
        try {
            helveticaBold = PdfFontFactory.createFont(StandardFonts.COURIER_BOLDOBLIQUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Paragraph paragraphWater = new Paragraph(new Text(water).addStyle(chinese));
        Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());
        canvas.setFontColor(ColorConstants.WHITE);
        canvas.setProperty(Property.FONT_SIZE, UnitValue.createPointValue(60));
        canvas.setProperty(Property.FONT, helveticaBold);
        canvas.showTextAligned(paragraphWater, 298, 421, pdfDoc.getPageNumber(page),
                TextAlignment.CENTER, VerticalAlignment.MIDDLE, 45);

        pdfCanvas.release();
    }

    /**
     * Tiling PDF pages
     *
     * @param pdfDoc   目标 PDF PdfDocument
     * @param origPage 原有 PDF PdfPage
     */
    private void tilePages(PdfDocument pdfDoc, PdfPage origPage) {
        //Original page
        PdfFormXObject pageCopy = null;
        try {
            pageCopy = origPage.copyAsFormXObject(pdfDoc);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //Original page size
        Rectangle orig = origPage.getPageSize();
        //Tile size
        Rectangle tileSize = PageSize.A4.rotate();
        // Transformation matrix
        AffineTransform transformationMatrix = AffineTransform.getScaleInstance(tileSize.getWidth() / orig.getWidth() * 2f, tileSize.getHeight() / orig.getHeight() * 2f);

        //The first tile
        PdfPage page = pdfDoc.addNewPage(PageSize.A4.rotate());
        PdfCanvas canvas = new PdfCanvas(page);
        canvas.concatMatrix(transformationMatrix);
        canvas.addXObject(pageCopy, 0, -orig.getHeight() / 2f);

        //The second tile
        page = pdfDoc.addNewPage(PageSize.A4.rotate());
        canvas = new PdfCanvas(page);
        canvas.concatMatrix(transformationMatrix);
        canvas.addXObject(pageCopy, -orig.getWidth() / 2f, -orig.getHeight() / 2f);

        //The third tile
        page = pdfDoc.addNewPage(PageSize.A4.rotate());
        canvas = new PdfCanvas(page);
        canvas.concatMatrix(transformationMatrix);
        canvas.addXObject(pageCopy, 0, 0);

        //The fourth tile
        page = pdfDoc.addNewPage(PageSize.A4.rotate());
        canvas = new PdfCanvas(page);
        canvas.concatMatrix(transformationMatrix);
        canvas.addXObject(pageCopy, -orig.getWidth() / 2f, 0);

    }
}