import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Andar {
    private final JPanel panel;
    private final ImageIcon img;
    private int posY;

    public Andar(JPanel panel) {
        this.panel = panel;
        this.img = new ImageIcon(getClass().getResource("./img/andar.png"));
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getPosY() {
        return posY;
    }

    public void draw(Graphics g) {
        img.paintIcon(panel, g, 0, posY);
    }

    public int getImgHeight() {
        return img.getIconHeight();
    }

    public int getImgWidth() {
        return img.getIconWidth();
    }
}
