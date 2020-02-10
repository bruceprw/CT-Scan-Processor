import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class Example extends Application {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    private int zSliderPos;
    private int ySliderPos;
    private int xSliderPos;
    private String imgPath = "/Users/bruceprw/IdeaProjects/CS-255-Assignment/src/CThead.raw";

    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();


        int width = 256;
        int height = 256;
        WritableImage medical_image = new WritableImage(width, height);
        ImageView imageView = new ImageView(medical_image);


        Button mip_button = new Button("MIP"); //an example button to switch to MIP mode
        //sliders to step through the slices (z and y directions) (remember 113 slices in z direction 0-112)
        Label label = new Label("Select Layer:");
        Slider zslider = new Slider(0, 112, 0);
        Label lz = new Label(" ");

        Button fileSelect = new Button("Select image data");

        Slider yslider = new Slider(0, 255, 0);
        Label ly = new Label();

        Slider xslider = new Slider(0, 112, 0);
        Label lx = new Label();


        mip_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MIP(medical_image);
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

        zslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        zSliderPos = newValue.intValue();
                        lz.setText("Z Layer: " + zSliderPos);
                        System.out.println(zSliderPos);
                        MIP(medical_image);
                    }
                });

        yslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        ySliderPos = newValue.intValue();
                        ly.setText("Y Layer: " + ySliderPos);
                        System.out.println(ySliderPos);
                        MIPSide(medical_image);
                    }
                });

        xslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        xSliderPos = newValue.intValue();
                        lx.setText("X Layer: " + xSliderPos);
                        System.out.println(xSliderPos);
                        MIPFront(medical_image);
                    }
                });


        FlowPane root = new FlowPane();
        root.setVgap(10);
        root.setHgap(5);
        String style = "-fx-background-color: rgba(85,178,255,0.8);";
        root.setStyle(style);
        //https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/
        root.setOrientation(Orientation.VERTICAL);
        root.getChildren().addAll(imageView, mip_button, fileSelect, zslider, lz, yslider, ly, xslider, lx);

		Scene scene = new Scene(root, 640, 480);
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


    public void MIP(WritableImage image) {
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
                datum = cthead[zSliderPos][j][i];
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


    public void MIPSide(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;

        for (j = 0; j < 113; j++) {
            for (i = 0; i < w; i++) {

                datum = cthead[j][i][ySliderPos];
                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(j, i, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop

    }


    public void MIPFront(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;

        for (j = 0; j < 113; j++) {
            for (i = 0; i < w; i++) {

                datum = cthead[j][xSliderPos][i];
                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(j, i, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop

    }

    public static void main(String[] args) {
        launch();
    }

}