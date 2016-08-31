import javax.swing.*;

public class ButtonFrame extends JFrame{
    public static final int DEFAULT_WIDTH=400;
    public static final int DEFAULT_HEIGHE=900;
    ButtonPanel mainPanel = new ButtonPanel();
    public ButtonFrame() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHE);
        setTitle("KursovaSPZ");
        add(mainPanel);
    }
}