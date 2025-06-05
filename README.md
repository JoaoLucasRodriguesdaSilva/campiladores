# MiniJava Parser usando JavaCC

Este repositório contém um arquivo de especificação de gramática (`MiniJava.jj`) para a linguagem MiniJava, projetado para ser usado com a ferramenta JavaCC. Este guia descreve passo a passo como configurar o ambiente, gerar um analisador sintático a partir da gramática e usá-lo para verificar a sintaxe de arquivos de código MiniJava.

## Visão Geral

O objetivo é usar o JavaCC para gerar um parser que possa ler um arquivo de código-fonte MiniJava (como `Factorial.java` ou `QuickSort.java`) e determinar se sua estrutura está sintaticamente correta de acordo com as regras definidas em `MiniJava.jj`. Se a sintaxe estiver correta, o parser indicará sucesso; caso contrário, apontará um erro léxico ou sintático.

## Pré-requisitos

Antes de começar, você precisará ter o seguinte instalado em seu sistema (Ubuntu):

1.  **Java Development Kit (JDK):** Versão 8 ou superior é recomendada. O JavaCC gera código Java, e você precisará do JDK para compilá-lo e executá-lo.
2.  **Ferramenta JavaCC:** O gerador de parser em si.
3.  **Código Fonte:** O arquivo `MiniJava.jj` (e arquivos de teste como `Factorial.java`, `QuickSort.java`).

## Passo a Passo

Siga estas etapas para configurar seu ambiente e executar o parser:

### Passo 1: Instalar o Java Development Kit (JDK)

1.  **Verifique se o JDK já está instalado:** Abra o terminal (Ctrl+Alt+T) e execute:
    ```bash
    java -version
    javac -version
    ```
    Se ambos os comandos mostrarem uma versão (por exemplo, 1.8.0 para Java 8, ou 11.0.x, 17.0.x, etc.), você pode pular para o Passo 2.

2.  **Instale o JDK (se necessário):** Se o JDK não estiver instalado, use o `apt` para instalar uma versão. Para instalar o OpenJDK 8 (compatível com o ambiente original mencionado):
    ```bash
    sudo apt update
    sudo apt install openjdk-8-jdk -y
    ```
    *Nota: Você pode instalar versões mais recentes como `openjdk-11-jdk` ou `openjdk-17-jdk` se preferir, mas certifique-se de que sejam compatíveis com o código gerado.*

3.  **Verifique a instalação novamente:** Após a instalação, execute `java -version` e `javac -version` para confirmar.

### Passo 2: Instalar o JavaCC

1.  **Instale pelo terminal:**

    ```bash
    sudo apt install javacc
    ```

2.  **Verifique a instalação do JavaCC:**
    ```bash
    javacc
    ```
    Você deve ver a mensagem de ajuda do JavaCC, indicando que ele foi instalado e configurado corretamente no PATH.

### Passo 3: Obter o Código Fonte

1.  **Clone o repositório:** Se o código estiver em um repositório Git:
    ```bash
    git clone <URL_DO_REPOSITORIO>
    cd <NOME_DO_DIRETORIO_CLONADO>
    ```
2.  **Ou crie os arquivos:** Se você recebeu os arquivos separadamente:
    *   Crie um diretório para o projeto: `mkdir minijava_parser && cd minijava_parser`
    *   Salve o código da gramática fornecido em um arquivo chamado `MiniJava.jj` dentro deste diretório.
    *   Salve os códigos de teste (como `Factorial.java`, `QuickSort.java`) no mesmo diretório.

### Passo 4: Gerar o Código do Parser

1.  **Navegue até o diretório:** Certifique-se de que você está no diretório `grammar/main`.
2.  **Execute o JavaCC:** Use o comando `javacc` para processar o arquivo de gramática:
    ```bash
    javacc MiniJava.jj
    ```
    *   **Saída Esperada:** O JavaCC lerá `MiniJava.jj` e gerará vários arquivos `.java` no mesmo diretório (como `MiniJavaParser.java`, `MiniJavaParserTokenManager.java`, `Token.java`, `ParseException.java`, etc.).

### Passo 5: Compilar o Código Java Gerado

1.  **Use o compilador Java (javac):** Volte para o diretório `grammar` e compile todos os arquivos `.java`:
    ```bash
    javac *.java
    ```
    *   **Saída Esperada:** Isso criará arquivos `.class` correspondentes para cada arquivo `.java`. Se houver erros de compilação Java aqui, verifique se o JDK está instalado corretamente e se não houve problemas na geração dos arquivos pelo JavaCC.

### Passo 6: Executar o Parser

1.  **Execute a classe principal do parser:** Agora você pode executar o parser compilado, passando o nome de um arquivo MiniJava como argumento.
    *   **Para testar `Factorial.java`:**
        ```bash
        java Main Factorial.java
        ```
    *   **Para testar `QuickSort.java`:**
        ```bash
        java Main QuickSort.java
        ```
    *   **Para testar `LinkedList.java`:**
        ```bash
        java Main LinkedList.java
        ```

2.  **Verifique a Saída:**
    *   **Se a sintaxe do arquivo de teste estiver correta** de acordo com a gramática `MiniJava.jj`, você vai ver a seguinte saída:
        ```
        Analisador MiniJava iniciado.
        Lendo do arquivo: Factorial.java

        Análise sintática concluída com sucesso!
        ```
    *   **Se houver um erro de sintaxe** no arquivo de teste (ou se a gramática `MiniJava.jj` não cobrir alguma estrutura usada no teste), você verá uma mensagem:
        ```
        Erro de Sintaxe:
        Encountered "..." at line X, column Y.
        Was expecting:
            ...
        ```
    *   **Se houver um erro léxico** (um caractere ou sequência inválida no arquivo de teste), você verá:
        ```
        Erro Léxico:
        Lexical error at line A, column B. Encountered: ...
        ```

3.  **Testando com Entrada Padrão (Opcional):**
    *   Você também pode executar o parser sem argumentos para digitar o código MiniJava diretamente no terminal:
        ```bash
        java Main
        ```
    *   Digite ou cole o código MiniJava.
    *   Quando terminar, sinalize o fim da entrada (EOF): Pressione `Ctrl+D` em uma linha vazia. O parser tentará analisar o que você digitou.

---

# Entendendo o Analisador MiniJava com JavaCC

Imagine que você tem uma nova linguagem de programação chamada "MiniJava". Computadores não entendem MiniJava (ou qualquer linguagem de programação) sozinhos. Você precisa ensiná-los. O objetivo aqui é criar um programa que funcione como um **verificador de gramática** para MiniJava. Ele não vai *executar* o código MiniJava, mas vai ler um programa MiniJava e dizer: "Sim, este código segue as regras de estrutura do MiniJava" ou "Opa, tem algo errado aqui nesta linha".

Pense nisso como verificar se um texto em português está gramaticalmente correto, mesmo sem entender completamente o *significado* do texto.

## Passo 1: O Livro de Regras (`MiniJava.jj`)

Este é o arquivo mais importante. Pense nele como um **livro de regras completo** para a linguagem MiniJava, escrito de uma forma que uma ferramenta especial (o JavaCC) possa entender. Ele tem algumas partes principais:

1.  **O Dicionário (Tokens):**
    *   Nas seções `TOKEN` e `SKIP`, você define todas as "palavras" e "símbolos" válidos da linguagem.
    *   `TOKEN`: Lista as palavras-chave (`class`, `if`, `while`, `int`), os símbolos (`{`, `}`, `(`, `)`, `=`, `+`, `;`), e como reconhecer nomes (`IDENTIFIER`) e números (`INTEGER_LITERAL`). É como dizer: "Se você vir os caracteres 'c', 'l', 'a', 's', 's' juntos, reconheça isso como a palavra-chave `CLASS`".
    *   `SKIP`: Lista coisas que devem ser ignoradas, como espaços, tabulações e comentários (`// ...` ou `/* ... */`). Eles ajudam os humanos a ler, mas não fazem parte da estrutura lógica que o computador precisa verificar.
    *   *Por que isso?* O computador precisa primeiro quebrar o código fonte (um monte de texto) em pedaços significativos (os tokens), como nós quebramos frases em palavras.

2.  **As Regras de Gramática (Produções BNF):**
    *   Esta é a maior parte do arquivo, com seções como `void Program() : {} { ... }`, `void Statement() : {} { ... }`, etc.
    *   Cada uma dessas seções define uma **regra de estrutura**. É como as regras de gramática do português que dizem como formar frases válidas.
    *   `void Program() : {} { MainClass() ( ClassDeclaration() )* <EOF> }` significa: "Um programa MiniJava válido *deve* ter uma `MainClass`, seguida por zero ou mais `ClassDeclaration`s, e terminar no fim do arquivo (`<EOF>`)".
    *   `void Statement() : {} { ... | <IF> <LPAREN> Expression() <RPAREN> Statement() <ELSE> Statement() | ... }` significa: "Um `Statement` (comando) pode ser, entre outras coisas, a palavra `if`, seguida por `(`, uma `Expression` (algo que calcula um valor), `)`, outro `Statement`, a palavra `else`, e mais um `Statement`".
    *   *Por que isso?* Isso ensina ao computador a ordem correta em que as "palavras" (tokens) devem aparecer para formar estruturas válidas como classes, métodos, comandos `if`, laços `while`, etc.

3.  **"Espiadinhas" (Lookahead):**
    *   Às vezes, o computador fica em dúvida. Por exemplo, se ele lê um nome como `minhaVariavel`, ele poderia ser o início de uma declaração (`minhaVariavel OutraClasse;`) ou de uma atribuição (`minhaVariavel = 5;`).
    *   O `LOOKAHEAD(...)` é uma instrução para o computador: "Quando estiver em dúvida, dê uma espiada nos próximos tokens para decidir qual regra seguir". `LOOKAHEAD(2)` significa "espie 2 tokens à frente". `LOOKAHEAD( Type() <IDENTIFIER> <SEMICOLON> )` significa "espie se os próximos tokens formam um Tipo, um Nome e um Ponto-e-vírgula".
    *   *Por que isso?* Ajuda o computador a tomar a decisão correta quando a gramática tem pontos que parecem ambíguos olhando apenas para o próximo token.

4.  **O Programa Principal (Código Java em `PARSER_BEGIN/END`):**
    *   Dentro de `PARSER_BEGIN(MiniJavaParser)` e `PARSER_END(MiniJavaParser)`, há código Java normal. A parte mais importante é o método `main`.
    *   Este `main` é o ponto de partida do seu verificador. Ele configura as coisas para ler um arquivo MiniJava (ou a entrada do teclado), inicia o processo de verificação chamando a regra principal (`parser.Program()`), e lida com a impressão de mensagens de sucesso ou erro.
    *   *Por que isso?* O JavaCC gera as *partes* do verificador, mas você precisa desse código `main` para realmente iniciar o processo e interagir com o usuário/sistema.

## Passo 2: A Ferramenta Mágica (`javacc`)

Você não executa o arquivo `.jj` diretamente. Você usa a ferramenta `javacc`:

```bash
javacc MiniJava.jj
```

*   Pense no `javacc` como uma **fábrica especializada**. Você entrega a ele o seu livro de regras (`MiniJava.jj`).
*   A fábrica lê as regras (tokens e gramática) e **constrói automaticamente** o código Java de um programa verificador (o analisador léxico e sintático).
*   O resultado são vários arquivos `.java` (como `MiniJavaParser.java`, `MiniJavaParserTokenManager.java`, etc.). *Você não escreveu esses arquivos `.java` diretamente*, o `javacc` os gerou para você com base nas regras do `.jj`.

## Passo 3: Montando o Verificador (`javac`)

Os arquivos `.java` gerados pelo `javacc` são código fonte Java. Para que o computador possa executá-los, eles precisam ser compilados em bytecode Java (`.class` files).

```bash
javac *.java
```

*   O comando `javac` é o **compilador Java padrão**. Ele pega todos os arquivos `.java` gerados e os transforma em arquivos `.class` que a Máquina Virtual Java (JVM) pode entender e executar.
*   Pense nisso como **montar as peças** da máquina verificadora que a fábrica (`javacc`) produziu.

## Passo 4: Usando o Verificador (`java`)

Agora que o verificador está montado (compilado), você pode usá-lo para checar um arquivo MiniJava:

```bash
java MiniJavaParser QuickSort.java
```

*   O comando `java` inicia a **Máquina Virtual Java (JVM)**.
*   `MiniJavaParser` diz à JVM para começar executando o método `main` dentro da classe `MiniJavaParser` (que você definiu no bloco `PARSER_BEGIN/END` e que foi compilado no passo anterior).
*   `QuickSort.java` é o **arquivo de entrada** que você quer verificar.
*   O que acontece internamente:
    1.  O código no `main` abre o arquivo `QuickSort.java`.
    2.  O **Analisador Léxico** (gerado pelo `javacc`, agora parte do `MiniJavaParser`) lê `QuickSort.java` e o quebra em tokens (ignorando espaços/comentários), baseado nas regras `TOKEN`/`SKIP`.
    3.  O **Analisador Sintático** (também gerado pelo `javacc`) recebe esses tokens e verifica se eles seguem a ordem definida pelas regras gramaticais (começando por `Program()`, depois `MainClass()`, etc.), usando os `LOOKAHEAD`s quando necessário.

## Passo 5: Entendendo o Resultado

1.  **`Análise sintática concluída com sucesso!`**
    *   **O que significa:** O verificador conseguiu ler todo o arquivo `QuickSort.java` do início ao fim, e a sequência de tokens encontrada correspondeu perfeitamente às regras de gramática definidas em `MiniJava.jj`. A *estrutura* do código está correta segundo as regras que você deu.
    *   **O que NÃO significa:** Não significa que o programa `QuickSort.java` funciona corretamente, que não tem erros lógicos, ou que fará o que você espera. Significa apenas que ele está "escrito corretamente" de acordo com a gramática MiniJava.

2.  **`Erro de Sintaxe: ...`**
    *   **O que significa:** O verificador encontrou um ponto onde a sequência de tokens viola as regras gramaticais. Por exemplo, um ponto e vírgula faltando, uma chave no lugar errado, um `else` sem um `if` correspondente, etc. A mensagem geralmente indica a linha/coluna e o que ele encontrou versus o que ele esperava encontrar com base nas regras.

3.  **`Erro Léxico: ...`**
    *   **O que significa:** O analisador léxico encontrou um caractere ou sequência de caracteres que não corresponde a nenhum `TOKEN` definido e não está em `SKIP`. Por exemplo, um símbolo inválido como `#` ou `@` no meio do código.

## Resumo da Jornada

1.  **Você define as regras:** Cria o `MiniJava.jj` com o dicionário (tokens) e a gramática (regras de estrutura) do MiniJava.
2.  **JavaCC constrói o verificador:** Executa `javacc MiniJava.jj` para gerar o código Java do analisador.
3.  **Você monta o verificador:** Executa `javac *.java` para compilar o código gerado.
4.  **Você usa o verificador:** Executa `java MiniJavaParser SeuCodigo.java` para checar se `SeuCodigo.java` segue as regras.

O resultado final é um programa capaz de validar automaticamente se qualquer código MiniJava está estruturalmente correto, o primeiro passo fundamental na construção de um compilador ou interpretador completo!