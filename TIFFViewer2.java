import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class member { //used to return both the frame and a bool in last operation
    public JFrame frame;
    public boolean flag;

    public member() {
        frame = new JFrame();
        flag = false;
    }

    public member(JFrame the_frame, boolean the_flag) {
        frame = the_frame;
        flag = the_flag;
    }
}
//this code works on ubuntu linux
//I do not know if it will work on other distros or os
//probably will crash on windows
//selecting a file that isn't tiff will crash the program
public class TIFFViewer2 {
    //the following is used for frames1, 2, and 3
    static JFrame frame_operation(BufferedImage image1, BufferedImage image2, int height, int width) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width*2+80, height+160);

        //create buttons
        JButton nextButton = new JButton("Next");
        JButton cancel = new JButton("Exit");

        CountDownLatch nextLatch = new CountDownLatch(1);
        CountDownLatch cancelLatch = new CountDownLatch(1);
        // Add an ActionListener to the "Next" button
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextLatch.countDown();
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e2) {
                cancelLatch.countDown();
                frame.dispose();
                System.exit(0);
            }
        });

        // Add the "Next" button to the frame
        frame.add(nextButton, BorderLayout.NORTH);
        frame.getContentPane().add(new JLabel(new ImageIcon(image1)), BorderLayout.WEST);
        frame.getContentPane().add(new JLabel(new ImageIcon(image2)), BorderLayout.EAST);
        frame.add(cancel, BorderLayout.SOUTH);

        frame.setVisible(true);        
        try {
            nextLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        return frame;
    }

    static member last_operation( //return member instead of frame so that bool may be returned
        BufferedImage image1, BufferedImage image2, int height, int width
    ) {

        boolean flag = false; //this tells the program whether to return to frame1 or
        //move on to the next image
        JFrame frame = new JFrame();
        member ret = new member(frame, flag);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width*2+80, height+160);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        //create buttons
        JButton nextButton = new JButton("Next image");
        JButton cancel = new JButton("Exit");
        JButton returnButton = new JButton("Return to step 1");

        //this ensures that the buttons do not overlap
        panel.add(nextButton);
        panel.add(returnButton);

        CountDownLatch nextLatch = new CountDownLatch(1);
        CountDownLatch cancelLatch = new CountDownLatch(1);
        CountDownLatch returnLatch = new CountDownLatch(1);

        returnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e3) {
                returnLatch.countDown();
                nextLatch.countDown();
                ret.flag = true;
            }
        });

        // Add an ActionListener to the "Next" button
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextLatch.countDown();
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e2) {
                cancelLatch.countDown();
                frame.dispose();
                System.exit(0);
            }
        });

        // Add the "Next" button to the frame
        frame.add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(new JLabel(new ImageIcon(image1)), BorderLayout.WEST);
        frame.getContentPane().add(new JLabel(new ImageIcon(image2)), BorderLayout.EAST);
        frame.add(cancel, BorderLayout.SOUTH);

        frame.setVisible(true);
        
        try {
            nextLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        return ret;
    }

    public static void main(String[] args) {
        try {
            boolean flag = true; //used to decide when to quit the program
            //initialize before starting the loop
            JFrame frame1 = null; //original image and greyscale
            JFrame frame2 = null; //reduced brightness image
            JFrame frame3 = null; //ordered dithered image
            JFrame frame4 = null; //for the auto level image
            JFrame frame5 = null; //for the interlaced image
            JFrame frame6 = null; //for the auto and dark interlaced image
            JFrame frame7 = null; //for the increased brighness image
            member continue_ = new member(); 
            continue_.flag = true;
            while (flag) { //keep displaying open file box until user quits
                // Display a file chooser dialog to select a file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose an image file");
                int result = fileChooser.showOpenDialog(null);

                // Check if a file was selected
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    // Load the TIFF image using the file path
                    //this is the original image
                    BufferedImage og_image = ImageIO.read(selectedFile);
                    //this will be the new greyscale image
                    int width = og_image.getWidth();
                    int height = og_image.getHeight();
                    BufferedImage modified = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    BufferedImage reduc_bright = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    BufferedImage reduc_bright_grey = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    BufferedImage interlace = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    BufferedImage auto_inter = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );
                    
                    BufferedImage dark_auto = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    BufferedImage incr_bright = new BufferedImage(
                        width, 
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );
                    
                    //this converts the orginal image into greyscale
                    //even though Java was developed by a Canadian, everything here is spelled the American way
                    //examples are color instead of colour and gray instead of grey
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            Color originalColor = new Color(og_image.getRGB(x, y));
                            int grayValue = (int) (0.299 * originalColor.getRed()
                                    + 0.587 * originalColor.getGreen()
                                    + 0.114 * originalColor.getBlue());
                            Color grey_col = new Color(grayValue, grayValue, grayValue);
                            modified.setRGB(x, y, grey_col.getRGB());
                        }
                    }

                    //create darker images
                    for (int x=0; x < width; x++) {
                        for (int y=0; y<height; y++) {
                            Color originalColor = new Color(og_image.getRGB(x, y));
                            int darker_r = (int) (originalColor.getRed() / 2);
                            int darker_g = (int) (originalColor.getGreen() / 2);
                            int darker_b = (int) (originalColor.getBlue() / 2);
                            Color new_col = new Color(darker_r, darker_g, darker_b);
                            reduc_bright.setRGB(x, y, new_col.getRGB());

                            Color originalgrey = new Color(modified.getRGB(x, y));
                            int darker_gr = (int) (originalgrey.getRed() / 2);
                            int darker_gg = (int) (originalgrey.getGreen() / 2);
                            int darker_gb = (int) (originalgrey.getBlue() / 2);
                            Color new_grey = new Color(darker_gr, darker_gg, darker_gb);
                            reduc_bright_grey.setRGB(x, y,new_grey.getRGB());
                        }
                    }

                    BufferedImage ord_dith = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    //create 4x4 Bayer matrix
                    int array [][] = new int[4][4];
                    array[0][0] = 0;
                    array[0][1] = 8;
                    array[0][2] = 2;
                    array[0][3] = 10;
                    array[1][0] = 12;
                    array[1][1] = 4;
                    array[1][2] = 14;
                    array[1][3] = 6;
                    array[2][0] = 3;
                    array[2][1] = 11;
                    array[2][2] = 1;
                    array[2][3] = 9;
                    array[3][0] = 15;
                    array[3][1] = 7;
                    array[3][2] = 13;
                    array[3][3] = 5;

                    //create ordered dithering image
                    for (int x=0; x < width; x++) {
                        for (int y=0; y<height; y++) {
                            int i = x % 4;
                            int j = y % 4;
                            Color originalColor = new Color(og_image.getRGB(x, y));
                            int greyValue = (int) (0.299 * originalColor.getRed()
                                    + 0.587 * originalColor.getGreen()
                                    + 0.114 * originalColor.getBlue());
                            //divide by 15 to normalize the values
                            if (greyValue / 15 > array[i][j]) { //print a dot when darker
                                Color black = new Color(255, 255, 255);
                                ord_dith.setRGB(x, y, black.getRGB());
                            }
                            else { //do not print a dot
                                Color white = new Color(0, 0, 0);
                                ord_dith.setRGB(x, y, white.getRGB());
                            }
                        }
                    }
                    

                    BufferedImage auto_lvl = new BufferedImage(
                        width, 
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    //idea: find the max and min values
                    int min_r = 255;
                    int min_g = 255;
                    int min_b = 255;
                    int max_r = 0;
                    int max_g = 0;
                    int max_b = 0;
                    for (int x=0; x < width; x++) {
                        for (int y=0; y<height; y++) {
                            Color originalColor = new Color(og_image.getRGB(x, y));
                            int red = originalColor.getRed();
                            int green = originalColor.getGreen();
                            int blue = originalColor.getBlue();
                            if (red < min_r) min_r = red;
                            if (green < min_g) min_g = green;
                            if (blue < min_b) min_b = blue;
                            if (red > max_r) max_r = red;
                            if (green > max_g) max_g = green;
                            if (blue > max_b) max_b = blue;
                        }
                    }

                    int red_factor = 255 / (max_r - min_r);
                    int green_factor = 255 / (max_g - min_g);
                    int blue_factor = 255 / (max_b - min_b);

                    double factor = 1.4; //add some saturation so the image is more different
                    //create auto level image
                    for (int x=0; x < width; x++) {
                        for (int y=0; y<height; y++) {      
                            Color originalColor = new Color(og_image.getRGB(x, y));
                            int og_red = originalColor.getRed();
                            int og_green = originalColor.getGreen();
                            int og_blue = originalColor.getBlue();
                            int avg_col = (int) ((og_red + og_green + og_blue) / 3);
                            int new_red = (int) (avg_col + factor * ((og_red - min_r) * red_factor - avg_col));
                            int new_green = (int) (avg_col + factor * ((og_green - min_g) * green_factor - avg_col));
                            int new_blue = (int) (avg_col + factor * ((og_blue - min_b) * blue_factor - avg_col));

                            if (new_red > 255) new_red = 255;
                            if (new_green > 255) new_green = 255;
                            if (new_blue > 255) new_blue = 255;
                            if (new_red < 0) new_red = 0;
                            if (new_green < 0) new_green = 0;
                            if (new_blue < 0) new_blue = 0;

                            Color new_col = new Color(new_red, new_green, new_blue);
                            auto_lvl.setRGB(x, y, new_col.getRGB());
                        }
                    }
                    
                    //create interlaced image. nostalgic
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (x % 2 == 0) {
                                Color black = new Color(0, 0, 0);
                                interlace.setRGB(x, y, black.getRGB());
                            }
                            else {
                                Color originalColor = new Color(og_image.getRGB(x, y));
                                interlace.setRGB(x, y, originalColor.getRGB());
                            }
                        }
                    }

                    //auto level interlaced image
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (x % 2 == 0) {
                                Color black = new Color(0, 0, 0);
                                auto_inter.setRGB(x, y, black.getRGB());
                            }
                            else {
                                Color originalColor = new Color(auto_lvl.getRGB(x, y));
                                auto_inter.setRGB(x, y, originalColor.getRGB());
                            }
                        }
                    }
                    
                    //image interlaced with auto level and darker image
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (x % 2 == 0) {
                                Color darker = new Color(reduc_bright.getRGB(x, y));
                                dark_auto.setRGB(x, y, darker.getRGB());
                            }
                            else {
                                Color originalColor = new Color(auto_lvl.getRGB(x, y));
                                dark_auto.setRGB(x, y, originalColor.getRGB());                                
                            }
                        }
                    }

                    for (int x=0; x < width; x++) {
                        for (int y=0; y<height; y++) {
                            Color originalColor = new Color(og_image.getRGB(x, y));
                            int brighter_r = (int) (originalColor.getRed() * 2);
                            int brighter_g = (int) (originalColor.getGreen() * 2);
                            int brighter_b = (int) (originalColor.getBlue() * 2);
                            if (brighter_r > 255) brighter_r = 255;
                            if (brighter_g > 255) brighter_g = 255;
                            if (brighter_b > 255) brighter_b = 255;
                            Color new_col = new Color(brighter_r, brighter_g, brighter_b);
                            incr_bright.setRGB(x, y, new_col.getRGB());
                        }
                    }

                    continue_.flag = true;
                    while (continue_.flag) {
                        frame1 = frame_operation(og_image, modified, height, width);
                        if (frame1 == null) {
                            return;
                        }
                        frame1.dispose();
                        frame2 = frame_operation(reduc_bright, reduc_bright_grey, height, width);
                        if (frame2 == null) return;
                        frame2.dispose();
                        frame3 = frame_operation(modified, ord_dith, height, width);
                        if (frame3 == null) return;
                        frame3.dispose();
                        frame4 = frame_operation(og_image, auto_lvl, height, width);
                        if (frame4 == null) return;
                        frame4.dispose();
                        frame5 = frame_operation(og_image, interlace, height, width);
                        if (frame5 == null) return;
                        frame5.dispose();
                        frame6 = frame_operation(auto_lvl, auto_inter, height, width);
                        frame6.dispose();
                        frame7 = frame_operation(reduc_bright, dark_auto, height, width);
                        frame7.dispose();
                        continue_ = last_operation(og_image, incr_bright, height, width);
                        continue_.frame.dispose();
                    }
                }
                else { //a file was not selected
                    System.out.println("No file selected");
                    flag = false; //break out of loop
                }
            }
        } 
        catch (Exception e) { 
            e.printStackTrace(); //throw if there is a problem
        }
    }
}