
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Huffman {

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

    static class Node implements Comparable<Node> {

        char ch;
        int freq;
        Node left;
        Node right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return (left == null) && (right == null);
        }

        @Override
        public int compareTo(Node node) {
            // TODO Auto-generated method stub
            int compare = Integer.compare(this.freq, node.freq);
            if (compare != 0) {
                return compare;
            } else {
                return Integer.compare(this.ch, node.ch);
            }
        }

    }

    public static Node huffmanTree(int[] freq) {
        PriorityQueue<Node> pq = new PriorityQueue<Node>();
        for (char i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                pq.add(new Node(i, freq[i], null, null));
            }
        }
        if (pq.size() == 1) {
            pq.add(new Node('\0', 0, null, null));
        }
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.add(parent);
        }
        return pq.poll();

    }

    public static void table(Node node, String s, Map<Character, String> table) {
        if (!node.isLeaf()) {
            table(node.left, s + '0', table);
            table(node.right, s + '1', table);
        } else {
            table.put(node.ch, s);
        }
    }

    public static void writeTree(Node node, DataOutputStream dos) throws IOException {
        if (node.isLeaf()) {
            dos.writeBoolean(true);
            dos.writeChar(node.ch);
            return;
        }
        dos.writeBoolean(false);
        writeTree(node.left, dos);
        writeTree(node.right, dos);
    }

    public static String repeat(String str, int times) {
        return new String(new char[times]).replace("\0", str);
    }

    public static void compress(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String data = "";
        String line;
        while ((line = br.readLine()) != null) {
            data += line;
        }
        br.close();
        int[] freq = new int[256];
        for (char ch : data.toCharArray()) {
            freq[ch]++;
        }
        Node root = huffmanTree(freq);
        Map<Character, String> tb = new HashMap<>();
        table(root, "", tb);
        System.out.println(tb);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("tree.hf"));
        writeTree(root, dos);
        dos.close();
        String compressedData = "";
        for (int i = 0; i < data.toCharArray().length; i++) {
            char ch = data.toCharArray()[i];
            if (tb.containsKey(ch)) {
                compressedData += tb.get(ch);
            }
        }
        ArrayList<String> bytes = new ArrayList<String>();
        int c = 0;
        int header = 0;
        for (int i = 0; i < compressedData.length(); i += 8) {
            int len = compressedData.substring(i, Math.min(i + 8, compressedData.length())).length();
            if (len < 8) {
                header = 8 - len;
                bytes.add(compressedData.substring(i, Math.min(i + 8, compressedData.length())) + repeat("0", header));
            } else {
                bytes.add(compressedData.substring(i, Math.min(i + 8, compressedData.length())));
            }
            c++;
        }
        byte[] byt = new byte[c];
        int i = 0;
        for (String b : bytes) {
            int decimal = Integer.parseInt(b, 2);
            String hexStr = Integer.toString(decimal, 16);
            byt[i] = (byte) (Integer.parseInt(hexStr, 16));
            i++;
        }
        try {
            FileOutputStream fos = new FileOutputStream("compressed.hf");
            byte[] hea = ByteBuffer.allocate(4).putInt(header).array();
            fos.write(hea);
            fos.write(byt);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String intToBinary2(int i) {
        String binarized = Integer.toBinaryString(i);
        int len = binarized.length();
        String sixteenZeroes = "00000000";
        if (len < 8) {
            binarized = sixteenZeroes.substring(0, 8 - len).concat(binarized);
        } else {
            binarized = binarized.substring(len - 8);
        }
        return binarized;
    }

    public static void decompress(String fileName) throws IOException {
        DataInputStream din = new DataInputStream(new FileInputStream("tree.hf"));
        Node root = readTree(din);
        BufferedWriter wr = new BufferedWriter(new FileWriter("newData.txt", true));
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String compressedData = "";
        String newData = "";
        Map<Character, String> tb = new HashMap<>();
        table(root, "", tb);
        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] head = new byte[4];
            fis.read(head);
            int value = ((head[0] & 0xFF) << 24) | ((head[1] & 0xFF) << 16)
                    | ((head[2] & 0xFF) << 8) | (head[3] & 0xFF);
            byte fileContent[] = new byte[(int) file.length() - 4];
            fis.read(fileContent);
            for (byte b : fileContent) {
                int x = b & 0xff;
                String hex = Integer.toHexString(x);
                int num = Integer.parseInt(hex, 16);
                compressedData += intToBinary2(num);
            }
            compressedData = compressedData.substring(0, compressedData.length() - value);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int curPos = 0;
        String buffer = "";
        while (curPos < compressedData.length()) {
            buffer += compressedData.toCharArray()[curPos];
            if (tb.containsValue(buffer)) {
                newData += getKeyFromValue(tb, buffer);
                buffer = "";
            }
            curPos++;
        }
        wr.write(newData);
        din.close();
        wr.close();
        br.close();
    }

    private static Node readTree(DataInputStream din) throws IOException {
        boolean isLeaf = din.readBoolean();
        if (isLeaf) {
            return new Node(din.readChar(), -1, null, null);
        } else {
            return new Node('\0', -1, readTree(din), readTree(din));
        }
    }

    public static Object getKeyFromValue(Map<Character, String> hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        Huffman.prepareGUI();
        JButton compressBtn = new JButton("Compress File");
        JButton decompressBtn = new JButton("DeCompress File");
        compressBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\Huffman"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        Huffman.compress(filename);
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
                fileChooser.setCurrentDirectory(new File("E:\\eclipse-workspace\\Huffman"));

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getPath();
                    try {
                        Huffman.decompress(filename);
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
