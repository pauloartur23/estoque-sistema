-- ============================================================
-- Sistema de Gerenciamento de Estoque - Schema Completo
-- ============================================================

CREATE DATABASE IF NOT EXISTS estoque_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE estoque_db;

-- ============================================================
-- CADASTROS BÁSICOS
-- ============================================================

CREATE TABLE IF NOT EXISTS categoria (
                                         id          INT AUTO_INCREMENT PRIMARY KEY,
                                         nome        VARCHAR(80)  NOT NULL UNIQUE,
    descricao   VARCHAR(255),
    ativo       TINYINT(1)   NOT NULL DEFAULT 1
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS marca (
                                     id          INT AUTO_INCREMENT PRIMARY KEY,
                                     nome        VARCHAR(80)  NOT NULL UNIQUE,
    ativo       TINYINT(1)   NOT NULL DEFAULT 1
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fornecedor (
                                          id          INT AUTO_INCREMENT PRIMARY KEY,
                                          nome        VARCHAR(150) NOT NULL,
    documento   VARCHAR(20),
    telefone    VARCHAR(20),
    email       VARCHAR(120),
    endereco    VARCHAR(255),
    ativo       TINYINT(1)   NOT NULL DEFAULT 1
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
                                       id          INT AUTO_INCREMENT PRIMARY KEY,
                                       nome        VARCHAR(150) NOT NULL,
    documento   VARCHAR(20),
    telefone    VARCHAR(20),
    email       VARCHAR(120),
    endereco    VARCHAR(255),
    ativo       TINYINT(1)   NOT NULL DEFAULT 1
    ) ENGINE=InnoDB;

-- ============================================================
-- USUÁRIOS E PERMISSÕES
-- ============================================================

CREATE TABLE IF NOT EXISTS usuario (
                                       id           INT AUTO_INCREMENT PRIMARY KEY,
                                       nome         VARCHAR(120) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    senha_hash   VARCHAR(255) NOT NULL,
    perfil       ENUM('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA') NOT NULL DEFAULT 'VENDEDOR',
    ativo        TINYINT(1)   NOT NULL DEFAULT 1,
    data_criacao TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- ============================================================
-- PRODUTO (atualizado: referencia categoria, marca e fornecedor)
-- ============================================================

CREATE TABLE IF NOT EXISTS produto (
                                       id                INT AUTO_INCREMENT PRIMARY KEY,
                                       codigo            VARCHAR(30)     NOT NULL UNIQUE,
    nome              VARCHAR(120)    NOT NULL,
    descricao         VARCHAR(255),
    categoria_id      INT,
    marca_id          INT,
    fornecedor_id     INT,
    preco             DECIMAL(10,2)   NOT NULL,
    custo             DECIMAL(10,2)   DEFAULT 0,
    quantidade        INT             NOT NULL DEFAULT 0,
    quantidade_minima INT             NOT NULL DEFAULT 0,
    imagem_path       VARCHAR(500)    NULL,
    data_cadastro     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    ativo             TINYINT(1)      NOT NULL DEFAULT 1,
    CONSTRAINT fk_produto_categoria  FOREIGN KEY (categoria_id)  REFERENCES categoria(id),
    CONSTRAINT fk_produto_marca      FOREIGN KEY (marca_id)      REFERENCES marca(id),
    CONSTRAINT fk_produto_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id)
    ) ENGINE=InnoDB;

-- ============================================================
-- VENDAS
-- ============================================================

CREATE TABLE IF NOT EXISTS venda (
                                     id               INT AUTO_INCREMENT PRIMARY KEY,
                                     cliente_id       INT             NULL,
                                     data_venda       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
                                     valor_total      DECIMAL(10,2)   NOT NULL,
    desconto         DECIMAL(10,2)   NOT NULL DEFAULT 0,
    forma_pagamento  VARCHAR(30)     NOT NULL,
    nome_cliente     VARCHAR(120)    DEFAULT 'Consumidor Final',
    observacoes      VARCHAR(255),
    CONSTRAINT fk_venda_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
    ) ENGINE=InnoDB;

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

-- ============================================================
-- ENTRADA DE PRODUTOS (compras de fornecedores)
-- ============================================================

CREATE TABLE IF NOT EXISTS entrada_estoque (
                                               id              INT AUTO_INCREMENT PRIMARY KEY,
                                               fornecedor_id   INT             NULL,
                                               data_entrada    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
                                               numero_nota     VARCHAR(50),
    observacoes     VARCHAR(255),
    valor_total     DECIMAL(10,2)   NOT NULL DEFAULT 0,
    CONSTRAINT fk_entrada_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS entrada_item (
                                            id              INT AUTO_INCREMENT PRIMARY KEY,
                                            entrada_id      INT             NOT NULL,
                                            produto_id      INT             NOT NULL,
                                            quantidade      INT             NOT NULL,
                                            custo_unitario  DECIMAL(10,2)   NOT NULL,
    subtotal        DECIMAL(10,2)   NOT NULL,
    CONSTRAINT fk_entrada_item_entrada FOREIGN KEY (entrada_id) REFERENCES entrada_estoque(id) ON DELETE CASCADE,
    CONSTRAINT fk_entrada_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
    ) ENGINE=InnoDB;

-- ============================================================
-- SAÍDA DE PRODUTOS (avarias, perdas, uso interno - não-venda)
-- ============================================================

CREATE TABLE IF NOT EXISTS saida_estoque (
                                             id              INT AUTO_INCREMENT PRIMARY KEY,
                                             tipo_saida      VARCHAR(30)     NOT NULL,   -- Ex: 'Avaria', 'Perda', 'Uso Interno', 'Devolução ao Fornecedor'
    cliente_id      INT             NULL,
    data_saida      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    observacoes     VARCHAR(255),
    CONSTRAINT fk_saida_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS saida_item (
                                          id              INT AUTO_INCREMENT PRIMARY KEY,
                                          saida_id        INT             NOT NULL,
                                          produto_id      INT             NOT NULL,
                                          quantidade      INT             NOT NULL,
                                          motivo          VARCHAR(100),
    CONSTRAINT fk_saida_item_saida   FOREIGN KEY (saida_id)   REFERENCES saida_estoque(id) ON DELETE CASCADE,
    CONSTRAINT fk_saida_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
    ) ENGINE=InnoDB;

-- ============================================================
-- PEDIDOS (pedidos de compra a fornecedores, ainda não recebidos)
-- ============================================================

CREATE TABLE IF NOT EXISTS pedido (
                                      id              INT AUTO_INCREMENT PRIMARY KEY,
                                      fornecedor_id   INT             NOT NULL,
                                      data_pedido     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
                                      status          ENUM('PENDENTE', 'APROVADO', 'RECEBIDO', 'CANCELADO') NOT NULL DEFAULT 'PENDENTE',
    valor_total     DECIMAL(10,2)   NOT NULL DEFAULT 0,
    observacoes     VARCHAR(255),
    CONSTRAINT fk_pedido_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido_item (
                                           id              INT AUTO_INCREMENT PRIMARY KEY,
                                           pedido_id       INT             NOT NULL,
                                           produto_id      INT             NOT NULL,
                                           quantidade      INT             NOT NULL,
                                           preco_unitario  DECIMAL(10,2)   NOT NULL,
    subtotal        DECIMAL(10,2)   NOT NULL,
    CONSTRAINT fk_pedido_item_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
    ) ENGINE=InnoDB;

-- ============================================================
-- TRANSFERÊNCIAS (entre locais/depósitos de estoque)
-- ============================================================

CREATE TABLE IF NOT EXISTS local_estoque (
                                             id      INT AUTO_INCREMENT PRIMARY KEY,
                                             nome    VARCHAR(100) NOT NULL UNIQUE,
    ativo   TINYINT(1)   NOT NULL DEFAULT 1
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transferencia (
                                             id              INT AUTO_INCREMENT PRIMARY KEY,
                                             produto_id      INT             NOT NULL,
                                             origem_id       INT             NOT NULL,
                                             destino_id      INT             NOT NULL,
                                             quantidade      INT             NOT NULL,
                                             data_transferencia TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                             observacoes     VARCHAR(255),
    CONSTRAINT fk_transferencia_produto FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT fk_transferencia_origem  FOREIGN KEY (origem_id)  REFERENCES local_estoque(id),
    CONSTRAINT fk_transferencia_destino FOREIGN KEY (destino_id) REFERENCES local_estoque(id)
    ) ENGINE=InnoDB;

-- ============================================================
-- LOG UNIFICADO DE MOVIMENTAÇÕES (alimenta o Dashboard)
-- ============================================================

CREATE TABLE IF NOT EXISTS movimentacao_estoque (
                                                    id              INT AUTO_INCREMENT PRIMARY KEY,
                                                    tipo            ENUM('ENTRADA', 'SAIDA', 'VENDA', 'TRANSFERENCIA') NOT NULL,
    produto_id      INT             NOT NULL,
    quantidade      INT             NOT NULL,
    descricao       VARCHAR(255),
    data_movimento  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    referencia_id   INT,   -- id da venda/entrada/saida/transferencia relacionada
    CONSTRAINT fk_mov_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
    ) ENGINE=InnoDB;

-- ============================================================
-- DADOS INICIAIS (seed)
-- ============================================================

INSERT INTO categoria (nome, descricao) VALUES
                                            ('Periféricos', 'Mouses, teclados, headsets'),
                                            ('Monitores', 'Monitores e telas'),
                                            ('Eletrônicos', 'Componentes e acessórios eletrônicos'),
                                            ('Limpeza', 'Produtos de limpeza'),
                                            ('Outros', 'Itens diversos');

INSERT INTO marca (nome) VALUES
                             ('Genérica'), ('Logitech'), ('Dell'), ('Samsung');

INSERT INTO fornecedor (nome, documento, telefone, email, endereco) VALUES
                                                                        ('Distribuidora Tech Nordeste', '12.345.678/0001-90', '(84) 3333-4444', 'contato@technordeste.com.br', 'Mossoró - RN'),
                                                                        ('Fornecedor Genérico Ltda', '98.765.432/0001-10', '(84) 3222-1111', 'vendas@fornecedorgenerico.com.br', 'Mossoró - RN');

INSERT INTO cliente (nome, documento, telefone, email) VALUES
    ('Consumidor Final', NULL, NULL, NULL);

INSERT INTO local_estoque (nome) VALUES
                                     ('Depósito Principal'), ('Loja');

INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES
    ('Administrador', 'admin@estoquefacil.com.br', 'admin123', 'ADMIN');

-- Produtos de exemplo (usando os IDs de categoria/marca/fornecedor criados acima)
INSERT INTO produto (codigo, nome, descricao, categoria_id, marca_id, fornecedor_id, preco, custo, quantidade, quantidade_minima) VALUES
                                                                                                                                      ('P0001', 'Mouse Gamer',      'Mouse óptico USB 6400 DPI', 1, 2, 1, 79.90,  35.00, 25, 5),
                                                                                                                                      ('P0002', 'Teclado Mecânico', 'Teclado ABNT2 RGB',         1, 2, 1, 199.90, 90.00, 15, 5),
                                                                                                                                      ('P0003', 'Monitor 24"',      'Monitor Full HD IPS',       2, 3, 2, 699.00, 420.00, 8, 2);