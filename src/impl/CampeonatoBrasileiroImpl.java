package impl;

import dominio.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CampeonatoBrasileiroImpl {

    private List<Jogo> brasileirao;
    private List<Jogo> jogos;

    public CampeonatoBrasileiroImpl(Path arquivo, Predicate<Jogo> filtro) throws IOException {
        this.jogos = lerArquivo(arquivo);
        this.brasileirao = jogos.stream()
                .filter(filtro) // Filtrar por ano
                .collect(Collectors.toList());

    }
    public static List<Jogo> lerArquivo(Path file) throws IOException {
        List<Jogo> jogos = new ArrayList<>();
        Stream<String> stream = Files.lines(Paths.get(file.toString()));
        List<String> dados = stream.collect(Collectors.toList());
        stream.close();

        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 1; i < dados.size(); i++) {
            List<String> linha = new ArrayList<>(List.of(dados.get(i).split(";")));

            Integer rodada = Integer.valueOf(linha.get(0));
            LocalDate data = LocalDate.parse(linha.get(1), localDateFormatter);

            // 16h00 -> 16:00:00
            String horarioLocalTime = linha.get(2).replace('h',':').concat(":00");

            LocalTime horario = null;
            if (!horarioLocalTime.equals(":00")) {
                horario = LocalTime.parse(horarioLocalTime, DateTimeFormatter.ISO_TIME);
            }
            DayOfWeek dia = getDayOfWeek(linha.get(3));
            DataDoJogo datadoJogo = new DataDoJogo(data, horario, dia);

            Time mandante = new Time(linha.get(4));
            Time visitante = new Time(linha.get(5));
            Time vencedor = new Time(linha.get(6));
            String arena = linha.get(7);
            Integer mandantePlacar = Integer.valueOf(linha.get(8));
            Integer visitantePlacar = Integer.valueOf(linha.get(9));
            String estadoMandante = linha.get(10);
            String estadoVisitante = linha.get(11);
            String estadoVencedor = linha.get(12);

            Jogo jogo = new Jogo(
                    rodada,
                    datadoJogo,
                    mandante,
                    visitante,
                    vencedor,
                    arena,
                    mandantePlacar,
                    visitantePlacar,
                    estadoMandante,
                    estadoVisitante,
                    estadoVencedor
            );
            jogos.add(jogo);
        }
        return jogos;
    }

    public IntSummaryStatistics getEstatisticasPorJogo() {
        IntStream numGolsStream = this.brasileirao.stream().mapToInt(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar());
        IntSummaryStatistics estatisticas = numGolsStream.summaryStatistics();
        if (estatisticas.getCount() == 0) throw new RuntimeException();
        return estatisticas;
    }

    public Long getTotalVitoriasEmCasa() {
        List<Jogo> numGolsStream = this.brasileirao
                .stream()
                .filter(jogo -> Objects.equals(jogo.mandante(), jogo.vencedor()))
                .collect(Collectors.toList());
        return (long) numGolsStream.size();
    }

    public Long getTotalVitoriasForaDeCasa() {
        List<Jogo> numGolsStream = this.brasileirao
                .stream()
                .filter(jogo -> Objects.equals(jogo.visitante(), jogo.vencedor()))
                .collect(Collectors.toList());
        return (long) numGolsStream.size();
    }

    public Long getTotalEmpates() {
        List<Jogo> totalEmpatesStream = this.brasileirao
                .stream()
                .filter(jogo -> Objects.equals(jogo.mandantePlacar(), jogo.visitantePlacar()))
                .collect(Collectors.toList());
        return (long) totalEmpatesStream.size();
    }

    public Long getTotalJogosComMenosDe3Gols() {
        List<Jogo> totalJogosComMenosDe3Gols = this.brasileirao
                .stream()
                .filter(jogo -> (jogo.mandantePlacar() + jogo.visitantePlacar() < 3))
                .collect(Collectors.toList());
        return (long) totalJogosComMenosDe3Gols.size();
    }

    public Long getTotalJogosCom3OuMaisGols() {
        List<Jogo> totalJogosCom3OuMaisGols = this.brasileirao
                .stream()
                .filter(jogo -> (jogo.mandantePlacar() + jogo.visitantePlacar() >= 3))
                .collect(Collectors.toList());
        return (long) totalJogosCom3OuMaisGols.size();
    }

    public void getPlacarMaisMenosRepetido() {
        List<String> placares = this.brasileirao.stream().map(jogo -> jogo.mandantePlacar() + " x " + jogo.visitantePlacar()).collect(Collectors.toList());
        Set<String> placaresDistintos = new HashSet<>(placares);
        Iterator<String> placaresDistintosIterator = placaresDistintos.iterator();

        int maiorFreq = 0;
        String maiorPlacar = "";

        int menorFreq = 1;
        String menorPlacar = "";

        while (placaresDistintosIterator.hasNext()) {
            String placar = placaresDistintosIterator.next();
            int nPlacares = Collections.frequency(placares, placar);
            if (nPlacares > maiorFreq) {
                maiorFreq = nPlacares;
                maiorPlacar = placar;
            }
            if (nPlacares <= menorFreq) {
                menorFreq = nPlacares;
                menorPlacar = placar;
            }
        }

        System.out.println("Estatisticas (Placar mais repetido) - "
                + maiorPlacar + " (" + maiorFreq + " jogo(s))");
        System.out.println("Estatisticas (Placar menos repetido) - "
                + menorPlacar + " (" +menorFreq + " jogo(s))");
    }

    public List<Jogo> getListaJogosPorTime(Time time) {
        return this.brasileirao
                .stream()
                .filter(jogo -> (jogo.mandante().equals(time) || jogo.visitante().equals(time)))
                .collect(Collectors.toList());
    }
    public Long getTodasVitoriasPorTime(Time time, List<Jogo> numJogosTime) {
        List<Jogo> numVitorias = numJogosTime
                .stream()
                .filter(jogo -> (jogo.vencedor().equals(time)))
                .collect(Collectors.toList());
        return (long) numVitorias.size();
    }

    public Long getTodasDerrotasPorTime(Time time, List<Jogo> numJogosTime) {
        List<Jogo> numDerrotasTime = numJogosTime
                .stream()
                .filter(jogo -> (!jogo.vencedor().equals(time) && !jogo.vencedor().nome().equals("-")))
                .collect(Collectors.toList());
        // List<Jogo> numDerrotas = numJogos.stream().filter(jogo -> (jogo.vencedor() != time)).collect(Collectors.toList());
        return (long) numDerrotasTime.size();
    }

    public Long getTodosEmpatesPorTime(Time time, List<Jogo> numJogosTime) {
        List<Jogo> numEmpatesTime = numJogosTime
                .stream()
                .filter(jogo -> jogo.vencedor().nome().equals("-"))
                .collect(Collectors.toList());
        return (long) numEmpatesTime.size();
    }

    public Long getNumGolsPositivosPorTime(Time time, List<Jogo> listaJogosTime) {
        return (long) listaJogosTime
                .stream()
                .mapToInt(jogo -> {
                    Integer golsTime = null;
                    if (jogo.mandante().equals(time)) {
                        golsTime = jogo.mandantePlacar();
                    }
                    if (jogo.visitante().equals(time)) {
                        golsTime = jogo.visitantePlacar();
                    }
                    return golsTime;
                }).sum();
    }
    public Long getNumGolsSofridosPorTime(Time time, List<Jogo> listaJogosTime) {
        return (long) listaJogosTime
                .stream()
                .mapToInt(jogo -> {
                    Integer golsSofridosTime = null;
                    if (jogo.mandante().equals(time)) {
                        golsSofridosTime = jogo.visitantePlacar();
                    }
                    if (jogo.visitante().equals(time)) {
                        golsSofridosTime = jogo.mandantePlacar();
                    }
                    return golsSofridosTime;
                }).sum();
    }

    public Set<PosicaoTabela> getTabela() {
        Set<Time> timesVisitantes = this.brasileirao.stream().map(Jogo::visitante).collect(Collectors.toSet());
        Set<Time> timesMandantes = this.brasileirao.stream().map(Jogo::mandante).collect(Collectors.toSet());

        Set<Time> times = new HashSet<>();
        times.addAll(timesVisitantes);
        times.addAll(timesMandantes);

        Iterator<Time> timesIterator = times.iterator();
        Set<PosicaoTabela> tabela = new HashSet<>();

        while (timesIterator.hasNext()) {
            Time time = timesIterator.next();
            List<Jogo> listaJogosTime = getListaJogosPorTime(time);
            Long vitorias = getTodasVitoriasPorTime(time, listaJogosTime);
            Long derrotas = getTodasDerrotasPorTime(time, listaJogosTime);
            Long empates = getTodosEmpatesPorTime(time, listaJogosTime);
            Long golsPositivos = getNumGolsPositivosPorTime(time, listaJogosTime);
            Long golsSofridos = getNumGolsSofridosPorTime(time, listaJogosTime);
            Long saldoDeGols = golsPositivos - golsSofridos;
            Long jogos = (long) listaJogosTime.size();

            Long pontos = vitorias + saldoDeGols - derrotas;
            tabela.add(new PosicaoTabela(time, pontos, vitorias, derrotas, empates, golsPositivos, golsSofridos, saldoDeGols, jogos));
        }

        return tabela;
    }

    private static DayOfWeek getDayOfWeek(String diaDaSemana) {
        String dayOfWeek = switch (diaDaSemana.toLowerCase()) {
            case "segunda-feira" -> "MONDAY";
            case "terça-feira" -> "TUESDAY";
            case "quarta-feira" -> "WEDNESDAY";
            case "quinta-feira" -> "THURSDAY";
            case "sexta-feira" -> "FRIDAY";
            case "sábado" -> "SATURDAY";
            case "domingo" -> "SUNDAY";
            default -> diaDaSemana;
        };
        return DayOfWeek.valueOf(dayOfWeek);
    }
}