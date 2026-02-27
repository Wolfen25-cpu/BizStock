package bizstock.util;

import bizstock.dao.ProductDAO;
import bizstock.model.Product;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PdfExporter {

        private static final String OUTPUT_DIR =
    System.getProperty("user.dir") + "/Reportes_inventario";
    // ── Layout de la tabla ─────────────────────────────────────────────────────
    private static final float MARGIN      = 40f;
    private static final float PAGE_WIDTH  = PDRectangle.LETTER.getWidth();   // 612 pt
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();  // 792 pt
    private static final float TABLE_WIDTH = PAGE_WIDTH - MARGIN * 2;         // 532 pt

    // Columnas: { ancho, encabezado, alineación "L"/"R"/"C" }
    // Los anchos deben sumar TABLE_WIDTH (532)
    private static final Object[][] COLS = {
        {  36f, "ID",       "R" },
        { 180f, "Nombre",   "L" },
        {  70f, "Categoría","L" },
        {  52f, "Cantidad", "R" },
        {  52f, "Crítico",  "R" },
        {  52f, "Reorden",  "R" },
        {  90f, "Precio",   "R" },
    };

    private static final float ROW_H    = 18f;
    private static final float HEADER_H = 20f;
    private static final float CELL_PAD =  4f;

    // ── Colores ────────────────────────────────────────────────────────────────
    private static final Color C_HEADER_BG = new Color(31,  78, 121);
    private static final Color C_HEADER_FG = Color.WHITE;
    private static final Color C_ROW_ALT   = new Color(235, 244, 255);
    private static final Color C_BORDER    = new Color(180, 200, 220);
    private static final Color C_TITLE     = new Color(31,  78, 121);

    private PdfExporter() {}

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public static String exportInventory() throws Exception {
        ProductDAO dao = new ProductDAO();
        List<Product> list = dao.findAllActive();
        return writePdf("Inventario_Completo", "Reporte de Inventario", list);
    }

    public static String exportReorder() throws Exception {
        ProductDAO dao = new ProductDAO();
        List<Product> list = new ArrayList<>();
        list.addAll(dao.findCriticalAlerts());
        list.addAll(dao.findLowAlerts());
        return writePdf("Productos_a_Reorden", "Productos a Reordenar", list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generación del PDF
    // ─────────────────────────────────────────────────────────────────────────

    private static String writePdf(String filePrefix, String reportTitle,
                                   List<Product> products) throws Exception {
        // Crear carpeta si no existe
        File dir = new File(OUTPUT_DIR);
        dir.mkdirs();

        String stamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File out = new File(dir, filePrefix + "_" + stamp + ".pdf");

        PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        try (PDDocument doc = new PDDocument()) {

            if (products.isEmpty()) {
                addEmptyPage(doc, reportTitle, fontBold, fontRegular);
            } else {
                // Calcular filas por página
                float titleBlock   = 65f;
                float usable       = PAGE_HEIGHT - MARGIN * 2 - titleBlock - HEADER_H;
                int   rowsPerPage  = Math.max(1, (int) (usable / ROW_H));

                int pageIndex = 0;
                int i = 0;
                while (i < products.size()) {
                    int end      = Math.min(i + rowsPerPage, products.size());
                    List<Product> pageRows = products.subList(i, end);

                    PDPage page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);

                    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                        float y = PAGE_HEIGHT - MARGIN;
                        y = drawTitle(cs, reportTitle, pageIndex, y, fontBold, fontRegular);
                        drawTable(cs, pageRows, y, fontBold, fontRegular);
                    }

                    i = end;
                    pageIndex++;
                }
            }

            doc.save(out);
        }

        return out.getAbsolutePath();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Título del reporte
    // ─────────────────────────────────────────────────────────────────────────

    private static float drawTitle(PDPageContentStream cs, String title, int pageNum,
                                   float y, PDType1Font bold, PDType1Font regular) throws Exception {
        // Título
        cs.beginText();
        cs.setFont(bold, 18);
        setColor(cs, C_TITLE);
        cs.newLineAtOffset(MARGIN, y - 20);
        cs.showText("BizStock  \u2013  " + title);
        cs.endText();
        y -= 28;

        // Subtítulo
        String sub = "Generado: "
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss"))
            + (pageNum > 0 ? "     Página " + (pageNum + 1) : "");
        cs.beginText();
        cs.setFont(regular, 9);
        setColor(cs, Color.GRAY);
        cs.newLineAtOffset(MARGIN, y - 10);
        cs.showText(sub);
        cs.endText();
        y -= 20;

        // Línea separadora
        setColor(cs, C_TITLE);
        cs.setLineWidth(0.8f);
        cs.moveTo(MARGIN, y - 2);
        cs.lineTo(PAGE_WIDTH - MARGIN, y - 2);
        cs.stroke();

        return y - 10;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tabla con celdas y líneas reales
    // ─────────────────────────────────────────────────────────────────────────

    private static void drawTable(PDPageContentStream cs, List<Product> rows,
                                  float startY, PDType1Font bold, PDType1Font regular) throws Exception {
        float y = startY;

        // ── Encabezados ────────────────────────────────────────────────────────
        float x = MARGIN;
        setColor(cs, C_HEADER_BG);
        cs.addRect(x, y - HEADER_H, TABLE_WIDTH, HEADER_H);
        cs.fill();

        setColor(cs, C_HEADER_FG);
        for (Object[] col : COLS) {
            float  w     = (float)  col[0];
            String text  = (String) col[1];
            String align = (String) col[2];
            drawCell(cs, bold, 9, text, x, y, w, HEADER_H, align, C_HEADER_FG);
            x += w;
        }
        y -= HEADER_H;

        // ── Filas de datos ─────────────────────────────────────────────────────
        for (int i = 0; i < rows.size(); i++) {
            Product p    = rows.get(i);
            float   rowY = y;

            // Fondo alternado
            if (i % 2 == 1) {
                setColor(cs, C_ROW_ALT);
                cs.addRect(MARGIN, rowY - ROW_H, TABLE_WIDTH, ROW_H);
                cs.fill();
            }

            // Valores de la fila
            String[] vals = {
                String.valueOf(p.getId()),
                truncate(safe(p.getName()), 28),
                String.valueOf(p.getCategoryId()),
                String.valueOf(p.getQuantity()),
                String.valueOf(p.getCriticalLevel()),
                String.valueOf(p.getReorderLevel()),
                p.getPrice() != null ? "$" + p.getPrice().toPlainString() : "$0.00"
            };

            x = MARGIN;
            for (int c = 0; c < COLS.length; c++) {
                float  w     = (float)  COLS[c][0];
                String align = (String) COLS[c][2];
                drawCell(cs, regular, 9, vals[c], x, rowY, w, ROW_H, align, Color.BLACK);
                x += w;
            }

            // Línea inferior de fila
            setColor(cs, C_BORDER);
            cs.setLineWidth(0.3f);
            cs.moveTo(MARGIN, rowY - ROW_H);
            cs.lineTo(PAGE_WIDTH - MARGIN, rowY - ROW_H);
            cs.stroke();

            y -= ROW_H;
        }

        // ── Borde exterior ─────────────────────────────────────────────────────
        float tableBottom = y;
        float tableHeight = startY - tableBottom;
        setColor(cs, C_BORDER);
        cs.setLineWidth(0.8f);
        cs.addRect(MARGIN, tableBottom, TABLE_WIDTH, tableHeight);
        cs.stroke();

        // ── Líneas verticales ──────────────────────────────────────────────────
        x = MARGIN;
        for (int c = 0; c < COLS.length - 1; c++) {
            x += (float) COLS[c][0];
            cs.moveTo(x, startY);
            cs.lineTo(x, tableBottom);
            cs.stroke();
        }

        // ── Pie de tabla ───────────────────────────────────────────────────────
        cs.beginText();
        cs.setFont(regular, 8);
        setColor(cs, Color.GRAY);
        cs.newLineAtOffset(MARGIN, tableBottom - 14);
        cs.showText("Total: " + rows.size() + " producto(s)");
        cs.endText();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Página vacía
    // ─────────────────────────────────────────────────────────────────────────

    private static void addEmptyPage(PDDocument doc, String title,
                                     PDType1Font bold, PDType1Font regular) throws Exception {
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            drawTitle(cs, title, 0, PAGE_HEIGHT - MARGIN, bold, regular);
            cs.beginText();
            cs.setFont(regular, 12);
            setColor(cs, Color.GRAY);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT / 2);
            cs.showText("No hay datos para exportar.");
            cs.endText();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Dibuja texto dentro de una celda con alineación y padding correctos.
     * x, y = esquina superior-izquierda de la celda.
     */
    private static void drawCell(PDPageContentStream cs, PDType1Font font, float fontSize,
                                 String text, float x, float y, float w, float h,
                                 String align, Color color) throws Exception {
        if (text == null || text.isEmpty()) return;

        float textW  = font.getStringWidth(text) / 1000f * fontSize;
        float textH  = fontSize * 0.72f;
        float ty     = y - h / 2f + textH / 2f;   // centrado vertical

        float tx;
        switch (align) {
            case "R": tx = x + w - CELL_PAD - textW; break;
            case "C": tx = x + (w - textW) / 2f;     break;
            default:  tx = x + CELL_PAD;              break;  // "L"
        }

        // Recortar si se sale (no dibujar fuera de la celda)
        if (tx < x) tx = x + CELL_PAD;

        cs.beginText();
        cs.setFont(font, fontSize);
        setColor(cs, color);
        cs.newLineAtOffset(tx, ty);
        cs.showText(text);
        cs.endText();
    }

    private static void setColor(PDPageContentStream cs, Color c) throws Exception {
        cs.setNonStrokingColor(c);
        cs.setStrokingColor(c);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
