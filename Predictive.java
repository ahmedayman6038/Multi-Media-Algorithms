
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Predictive {

    static class Table {

        public int low;
        public int high;
        public int q;
        public int q1;
    }
    static JFrame mainFrame;
    static JLabel headerLabel;
    static JLabel statusLabel;
    static JPanel controlPanel;
    static JTextField numberOfBits;
    //static JTextField numberOfSteps;
    static int width;
    static int height;

    public static void prepareGUI() {
        mainFrame = new JFrame("Java Compress File Programme");
        mainFrame.setSize(400, 400);
        mainFrame.setLayout(new GridLayout(3, 1));
        mainFrame.setLocation(450, 200);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        headerLabel = new JLabel("Select Your Option", JLabel.CENTER);
        JLabel bits = new JLabel("Enter Number Of Bits: ", JLabel.CENTER);
        numberOfBits = new JTextField(20);
        //  JLabel  steps= new JLabel("Enter Number of Steps: ", JLabel.CENTER);
        //   numberOfSteps = new JTextField(20); 

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(bits);
        controlPanel.add(numberOfBits);
        //  controlPanel.add(steps);
        //   controlPanel.add(numberOfSteps);
        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.setVisible(true);
    }

    public static void compress(String fileName, int bits) throws IOException {
        int[][] pixels = readImage(fileName);
        ArrayList<Integer> quantization = new ArrayList<Integer>();
        ArrayList<Table> table = new ArrayList<Table>();
        ArrayList<Integer> data = new ArrayList<Integer>();
        ArrayList<Integer> diff = new ArrayList<Integer>();
        BufferedWriter wr = new BufferedWriter(new FileWriter("compressed.txt"));
        width = pixels[0].length;
        height = pixels.length;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data.add(pixels[i][j]);
            }
        }
        /*int low = 0;
		for (int i = 0; i < (int) Math.pow(2, bits); i++) {
			Table ta = new Table();
			ta.q = i;
			ta.low = low;
			ta.high = low + (step-1);
			ta.q1 = (int) Math.ceil((float)(ta.low + ta.high)/2);
			low = ta.high + 1;
			table.add(ta);
		}
		for (Table tab : table) {
			System.out.println(tab.q + " " + tab.low + " " + tab.high + " " +tab.q1);
		}*/
        diff.add(data.get(0));
        for (int i = 1; i < data.size(); i++) {
            int dif = data.get(i) - data.get(i - 1);
            diff.add(dif);
        }
        int max = diff.get(0);
        int min = diff.get(0);
        for (int i : diff) {
            if (i > max) {
                max = i;
            }
            if (i < min) {
                min = i;
            }
        }
        int levels = (int) Math.pow(2, bits);
        int steps = (max - min) / levels;
        System.out.println("steps " + steps);
        int low = 0;
        for (int i = 0; i < levels; i++) {
            Table ta = new Table();
            ta.q = i;
            ta.low = low;
            ta.high = low + (steps - 1);
            ta.q1 = (int) Math.ceil((float) (ta.low + ta.high) / 2);
            low = ta.high + 1;
            table.add(ta);
            if (ta.high > 255) {
                break;
            }
        }
        for (Table tab : table) {
            System.out.println(tab.q + " " + tab.low + " " + tab.high + " " + tab.q1);
        }
        quantization.add(data.get(0));
        for (int i = 1; i < data.size(); i++) {
            //int dif = data.get(i) - data.get(i-1);
            int dif = diff.get(i);
            for (Table t : table) {
                if (Math.abs(dif) >= t.low && Math.abs(dif) <= t.high) {
                    if (dif < 0) {
                        if (t.q == 0) {
                            quantization.add(1000);
                        } else {
                            quantization.add(-t.q);
                        }
                    } else {
                        quantization.add(t.q);
                    }
                }
            }
        }

        /*for (int q : quantization) {
			System.out.println(q);
		}*/
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
        wr.write("~");
        for (int q : quantization) {
            wr.write(String.valueOf(q) + ",");
        }
        wr.close();
    }

    public static void decompress(String fileName) throws IOException {
        int[][] pixels = new int[height][width];
        ArrayList<Integer> dequantization = new ArrayList<Integer>();
        ArrayList<Integer> quantization = new ArrayList<Integer>();
        ArrayList<Integer> decoded = new ArrayList<Integer>();
        ArrayList<Table> table = new ArrayList<Table>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
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
        System.out.println(tableData);
        System.out.println(compData);
        String[] strTable = tableData.split(",");
        String[] strComp = compData.split(",");
        for (int i = 0; i < strTable.length; i += 4) {
            int low = Integer.parseInt(strTable[i]);
            int high = Integer.parseInt(strTable[i + 1]);
            int q = Integer.parseInt(strTable[i + 2]);
            int q1 = Integer.parseInt(strTable[i + 3]);
            Table ta = new Table();
            ta.low = low;
            ta.high = high;
            ta.q = q;
            ta.q1 = q1;
            table.add(ta);
        }
        for (String s : strComp) {
            quantization.add(Integer.parseInt(s));
        }
        for (Table ta : table) {
            System.out.println(ta.low + "()" + ta.high + "()" + ta.q + "()" + ta.q1);
        }
        dequantization.add(quantization.get(0));
        for (int i = 1; i < quantization.size(); i++) {
            boolean positive = true;
            int val = quantization.get(i);
            int q;
            if (val == 1000) {
                q = 0;
                positive = false;
            } else if (val < 0) {
                q = -val;
                positive = false;
            } else {
                q = val;
            }
            for (Table t : table) {
                if (q == t.q) {
                    if (positive) {
                        dequantization.add(t.q1);
                    } else {
                        dequantization.add(-t.q1);
                    }
                }
            }
        }
        for (Integer i : dequantization) {
            System.out.println(i);
        }
        int last = dequantization.get(0);
        decoded.add(last);
        for (int i = 1; i < dequantization.size(); i++) {
            last = last + dequantization.get(i);
            decoded.add(last);

        }
        System.out.println("--------------");
        for (int i : decoded) {
            System.out.println(i);
        }
        int k = 0;
        for (int z = 0; z < height; z++) {
            for (int j = 0; j < width && k < decoded.size(); j++) {
                pixels[z][j] = decoded.get(k);
                k++;
            }
        }
        writeImage(pixels, "newOutput.jpg");
        br.close();
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
        /*int[] data = {15,16,24,33,44,68};*/
        Predictive.prepareGUI();
        JButton compressBtn = new JButton("Compress File");
        JButton decompressBtn = new JButton("DeCompress File");
        compressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\Predictive"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        String bits = Predictive.numberOfBits.getText();
                        // String steps = Predictive.numberOfSteps.getText();
                        Predictive.compress(filename, Integer.parseInt(bits));
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
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\Predictive"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        Predictive.decompress(filename);
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
        Predictive.controlPanel.add(compressBtn);
        Predictive.controlPanel.add(decompressBtn);
        Predictive.mainFrame.setVisible(true);
    }
}
