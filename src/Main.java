import javax.swing.*;
import java.io.IOException;

public class Main {
    static String inFile = "", outFile = "";
    public static void main(String[] args) throws IOException {

        ButtonFrame buttonFrame = new ButtonFrame();
        buttonFrame.setVisible(true);
        buttonFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            inFile = new ChooseFrame().path;
            new ButtonPanel().display.setText(new ButtonPanel().fileAddress);
        }
        catch (Exception e){
            System.out.println("Some problem");
        }

        if (args.length > 1 ) outFile = args[1];
        else {
            outFile = inFile;
            if (outFile.lastIndexOf('.') > 0)
                outFile = outFile.substring( 0, outFile.lastIndexOf('.') ) + ".ASM";
            else outFile += ".ASM";
        }
    }
}
