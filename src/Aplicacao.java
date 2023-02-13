import dominio.PosicaoTabela;
import impl.CampeonatoBrasileiroImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Aplicacao {

    public static void main(String[] args) throws IOException {

        // obter caminho do arquivo
        Path file = Path.of("campeonato-brasileiro.csv");

        // obter a implementação: (ponto extra - abstrair para interface)
        CampeonatoBrasileiroImpl resultados =
                new CampeonatoBrasileiroImpl(file, (jogo) -> jogo.data().data().getYear() == 2020 || jogo.data().data().getYear() == 2021);

        // imprimir estatisticas
        imprimirEstatisticas(resultados);

        // imprimir tabela ordenada
        imprimirTabela(resultados.getTabela());

    }

    private static void imprimirEstatisticas(CampeonatoBrasileiroImpl brasileirao) {
        IntSummaryStatistics statistics = brasileirao.getEstatisticasPorJogo();

        System.out.println("Estatisticas (Total de gols) - " + statistics.getSum());
        System.out.println("Estatisticas (Total de jogos) - " + statistics.getCount());
        System.out.println("Estatisticas (Media de gols) - " + statistics.getAverage());

        brasileirao.getPlacarMaisMenosRepetido();

        Long jogosCom3OuMaisGols = brasileirao.getTotalJogosCom3OuMaisGols();
        Long jogosComMenosDe3Gols = brasileirao.getTotalJogosComMenosDe3Gols();

        System.out.println("Estatisticas (3 ou mais gols) - " + jogosCom3OuMaisGols);
        System.out.println("Estatisticas (-3 gols) - " + jogosComMenosDe3Gols);

        Long totalVitoriasEmCasa = brasileirao.getTotalVitoriasEmCasa();
        Long vitoriasForaDeCasa = brasileirao.getTotalVitoriasForaDeCasa();
        Long empates = brasileirao.getTotalEmpates();

        System.out.println("Estatisticas (Vitorias Fora de casa) - " + vitoriasForaDeCasa);
        System.out.println("Estatisticas (Vitorias Em casa) - " + totalVitoriasEmCasa);
        System.out.println("Estatisticas (Empates) - " + empates);
    }

    public static void imprimirTabela(Set<PosicaoTabela> posicoes) {
        System.out.println();
        System.out.println("## TABELA CAMPEONADO BRASILEIRO: ##");
        AtomicInteger colocacao = new AtomicInteger(1);
        posicoes
                .stream()
                .sorted(Comparator.comparingLong(PosicaoTabela::pontos).reversed())
                .forEach(posicao -> System.out.println(colocacao.getAndIncrement() +". " + posicao));
        System.out.println();
        System.out.println();
    }
}