
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

public class Main {

    static JFrame mainFrame;
    static JLabel headerLabel;
    static JLabel statusLabel;
    static JPanel controlPanel;
    static JTextField vectorWidth;
    static JTextField vectorHeigh;
    static JTextField codeBook;
    static int imgWidth;
    static int imgHeigh;

    static class Book {

        public int[][] vector;
        ArrayList<int[][]> nearstVectors;
        public int code;

        public Book(int[][] vector, int code, ArrayList<int[][]> nearstVectors) {
            this.vector = vector;
            this.code = code;
            this.nearstVectors = nearstVectors;
        }
    }

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
        JLabel widthLabel = new JLabel("Enter Vector Width: ", JLabel.CENTER);
        vectorWidth = new JTextField(20);
        JLabel heighLabel = new JLabel("Enter Vector Heigh: ", JLabel.CENTER);
        vectorHeigh = new JTextField(20);
        JLabel codeBookLabel = new JLabel("Enter CodeBook Size: ", JLabel.CENTER);
        codeBook = new JTextField(20);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(widthLabel);
        controlPanel.add(vectorWidth);
        controlPanel.add(heighLabel);
        controlPanel.add(vectorHeigh);
        controlPanel.add(codeBookLabel);
        controlPanel.add(codeBook);
        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.setVisible(true);
    }

    public static void compress(String fileName, int vectorWidth, int vectorHeigh, int codeBookSize) throws IOException {
        int[][] data = readImage(fileName);
        imgWidth = data[0].length;
        imgHeigh = data.length;
        BufferedWriter wr = new BufferedWriter(new FileWriter("compressed.txt"));
        int blocksY = (int) Math.ceil((float) data[0].length / vectorWidth);
        ArrayList<int[][]> vectors = new ArrayList<int[][]>();
        ArrayList<Book> codeBook = new ArrayList<Book>();
        ArrayList<Integer> compressed = new ArrayList<Integer>();

        // divide image into blocks
        vectors = createSubArrays(data, vectorHeigh, vectorWidth);

        // Split the image with specific code book
        split(vectors, log(codeBookSize, 2), null, codeBook);

        // initialize code to each block
        for (int x = 0; x < codeBook.size(); x++) {
            codeBook.get(x).code = x;
        }

        // Start encoding process
        for (int[][] is : vectors) {
            for (int x = 0; x < codeBook.size(); x++) {
                for (int s = 0; s < codeBook.get(x).nearstVectors.size(); s++) {
                    if (is.equals(codeBook.get(x).nearstVectors.get(s))) {
                        compressed.add(codeBook.get(x).code);
                    }
                }
            }
        }

        // Write All Data in File
        wr.write(String.valueOf(blocksY) + ",");
        wr.write(String.valueOf(vectorWidth) + ",");
        wr.write(String.valueOf(vectorHeigh) + ",");
        wr.write("~");
        for (Book bo : codeBook) {
            for (int i = 0; i < bo.vector.length; i++) {
                for (int j = 0; j < bo.vector[0].length; j++) {
                    wr.write(String.valueOf(bo.vector[i][j]) + ",");
                }
            }

        }
        wr.write("~");
        for (int in : compressed) {
            wr.write(String.valueOf(in) + ",");
        }
        wr.close();

    }

    public static void deCompress(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        ArrayList<Book> codeBook = new ArrayList<Book>();
        ArrayList<Integer> compressed = new ArrayList<Integer>();
        ArrayList<int[][]> deCompressd = new ArrayList<int[][]>();
        ArrayList<int[][]> image = new ArrayList<int[][]>();
        int[][] pixels = null;
        String data = "";
        String headerData = "";
        String vectorsData = "";
        String compData = "";
        String line;

        // read all data from file
        while ((line = br.readLine()) != null) {
            data += line;
        }
        String[] strData = data.split("~");
        headerData = strData[0];
        vectorsData = strData[1];
        compData = strData[2];
        String[] strHeader = headerData.split(",");
        String[] strVectors = vectorsData.split(",");
        String[] strComp = compData.split(",");

        // assign the readied data to it's variables
        int blocksY = Integer.parseInt(strHeader[0]);
        int vectorWidth = Integer.parseInt(strHeader[1]);
        int vectorHeigh = Integer.parseInt(strHeader[2]);
        ArrayList<int[][]> vectors = new ArrayList<int[][]>();
        int counter = 0;
        while (counter < strVectors.length) {
            int[][] temp = new int[vectorHeigh][vectorWidth];
            for (int i = 0; i < vectorHeigh; i++) {
                for (int j = 0; j < vectorWidth; j++) {
                    temp[i][j] = Integer.parseInt(strVectors[counter]);
                    counter++;
                }
            }
            vectors.add(temp);
        }
        for (int i = 0; i < vectors.size(); i++) {
            codeBook.add(new Book(vectors.get(i), i, null));
        }
        for (String str : strComp) {
            compressed.add(Integer.parseInt(str));
        }

        // start decodeing process 
        for (int i : compressed) {
            for (Book book : codeBook) {
                if (book.code == i) {
                    deCompressd.add(book.vector);
                }
            }
        }

        // convert blocks of image to one block
        int[][] temp = deCompressd.get(0);
        int count = 1;
        int x = 0;
        boolean first = true;
        while (x < deCompressd.size() - 1) {
            temp = combineTwoArrayX(temp, deCompressd.get(++x));
            if (count == blocksY - 1) {
                if (first) {
                    pixels = temp;
                    first = false;
                } else {
                    pixels = combineTwoArrayY(pixels, temp);
                }
                if (x < deCompressd.size() - 1) {
                    temp = deCompressd.get(++x);
                }
                count = 1;
            } else {
                count++;
            }
        }

        //delete all padding and get the orignal size of image
        image = createSubArrays(pixels, imgWidth, imgHeigh);

        // write image into file
        writeImage(image.get(0), "newOutput.jpg");

        br.close();
    }

    public static void split(ArrayList<int[][]> vectors, int itr, int[][] oldAvg, ArrayList<Book> codeBook) {
        ArrayList<int[][]> leftVectors = new ArrayList<int[][]>();
        ArrayList<int[][]> rightVectors = new ArrayList<int[][]>();
        int[][] average;

        // check if no data assigned to the vector make the new average equal to the old average
        // else calculate the new average again
        if (vectors.size() == 0) {
            average = oldAvg;
        } else {
            average = averageCalculate(vectors);
        }

        // base case to stop looping
        if (itr == 0) {
            codeBook.add(new Book(average, 0, vectors));
            return;
        }

        int[][] lowerAverage = new int[average.length][average[0].length];
        int[][] upperAverage = new int[average.length][average[0].length];

        // calculate the lower average of data
        for (int i = 0; i < average.length; i++) {
            for (int j = 0; j < average[0].length; j++) {
                lowerAverage[i][j] = average[i][j];
            }
        }

        // calculate the upper average of data
        for (int i = 0; i < average.length; i++) {
            for (int j = 0; j < average[0].length; j++) {
                upperAverage[i][j] = average[i][j] + 1;
            }
        }

        // assign data to left vector and right vector
        int counter = 0;
        for (int x = 0; x < vectors.size(); x++) {
            for (int i = 0; i < vectors.get(x).length; i++) {
                for (int j = 0; j < vectors.get(x)[0].length; j++) {
                    if (vectors.get(x)[i][j] <= lowerAverage[i][j]) {
                        counter++;
                    }
                }
            }
            if (counter >= Math.ceil((float) (average.length * average[0].length) / 2)) {
                leftVectors.add(vectors.get(x));
            } else {
                rightVectors.add(vectors.get(x));
            }
            counter = 0;
        }

        itr--;

        // split the left data in vector again
        split(leftVectors, itr, lowerAverage, codeBook);

        // split the right data in vector again
        split(rightVectors, itr, upperAverage, codeBook);
    }

    public static int log(int x, int base) {
        return (int) (Math.log(x) / Math.log(base));
    }

    public static int[][] averageCalculate(ArrayList<int[][]> vectors) {
        int[][] sum = new int[vectors.get(0).length][vectors.get(0)[0].length];
        int[][] avg = new int[vectors.get(0).length][vectors.get(0)[0].length];
        for (int x = 0; x < vectors.size(); x++) {
            for (int i = 0; i < vectors.get(x).length; i++) {
                for (int j = 0; j < vectors.get(x)[0].length; j++) {
                    sum[i][j] += vectors.get(x)[i][j];
                }
            }
        }
        for (int i = 0; i < sum.length; i++) {
            for (int j = 0; j < sum[0].length; j++) {
                avg[i][j] = sum[i][j] / vectors.size();
            }
        }
        return avg;
    }

    public static ArrayList<int[][]> createSubArrays(int inputArray[][], int subRows, int subCols) {
        ArrayList<int[][]> subArrays = new ArrayList<int[][]>();
        for (int r = 0; r < inputArray.length; r += subRows) {
            for (int c = 0; c < inputArray[r].length; c += subCols) {
                int subArray[][] = new int[subRows][subCols];
                fillSubArray(inputArray, r, c, subArray);
                subArrays.add(subArray);
            }
        }
        return subArrays;
    }

    public static void fillSubArray(int[][] inputArray, int r0, int c0, int subArray[][]) {
        for (int r = 0; r < subArray.length; r++) {
            for (int c = 0; c < subArray[r].length; c++) {
                int ir = r0 + r;
                int ic = c0 + c;
                if (ir < inputArray.length && ic < inputArray[ir].length) {
                    subArray[r][c] = inputArray[ir][ic];
                }
            }
        }
    }

    public static int[][] combineTwoArrayX(int[][] arr1, int[][] arr2) {
        int[][] result = new int[arr1.length][arr1[0].length + arr2[0].length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new int[arr1[i].length + arr2[i].length];
            System.arraycopy(arr1[i], 0, result[i], 0, arr1[i].length);
            System.arraycopy(arr2[i], 0, result[i], arr1[i].length, arr2[i].length);
        }
        return result;
    }

    public static int[][] combineTwoArrayY(int[][] arr1, int[][] arr2) {
        int[][] result = new int[arr1.length + arr2.length][arr1[0].length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static int[][] readImage(String path) {
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

    public static void writeImage(int[][] imagePixels, String outPath) {
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

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        prepareGUI();
        JButton compressBtn = new JButton("Compress File");
        JButton decompressBtn = new JButton("DeCompress File");
        compressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\VectorQuantization"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        String width = vectorWidth.getText();
                        String heigh = vectorHeigh.getText();
                        String code = codeBook.getText();
                        compress(filename, Integer.parseInt(width), Integer.parseInt(heigh), Integer.parseInt(code));
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
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\VectorQuantization"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        deCompress(filename);
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
