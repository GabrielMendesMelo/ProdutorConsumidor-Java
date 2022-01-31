import java.awt.Graphics;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;

public class Elevador extends Thread {
    private volatile boolean rodando = false;
    private static final int INTERVALO_EXECUCAO = 32;
    private static final Semaphore ELEVADOR_SEM = new Semaphore(1);

    private static int pos;
    private static int posDestino;
    private static int andarAtual;
    private static int andarDestino;

    private static Predio predio;
    private static ImageIcon portaAberta;
    private static ImageIcon portaFechada;

    private static boolean portaEstaAberta = false;
    private static boolean chegouAoDestino = true;
    private static boolean estaOcupado = false;
    private static boolean podeSerChamado = false;

    public Elevador(Predio predio, int posInicial, int andarInicial) {
        Elevador.pos = posInicial;
        Elevador.andarAtual = andarInicial;

        Elevador.predio = predio;
        
        portaAberta = new ImageIcon(getClass().getResource("./img/elevador-aberto.png"));
        portaFechada = new ImageIcon(getClass().getResource("./img/elevador-fechado.png"));
    }

    public void draw(Graphics g) {
        if (portaEstaAberta) {
            portaAberta.paintIcon(predio, g, 10, pos);
        } else {
            portaFechada.paintIcon(predio, g, 10, pos);
        }
    }

    public void comecar() {
        this.start();
        this.rodando = true;
    }

    public void parar() {
        this.rodando = false;
    }
    
    private void mover() {        
        if (!chegouAoDestino) {
            if (posDestino < pos) {
                pos -= 3;
            } else if (posDestino > pos) {
                pos += 3;
            } else {
                andarAtual = andarDestino;
                chegouAoDestino = true;
                podeSerChamado = false;
                ELEVADOR_SEM.release();
                
                if (acabou()) {
                    predio.parar();
                }

            }
            predio.repintar();
        } else {
            if (predio.getFilas().get(andarAtual) == 0) {
                podeSerChamado = true;
            }
        }
    }

    @Override
    public void run() {
        super.run();

        while (rodando) {
            mover();

            try {
                Thread.sleep(INTERVALO_EXECUCAO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static int getLargura() {
        return portaAberta.getIconWidth();
    }

    public void abrirPorta() {
        portaEstaAberta = true;
        predio.repintar();
    }

    public void fecharPorta() {
        portaEstaAberta = false;
        predio.repintar();
    }

    public void visitarAndar(int andar) {
        podeSerChamado = false;
        andarDestino = andar;
        posDestino = predio.getAndares().get(andar).getPosY();
        chegouAoDestino = false;
    }

    public static boolean getEstaNoDestino() {
        return chegouAoDestino;
    }

    public static int getAndarAtual() {
        return andarAtual;
    }

    public static boolean getEstaOcupado() {
        return estaOcupado;
    }

    public static boolean getPodeSerChamado() {
        return podeSerChamado;
    }

    public static void setEstaOcupado(boolean estaOcupado) {
        Elevador.estaOcupado = estaOcupado;
    }

    public static final Semaphore getElevadorSem() {
        return ELEVADOR_SEM;
    }

    public static final int getIntervaloExecucao() {
        return INTERVALO_EXECUCAO;
    }

    private static boolean acabou() {
        for (int i : predio.getFilas()) {
            if (i > 0) {
                return false;
            }
        }
        return true;
    }
}
