import java.awt.Graphics;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JPanel;

public class Predio extends JPanel {
    private final int N_ANDARES;
    private static Semaphore filaSem = new Semaphore(0);
    
    private final List<Andar> andares = new ArrayList<>();
    
    private Passageiro[] passageiros;
    private final Elevador elevador;

    private List<Integer> filas = new ArrayList<>();

    public Predio(int nAndares, int andarInicial) {
        super();
        this.N_ANDARES = nAndares;

        for (int i = 0; i < N_ANDARES; i++) {
            andares.add(new Andar(this));
            andares.get(i).setPosY( i * andares.get(i).getImgHeight());
        }
        Collections.reverse(andares);

        this.elevador = new Elevador(this, andares.get(andarInicial).getPosY(), andarInicial);
    }

    public void comecar() {
        for (Passageiro p : passageiros) {
            p.comecar();
        }
        elevador.comecar();
    }

    public void parar() {
        for (Passageiro p : passageiros) {
            try {
                p.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            p.parar();
        }
        elevador.parar();
        System.out.println("ACABOU!");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        for (int i = 0; i < andares.size(); i++) {
            andares.get(i).draw(g);
        }
        elevador.draw(g);
        
        if (passageiros.length > 0) {
            for (Passageiro p : passageiros) {
                p.draw(g);
            }    
        }
        
    }

    public void setPassageiros(Passageiro[] passageiros) {
        this.passageiros = passageiros;
    }

    public Elevador getElevador() {
        return this.elevador;
    }

    public int getAlturaPredio() {
        return (N_ANDARES + 1) * andares.get(0).getImgHeight();
    }

    public List<Andar> getAndares() {
        return andares;
    }

    public List<Integer> getFilas() {
        return this.filas;
    }

    public void setFilas(int indice, boolean aumentar) {
        if (aumentar) {
            this.filas.set(indice, filas.get(indice) + 1);
        } else {
            this.filas.set(indice, filas.get(indice) - 1);
        }        
    }

    public void setFilas(List<Integer> filas) {
        this.filas = filas;
    }

    public void repintar() {
        revalidate();
        repaint();
    }

    public static Semaphore getFilaSem() {
        return filaSem;
    }

    public static void setFilaSem(Semaphore sem) {
        filaSem = sem;
    }
}
