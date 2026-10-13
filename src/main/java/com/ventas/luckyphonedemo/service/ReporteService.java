package com.ventas.luckyphonedemo.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.ItemCelularRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ProductoRepository productoRepository;
    private final ItemCelularRepository itemCelularRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter SIMPLE_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Calcula la fecha de corte según el filtro de periodo seleccionado
     */
    private LocalDateTime calcularFechaInicio(String periodo) {
        if (periodo == null) return LocalDateTime.of(2020, 1, 1, 0, 0);
        return switch (periodo.toLowerCase().trim()) {
            case "dias", "dia" -> LocalDateTime.now().minusDays(7);
            case "semanas", "semana" -> LocalDateTime.now().minusWeeks(4);
            case "meses", "mes" -> LocalDateTime.now().minusMonths(6);
            case "anios", "anio", "ano" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.of(2020, 1, 1, 0, 0);
        };
    }

    private String obtenerEtiquetaPeriodo(String periodo) {
        if (periodo == null) return "Histórico Completo";
        return switch (periodo.toLowerCase().trim()) {
            case "dias", "dia" -> "Últimos 7 Días";
            case "semanas", "semana" -> "Últimas 4 Semanas";
            case "meses", "mes" -> "Últimos 6 Meses";
            case "anios", "anio", "ano" -> "Último Año";
            default -> "Histórico Completo";
        };
    }

    // =========================================================================
    // 1. GENERACIÓN DE REPORTE EXCEL (APACHE POI)
    // =========================================================================
    @Transactional(readOnly = true)
    public byte[] generarExcelInventario(String categoria, String periodo) throws IOException {
        LocalDateTime fechaInicio = calcularFechaInicio(periodo);
        List<Producto> productos = productoRepository.findAll();

        if (categoria != null && !categoria.equalsIgnoreCase("TODOS") && !categoria.isBlank()) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getNombre().toLowerCase().contains(categoria.toLowerCase()))
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Auditoría de Inventario");

            // Estilos
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("S/ #,##0.00"));

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Título Superior
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(35);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LUCKYPHONE - REPORTE DE AUDITORÍA Y ROTACIÓN DE INVENTARIO");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 14));

            // Metadatos
            Row metaRow = sheet.createRow(1);
            metaRow.createCell(0).setCellValue("Filtro Periodo: " + obtenerEtiquetaPeriodo(periodo) +
                    " | Categoría: " + (categoria != null ? categoria : "TODOS") +
                    " | Generado: " + LocalDateTime.now().format(DATE_FORMATTER));

            // Cabeceras de Tabla
            String[] columnas = {
                    "#", "ID Producto", "Categoría", "Marca", "Modelo", "Condición", "Color",
                    "Capacidad / RAM", "IMEI 1", "IMEI 2", "N° Serie", "Batería",
                    "Estado Unidad", "Fecha Venta", "Precio (S/)"
            };

            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < columnas.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int correlativo = 1;
            double sumaPrecios = 0.0;

            for (Producto p : productos) {
                boolean esCelular = p.getCategoria() != null && "Celulares".equalsIgnoreCase(p.getCategoria().getNombre());
                List<ItemCelular> items = p.getItemsCelulares();

                if (esCelular && items != null && !items.isEmpty()) {
                    for (ItemCelular it : items) {
                        // Filtrar por periodo si aplica
                        if (it.getFechaVenta() != null && it.getFechaVenta().isBefore(fechaInicio)) {
                            continue;
                        }

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(correlativo++);
                        row.createCell(1).setCellValue(p.getId());
                        row.createCell(2).setCellValue(p.getCategoria() != null ? p.getCategoria().getNombre() : "Celulares");
                        row.createCell(3).setCellValue(p.getMarca() != null ? p.getMarca() : "Apple");
                        row.createCell(4).setCellValue(p.getModelo() != null ? p.getModelo() : p.getNombre());
                        row.createCell(5).setCellValue(p.getEstado() != null ? p.getEstado() : "Nuevo / Sellado");
                        row.createCell(6).setCellValue(p.getColor() != null ? p.getColor() : "-");
                        row.createCell(7).setCellValue((p.getAlmacenamiento() != null ? p.getAlmacenamiento() : "") + " / " + (p.getRam() != null ? p.getRam() : ""));
                        row.createCell(8).setCellValue(it.getImei1() != null ? it.getImei1() : "-");
                        row.createCell(9).setCellValue(it.getImei2() != null ? it.getImei2() : "-");
                        row.createCell(10).setCellValue(it.getNumeroSerial() != null ? it.getNumeroSerial() : "-");
                        row.createCell(11).setCellValue(it.getBateria() != null ? it.getBateria() : "100%");
                        row.createCell(12).setCellValue(it.getEstadoDispositivo() != null ? it.getEstadoDispositivo().name() : "Disponible");
                        row.createCell(13).setCellValue(it.getFechaVenta() != null ? it.getFechaVenta().format(SIMPLE_DATE) : "-");

                        org.apache.poi.ss.usermodel.Cell priceCell = row.createCell(14);
                        priceCell.setCellValue(p.getPrecio());
                        priceCell.setCellStyle(currencyStyle);
                        sumaPrecios += p.getPrecio();
                    }
                } else {
                    // Productos sin unidades físicas (accesorios)
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(correlativo++);
                    row.createCell(1).setCellValue(p.getId());
                    row.createCell(2).setCellValue(p.getCategoria() != null ? p.getCategoria().getNombre() : "Accesorio");
                    row.createCell(3).setCellValue(p.getMarca() != null ? p.getMarca() : "Genérico");
                    row.createCell(4).setCellValue(p.getModelo() != null ? p.getModelo() : p.getNombre());
                    row.createCell(5).setCellValue(p.getEstado() != null ? p.getEstado() : "Nuevo");
                    row.createCell(6).setCellValue(p.getColor() != null ? p.getColor() : "-");
                    row.createCell(7).setCellValue("-");
                    row.createCell(8).setCellValue("N/A");
                    row.createCell(9).setCellValue("N/A");
                    row.createCell(10).setCellValue("N/A");
                    row.createCell(11).setCellValue("-");
                    row.createCell(12).setCellValue("Stock: " + p.getStock());
                    row.createCell(13).setCellValue("-");

                    org.apache.poi.ss.usermodel.Cell priceCell = row.createCell(14);
                    priceCell.setCellValue(p.getPrecio());
                    priceCell.setCellStyle(currencyStyle);
                    sumaPrecios += p.getPrecio();
                }
            }

            // Fila de Resumen / Totales
            Row totalRow = sheet.createRow(rowIdx + 1);
            org.apache.poi.ss.usermodel.Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL AUDITADO (" + (correlativo - 1) + " registros):");
            org.apache.poi.ss.usermodel.Cell totalValue = totalRow.createCell(14);
            totalValue.setCellValue(sumaPrecios);
            totalValue.setCellStyle(currencyStyle);

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // 2. GENERACIÓN DE REPORTE PDF CONTABLE A4 (OPENPDF)
    // =========================================================================
    @Transactional(readOnly = true)
    public byte[] generarPdfContable(String categoria, String periodo) throws DocumentException {
        LocalDateTime fechaInicio = calcularFechaInicio(periodo);
        List<Producto> productos = productoRepository.findAll();

        if (categoria != null && !categoria.equalsIgnoreCase("TODOS") && !categoria.isBlank()) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getNombre().toLowerCase().contains(categoria.toLowerCase()))
                    .collect(Collectors.toList());
        }

        Document document = new Document(PageSize.A4, 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Tipografías
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(49, 46, 129));
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
        Font monoFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.BLACK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(49, 46, 129));

        // Cabecera Editorial
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("LUCKYPHONE S.A.C.", titleFont));
        leftCell.addElement(new Paragraph("RUC: 20609876543 | Venta Especializada de Telefonía Móvil", subTitleFont));
        leftCell.addElement(new Paragraph("Dirección: Av. Tecnología 1024, Lima - Perú | Tel: +51 912 345 678", subTitleFont));

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(new Paragraph("REPORTE CONTABLE DE ROTACIÓN FÍSICA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK)));
        rightCell.addElement(new Paragraph("Rango: " + obtenerEtiquetaPeriodo(periodo), subTitleFont));
        rightCell.addElement(new Paragraph("Categoría: " + (categoria != null ? categoria : "TODAS"), subTitleFont));
        rightCell.addElement(new Paragraph("Fecha Emisión: " + LocalDateTime.now().format(DATE_FORMATTER), subTitleFont));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        document.add(new Paragraph(" "));

        // Tabla Principal Contable
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5, 22, 13, 16, 14, 10, 10, 10});

        String[] headers = {"#", "Modelo / Equipo", "Condición", "IMEI 1", "N° Serie", "Estado", "F. Venta", "Precio (S/)"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, headerFont));
            c.setBackgroundColor(new Color(49, 46, 129));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setPadding(4);
            table.addCell(c);
        }

        int correlativo = 1;
        double montoTotal = 0.0;

        for (Producto p : productos) {
            boolean esCelular = p.getCategoria() != null && "Celulares".equalsIgnoreCase(p.getCategoria().getNombre());
            List<ItemCelular> items = p.getItemsCelulares();

            if (esCelular && items != null && !items.isEmpty()) {
                for (ItemCelular it : items) {
                    if (it.getFechaVenta() != null && it.getFechaVenta().isBefore(fechaInicio)) {
                        continue;
                    }

                    Color rowBg = (correlativo % 2 == 0) ? new Color(248, 250, 252) : Color.WHITE;

                    PdfPCell c0 = new PdfPCell(new Phrase(String.valueOf(correlativo++), cellFont));
                    c0.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c0.setBackgroundColor(rowBg);
                    table.addCell(c0);

                    PdfPCell c1 = new PdfPCell(new Phrase(p.getModelo() != null ? p.getModelo() : p.getNombre(), cellFont));
                    c1.setBackgroundColor(rowBg);
                    table.addCell(c1);

                    PdfPCell c2 = new PdfPCell(new Phrase(p.getEstado() != null ? p.getEstado() : "-", cellFont));
                    c2.setBackgroundColor(rowBg);
                    table.addCell(c2);

                    PdfPCell c3 = new PdfPCell(new Phrase(it.getImei1() != null ? it.getImei1() : "-", monoFont));
                    c3.setBackgroundColor(rowBg);
                    table.addCell(c3);

                    PdfPCell c4 = new PdfPCell(new Phrase(it.getNumeroSerial() != null ? it.getNumeroSerial() : "-", monoFont));
                    c4.setBackgroundColor(rowBg);
                    table.addCell(c4);

                    PdfPCell c5 = new PdfPCell(new Phrase(it.getEstadoDispositivo() != null ? it.getEstadoDispositivo().name() : "Disponible", cellFont));
                    c5.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c5.setBackgroundColor(rowBg);
                    table.addCell(c5);

                    PdfPCell c6 = new PdfPCell(new Phrase(it.getFechaVenta() != null ? it.getFechaVenta().format(SIMPLE_DATE) : "-", cellFont));
                    c6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c6.setBackgroundColor(rowBg);
                    table.addCell(c6);

                    PdfPCell c7 = new PdfPCell(new Phrase(String.format("S/ %.2f", p.getPrecio()), cellFont));
                    c7.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c7.setBackgroundColor(rowBg);
                    table.addCell(c7);

                    montoTotal += p.getPrecio();
                }
            } else {
                Color rowBg = (correlativo % 2 == 0) ? new Color(248, 250, 252) : Color.WHITE;

                PdfPCell c0 = new PdfPCell(new Phrase(String.valueOf(correlativo++), cellFont));
                c0.setHorizontalAlignment(Element.ALIGN_CENTER);
                c0.setBackgroundColor(rowBg);
                table.addCell(c0);

                PdfPCell c1 = new PdfPCell(new Phrase(p.getModelo() != null ? p.getModelo() : p.getNombre(), cellFont));
                c1.setBackgroundColor(rowBg);
                table.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Phrase(p.getEstado() != null ? p.getEstado() : "Nuevo", cellFont));
                c2.setBackgroundColor(rowBg);
                table.addCell(c2);

                PdfPCell c3 = new PdfPCell(new Phrase("N/A", monoFont));
                c3.setBackgroundColor(rowBg);
                table.addCell(c3);

                PdfPCell c4 = new PdfPCell(new Phrase("N/A", monoFont));
                c4.setBackgroundColor(rowBg);
                table.addCell(c4);

                PdfPCell c5 = new PdfPCell(new Phrase("Stock: " + p.getStock(), cellFont));
                c5.setHorizontalAlignment(Element.ALIGN_CENTER);
                c5.setBackgroundColor(rowBg);
                table.addCell(c5);

                PdfPCell c6 = new PdfPCell(new Phrase("-", cellFont));
                c6.setHorizontalAlignment(Element.ALIGN_CENTER);
                c6.setBackgroundColor(rowBg);
                table.addCell(c6);

                PdfPCell c7 = new PdfPCell(new Phrase(String.format("S/ %.2f", p.getPrecio()), cellFont));
                c7.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c7.setBackgroundColor(rowBg);
                table.addCell(c7);

                montoTotal += p.getPrecio();
            }
        }

        document.add(table);

        // Bloque de Totales y Firmas
        document.add(new Paragraph(" "));
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{60, 40});

        PdfPCell sumLeft = new PdfPCell();
        sumLeft.setBorder(Rectangle.NO_BORDER);
        sumLeft.addElement(new Paragraph("TOTAL UNIDADES AUDITADAS: " + (correlativo - 1) + " ítems", totalFont));

        PdfPCell sumRight = new PdfPCell();
        sumRight.setBorder(Rectangle.NO_BORDER);
        sumRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph pTotal = new Paragraph(String.format("TOTAL VALORIZADO: S/ %.2f", montoTotal), totalFont);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        sumRight.addElement(pTotal);

        summaryTable.addCell(sumLeft);
        summaryTable.addCell(sumRight);
        document.add(summaryTable);

        // Firmas
        document.add(new Paragraph("\n\n"));
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);

        PdfPCell sign1 = new PdfPCell(new Paragraph("_____________________________________\nFirma Auditor / Contador Externo\nColegiatura CCP / Matrícula", subTitleFont));
        sign1.setBorder(Rectangle.NO_BORDER);
        sign1.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell sign2 = new PdfPCell(new Paragraph("_____________________________________\nAdministración LuckyPhone\nControl Interno de Almacén", subTitleFont));
        sign2.setBorder(Rectangle.NO_BORDER);
        sign2.setHorizontalAlignment(Element.ALIGN_CENTER);

        signTable.addCell(sign1);
        signTable.addCell(sign2);
        document.add(signTable);

        document.close();
        return out.toByteArray();
    }
}