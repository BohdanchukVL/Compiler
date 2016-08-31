import javax.swing.*;
import java.io.File;

public class ChooseFrame extends JFrame{
    String path;
    File file;
    public ChooseFrame(){
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(this);
        try {
            file = chooser.getSelectedFile();
            path = file.getPath();
            new ButtonPanel().display.setText(path);
        }
        catch (Exception e){

        }
    }
}
