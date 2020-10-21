package sample;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import static sample.Data.*;

public class Word {

    public static void createWord7(String dest) throws IOException {
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 1.Simple Text Line
        Style normal = new Style();
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        normal.setFont(font).setFontSize(14);
        Style code = new Style();
        PdfFont monospace = PdfFontFactory.createFont(StandardFonts.COURIER);
        code.setFont(monospace).setFontColor(ColorConstants.RED).setBackgroundColor(ColorConstants.LIGHT_GRAY);
        Paragraph pgFirstLine = new Paragraph();
        pgFirstLine.add(new Text("The Strange Case of ").addStyle(normal));
        pgFirstLine.add(new Text("Dr. Jekyll").addStyle(code));
        pgFirstLine.add(new Text(" and ").addStyle(normal));
        pgFirstLine.add(new Text("Mr. Hyde").addStyle(code));
        pgFirstLine.add(new Text(".").addStyle(normal));
        document.add(pgFirstLine);

        // 2.Simple List example
        List list = new List()
                .setSymbolIndent(12)
                .setListSymbol("\u2022")
                .setFont(font);
        list.add(new ListItem("Never gonna give you up"))
                .add(new ListItem("Never gonna let you down"))
                .add(new ListItem("Never gonna run around and desert you"))
                .add(new ListItem("Never gonna make you cry"))
                .add(new ListItem("Never gonna say goodbye"))
                .add(new ListItem("Never gonna tell a lie and hurt you"));
        document.add(list);

        // 3.Simple image example.
        Image fox = new Image(ImageDataFactory.create(FOX));
        Image dog = new Image(ImageDataFactory.create(DOG));
        Paragraph paragraphImage = new Paragraph("The quick brown ")
                .add(fox)
                .add(" jumps over the lazy ")
                .add(dog);
        document.add(paragraphImage);

        // 4.Simple table example.
        final String data1 = "src/main/resources/data/united_states.csv";
        final PdfFont boldTable = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        final PdfFont fontTable = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true);
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1, 3, 4, 3, 3, 3, 3, 1})).useAllAvailableWidth();
        BufferedReader br = new BufferedReader(new FileReader(data1));
        String line = br.readLine();
        process(table, line, boldTable, true);
        while ((line = br.readLine()) != null) {
            process(table, line, fontTable, false);
        }
        br.close();
        document.add(table);

        // 5.Simple event handler example.
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new MyEventHandler("爱丽家居", "水印"));

        // 6.中文文章
        Style chinese = new Style();
        PdfFont fontChinese = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true);
        chinese.setFont(fontChinese);
        Paragraph paragraphMy = new Paragraph(new Text(LONG_ARTICLE).addStyle(chinese));
        paragraphMy.setFirstLineIndent(0); // 首行缩进
        paragraphMy.setMarginLeft(20);// 整体缩进
        paragraphMy.setTextAlignment(TextAlignment.LEFT);// 居右显示
        document.add(paragraphMy);

        document.close();
    }

    private static void process(Table table, String line, PdfFont font, boolean isHeader) {
        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        while (tokenizer.hasMoreTokens()) {
            if (isHeader) {
                table.addHeaderCell(new Cell().add(new Paragraph(tokenizer.nextToken()).setFont(font)));
            } else {
                table.addCell(new Cell().add(new Paragraph(tokenizer.nextToken()).setFont(font)));
            }
        }
    }

    public static void main(String[] args) {
        File file = new File(DEST);
        file.getParentFile().mkdirs();

        try {
            createWord7(DEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
