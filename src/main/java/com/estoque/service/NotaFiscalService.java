package com.estoque.service;

import com.estoque.model.ItemVenda;
import com.estoque.model.Venda;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import com.lowagie.text.pdf.draw.LineSeparator;

/**
 * Gera a Nota Fiscal em PDF a partir de uma Venda já finalizada.
 * Usa a biblioteca OpenPDF (com.lowagie.text).
 */
public class NotaFiscalService {

    private static final String PASTA_SAIDA = "notas_fiscais";
    private static final DateTimeFormatter FORMATADOR_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String NOME_EMPRESA = "Estoque Fácil Sistemas Ltda.";
    private static final String CNPJ_EMPRESA = "00.000.000/0001-00";
    private static final String ENDERECO_EMPRESA = "Mossoró - RN";

    public File gerar(Venda venda) throws IOException {
        File pasta = new File(PASTA_SAIDA);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivo = new File(pasta, "nota_fiscal_" + venda.getId() + ".pdf");

        Document documento = new Document(PageSize.A4, 40, 40, 50, 40);
        try {
            PdfWriter.getInstance(documento, new FileOutputStream(arquivo));
            documento.open();

            adicionarCabecalho(documento, venda);
            adicionarTabelaItens(documento, venda);
            adicionarTotal(documento, venda);
            adicionarRodape(documento);

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar o PDF da nota fiscal.", e);
        } finally {
            documento.close();
        }

        return arquivo;
    }

    /** Gera o PDF e já abre no visualizador padrão do sistema operacional. */
    public File gerarEAbrir(Venda venda) throws IOException {
        File arquivo = gerar(venda);
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(arquivo);
        }
        return arquivo;
    }

    private void adicionarCabecalho(Document documento, Venda venda) throws DocumentException {
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font fonteEmpresa = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font fonteInfo = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph titulo = new Paragraph("NOTA FISCAL DE VENDA", fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph empresa = new Paragraph(
                NOME_EMPRESA + "\nCNPJ: " + CNPJ_EMPRESA + "  |  " + ENDERECO_EMPRESA, fonteEmpresa);
        empresa.setAlignment(Element.ALIGN_CENTER);
        empresa.setSpacingAfter(15);
        documento.add(empresa);

        LineSeparator separador = new LineSeparator();
        documento.add(new Chunk(separador));

        Paragraph info = new Paragraph();
        info.setSpacingBefore(10);
        info.setSpacingAfter(15);
        info.add(new Phrase("Nota Fiscal Nº: " + String.format("%06d", venda.getId()) + "\n", fonteInfo));
        info.add(new Phrase("Data/Hora: " + venda.getDataVenda().format(FORMATADOR_DATA) + "\n", fonteInfo));
        info.add(new Phrase("Cliente: " + venda.getNomeCliente() + "\n", fonteInfo));
        info.add(new Phrase("Forma de pagamento: " + venda.getFormaPagamento(), fonteInfo));
        documento.add(info);
    }

    private void adicionarTabelaItens(Document documento, Venda venda) throws DocumentException {
        PdfPTable tabela = new PdfPTable(new float[]{2f, 5f, 1.3f, 1.8f, 1.8f});
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10);

        Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Color corCabecalho = new Color(52, 73, 94);

        adicionarCelulaCabecalho(tabela, "Código", fonteCabecalho, corCabecalho);
        adicionarCelulaCabecalho(tabela, "Produto", fonteCabecalho, corCabecalho);
        adicionarCelulaCabecalho(tabela, "Qtd.", fonteCabecalho, corCabecalho);
        adicionarCelulaCabecalho(tabela, "Preço Unit.", fonteCabecalho, corCabecalho);
        adicionarCelulaCabecalho(tabela, "Subtotal", fonteCabecalho, corCabecalho);

        Font fonteCorpo = FontFactory.getFont(FontFactory.HELVETICA, 10);
        boolean linhaAlternada = false;

        for (ItemVenda item : venda.getItens()) {
            Color corFundo = linhaAlternada ? new Color(245, 245, 245) : Color.WHITE;

            adicionarCelulaCorpo(tabela, item.getProduto().getCodigo(), fonteCorpo, corFundo, Element.ALIGN_LEFT);
            adicionarCelulaCorpo(tabela, item.getProduto().getNome(), fonteCorpo, corFundo, Element.ALIGN_LEFT);
            adicionarCelulaCorpo(tabela, String.valueOf(item.getQuantidade()), fonteCorpo, corFundo, Element.ALIGN_CENTER);
            adicionarCelulaCorpo(tabela, formatarMoeda(item.getPrecoUnitario()), fonteCorpo, corFundo, Element.ALIGN_RIGHT);
            adicionarCelulaCorpo(tabela, formatarMoeda(item.getSubtotal()), fonteCorpo, corFundo, Element.ALIGN_RIGHT);

            linhaAlternada = !linhaAlternada;
        }

        documento.add(tabela);
    }

    private void adicionarCelulaCabecalho(PdfPTable tabela, String texto, Font fonte, Color corFundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBackgroundColor(corFundo);
        celula.setPadding(6);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabela.addCell(celula);
    }

    private void adicionarCelulaCorpo(PdfPTable tabela, String texto, Font fonte, Color corFundo, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBackgroundColor(corFundo);
        celula.setPadding(6);
        celula.setHorizontalAlignment(alinhamento);
        tabela.addCell(celula);
    }

    private void adicionarTotal(Document documento, Venda venda) throws DocumentException {
        Font fonteTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        Paragraph total = new Paragraph("VALOR TOTAL: " + formatarMoeda(venda.getValorTotal()), fonteTotal);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(15);
        documento.add(total);
    }

    private void adicionarRodape(Document documento) throws DocumentException {
        Font fonteRodape = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

        Paragraph rodape = new Paragraph(
                "\n\nDocumento gerado eletronicamente pelo Sistema de Gerenciamento de Estoque.",
                fonteRodape);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(30);
        documento.add(rodape);
    }

    private String formatarMoeda(BigDecimal valor) {
        return "R$ " + String.format("%,.2f", valor).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
