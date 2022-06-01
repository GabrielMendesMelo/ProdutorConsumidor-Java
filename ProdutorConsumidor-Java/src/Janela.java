import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Janela extends JFrame {
    public Janela(Predio predio) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(predio.getAndares().get(0).getImgWidth(), predio.getAlturaPredio()));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(predio, BorderLayout.CENTER);
        setVisible(true);

        JPanel pnlGUI = new JPanel();
        pnlGUI.setLayout(new FlowLayout(FlowLayout.LEFT));
        Button btnComecar = new Button("Começar");
        Button btnParar = new Button("Parar");

        pnlGUI.add(btnComecar);
        pnlGUI.add(btnParar);

        add(pnlGUI, BorderLayout.SOUTH);

        btnComecar.addActionListener(e -> {
            pnlGUI.remove(btnComecar);
            predio.comecar();
        });
        
        btnParar.addActionListener(e -> predio.parar());

        pack();
    }
}
