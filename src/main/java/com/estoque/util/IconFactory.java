package com.estoque.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carrega os ícones SVG (extraídos da biblioteca @edusites/icons) e os
 * renderiza como imagens nativas do JavaFX (via snapshot).
 * <p>
 * IMPORTANTE — por que snapshot e não um Group com Scale direto:
 * aplicar um Transform de escala num Group muda o tamanho VISUAL dele,
 * mas não muda o layoutBounds (o "espaço reservado" que os containers
 * pais usam pra posicionar o nó). Isso fazia o ícone ser desenhado
 * pequeno mas "reservar" um espaço enorme (tamanho original do SVG,
 * ~100x100), estourando pra fora do botão. Tirando um snapshot já no
 * tamanho final, o ícone vira uma Image comum — cujo tamanho reportado
 * e desenhado são sempre iguais.
 */
public final class IconFactory {

    private static final Pattern PATH_PATTERN = Pattern.compile("d=\"([^\"]+)\"");
    private static final Pattern VIEWBOX_PATTERN = Pattern.compile("viewBox=\"([\\d.\\-\\s]+)\"");

    private IconFactory() {
    }

    public static Node criar(String nomeIcone, double tamanho) {
        return criar(nomeIcone, tamanho, Color.web("#FF6A00"));
    }

    public static Node criar(String nomeIcone, double tamanho, Paint cor) {
        String caminho = "/icons/" + nomeIcone + ".svg";

        try (InputStream is = IconFactory.class.getResourceAsStream(caminho)) {
            if (is == null) {
                System.err.println("Ícone não encontrado: " + caminho);
                return criarFallback(tamanho, cor);
            }
            String conteudoSvg = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return construirNode(conteudoSvg, tamanho, cor);

        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone '" + nomeIcone + "': " + e.getMessage());
            return criarFallback(tamanho, cor);
        }
    }

    private static Node construirNode(String conteudoSvg, double tamanho, Paint cor) {
        double viewBoxLargura = 100;
        double viewBoxAltura = 100;

        Matcher viewBoxMatcher = VIEWBOX_PATTERN.matcher(conteudoSvg);
        if (viewBoxMatcher.find()) {
            String[] partes = viewBoxMatcher.group(1).trim().split("\\s+");
            if (partes.length == 4) {
                viewBoxLargura = Double.parseDouble(partes[2]);
                viewBoxAltura = Double.parseDouble(partes[3]);
            }
        }

        Group grupoPaths = new Group();
        Matcher pathMatcher = PATH_PATTERN.matcher(conteudoSvg);
        while (pathMatcher.find()) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathMatcher.group(1));
            svgPath.setFill(cor);
            grupoPaths.getChildren().add(svgPath);
        }

        if (grupoPaths.getChildren().isEmpty()) {
            return criarFallback(tamanho, cor);
        }

        double escala = tamanho / Math.max(viewBoxLargura, viewBoxAltura);
        grupoPaths.getTransforms().add(new Scale(escala, escala, 0, 0));

        return renderizarComoImagem(grupoPaths, tamanho);
    }

    private static Node criarFallback(double tamanho, Paint cor) {
        SVGPath ponto = new SVGPath();
        ponto.setContent("M50,10 a40,40 0 1,0 0.1,0 Z");
        ponto.setFill(cor);
        Group grupo = new Group(ponto);
        double escala = tamanho / 100.0;
        grupo.getTransforms().add(new Scale(escala, escala, 0, 0));
        return renderizarComoImagem(grupo, tamanho);
    }

    /**
     * Renderiza o Group (já escalado visualmente) para uma Image de
     * tamanho exato (tamanho x tamanho), fundo transparente, e retorna
     * um ImageView — que É corretamente redimensionável e sempre
     * reporta o tamanho real que ocupa.
     */
    private static Node renderizarComoImagem(Group grupoEscalado, double tamanho) {
        Pane hospedeiro = new Pane(grupoEscalado);
        hospedeiro.setPrefSize(tamanho, tamanho);
        hospedeiro.setMinSize(tamanho, tamanho);
        hospedeiro.setMaxSize(tamanho, tamanho);

        hospedeiro.applyCss();
        hospedeiro.layout();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        Image imagem = hospedeiro.snapshot(params, null);

        ImageView imageView = new ImageView(imagem);
        imageView.setFitWidth(tamanho);
        imageView.setFitHeight(tamanho);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
}