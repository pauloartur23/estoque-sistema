package com.estoque.service;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

/**
 * Responsável por salvar as fotos de produto localmente (pasta
 * "imagens_produtos") e por converter imagens para exibição
 * (JavaFX Image ou Base64 para uso no WebView/HTML).
 */
public class ImagemService {

    private static final String PASTA_IMAGENS = "imagens_produtos";

    /**
     * Copia o arquivo escolhido pelo usuário para a pasta interna de
     * imagens do sistema, com um nome único, e retorna o caminho salvo.
     */
    public String salvar(String codigoProduto, File arquivoOrigem) throws IOException {
        File pasta = new File(PASTA_IMAGENS);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        String extensao = obterExtensao(arquivoOrigem.getName());
        String nomeArquivo = codigoProduto + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;
        Path destino = new File(pasta, nomeArquivo).toPath();

        Files.copy(arquivoOrigem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        return destino.toAbsolutePath().toString();
    }

    /** Carrega a imagem do caminho salvo para exibir em um ImageView do JavaFX. */
    public Image carregarParaPreview(String caminho, double largura, double altura) {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            return null;
        }
        return new Image(arquivo.toURI().toString(), largura, altura, true, true);
    }

    /**
     * Converte a imagem para Base64 (data URI), para ser embutida
     * diretamente em HTML exibido no WebView — evita problemas de
     * carregamento de arquivos locais via file://.
     */
    public String converterParaBase64(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(arquivo)) {
            byte[] bytes = fis.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mime = mimeTypeDaExtensao(obterExtensao(arquivo.getName()));
            return "data:" + mime + ";base64," + base64;
        } catch (IOException e) {
            return null;
        }
    }

    private String obterExtensao(String nomeArquivo) {
        int ponto = nomeArquivo.lastIndexOf('.');
        return ponto >= 0 ? nomeArquivo.substring(ponto).toLowerCase() : ".png";
    }

    private String mimeTypeDaExtensao(String extensao) {
        return switch (extensao) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/png";
        };
    }
}