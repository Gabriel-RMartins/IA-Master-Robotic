package maps;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import interf.IUIConfiguration;
import viewer.PathViewer;
import algoritmo.AG;

/**
 * Exemplo que mostra como desenhar um caminho no visualizador.
 */
public class PathDrawingSample {
    public static IUIConfiguration conf;

    // Caminho do arquivo CSV
    public static String caminhoArquivo = "Resultados.csv";

    // Variaveis Globais

    /**
     * ID do mapa a usar.
     * 
     * Ver o Maps.java. 
     */
    public static int mapId = 3;
    
    public static void main(String args[]) throws InterruptedException, Exception {

        System.out.println("Início da execução!");

        conf = Maps.getMap(mapId);

        AG ag = new AG(conf.getStart(), conf.getEnd(), conf.getObstacles(), conf);
        ag.execute();

        PathViewer pv = new PathViewer(PathDrawingSample.conf);
        pv.setFitness(ag.bestSolution.getFitness());
        pv.setStringPath(ag.bestSolution.getPath().toString());
        pv.paintPath(ag.bestSolution.getPath());

    }

    public static void escreverCSV(String texto, String caminhoArquivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo, true))) {
            writer.write(texto);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao escrever no arquivo CSV: " + e.getMessage());
        }
    }

    public static void apagarFicheiro(String caminhoArquivo) {
        try {
            File file = new File(caminhoArquivo);
            if (file.exists()) {
                file.delete();
                System.out.println("Arquivo CSV apagado: " + caminhoArquivo);
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao apagar o arquivo CSV: " + e.getMessage());
        }
    }

}
