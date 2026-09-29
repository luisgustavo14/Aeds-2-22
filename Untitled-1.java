LISTA 1: Classes, herança e polimorfismo (Proj0a)

O que foi feito: uma classe abstrata Produto com duas filhas, ProdutoNaoPerecivel e ProdutoPerecivel. Cada filha calcula valorVenda() do seu jeito.

java
public abstract class Produto {
    private static final double MARGEM_PADRAO = 0.2;
    private String descricao;
    protected double precoCusto, margemLucro;   // protected: filhas enxergam

    private void init(String d, double pc, double m) {
        if (d == null || d.length() < 3 || pc <= 0 || m <= 0)
            throw new IllegalArgumentException("Dados inválidos");
        descricao = d; precoCusto = pc; margemLucro = m;
    }
    protected Produto(String d, double pc, double m) { init(d, pc, m); }
    protected Produto(String d, double pc) { init(d, pc, MARGEM_PADRAO); }

    public abstract double valorVenda();          // cada filha implementa

    @Override public String toString() {
        return String.format("%s: R$ %.2f", descricao, valorVenda());
    }
}

public class ProdutoPerecivel extends Produto {
    private static final double DESCONTO = 0.25;
    private static final int PRAZO_DESCONTO = 7;
    private LocalDate dataDeValidade;

    public ProdutoPerecivel(String d, double pc, double m, LocalDate v) {
        super(d, pc, m);
        if (v.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Validade vencida");
        dataDeValidade = v;
    }
    @Override public double valorVenda() {
        LocalDate hoje = LocalDate.now();
        if (dataDeValidade.isBefore(hoje))
            throw new IllegalStateException("Fora da validade");
        double v = precoCusto * (1 + margemLucro);
        if (ChronoUnit.DAYS.between(hoje, dataDeValidade) <= PRAZO_DESCONTO)
            v *= (1 - DESCONTO);
        return v;
    }
}
// ProdutoNaoPerecivel: super(...) nos construtores + valorVenda() { return precoCusto * (1 + margemLucro); }

Essenciais e como reutilizar:

Peça	Função	Outras situações que ela pode pedir
abstract class + método abstrato	Define o contrato; força as filhas a implementar	Novo tipo de produto (ex.: ProdutoImportado com imposto), Funcionario → Horista/Mensalista, Forma → Circulo/Retangulo
extends + super(...)	Herda atributos e reaproveita o construtor da mãe	Qualquer filha nova; super sempre na 1ª linha
@Override	Sobrescreve o comportamento da mãe	toString, valorVenda, equals, gerarDadosTexto
Construtor sobrecarregado + init privado	Evita repetir validação	Construtor com/sem parâmetro opcional (margem padrão)
throw new IllegalArgumentException no construtor	Impede objeto inválido	Qualquer regra ("não pode data anterior a hoje", "preço > 0")
LocalDate.now(), isBefore, isAfter, ChronoUnit.DAYS.between	Datas e prazos	Vencimento, desconto por prazo, multa por atraso
constante static final	Evita "número mágico"	Desconto, prazo, taxa
Polimorfismo: Produto p = new ProdutoPerecivel(...); p.valorVenda();	Variável da mãe, comportamento da filha	Vetor Produto[] misto e um for chamando valorVenda()

Mapa mental:

                    PRODUTO (abstract)
        ┌───────────────┼────────────────┐
   ATRIBUTOS       CONSTRUTORES      MÉTODOS
 private desc      2 sobrecargas     abstract valorVenda()
 protected custo   → init() valida   toString()
 protected margem  → MARGEM_PADRAO
        └───────────────┬────────────────┘
                    extends + super()
             ┌──────────┴───────────┐
   NaoPerecivel                 Perecivel
   custo*(1+margem)      + LocalDate validade
                         + valida no construtor
                         + desconto 25% se ≤ 7 dias
                         + erro se vencido
LISTA 2: Arquivos-texto (Proj0b)

Formato do arquivo: linha 1 é N; depois tipo;descrição;custo;margem;[validade] (tipo 1 = não perecível, 2 = perecível; data dd/mm/aaaa).

Ideia central: objeto → texto (gerarDadosTexto) e texto → objeto (criarDoTexto), mais ler e gravar o arquivo.

java
// equals sem sensibilidade a caixa (em Produto)
@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Produto)) return false;
    return descricao.equalsIgnoreCase(((Produto) o).descricao);
}

// gerarDadosTexto (Perecivel)
@Override public String gerarDadosTexto() {
    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    return String.format(Locale.US, "2;%s;%.2f;%.2f;%s",
            descricao, precoCusto, margemLucro, dataDeValidade.format(f));
}   // atenção: descricao é private na mãe → use getter ou torne protected

// criarDoTexto (static na Produto): split + tipo + instancia
static Produto criarDoTexto(String linha) {
    String[] p = linha.split(";");
    int tipo = Integer.parseInt(p[0]);
    String desc = p[1];
    double custo = Double.parseDouble(p[2]);
    double margem = Double.parseDouble(p[3]);
    if (tipo == 1) return new ProdutoNaoPerecivel(desc, custo, margem);
    LocalDate val = LocalDate.parse(p[4], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    return new ProdutoPerecivel(desc, custo, margem, val);
}

// lerProdutos
static Produto[] lerProdutos(String nome) {
    try (Scanner arq = new Scanner(new File(nome), "UTF-8")) {
        int n = Integer.parseInt(arq.nextLine().trim());
        Produto[] v = new Produto[n];
        for (int i = 0; i < n; i++) v[i] = Produto.criarDoTexto(arq.nextLine());
        return v;
    } catch (Exception e) { return new Produto[0]; }   // vazio se der problema
}

// salvarProdutos (sobrescreve)
static void salvarProdutos(String nome) {
    try (PrintWriter pw = new PrintWriter(nome, "UTF-8")) {
        pw.println(qtdProdutos);
        for (int i = 0; i < qtdProdutos; i++) pw.println(produtos[i].gerarDadosTexto());
    } catch (Exception e) { System.out.println("Erro ao salvar"); }
}

// localizar (busca linear, ignora caixa)
for (Produto p : produtos)
    if (p.toString().toLowerCase().contains(busca.toLowerCase())) { ... }

// cadastrar: vetor cheio → cresce
produtos = Arrays.copyOf(produtos, produtos.length + 1);
produtos[produtos.length - 1] = novo;

Essenciais e como reutilizar:

Peça	Função	Outras situações
split(";")	Quebra linha em campos	Qualquer CSV: alunos, livros, funcionários (troque o separador)
Integer/Double.parseInt/parseDouble	Texto → número	Todo campo numérico lido de arquivo
LocalDate.parse e DateTimeFormatter	Texto ↔ data	Datas em qualquer formato
String.format("%.2f") (com Locale.US)	Número → texto formatado	Gerar linha de saída; cuidado com vírgula decimal
Scanner(new File(...)) + nextLine()	Ler arquivo linha a linha	Ler N e depois N registros; hasNextLine() se não houver N
PrintWriter + println	Gravar arquivo	Salvar qualquer coleção
try-with-resources + catch	Fecha o arquivo e trata erro	Todo acesso a arquivo
Decisão por tipo (if/switch no tipo)	Fábrica de objetos	Novo tipo 3 → mais um caso
equalsIgnoreCase / toLowerCase	Comparar sem caixa	Buscas, equals
Arrays.copyOf	Aumentar vetor	Cadastrar em vetor cheio
Menu com do-while + switch	Programa principal	Listar, cadastrar, buscar, salvar

Mapa mental:

                 ARQUIVO-TEXTO
        ┌───────────┴────────────┐
      LER                     GRAVAR
  Scanner(File)            PrintWriter
  linha 1 → N              println(N)
  N vezes nextLine()       N vezes println(gerarDadosTexto())
       │                        │
  criarDoTexto()          gerarDadosTexto()
  split(";")              String.format
  parse Int/Double/Date   (abstract; cada filha faz o seu)
  tipo 1 ou 2 → new ...
        └───────────┬────────────┘
        MENU: listar | cadastrar | localizar | salvar
        (vetor Produto[] + Arrays.copyOf + equalsIgnoreCase)
LISTA 3: Recursividade

Receita única: (1) caso base (quando para); (2) passo recursivo (resolve 1 item + chama a si mesmo com problema menor). Em vetores, o índice vira parâmetro.

java
// 1) soma dos pares até o limite
static int somaPares(int n) {
    if (n < 2) return 0;                    // base
    if (n % 2 != 0) return somaPares(n - 1);
    return n + somaPares(n - 2);
}
// 2) soma de vetor double
static double soma(double[] v, int i) {
    if (i == v.length) return 0;            // base: passou do fim
    return v[i] + soma(v, i + 1);
}
// 3) contar repetições de x
static int conta(int[] v, int i, int x) {
    if (i == v.length) return 0;
    return (v[i] == x ? 1 : 0) + conta(v, i + 1, x);
}
// chamada: soma(vetor, 0)   conta(vetor, 0, x)

Menu principal:

java
Scanner sc = new Scanner(System.in);
int op;
do {
    System.out.println("1-Soma pares  2-Soma vetor  3-Contar repetições  0-Sair");
    op = sc.nextInt();
    switch (op) {
        case 1 -> { System.out.print("Limite: "); System.out.println(somaPares(sc.nextInt())); }
        case 2 -> { /* lê tamanho, preenche double[], chama soma(v, 0) */ }
        case 3 -> { /* lê tamanho, preenche int[], lê x, chama conta(v, 0, x) */ }
    }
} while (op != 0);

Como reutilizar (mesmo molde, muda só a conta):

Pedido possível	Base	Passo
Maior/menor do vetor	i == length-1 → v[i]	Math.max(v[i], maior(v, i+1))
Média	soma recursiva / length	reaproveita a soma
Produto / fatorial	n <= 1 → 1	n * fat(n-1)
Contar pares/ímpares	i == length → 0	(v[i]%2==0 ? 1:0) + ...
Existe o valor?	i == length → false	v[i]==x || existe(v,i+1,x)
Fibonacci	n <= 1 → n	fib(n-1) + fib(n-2)
Potência	exp == 0 → 1	base * pot(base, exp-1)
Soma dos dígitos	n == 0 → 0	n%10 + soma(n/10)
Inverter string / palíndromo	i >= j → true	compara extremos, i+1, j-1

Mapa mental:

        RECURSIVIDADE
   ┌────────┴─────────┐
 CASO BASE        PASSO RECURSIVO
 (para aqui)      (1 item + chamada menor)
   │                  │
 n<2 / i==length   n-2 / i+1
        │
  VETOR → parâmetro i (começa em 0)
  MENU → do-while + switch + Scanner
LISTA 4: Contagem de operações e tempo (Proj1a)

O enunciado não traz o código dos 4 algoritmos (vêm do repositório e da explicação em aula), então aqui vai só a técnica, que é o que se repete.

java
static long operacoes = 0;                  // contador global

static void algoritmo(int[] v) {
    for (int i = 0; i < v.length; i++) {
        operacoes++;                        // conta a operação relevante (comparação, troca, soma...)
        ...
    }
}

operacoes = 0;
long ini = System.nanoTime();
algoritmo(vetor);
long fim = System.nanoTime();
double ms = (fim - ini) / 1_000_000.0;
System.out.println(n + ";" + operacoes + ";" + ms);   // vira tabela p/ planilha

O que reutilizar:

Contador: incremente dentro do laço, na operação dominante (a que se repete mais).
System.nanoTime() antes e depois; ms = (fim − início) / 1.000.000.
Gerador de vetores com tamanhos crescentes (Pequeno, Médio, Grande). Rode cada tamanho e anote operações e tempo.
Comparar com a teoria (dobrando n):
Complexidade	Operações ao dobrar n
O(log n)	+1 passo
O(n)	×2
O(n log n)	pouco mais que ×2
O(n²)	×4
O(2ⁿ) / O(n!)	explode (por isso o algoritmo 4 usa n pequeno, provavelmente)
Operações são exatas e repetíveis; o tempo oscila (máquina, JVM). Por isso os dois gráficos podem diferir um pouco, e é isso que a Tarefa 2 pede para comentar.

Mapa mental:

   EFICIÊNCIA
 ┌─────┴──────┐
CONTAR       MEDIR
operacoes++  nanoTime() fim-ini
 │            │
 └──► gerador de vetores (P / M / G)
        │
   planilha → gráficos (n × ops, n × tempo)
        │
   compara com O(n), O(n²), O(log n)...
Cola universal para a prova
Classe nova com tipos diferentes → abstract + extends + super + @Override.
Ler de arquivo → Scanner(File) → nextLine → split(";") → parse → new.
Gravar em arquivo → PrintWriter → println(objeto.gerarDadosTexto()).
Buscar → for + equalsIgnoreCase/contains.
Recursão → escreva primeiro o caso base, depois o passo com i+1 ou n-1.
Validação → throw new IllegalArgumentException no construtor.
Menu → do { ... } while (op != 0) com switch.
Medição → contador ++ + nanoTime().

Se quiser, mande o código dos 4 algoritmos do Proj1a ou o conteúdo do App.java do zip (cole aqui) e eu adapto os exemplos ao que a professora fez. Se o terminal voltar, também gero o arquivo DOCX ou markdown.