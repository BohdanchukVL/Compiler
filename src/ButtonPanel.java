import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ButtonPanel extends JPanel{
    String fileAddress;
    JTextArea display= new JTextArea();
    public ButtonPanel() {
        JButton mainButton = new JButton("Compile");
        JButton resultButton = new JButton("Show result");
        add(mainButton);
        add(resultButton);
        add(display);

        setLayout(null);
        mainButton.setSize(200, 50);
        resultButton.setSize(200, 50);
        display.setSize(375, 800);
        mainButton.setLocation(0, 0);
        resultButton.setLocation(200, 0);
        display.setLocation(5, 55);

        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Parser parse = null;
                try {
                    parse = new Parser(new Main().inFile, new Main().outFile);
                    parse.program();
                    fileAddress = new Main().inFile;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    parse.lex.in.close();
                    parse.out.close();
                    display.setText("Translation competed!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        resultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> lines = new ArrayList<String>();
                String liness = "";
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(new Main().outFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                    for(int i = 0; i < lines.size(); i++){
                        liness = liness + "\n" + lines.get(i);
                    }
                    display.setText(liness);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        });

    }
}