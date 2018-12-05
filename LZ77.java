
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class compression {

    static JFrame mainFrame;
    static JLabel headerLabel;
    static JLabel statusLabel;
    static JPanel controlPanel;

    public static void compress(String file) throws IOException {
        String fileName = file;
        String compressedFile = "data.lz";
        BufferedWriter writer = null;
        FileReader fileReader = null;
        String line = null;
        String data = "";
        String searchWindow = "";
        String lookAheadWindow = "";
        int currentPos = 0;
        int position = 0;
        int lenght = 0;
        char nextChar = 0;
        try {
            fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            writer = new BufferedWriter(new FileWriter(compressedFile, true));
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                data += line + "\n";
            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        while (currentPos < data.length()) {
            lookAheadWindow += data.charAt(currentPos);
            if (searchWindow.lastIndexOf(lookAheadWindow) == -1) {
                position = 0;
                lenght = 0;
                nextChar = lookAheadWindow.charAt(lookAheadWindow.length() - 1);
                writer.write(nextChar);
                currentPos++;
            } else {
                while (searchWindow.lastIndexOf(lookAheadWindow) != -1 && currentPos < data.length() - 1) {
                    position = searchWindow.length() - searchWindow.lastIndexOf(lookAheadWindow);
                    lenght = lookAheadWindow.length();
                    currentPos++;
                    lookAheadWindow += data.charAt(currentPos);
                }
                nextChar = lookAheadWindow.charAt(lookAheadWindow.length() - 1);
                if (lenght > 4) {
                    writer.write('$');
                    writer.write(Integer.toString(position));
                    writer.write('$');
                    writer.write(Integer.toString(lenght));
                    writer.write('$');
                    writer.write(nextChar);
                } else {
                    writer.write(lookAheadWindow);
                }
                currentPos++;
            }
            searchWindow += lookAheadWindow;
            lookAheadWindow = "";
            System.out.println("<" + position + "," + lenght + "," + nextChar + ">");
        }
        writer.close();
    }

    static void decompress(String file) throws IOException {
        String newFile = "newdata.txt";
        FileReader fileReader = null;
        BufferedWriter writer = null;
        String line = null;
        String data = "";
        String newData = "";
        int nextPos = 0;
        int pos;
        int len;
        String Position = "";
        String Lenght = "";
        try {
            fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            writer = new BufferedWriter(new FileWriter(newFile, true));
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                data += line + "\n";
            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "'");
        }
        while (nextPos < data.length()) {
            if (data.charAt(nextPos) == '$') {
                nextPos++;
                while (data.charAt(nextPos) != '$') {
                    Position += data.charAt(nextPos);
                    nextPos++;
                }
                nextPos++;
                while (data.charAt(nextPos) != '$') {
                    Lenght += data.charAt(nextPos);
                    nextPos++;
                }
                nextPos++;
                pos = newData.length() - Integer.parseInt(Position);
                len = Integer.parseInt(Lenght);
                while (len > 0) {
                    newData += newData.charAt(pos);
                    pos++;
                    len--;
                }
                newData += data.charAt(nextPos);
                Position = "";
                Lenght = "";
                nextPos++;
            } else {
                newData += data.charAt(nextPos);
                nextPos++;
            }
        }
        String[] words = newData.split("\n");
        for (String word : words) {
            writer.write(word);
            writer.newLine();
        }
        //writer.write(newData);
        writer.close();
    }

    static void prepareGUI() {
        mainFrame = new JFrame("Java Compress File Programme");
        mainFrame.setSize(400, 300);
        mainFrame.setLayout(new GridLayout(3, 1));
        mainFrame.setLocation(450, 200);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        headerLabel = new JLabel("Select Your Option", JLabel.CENTER);
        // statusLabel = new JLabel("Ahmed Ayman",JLabel.CENTER);    
        //    statusLabel.setSize(350,100);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        //  mainFrame.add(statusLabel);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        compression.prepareGUI();
        JButton compressBtn = new JButton("Compress File");
        JButton decompressBtn = new JButton("DeCompress File");
        compressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\LZ77"));
                // fileChooser.setSelectedFile(new File("README.html"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        compression.compress(filename);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null, "Your File " + filename + " Compressed Successfully", "Message", JOptionPane.INFORMATION_MESSAGE);
                } else if (result == JFileChooser.CANCEL_OPTION) {
                    JOptionPane.showMessageDialog(null, "You selected nothing.");
                } else if (result == JFileChooser.ERROR_OPTION) {
                    JOptionPane.showMessageDialog(null, "An error occurred.");
                }
            }
        });
        decompressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\LZ77"));
                //  fileChooser.setSelectedFile(new File("README.html"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        compression.decompress(filename);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    JOptionPane.showMessageDialog(null, "Your File " + filename + " DeCompressed Successfully", "Message", JOptionPane.INFORMATION_MESSAGE);
                } else if (result == JFileChooser.CANCEL_OPTION) {
                    JOptionPane.showMessageDialog(null, "You selected nothing.");
                } else if (result == JFileChooser.ERROR_OPTION) {
                    JOptionPane.showMessageDialog(null, "An error occurred.");
                }
            }
        });
        controlPanel.add(compressBtn);
        controlPanel.add(decompressBtn);
        mainFrame.setVisible(true);
    }

}
