
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class LZW {

    static JFrame mainFrame;
    static JLabel headerLabel;
    static JLabel statusLabel;
    static JPanel controlPanel;

    public static void prepareGUI() {
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

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.setVisible(true);
    }

    public static void compression(String file) throws IOException {
        HashMap<String, Integer> dictionary = new HashMap<>();
        ArrayList<Integer> index = new ArrayList<Integer>();
        ArrayList<String> bytes = new ArrayList<String>();
        String bits = "";
        String line;
        String data = "";
        String lookAheadWindow = "";
        int currentPos = 0;
        int nextIndex = 128;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                data += line + "\n";
            }

            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "'");
        }
        for (int i = 0; i < 128; i++) {
            dictionary.put(Character.toString((char) i), i);
        }
        int value = 0;
        while (currentPos < data.length() - 1) {
            int c = 0;
            int bufferLen = currentPos;

            lookAheadWindow += data.charAt(bufferLen);
            while (dictionary.containsKey(lookAheadWindow)) {
                value = dictionary.get(lookAheadWindow);
                bufferLen++;
                if (bufferLen == data.length()) {
                    break;
                }
                lookAheadWindow += data.charAt(bufferLen);
                c++;
            }

            dictionary.put(lookAheadWindow, nextIndex);
            index.add(value);
            lookAheadWindow = "";
            nextIndex++;
            if (c == 0) {
                break;
            }
            currentPos += c;
        }

        for (int val : index) {
            bits += intToBinary12Bit(val);
        }

        int coun = 0;
        for (int i = 0; i < bits.length(); i += 8) {
            if (bits.substring(i, Math.min(i + 8, bits.length())).length() == 4) {
                bytes.add("0000" + bits.substring(i, Math.min(i + 8, bits.length())));
            } else {
                bytes.add(bits.substring(i, Math.min(i + 8, bits.length())));
            }
            coun++;
        }
        byte[] byt = new byte[coun];
        int i = 0;
        for (String b : bytes) {
            int decimal = Integer.parseInt(b, 2);
            String hexStr = Integer.toString(decimal, 16);
            byt[i] = (byte) (Integer.parseInt(hexStr, 16));
            i++;
        }
        try {
            FileOutputStream fos = new FileOutputStream("data.lzw");
            fos.write(byt);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompress(String fileName) throws IOException {
        ArrayList<Integer> index = new ArrayList<Integer>();
        HashMap<Integer, String> dictionary = new HashMap<>();
        String stream = "";
        String data = "";
        int nextIndex = 128;
        for (int i = 0; i < 128; i++) {
            dictionary.put(i, Character.toString((char) i));
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("newdata.txt", true));
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            byte fileContent[] = new byte[(int) file.length()];
            fis.read(fileContent);
            for (byte b : fileContent) {
                int x = b & 0xff;
                String hex = Integer.toHexString(x);
                int num = Integer.parseInt(hex, 16);
                stream += intToBinary8Bit(num);
            }
            boolean check = false;
            for (int j = 0; j < stream.length(); j += 12) {
                String nu = stream.substring(j, Math.min(j + 12, stream.length()));
                if (nu.length() != 12) {
                    check = true;
                }
                index.add(Integer.parseInt(nu, 2));
            }
            if (check) {
                index.set(index.size() - 2, index.get(index.size() - 2) + index.get(index.size() - 1));
                index.remove(index.size() - 1);
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int iter = 1;
        String s1 = "";
        String s2 = "";
        String sv = "";
        for (int in : index) {
            if (dictionary.containsKey(in)) {
                data += dictionary.get(in);
                if (iter == 1) {
                    s1 += dictionary.get(in);
                }
                if (iter == 2) {
                    s2 = dictionary.get(in);
                    sv = s1 + s2.charAt(0);
                    dictionary.put(nextIndex, sv);
                    iter = 1;
                    s1 = s2;
                    s2 = "";
                    nextIndex++;
                }
            } else {
                sv = s1 + s1.charAt(0);
                data += sv;
                dictionary.put(nextIndex, sv);
                s1 = s1 + s1.charAt(0);
                s2 = "";
                nextIndex++;
                iter = 1;
            }
            iter++;
        }
        String[] lines = data.split("\n");
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();

    }

    public static String intToBinary12Bit(int i) {
        String binary = Integer.toBinaryString(i);
        int len = binary.length();
        String format = "000000000000";
        if (len < 12) {
            binary = format.substring(0, 12 - len).concat(binary);
        } else {
            binary = binary.substring(len - 12);
        }
        return binary;
    }

    public static String intToBinary8Bit(int i) {
        String binary = Integer.toBinaryString(i);
        int len = binary.length();
        String format = "00000000";
        if (len < 8) {
            binary = format.substring(0, 8 - len).concat(binary);
        } else {
            binary = binary.substring(len - 8);
        }
        return binary;
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        LZW.prepareGUI();
        JButton compressBtn = new JButton("Compress File");
        JButton decompressBtn = new JButton("DeCompress File");
        compressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\LZW"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        LZW.compression(filename);
                    } catch (IOException e1) {
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
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\LZW"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        LZW.decompress(filename);
                    } catch (IOException e1) {
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
