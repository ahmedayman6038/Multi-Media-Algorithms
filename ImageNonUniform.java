package imageNonUniform;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ImageClass {

    static ArrayList<Integer> averageLIST = new ArrayList<Integer>();
    static ArrayList<Integer> pixelsIMG = new ArrayList<Integer>();
    static int globalHeight, globalWidth;
    static int[][] table;

    public static void main(String[] args) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("E:\\"));
        // fileChooser.setSelectedFile(new File("README.html"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();

            int[][] pixels = ImageClass.readImage(filename);

            //FileWriter fw = new FileWriter("image2.txt");
            //ImageClass.writeImage(pixels, "E:\\5.png");
            //table = new int [globalHeight][globalWidth];
            for (int i = 0; i < globalHeight; i++) {
                for (int j = 0; j < globalWidth; j++) {
                    //  System.out.print(pixels[i][j]+"   ");
                    pixelsIMG.add(pixels[i][j]);
                }
                //System.out.println();
            }
            compress("m.txt");
            decompression("uncompressed.txt");

            /*for(int i=0,x=0;i<globalWidth;i++){
         	  for (int j = 0; j < globalHeight; j++,x++) {
         		  fw.write(Integer.toString(averageLIST.get(x)));  
         	  }
           }*/
            JOptionPane.showMessageDialog(null, "Your File " + filename + "\n Compressed is Successfully\n", "Message", JOptionPane.INFORMATION_MESSAGE);
        } else if (result == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(null, "You selected nothing.");
        } else if (result == JFileChooser.ERROR_OPTION) {
            JOptionPane.showMessageDialog(null, "An error occurred.");
        }////////////////////////////////////////////
        ///abca///

    }

    public static void creatCode(ArrayList<Integer> data, int level) {
        double average = 0;
        int sum = 0;
        int A = 0;

        for (int i = 0; i < data.size(); i++) {
            sum += data.get(i);
        }
        average = (double) sum / (double) data.size();
        A = (int) average;
        if (level == 0) {
            //System.out.println(average);
            averageLIST.add((int) average);
            return;
        } else if (average - A == 0) {
            averageLIST.add((int) average);

            return;
        } else {
            ArrayList<Integer> subarr1 = new ArrayList<Integer>();
            ArrayList<Integer> subarr2 = new ArrayList<Integer>();
            int s1 = A;
            int s2 = A + 1;
            for (int i = 0; i < data.size(); i++) {
                if (Math.abs(data.get(i) - s1) < Math.abs(data.get(i) - s2)) {///////////Math.abs(-5)
                    subarr1.add((int) data.get(i));
                    //System.out.print(">>"+data.get(i));
                } else {
                    subarr2.add((int) data.get(i));
                    //System.out.print(">"+data.get(i));
                }

            }
            creatCode(subarr1, level - 1);
            creatCode(subarr2, level - 1);

        }

    }

    public static void compress(String filename) throws IOException {
        FileWriter wr = new FileWriter("uncompressed.txt");
        ArrayList<Integer> arr = new ArrayList<Integer>();

        arr = pixelsIMG;
        /*arr.add(6);//{6, 15, 17, 60, 100, 90, 66, 59, 18, 3, 5, 16, 14,67, 63, 2, 98, 92};
		arr.add(15);
		arr.add(17);
		arr.add(60);
		arr.add(100);
		arr.add(90);
		arr.add(66);
		arr.add(59);
		arr.add(18);
		arr.add(3);
		arr.add(5);
		arr.add(16);
		arr.add(14);
		arr.add(67);
		arr.add(63);
		arr.add(2);
		arr.add(98);
		arr.add(92);
         */
        creatCode(arr, 2);// ��� ���� ��� ������� �� ����� ������� ������ �� �������� �� 

        ArrayList<Integer> avg_arr = new ArrayList<Integer>();
        for (int i = 0; i < averageLIST.size() - 1; i++) {
            int a = (averageLIST.get(i) + averageLIST.get(i + 1)) / 2;
            avg_arr.add(a);
        }
        avg_arr.add(255);
        System.out.println(averageLIST.size());
        ArrayList<Integer> data_compress = new ArrayList<Integer>();
        for (int i = 0; i < arr.size(); i++) {
            for (int j = 0; j < avg_arr.size(); j++) {
                if (arr.get(i) <= avg_arr.get(j)) {
                    data_compress.add(j);

                    break;
                }
            }
            //System.out.println(Integer.toBinaryString(i));
        }
        for (int i = 0; i < averageLIST.size(); i++) {
            //System.out.println(avg_arr.get(i));
            wr.write(averageLIST.get(i) + "_");
        }
        wr.write("|");
        for (int i = 0; i < data_compress.size(); i++) {
            wr.write(Integer.toBinaryString(data_compress.get(i)) + "_");
        }
        wr.close();

    }

    public static int[][] readImage(String path) {

        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));

            int hieght = img.getHeight();
            int width = img.getWidth();
            globalHeight = hieght;
            globalWidth = width;

            int[][] imagePixels = new int[hieght][width];
            for (int x = 0; x < hieght; x++) {
                for (int y = 0; y < width; y++) {

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

    public static void decompression(String filename) throws IOException {
        String line = "";
        String data = "";
        ArrayList<Integer> De_Qoantization = new ArrayList<Integer>();
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                data += line;
            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + "'");
        }
        String[] DataSplited = data.split("\\|");
        String[] m = DataSplited[0].split("_");
        String[] i = DataSplited[1].split("_");

        ArrayList<Integer> map = new ArrayList<Integer>();
        ArrayList<Integer> img_data = new ArrayList<Integer>();
        for (int j = 0; j < m.length; j++) {
            map.add(Integer.parseInt(m[j]));
        }
        for (int j = 0; j < i.length; j++) {
            img_data.add(Integer.parseInt(i[j], 2));
            //System.out.println(Integer.parseInt(i[j],2));
        }
        for (int x = 0; x < img_data.size(); x++) {
            De_Qoantization.add(map.get(img_data.get(x)));
            //System.out.println(img_data.size());
        }

        int[][] pixels2 = new int[globalHeight][globalWidth];
        int k = 0;
        for (int z = 0; z < globalHeight; z++) {
            for (int j = 0; j < globalWidth && k < De_Qoantization.size(); j++) {
                System.out.println(k);
                pixels2[z][j] = De_Qoantization.get(k);
                k++;
            }
        }
        ImageClass.writeImage(pixels2, "F:\\5.png");

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

}
