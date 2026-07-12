package com.estoque.service;

import com.estoque.model.Produto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Responsável por gerar códigos de barras (Code128) a partir do código do
 * produto e por montar o PDF de etiquetas prontas para imprimir e colar
 * no produto físico.
 */
public class BarcodeService {

    private static final String PASTA_ETIQUETAS = "etiquetas";

    /** Gera a imagem (bitmap) do código de barras Code128 a partir do código do produto. */
    public BufferedImage gerarImagem(String codigo, int largura, int altura) throws WriterException {
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(codigo, BarcodeFormat.CODE_128, largura, altura);

        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                imagem.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        return imagem;
    }

    /** Converte o código de barras gerado em uma Image do JavaFX, para exibir na tela (preview). */
    public javafx.scene.image.Image gerarImagemJavaFX(String codigo, int largura, int altura)
            throws WriterException, IOException {
        BufferedImage bufferedImage = gerarImagem(codigo, largura, altura);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return new javafx.scene.image.Image(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * Gera um PDF com várias etiquetas (código de barras + nome + preço),
     * prontas para imprimir em uma folha comum e colar nos produtos físicos.
     *
     * @param produtos        produtos que terão etiqueta gerada
     * @param copiasPorProduto quantas etiquetas repetir por produto (ex.: uma por unidade em estoque)
     */
    public File gerarFolhaDeEtiquetas(List<Produto> produtos, int copiasPorProduto) throws IOException, WriterException {
        File pasta = new File(PASTA_ETIQUETAS);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivo = new File(pasta, "etiquetas_" + System.currentTimeMillis() + ".pdf");

        Document documento = new Document(PageSize.A4, 20, 20, 20, 20);
        try {
            PdfWriter.getInstance(documento, new FileOutputStream(arquivo));
            documento.open();

            PdfPTable tabela = new PdfPTable(3);
            tabela.setWidthPercentage(100);

            for (Produto produto : produtos) {
                for (int i = 0; i < copiasPorProduto; i++) {
                    tabela.addCell(montarCelulaEtiqueta(produto));
                }
            }

            documento.add(tabela);

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF de etiquetas.", e);
        } finally {
            documento.close();
        }

        return arquivo;
    }

    private PdfPCell montarCelulaEtiqueta(Produto produto) throws WriterException, IOException {
        BufferedImage bufferedImage = gerarImagem(produto.getCodigo(), 260, 70);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        Image imagemBarcode = Image.getInstance(baos.toByteArray());
        imagemBarcode.scaleToFit(150, 45);

        Font fonteNome = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontePreco = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(255, 106, 0));
        Font fonteCodigo = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        PdfPTable interna = new PdfPTable(1);

        PdfPCell celulaNome = new PdfPCell(new Paragraph(produto.getNome(), fonteNome));
        celulaNome.setBorder(Rectangle.NO_BORDER);
        celulaNome.setHorizontalAlignment(Element.ALIGN_CENTER);
        interna.addCell(celulaNome);

        PdfPCell celulaImagem = new PdfPCell(imagemBarcode, false);
        celulaImagem.setBorder(Rectangle.NO_BORDER);
        celulaImagem.setHorizontalAlignment(Element.ALIGN_CENTER);
        interna.addCell(celulaImagem);

        PdfPCell celulaCodigo = new PdfPCell(new Paragraph(produto.getCodigo(), fonteCodigo));
        celulaCodigo.setBorder(Rectangle.NO_BORDER);
        celulaCodigo.setHorizontalAlignment(Element.ALIGN_CENTER);
        interna.addCell(celulaCodigo);

        PdfPCell celulaPreco = new PdfPCell(new Paragraph("R$ " + produto.getPreco(), fontePreco));
        celulaPreco.setBorder(Rectangle.NO_BORDER);
        celulaPreco.setHorizontalAlignment(Element.ALIGN_CENTER);
        interna.addCell(celulaPreco);

        PdfPCell celulaEtiqueta = new PdfPCell(interna);
        celulaEtiqueta.setPadding(10);
        celulaEtiqueta.setBorderColor(Color.LIGHT_GRAY);
        return celulaEtiqueta;
    }

    public void abrirArquivo(File arquivo) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(arquivo);
        }
    }
}