-- ============================================================
-- Sistema de Gerenciamento de Estoque - Schema do Banco de Dados
-- ============================================================

CREATE DATABASE IF NOT EXISTS estoque_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE estoque_db;

-- Tabela de produtos
CREATE TABLE IF NOT EXISTS produto (
                                       id                INT AUTO_INCREMENT PRIMARY KEY,
                                       codigo            VARCHAR(30)     NOT NULL UNIQUE,
    nome              VARCHAR(120)    NOT NULL,
    descricao         VARCHAR(255),
    categoria         VARCHAR(60),
    preco             DECIMAL(10,2)   NOT NULL,
    quantidade        INT             NOT NULL DEFAULT 0,
    quantidade_minima INT             NOT NULL DEFAULT 0,
    data_cadastro     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    ativo             TINYINT(1)      NOT NULL DEFAULT 1
    ) ENGINE=InnoDB;

-- Tabela de vendas (cabeçalho da nota fiscal)
CREATE TABLE IF NOT EXISTS venda (
                                     id               INT AUTO_INCREMENT PRIMARY KEY,
                                     data_venda       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
                                     valor_total      DECIMAL(10,2)   NOT NULL,
    forma_pagamento  VARCHAR(30)     NOT NULL,
    nome_cliente     VARCHAR(120)    DEFAULT 'Consumidor Final'
    ) ENGINE=InnoDB;

-- Tabela de itens da venda (corpo da nota fiscal)
CREATE TABLE IF NOT EXISTS item_venda (
                                          id               INT AUTO_INCREMENT PRIMARY KEY,
                                          venda_id         INT             NOT NULL,
                                          produto_id       INT             NOT NULL,
                                          quantidade       INT             NOT NULL,
                                          preco_unitario   DECIMAL(10,2)   NOT NULL,
    subtotal         DECIMAL(10,2)   NOT NULL,
    CONSTRAINT fk_item_venda   FOREIGN KEY (venda_id)   REFERENCES venda(id)   ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
    ) ENGINE=InnoDB;

-- Alguns produtos de exemplo (opcional)
INSERT INTO produto (codigo, nome, descricao, categoria, preco, quantidade, quantidade_minima) VALUES
                                                                                                   ('P0001', 'Mouse Gamer',      'Mouse óptico USB 6400 DPI', 'Periféricos', 79.90, 25, 5),
                                                                                                   ('P0002', 'Teclado Mecânico', 'Teclado ABNT2 RGB',         'Periféricos', 199.90, 15, 5),
                                                                                                   ('P0003', 'Monitor 24"',      'Monitor Full HD IPS',       'Monitores',   699.00, 8,  2);