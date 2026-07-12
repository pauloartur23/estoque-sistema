package com.estoque.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões com o banco de dados MySQL.
 * Padrão utilizado: Singleton.
 * Ajuste URL, USUARIO e SENHA conforme seu ambiente.
 * <p>
 * useUnicode + characterEncoding garantem que acentos (ex: "Periféricos")
 * não cheguem corrompidos do banco para o Java.
 */
public class ConexaoFactory {

    private static final String URL =
            "jdbc:mysql://localhost:3306/estoque_db?useSSL=false&serverTimezone=UTC" +
                    "&useUnicode=true&characterEncoding=UTF-8";
    private static final String USUARIO = "root";
    private static final String SENHA = "123456";

    private static ConexaoFactory instancia;

    private ConexaoFactory() {
    }

    public static ConexaoFactory getInstance() {
        if (instancia == null) {
            instancia = new ConexaoFactory();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}