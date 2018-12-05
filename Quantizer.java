
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class quantizer {

    static class Table {

        public float low;
        public float high;
        public int q;
        public int q1;
    }
    static JFrame mainFrame;
    static JLabel headerLabel;
    static JLabel statusLabel;
    static JPanel controlPanel;
    static JTextField numberOfBits;
    static int height;
    static int width;

    public static void prepareGUI() {
        mainFrame = new JFrame("Java Compress File Programme");
        mainFrame.setSize(300, 300);
        mainFrame.setLayout(new GridLayout(3, 1));
        mainFrame.setLocation(450, 200);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        headerLabel = new JLabel("Select Your Option", JLabel.CENTER);
        JLabel numberlabel = new JLabel("Enter Number Of Bits: ", JLabel.CENTER);
        numberOfBits = new JTextField(20);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(numberlabel);
        controlPanel.add(numberOfBits);
        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.setVisible(true);
    }

    static void compress(String fileName, int n) throws IOException {
        int[][] pixels = readImage(fileName);
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        BufferedWriter wr = new BufferedWriter(new FileWriter("compressed.txt"));
        ArrayList<Integer> data = new ArrayList<Integer>();
        ArrayList<Integer> averages = new ArrayList<Integer>();
        ArrayList<Table> table = new ArrayList<Table>();
        ArrayList<Integer> quantization = new ArrayList<Integer>();
        width = pixels[0].length;
        height = pixels.length;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data.add(pixels[i][j]);
            }
        }
        split(data, n, averages);
        System.out.println("Averages = " + averages);
        float low = 0;
        float high;
        for (int j = 0; j < averages.size(); j++) {
            System.out.println(j);
            Table ran = new Table();
            ran.low = low;
            System.out.println(ran.low);
            if (j == averages.size() - 1) {
                high = 255;
            } else {
                float sum = averages.get(j) + averages.get(j + 1);
                high = sum / 2;
            }
            ran.high = high;
            System.out.println(ran.high);
            ran.q = j;
            System.out.println(ran.q);
            ran.q1 = averages.get(j);
            System.out.println(ran.q1);
            table.add(ran);
            low = high;
        }
        for (Table ta : table) {
            System.out.println(ta.low + "()" + ta.high + "()" + ta.q + "()" + ta.q1);
        }
        System.out.println("table Size" + table.size());;
        for (int d : data) {
            for (Table t : table) {
                if (d >= t.low && d <= t.high) {
                    quantization.add(t.q);
                    break;
                }
            }
        }
        System.out.println("qunt size " + quantization.size());
        //System.out.println(quantization);
        for (Table ta : table) {
            String l = String.valueOf(ta.low);
            wr.write(l + ",");
            String h = String.valueOf(ta.high);
            wr.write(h + ",");
            String q = String.valueOf(ta.q);
            wr.write(q + ",");
            String q1 = String.valueOf(ta.q1);
            wr.write(q1 + ",");
        }
        wr.write('~');
        for (int i : quantization) {
            String c = String.valueOf(i);
            wr.write(c + ",");
        }
        br.close();
        wr.close();
    }

    static void deCompress(String fileName) throws IOException {
        int[][] pixels = new int[height][width];
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        ArrayList<Table> table = new ArrayList<Table>();
        ArrayList<Integer> quantization = new ArrayList<Integer>();
        ArrayList<Integer> deQuantization = new ArrayList<Integer>();
        String data = "";
        String compData = "";
        String tableData = "";
        String line;
        while ((line = br.readLine()) != null) {
            data += line;
        }
        String[] strData = data.split("~");
        tableData = strData[0];
        compData = strData[1];
        //  System.out.println(compData);
        // System.out.println(tableData);
        String[] strComp = compData.split(",");
        String[] strTable = tableData.split(",");
        for (String s : strComp) {
            quantization.add(Integer.parseInt(s));
        }
        System.out.println("quantization size " + quantization.size());
        for (int i = 0; i < strTable.length; i += 4) {
            float low = Float.parseFloat(strTable[i]);
            float high = Float.parseFloat(strTable[i + 1]);
            int q = Integer.parseInt(strTable[i + 2]);
            int q1 = Integer.parseInt(strTable[i + 3]);
            Table ta = new Table();
            ta.low = low;
            ta.high = high;
            ta.q = q;
            ta.q1 = q1;
            table.add(ta);
        }
        for (Table ta : table) {
            System.out.println(ta.low + "()" + ta.high + "()" + ta.q + "()" + ta.q1);
        }
        for (int s : quantization) {
            for (Table t : table) {
                if (s == t.q) {
                    deQuantization.add(t.q1);
                }
            }
        }
        System.out.println("deComprssed size " + deQuantization.size());
        //System.out.println(deQuantization);
        int k = 0;
        for (int z = 0; z < height; z++) {
            for (int j = 0; j < width && k < deQuantization.size(); j++) {
                // System.out.println(k);
                pixels[z][j] = deQuantization.get(k);
                k++;
            }
        }
        writeImage(pixels, "newOutput.jpg");
        br.close();
    }

    static void split(ArrayList<Integer> data, int n, ArrayList<Integer> averages) {
        ArrayList<Integer> leftData = new ArrayList<Integer>();
        ArrayList<Integer> rightData = new ArrayList<Integer>();
        float average = average(data);
        int lowerAverage = (int) average;
        int upperAverage = (int) (average + 1);
        if (n == 0) {
            averages.add((int) average);
            return;
        }
        for (int x : data) {
            if (Math.abs(x - lowerAverage) <= Math.abs(x - upperAverage)) {
                //if(x<=lowerAverage) {
                leftData.add(x);
            } else {
                rightData.add(x);
            }
        }
        n--;
        //	System.out.println(average + " " + lowerAverage + " " + upperAverage);
        //	System.out.println("Left = "+leftData);
        //	System.out.println("Right = "+rightData);
        split(leftData, n, averages);
        split(rightData, n, averages);

    }

    static float average(ArrayList<Integer> array) {
        float sum = 0;
        for (int i : array) {
            sum += i;
        }
        return sum / array.size();
    }

    static int[][] readImage(String path) {
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));

            int hieght = img.getHeight();
            int width = img.getWidth();

            int[][] imagePixels = new int[hieght][width];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < hieght; y++) {

                    int pixel = img.getRGB(x, y);

                    int red = (pixel & 0x00ff0000) >> 16;
                    int grean = (pixel & 0x0000ff00) >> 8;
                    int blue = pixel & 0x000000ff;
                    int alpha = (pixel & 0xff000000) >> 24;
                    imagePixels[y][x] = red;
                }
            }

            return imagePixels;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }

    }

    static void writeImage(int[][] imagePixels, String outPath) {
        BufferedImage image = new BufferedImage(imagePixels.length, imagePixels[0].length, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < imagePixels.length; y++) {
            for (int x = 0; x < imagePixels[y].length; x++) {
                int value = -1 << 24;
                value = 0xff000000 | (imagePixels[y][x] << 16) | (imagePixels[y][x] << 8) | (imagePixels[y][x]);
                image.setRGB(x, y, value);

            }
        }

        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

public class Main {

    public static void main(String[] args) throws IOException {
        quantizer.prepareGUI();
        JButton compressBtn = new JButton("Compress File");
        JButton decompressBtn = new JButton("DeCompress File");
        compressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\Quan"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        String bits = quantizer.numberOfBits.getText();
                        quantizer.compress(filename, Integer.parseInt(bits));
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
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\Quan"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        quantizer.deCompress(filename);
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
        quantizer.controlPanel.add(compressBtn);
        quantizer.controlPanel.add(decompressBtn);
        quantizer.mainFrame.setVisible(true);
    }

}
