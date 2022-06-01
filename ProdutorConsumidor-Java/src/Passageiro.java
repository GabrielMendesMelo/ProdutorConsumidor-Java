import java.awt.Graphics;
import java.awt.Toolkit;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;

public class Passageiro extends Thread {
    private volatile boolean rodando = false;
    private final int TEMPO_ESPERA = 100;

    private int lugarNaFila;
    private int andarAtual;
    private int andarDestino;
    private int posX;
    private int posY;
    private int posXDestino;
    private int posYDestino;

    private final int POS_DENTRO_ELEVADOR = 10;
    
    private Predio predio;
    private ImageIcon img;

    private boolean estaNoElevador = false;
    private boolean chegouAoDestino = true;

    public Passageiro(int id, int lugarNaFila, int andarInicial, Predio predio) {
        this.lugarNaFila = lugarNaFila;
        this.andarAtual = andarInicial;

        this.posXDestino = POS_DENTRO_ELEVADOR;
        this.predio = predio;

        this.posX = predio.getElevador().getLargura() * lugarNaFila;
        this.posY = predio.getAndares().get(andarInicial).getPosY();

        img = new ImageIcon(getClass().getResource("./img/passageiro" + ThreadLocalRandom.current().nextInt(1, 9) + ".png"));
    }

    public void draw(Graphics g) {
        img.paintIcon(predio, g, posX, posY);
    }

    public void comecar() {
        this.start();
        this.rodando = true;
    }

    public void parar() {
        this.rodando = false;
    }

    private void esperar() {
        if (lugarNaFila != 0) {
            try {
                Thread.sleep(TEMPO_ESPERA);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void avancarNaFila() {
        if (!estaNoElevador &&
            lugarNaFila > 1 &&
            andarAtual == predio.getElevador().getAndarAtual() &&
            predio.getElevador().getEstaOcupado() && 
            chegouAoDestino) {
        
            if (Predio.getFilaSem().tryAcquire()) {
                chegouAoDestino = false;
                lugarNaFila--;
                posXDestino = predio.getElevador().getLargura() * lugarNaFila;
                
                while (posX > posXDestino) {
                    posX--;
                    predio.repintar();
                    
                    try {
                        Thread.sleep(predio.INTERVALO_EXECUCAO / 6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                chegouAoDestino = true;
                Predio.getFilaSem().release();
                if (lugarNaFila == 1) {
                    posXDestino = POS_DENTRO_ELEVADOR;
                }
                if (Predio.getFilaSem().availablePermits() == predio.getFilas().get(andarAtual)) {
                    Predio.setFilaSem(new Semaphore(0));
                }

                try {
                    Thread.sleep(TEMPO_ESPERA);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }    

    private void entrarNoElevador() {
        if (!estaNoElevador && 
            lugarNaFila == 1 && 
            andarAtual == predio.getElevador().getAndarAtual() && 
            !predio.getElevador().getEstaOcupado() &&
            predio.getElevador().getEstaNoDestino() && 
            chegouAoDestino) {
            
            if (predio.getElevador().getElevadorSem().tryAcquire()) {
                abrirPorta();
                while (posX > posXDestino) {
                    posX--;
                    predio.repintar();
                    
                    try {
                        Thread.sleep(predio.INTERVALO_EXECUCAO / 6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                estaNoElevador = true;
                chegouAoDestino = false;
                lugarNaFila = 0;
                predio.setFilas(andarAtual, false);
                predio.getElevador().setEstaOcupado(true);
                fecharPorta();

                Predio.setFilaSem(new Semaphore(predio.getFilas().get(andarAtual)));

                int destinoAux = ThreadLocalRandom.current().nextInt(0, predio.getAndares().size());
                while (destinoAux == andarAtual) {
                    destinoAux = ThreadLocalRandom.current().nextInt(0, predio.getAndares().size());
                }
                visitarAndar(destinoAux);
            }
        }
    }

    private void sairDoElevador() {
        if (estaNoElevador && 
            lugarNaFila == 0 && 
            andarAtual == predio.getElevador().getAndarAtual() &&
            predio.getElevador().getEstaNoDestino()) {

            Predio.setFilaSem(new Semaphore(0));

            posXDestino = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

            abrirPorta();
            while (posX < posXDestino) {
                posX++;
                if (estaNoElevador && posX >= predio.getElevador().getLargura()) {
                    fecharPorta();
                    estaNoElevador = false;
                    lugarNaFila = -1;
                    predio.getElevador().setEstaOcupado(false);
                    predio.getElevador().getElevadorSem().release();
                }
                predio.repintar();
                
                try {
                    Thread.sleep(predio.INTERVALO_EXECUCAO / 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            parar();
        }
    }

    private void chamarElevador() {
        if (!estaNoElevador &&
            lugarNaFila == 1 &&
            andarAtual != predio.getElevador().getAndarAtual() &&
            !predio.getElevador().getEstaOcupado() &&
            predio.getElevador().getEstaNoDestino() &&
            predio.getElevador().getPodeSerChamado() &&
            chegouAoDestino) {

            if (predio.getElevador().getElevadorSem().tryAcquire()) {
                predio.getElevador().visitarAndar(andarAtual);
            }
        }
    }

    private void moverY() {
        if (!chegouAoDestino &&
            estaNoElevador && 
            lugarNaFila == 0) {
            if (posYDestino < posY) {
                posY -= 3;
            } else if (posYDestino > posY) {
                posY += 3;
            } else {
                chegouAoDestino = true;
                andarAtual = andarDestino;
            }
            predio.repintar();
        }
    }

    private void update() {
        esperar();
        chamarElevador();
        avancarNaFila();
        entrarNoElevador();
        sairDoElevador();
        moverY();
    }

    @Override
    public void run() {
        super.run();

        while (rodando) {
            update();

            try {
                Thread.sleep(predio.INTERVALO_EXECUCAO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void abrirPorta() {
        predio.getElevador().abrirPorta();
    }
  
    public void fecharPorta() {
        predio.getElevador().fecharPorta();
    }

    public void visitarAndar(int andar) {
        andarDestino = andar;
        posXDestino = predio.getElevador().getLargura() * (predio.getFilas().get(andarDestino) + 1);
        predio.getElevador().visitarAndar(andar);
        posYDestino = predio.getAndares().get(andar).getPosY();
        chegouAoDestino = false;        
    }
}
