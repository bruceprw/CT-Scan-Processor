/**
 * @author Bruce Williams (972648)
 * All code is my own.
 */

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import sun.swing.plaf.GTKKeybindings;

import java.io.*;

import static java.lang.Integer.max;






public class Example extends Application {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    private int zSliderPos;
    private int ySliderPos;
    private int xSliderPos;
    private int rSliderPos;
    private float nnFactor = 1;
    private String imgPath = "/Users/bruceprw/IdeaProjects/CS-255-Assignment/src/CThead.raw";

    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();


        int width = 256;
        int height = 256;
        WritableImage medical_image = new WritableImage(width, height);
        WritableImage medical_image_top = new WritableImage(width, height);
        WritableImage medical_image_side = new WritableImage(width, height);
        WritableImage medical_image_front = new WritableImage(width, height);

        ImageView imageView = new ImageView(medical_image);
        ImageView sideView = new ImageView(medical_image_side);
        ImageView frontView = new ImageView(medical_image_front);

        Button mip_button_top = new Button("MIP Top"); //an example button to switch to MIP mode
        Button mip_button_side = new Button("MIP Side"); //an example button to switch to MIP mode
        Button mip_button_front = new Button("MIP Front"); //an example button to switch to MIP mode
        Button resizeButton = new Button("Resize(up to 2x)");
        Popup resizeMenu = new Popup();
        //sliders to step through the slices (z and y directions) (remember 113 slices in z direction 0-112)
        Label label = new Label("Select Layer:");
        Slider zslider = new Slider(0, 112, 0);
        Label lz = new Label(" ");

        Button fileSelect = new Button("Select image data");

        Slider yslider = new Slider(0, 255, 0);
        Label ly = new Label();

        Slider xslider = new Slider(0, 255, 0);
        Label lx = new Label();

        Button back = new Button("<- Back");



        GridPane grid = new GridPane();
        Scene scene = new Scene(grid, 960, 540);

        GridPane resizeGrid = new GridPane();
        Scene resizeWindow = new Scene(resizeGrid,960,540);







        mip_button_top.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MIPButtonTop(medical_image);
            }
        });

        mip_button_side.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MIPButtonSide(medical_image_side);
            }
        });

        mip_button_front.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MIPButtonFront(medical_image_front);
            }
        });

        fileSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select image data");
                File file = fileChooser.showOpenDialog(stage);
                imgPath = file.getAbsolutePath();

            }
        });

        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.setScene(scene);

            }
        });

        zslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        zSliderPos = newValue.intValue();
                        lz.setText("Z Layer: " + zSliderPos);
                        System.out.println(zSliderPos);
                        MIPTop(medical_image);
                    }
                });

        yslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        ySliderPos = newValue.intValue();
                        ly.setText("Y Layer: " + ySliderPos);
                        System.out.println(ySliderPos);
                        MIPSide(medical_image_side);
                    }
                });

        xslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        xSliderPos = newValue.intValue();
                        lx.setText("X Layer: " + xSliderPos);
                        System.out.println(xSliderPos);
                        MIPFront(medical_image_front);
                    }
                });






        Label resizeLabel = new Label();

        Label nnInputLabel = new Label("Enter resize factor");
        TextField nnInput = new TextField ();
        HBox hb = new HBox();
        hb.getChildren().addAll(nnInputLabel, nnInput);
        hb.setSpacing(10);

        Slider resizeSlider = new Slider(1,10,1);
        resizeMenu.getContent().add(resizeLabel);
        resizeMenu.getContent().add(resizeSlider);


        resizeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               //stage.setScene(resizeWindow);
               nnFactor = Float.parseFloat(nnInput.getText());
               if (nnFactor > 2) {
                   nnFactor=2;
               }
                Rectangle2D viewRectangle = new Rectangle2D(1,1,width*nnFactor,height*nnFactor);
               WritableImage medical_image_resize = new WritableImage((int) (width*nnFactor), (int) (height*nnFactor));
               ImageView resizeView = new ImageView(medical_image_resize);

               resizeGrid.add(resizeView,1,1);
               imageView.setFitWidth(width*nnFactor);
               //imageView.setViewport(viewRectangle);
               imageView.setFitHeight(height*nnFactor);
               sideView.setFitWidth(width*nnFactor);
               sideView.setFitHeight(height*nnFactor);
               frontView.setFitWidth(width*nnFactor);
               frontView.setFitHeight(height*nnFactor);
               //nearestNeighbour(medical_image_resize,nnFactor);
            }
        });

        String style = "-fx-background-color: rgba(85,178,255,0.8);";

        resizeGrid.add(resizeSlider,1,2);
        resizeGrid.add(back,0,0);
        //resizeGrid.add(resizeSlider, 2, 5);
        resizeGrid.add(resizeLabel, 2, 6);
        resizeGrid.add(nnInputLabel,2,7);
        resizeGrid.add(nnInput,3,7);
        resizeGrid.setStyle(style);

        grid.setVgap(10);
        grid.setHgap(5);
        grid.setStyle(style);

        grid.add(imageView, 0, 0);
        grid.add(sideView, 1, 0);
        grid.add(frontView, 2, 0);
        grid.add(zslider, 0, 1);
        grid.add(lz, 0, 2);
        grid.add(yslider, 1,1);
        grid.add(ly,1,2);
        grid.add(xslider,2,1);
        grid.add(lx,2,2);
        grid.add(mip_button_top,0,3);
        grid.add(mip_button_side,1,3);
        grid.add(mip_button_front,2,3);
        grid.add(fileSelect, 1, 4);
        grid.add(resizeButton, 1, 8);
        grid.add(resizeLabel, 0, 6);
        grid.add(nnInputLabel,0,7);
        grid.add(nnInput,1,7);
       // grid.add(sp,5,0);


        stage.setScene(scene);
        stage.show();
    }

    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
        File file = new File(imgPath);
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min = Short.MAX_VALUE;
        max = Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                for (i = 0; i < 256; i++) {

                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read = (short) ((b2 << 8) | b1); //and swizzle the bytes around **
                    //use this read for CTHead read=(short)((b2<<8) | b1);
                    if (read < min) min = read; //update the minimum
                    if (read > max) max = read; //update the maximum
                    cthead[k][j][i] = read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        System.out.println(min + " " + max); //diagnostic - for CThead this should be -1117, 2248
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
    }


	 /*
        This function shows how to carry out an operation on an image.
        It obtains the dimensions of the image, and then loops through
        the image carrying out the copying of a slice of data into the
		image.
    */

    /**
     * Maximum intensity projection from the top down view.
     * @param image source image.
     */
    public void MIPButtonTop(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        int maximum = 0;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        int datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                maximum=-1117;

                for (k = 0; k < 112; k++) {
                    maximum = max(cthead[k][(int) Math.floor(j/nnFactor)][(int) Math.floor(i/nnFactor)], maximum);
                }
                   datum = maximum;

                    //calculate the colour by performing a mapping from [min,max] -> [0,255]
                    col = (((float) datum - (float) min) / ((float) (max - min)));
                    for (c = 0; c < 3; c++) {
                        //and now we are looping through the bgr components of the pixel
                        //set the colour component c of pixel (i,j)
                        image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                        //					data[c+3*i+3*j*w]=(byte) col;
                    } // colour loop
                } // column loop
            } // row loop
        }


    /**
     * Displays slices from the top view.
     * @param image source image.
     */
    public void MIPTop(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                datum = cthead[zSliderPos][(int) Math.floor(j/nnFactor)][(int) Math.floor(i/nnFactor)];
                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop
    }

    /**
     * MIP from side view.
     * @param image source image.
     */
    public void MIPButtonSide(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        int maximum = 0;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        int datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j = 0; j < 112; j++) {
            for (i = 0; i < w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                maximum=-1117;
                for (k = 0; k < h; k++) {
                    maximum = max(cthead[(int) Math.floor(j/nnFactor)][(int)Math.floor(i/nnFactor)][k], maximum);
                }
                datum = maximum;

                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop
    }

    /**
     * Slices from side view.
     * @param image source image.
     */
    public void MIPSide(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;

        for (j = 0; j < 112; j++) {
            for (i = 0; i < w; i++) {

                datum = cthead[(int)Math.floor(j/nnFactor)][(int)Math.floor(i/nnFactor)][ySliderPos];
                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop

    }

    /**
     * MIP from front view.
     * @param image source image.
     */
    public void MIPButtonFront(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        int maximum = 0;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        int datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j = 0; j < 112; j++) {
            for (i = 0; i < w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                maximum=-1117;
                for (k = 0; k < h; k++) {
                    maximum = max(cthead[(int)Math.floor(j/nnFactor)][k][(int)Math.floor(i/nnFactor)], maximum);
                }
                datum = maximum;

                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop
    }

    /**
     * Slices from front view.
     * @param image source image.
     */
    public void MIPFront(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;

        for (j = 0; j < 113; j++) {
            for (i = 0; i < w; i++) {

                datum = cthead[(int)Math.floor(j/nnFactor)][xSliderPos][(int)Math.floor(i/nnFactor)];
                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop

    }



    /**
     * Carries out nearest neighbour resizing on an image.
     * @param image source image.
     * @param factor factor that the image will be resized by.
     * @return image2 the resized image.
     */
    public WritableImage nearestNeighbour(WritableImage image, float factor) {
        float w1 = (float) image.getWidth();
        float h1 = (float) image.getWidth();
        float w2 = (w1*factor);
        float h2 = (h1*factor);

        WritableImage image2 = new WritableImage((int)w2, (int)h2);
        PixelWriter image_writer = image.getPixelWriter();


        float x;
        float y;
        short datum;
        int i = 0;
        int j = 0;
        float col = 0;
        Color tempCol;




        for (j=0; j<h2; j++) {
            for (i=0; i<w2; i++) {
                y = (j*h1/h2);
                x = (i*w1/w2);
                tempCol = image.getPixelReader().getColor((int)x,(int)y);
                image2.getPixelWriter().setColor(i,j,tempCol);

            }
        }



        return image2;


    }
    public static void main(String[] args) {
        launch();
    }

}