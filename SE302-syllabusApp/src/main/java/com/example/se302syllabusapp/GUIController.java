package com.example.se302syllabusapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public class GUIController implements Initializable {


    @FXML
    public Button compareButton;
    @FXML
    public BorderPane syllabusParent;
    public Button saveButton;
    @FXML
    public TextArea descriptionValue;
    @FXML
    public Label pathLabel;
    @FXML
    public VBox page1;
    public VBox page2;
    public VBox page3;
    public VBox page4;
    public VBox page5;

    @FXML
    public TextArea versionDescriptions;
    @FXML
    private VBox parentVBox;
    @FXML
    private HBox parentHBox;
    private Stage popup;

    @FXML
    private ComboBox<String> comboBox;


    @FXML
    private CheckBox coreCourse;
    @FXML
    private CheckBox faceToFace;
    @FXML
    private CheckBox shortCycle;
    @FXML
    private CheckBox required;
    @FXML
    private CheckBox english;
    FileManager fileManager;
    // Todo bunlari dosyala
    private Stage primaryStage;
    Controllers controllers;
    VersionController versionController;

    ArrayList<String> syllabusData;
    private int INDEX_FOR_DATA_PASSING;
    private final String[] ContributionLevelValues = {"1","2","3","4","5"};
    private ArrayList<String> ContributionLevelValuesList;

    private TextField textField;

    private  CheckBox checkBox;

    public GUIController(){
        syllabusData = new ArrayList<>(500);
        controllers = new Controllers();
        ContributionLevelValuesList = new ArrayList<>();
        ContributionLevelValuesList.addAll(List.of(ContributionLevelValues));
        INDEX_FOR_DATA_PASSING = 0;
    }



    public void importSyllabus(){

        File storagePath = new File("storage");
        if (!storagePath.exists()) {
            System.out.println("Storage file does not exist!");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose JSON File");
        chooser.setInitialDirectory(storagePath);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        File selectedFile = chooser.showOpenDialog(new Popup());

        SyllabusData syllabusData;

        if (selectedFile != null) {
            setControllers(new Controllers(new SyllabusData(),new SyllabusData()));
            getControllers().setJsonFile(new File(selectedFile.getAbsolutePath()));
            syllabusData = getControllers().read();



            if (syllabusData != null) {

                FXMLLoader syllabusLoader = new FXMLLoader(getClass().getResource("SyllabusSheet.fxml"));
                ArrayList<String> data = syllabusData.getAttributes(new ArrayList<>());

                try {
                    Node syllabusSheet = syllabusLoader.load();

                    // This two line for allocating syllabus data only
                    BorderPane borderPane = (BorderPane) syllabusSheet;
                    ScrollPane scrollPane = (ScrollPane) borderPane.getChildren().get(1);

                    ContributionLevelValuesList = new ArrayList<>();
                    ContributionLevelValuesList.addAll(List.of(ContributionLevelValues));

                    // Passing only JSON
                    passValuesToSyllabusSheet(scrollPane.getContent(),data);

                    Node node1 = borderPane.getRight();
                    ArrayList<String> paths = new ArrayList<>();
                    paths.add(selectedFile.getPath().replace(".json",".txt"));

                    passDescription(node1,paths);

                    try {
                        int startIndex = selectedFile.getPath().indexOf("V");
                        int endIndex = selectedFile.getPath().indexOf("\\" ,startIndex);
                        String version = selectedFile.getPath().substring(startIndex, endIndex);

                        ((Label)((AnchorPane) borderPane.getRight()).getChildren().get(6)).setText(selectedFile.getPath());
                        ((Text)((AnchorPane) borderPane.getRight()).getChildren().get(5)).setText("Description of " + version + ": ");
                        ((CheckBox)((AnchorPane) borderPane.getRight()).getChildren().get(3)).setText("Edit " + version );

                        if (selectedFile.getPath().split("storage\\\\")[1].startsWith("en")) {
                            ((ComboBox)((AnchorPane) borderPane.getRight()).getChildren().get(1)).setValue("English");
                        }
                        else {
                            ((ComboBox)((AnchorPane) borderPane.getRight()).getChildren().get(1)).setValue("Turkish");
                        }
                        ((ComboBox)((AnchorPane) borderPane.getRight()).getChildren().get(1)).setDisable(true);
                    }catch (Exception ignore){

                    }


                    INDEX_FOR_DATA_PASSING = 0;

                    if (!parentVBox.getChildren().isEmpty())
                        parentVBox.getChildren().remove(1);

                    parentVBox.getChildren().add(syllabusSheet);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }



    }

    public void addingSyllabus() {

        FXMLLoader syllabusLoader = new FXMLLoader(getClass().getResource("SyllabusSheet.fxml"));

        try {
            Node syllabusSheet = syllabusLoader.load();

            BorderPane borderPane = (BorderPane) syllabusSheet;

            ScrollPane scrollPane = (ScrollPane) (((BorderPane) syllabusSheet).getChildren().get(1));
            HBox hBox = (HBox) scrollPane.getContent();

            ((CheckBox) hBox.lookup("#english")).setSelected(true);
            ((CheckBox) hBox.lookup("#required")).setSelected(true);
            ((CheckBox) hBox.lookup("#shortCycle")).setSelected(true);
            ((CheckBox) hBox.lookup("#faceToFace")).setSelected(true);
            ((CheckBox) hBox.lookup("#coreCourse")).setSelected(true);

            ((CheckBox)((AnchorPane) borderPane.getRight()).getChildren().get(3)).setDisable(true);
            ((CheckBox)((AnchorPane) borderPane.getRight()).getChildren().get(3)).setText("Edit Version");


            if (!parentVBox.getChildren().isEmpty())
                parentVBox.getChildren().remove(1);

            parentVBox.getChildren().add(syllabusSheet);



        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void compareVersions(VBox parentVBox, String path1, String path2, String v1, String v2) {
        if (popup != null) {
            popup.close();
        }

        SyllabusData syllabusData1;
        setControllers(new Controllers(new SyllabusData(), new SyllabusData()));
        getControllers().setJsonFile(new File(path1));
        syllabusData1 = getControllers().read();
        ArrayList<String> data1 = syllabusData1.getAttributes(new ArrayList<>());

        SyllabusData syllabusData2;
        setControllers(new Controllers(new SyllabusData(), new SyllabusData()));
        getControllers().setJsonFile(new File(path2));
        syllabusData2 = getControllers().read();
        ArrayList<String> data2 = syllabusData2.getAttributes(new ArrayList<>());

        ArrayList<Integer> indexDifferences = new ArrayList<>();

        for (int i = 0; i < data1.size(); i++) {
            if (!data1.get(i).equals(data2.get(i))){
                indexDifferences.add(i);
            }
        }


        FXMLLoader compareLoader = new FXMLLoader(getClass().getResource("ComparePage.fxml"));
        try {
            // version numbers and description will be added
            BorderPane syllabusSheet = compareLoader.load();
            ScrollPane scrollPane = (ScrollPane) syllabusSheet.getChildren().get(1);
            HBox hBox = (HBox) scrollPane.getContent();
            VBox syllabus1 = (VBox) hBox.getChildren().get(0);
            VBox syllabus2 = (VBox) hBox.getChildren().get(1);

            ((Label)((AnchorPane)syllabusSheet.getRight()).getChildren().get(1)).setText("Description of the left side(" + v1 + "):  ");
            ((Label)((AnchorPane)syllabusSheet.getRight()).getChildren().get(3)).setText("Description of the right side(" + v2 + "):  ");

            ContributionLevelValuesList = new ArrayList<>();
            ContributionLevelValuesList.addAll(List.of(ContributionLevelValues));

            ArrayList<String> paths = new ArrayList<>();
            paths.add(path1.replace(".json", ".txt"));
            paths.add(path2.replace(".json", ".txt"));
            Node node1 = syllabusSheet.getRight();

            passValuesToSyllabusSheetCompare(syllabus1, data1, indexDifferences);
            INDEX_FOR_DATA_PASSING = 0;
            passValuesToSyllabusSheetCompare(syllabus2, data2, indexDifferences);
            INDEX_FOR_DATA_PASSING = 0;

            passDescription(node1,paths);

            if (!parentVBox.getChildren().isEmpty())
                parentVBox.getChildren().remove(1);

            parentVBox.getChildren().add(syllabusSheet);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public void compareVersionsPopup(){
        BorderPane borderPane = new BorderPane();

        // Merkez
        ChoiceBox<String> langChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> courseChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> versionChoiceBox1 = new ChoiceBox<>();
        ChoiceBox<String> versionChoiceBox2 = new ChoiceBox<>();

        VBox centerVBox = new VBox();
        AnchorPane centerAnchorPane = new AnchorPane();
        HBox topHBox = new HBox();
        AnchorPane topAnchorPane = new AnchorPane();
        Text courseText = new Text("Language:");

        langChoiceBox.getItems().addAll("en","tr");
        Label languageLabel = new Label();
        langChoiceBox.setOnAction(event -> {
            String selectedOption = langChoiceBox.getValue();
            languageLabel.setText( selectedOption);
            courseChoiceBox.getItems().clear();
            versionChoiceBox1.getItems().clear();
            versionChoiceBox2.getItems().clear();
            try {
                Files.newDirectoryStream(Paths.get("storage/" + selectedOption))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            courseChoiceBox.getItems().add(fileName);
                        });
            } catch (IOException ignore) {

            }
        });

        courseText.setFont(new Font(14.0));
        courseText.setLayoutX(49);
        courseText.setLayoutY(55);
        courseText.setWrappingWidth(64.13671875);

        langChoiceBox.setLayoutX(135.0);
        langChoiceBox.setLayoutY(37.0);
        langChoiceBox.setPrefWidth(150);

        languageLabel.setLayoutX(344);
        languageLabel.setLayoutY(41);
        languageLabel.setPrefWidth(193);
        languageLabel.setPrefHeight(17);


        topAnchorPane.getChildren().addAll(courseText, langChoiceBox, languageLabel);
        topHBox.getChildren().add(topAnchorPane);
        centerAnchorPane.getChildren().add(topHBox);

        VBox vBox2 = new VBox();
        vBox2.setPrefHeight(297);
        vBox2.setPrefWidth(600);
        vBox2.setSpacing(20);

        AnchorPane anchorPane = new AnchorPane();

        HBox hBox2 = new HBox();
        hBox2.setLayoutY(25);
        hBox2.prefHeight(100);
        hBox2.prefWidth(600);

        AnchorPane anchorPane2 = new AnchorPane();
        anchorPane2.prefHeight(100);
        anchorPane2.prefWidth(607);

        Text text1 = new Text("Course: ");
        text1.setFont(new Font(14));
        text1.setLayoutX(51);
        text1.setLayoutY(46);
        text1.setWrappingWidth(88.13671875);

        courseChoiceBox.setLayoutX(135.0);
        courseChoiceBox.setLayoutY(37.0);
        courseChoiceBox.setPrefWidth(150);

        Label courseLabel = new Label();
        courseLabel.setLayoutX(344);
        courseLabel.setLayoutY(42);

        ArrayList<String> paths = new ArrayList<>();

        courseChoiceBox.setOnAction(event -> {
            String selectedOption = courseChoiceBox.getValue();
            courseLabel.setText( selectedOption);
            versionChoiceBox1.getItems().clear();
            versionChoiceBox2.getItems().clear();
            try {
                Files.newDirectoryStream(Paths.get("storage/" + langChoiceBox.getValue() + "/" + selectedOption))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            versionChoiceBox1.getItems().add(fileName);
                        });
            } catch (IOException ignore) {

            }
        });

        anchorPane2.getChildren().setAll(text1,courseChoiceBox,courseLabel);
        hBox2.getChildren().setAll(anchorPane2);

//        2. hBox

        HBox hBox3 = new HBox();
        hBox3.setLayoutY(100);
        hBox3.prefHeight(100);
        hBox3.prefWidth(600);

        AnchorPane anchorPane3 = new AnchorPane();
        anchorPane3.prefHeight(100);
        anchorPane3.prefWidth(607);

        Text text2 = new Text("First chosen version: ");
        text2.setFont(new Font(14));
        text2.setLayoutX(51);
        text2.setLayoutY(46);
        text2.setWrappingWidth(88.13671875);

        versionChoiceBox1.setLayoutX(135.0);
        versionChoiceBox1.setLayoutY(37.0);
        versionChoiceBox1.setPrefWidth(150);

        Label versionLabel1 = new Label();
        versionLabel1.setLayoutX(344);
        versionLabel1.setLayoutY(42);

        versionChoiceBox1.setOnAction(event -> {
            String selectedOption = versionChoiceBox1.getValue();
            versionLabel1.setText( selectedOption);
            versionChoiceBox2.getItems().clear();
            versionChoiceBox2.getItems().addAll(versionChoiceBox1.getItems());
            versionChoiceBox2.getItems().remove(selectedOption);

        });

        anchorPane3.getChildren().setAll(text2,versionChoiceBox1, versionLabel1);
        hBox3.getChildren().setAll(anchorPane3);

        //        3. hBox

        HBox hBox4 = new HBox();
        hBox4.setLayoutY(180);
        hBox4.prefHeight(100);
        hBox4.prefWidth(600);

        AnchorPane anchorPane4 = new AnchorPane();
        anchorPane4.prefHeight(100);
        anchorPane4.prefWidth(607);

        Text text3 = new Text("Second chosen version: ");
        text3.setFont(new Font(14));
        text3.setLayoutX(51);
        text3.setLayoutY(46);
        text3.setWrappingWidth(90.13671875);

        versionChoiceBox2.setLayoutX(135.0);
        versionChoiceBox2.setLayoutY(37.0);
        versionChoiceBox2.setPrefWidth(150);

        Label versionLabel2 = new Label();
        versionLabel2.setLayoutX(344);
        versionLabel2.setLayoutY(42);

        versionChoiceBox2.setOnAction(event -> {
            String selectedOption = versionChoiceBox2.getValue();
            versionLabel2.setText( selectedOption);
        });

        anchorPane4.getChildren().setAll(text3,versionChoiceBox2, versionLabel2);
        hBox4.getChildren().setAll(anchorPane4);

        anchorPane.getChildren().setAll(hBox2,hBox3,hBox4);
        vBox2.getChildren().setAll(anchorPane);

        centerVBox.getChildren().setAll(centerAnchorPane, vBox2);

        // Alt
        AnchorPane bottomAnchorPane = new AnchorPane();
        bottomAnchorPane.setPrefHeight(23);
        bottomAnchorPane.setPrefWidth(600);

        Button compareButton = new Button("Compare");
        compareButton.setLayoutX(511.0);
        compareButton.setLayoutY(-12.0);
        compareButton.setPrefHeight(25);
        compareButton.setPrefWidth(69);

        bottomAnchorPane.getChildren().add(compareButton);
        borderPane.setBottom(bottomAnchorPane);
        borderPane.setCenter(centerVBox);

        // Add your other components here
        compareButton.setOnAction(event -> {
            if (versionChoiceBox2.getValue()== null){
                showAlert("Empty ChoiceBox", "Fill in all ChoiceBoxes!");
            }else {
                String firstFilePath = "storage\\" + langChoiceBox.getValue() + "\\" + courseChoiceBox.getValue() + "\\" + versionChoiceBox1.getValue() + "\\" + courseChoiceBox.getValue() + ".json";
                String secondFilePath = "storage\\" + langChoiceBox.getValue() + "\\" + courseChoiceBox.getValue() + "\\" + versionChoiceBox2.getValue() + "\\" + courseChoiceBox.getValue() + ".json";
                compareVersions(parentVBox, firstFilePath, secondFilePath, versionChoiceBox1.getValue(), versionChoiceBox2.getValue());
                popup.close();
            }
        });

        Scene scene = new Scene(borderPane, 600, 400);
        // Scene
        setPopup(new Stage());
        popup.initOwner(getPrimaryStage());
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Compare Versions");
        popup.setResizable(false);
        popup.setScene(scene);

        popup.showAndWait();
    }

    public void deletePopup(){
        Controllers controllers1 = new Controllers();
        BorderPane borderPane = new BorderPane();

        // Merkez
        ChoiceBox<String> langChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> courseChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> versionChoiceBox = new ChoiceBox<>();

        VBox centerVBox = new VBox();
        AnchorPane centerAnchorPane = new AnchorPane();
        HBox topHBox = new HBox();
        AnchorPane topAnchorPane = new AnchorPane();
        Text courseText = new Text("Language:");

        langChoiceBox.getItems().addAll("en","tr");
        Label languageLabel = new Label();
        langChoiceBox.setOnAction(event -> {
            String selectedOption1 = langChoiceBox.getValue();
            languageLabel.setText( selectedOption1);
            courseChoiceBox.getItems().clear();
            try {
                Files.newDirectoryStream(Paths.get("storage/" + selectedOption1))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            courseChoiceBox.getItems().add(fileName);
                        });
            } catch (IOException ignored) {
            }
        });

        courseText.setFont(new Font(14.0));
        courseText.setLayoutX(49);
        courseText.setLayoutY(55);
        courseText.setWrappingWidth(65.13671875);

        langChoiceBox.setLayoutX(135.0);
        langChoiceBox.setLayoutY(37.0);
        langChoiceBox.setPrefWidth(150);

        languageLabel.setLayoutX(344);
        languageLabel.setLayoutY(41);
        languageLabel.setPrefWidth(193);
        languageLabel.setPrefHeight(17);


        topAnchorPane.getChildren().addAll(courseText, langChoiceBox, languageLabel);
        topHBox.getChildren().add(topAnchorPane);
        centerAnchorPane.getChildren().add(topHBox);

        VBox vBox2 = new VBox();
        vBox2.setPrefHeight(297);
        vBox2.setPrefWidth(600);
        vBox2.setSpacing(20);

        AnchorPane anchorPane = new AnchorPane();

        HBox hBox2 = new HBox();
        hBox2.setLayoutY(25);
        hBox2.prefHeight(100);
        hBox2.prefWidth(600);

        AnchorPane anchorPane2 = new AnchorPane();
        anchorPane2.prefHeight(100);
        anchorPane2.prefWidth(607);

        Text text1 = new Text("Course: ");
        text1.setFont(new Font(14));
        text1.setLayoutX(51);
        text1.setLayoutY(46);
        text1.setWrappingWidth(88.13671875);

        courseChoiceBox.setLayoutX(135.0);
        courseChoiceBox.setLayoutY(37.0);
        courseChoiceBox.setPrefWidth(150);

        Label courseLabel = new Label();
        courseLabel.setLayoutX(344);
        courseLabel.setLayoutY(42);

        courseChoiceBox.setOnAction(event -> {
            String selectedOption2 = courseChoiceBox.getValue();
            courseLabel.setText( selectedOption2);
            versionChoiceBox.getItems().clear();
            try {
                Files.newDirectoryStream(Paths.get("storage/" + langChoiceBox.getValue() + "/" + selectedOption2))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            versionChoiceBox.getItems().add(fileName);
                        });
            } catch (IOException ignore) {

            }
        });

        anchorPane2.getChildren().setAll(text1,courseChoiceBox,courseLabel);
        hBox2.getChildren().setAll(anchorPane2);

//        2. hBox

        HBox hBox3 = new HBox();
        hBox3.setLayoutY(149);
        hBox3.prefHeight(100);
        hBox3.prefWidth(600);

        AnchorPane anchorPane3 = new AnchorPane();
        anchorPane3.prefHeight(100);
        anchorPane3.prefWidth(607);

        Text text2 = new Text("Version: ");
        text2.setFont(new Font(14));
        text2.setLayoutX(51);
        text2.setLayoutY(46);
        text2.setWrappingWidth(88.13671875);

        versionChoiceBox.setLayoutX(135.0);
        versionChoiceBox.setLayoutY(37.0);
        versionChoiceBox.setPrefWidth(150);

        Label versionLabel = new Label();
        versionLabel.setLayoutX(344);
        versionLabel.setLayoutY(42);

        versionChoiceBox.setOnAction(event -> {
            String selectedOption3 = versionChoiceBox.getValue();
            versionLabel.setText( selectedOption3);
        });

        anchorPane3.getChildren().setAll(text2,versionChoiceBox, versionLabel);
        hBox3.getChildren().setAll(anchorPane3);

        anchorPane.getChildren().setAll(hBox2,hBox3);
        vBox2.getChildren().setAll(anchorPane);

        centerVBox.getChildren().setAll(centerAnchorPane, vBox2);

        // Alt
        AnchorPane bottomAnchorPane = new AnchorPane();
        bottomAnchorPane.setPrefHeight(23);
        bottomAnchorPane.setPrefWidth(600);

        Button compareButton = new Button("Delete");
        compareButton.setLayoutX(511.0);
        compareButton.setLayoutY(-12.0);
        compareButton.setPrefHeight(25);
        compareButton.setPrefWidth(69);

        bottomAnchorPane.getChildren().add(compareButton);
        borderPane.setBottom(bottomAnchorPane);
        borderPane.setCenter(centerVBox);

        // Add your other components here
        compareButton.setOnAction(event -> {
            if (versionChoiceBox.getValue() == null){
                showAlert("Empty ChoiceBox", "Fill in all ChoiceBoxes!");
            } else{
                controllers1.delete("storage/" + langChoiceBox.getValue() + "/" +
                        courseChoiceBox.getValue() + "/" + versionChoiceBox.getValue(), courseChoiceBox.getValue());
                popup.close();
            }
        });

        Scene scene = new Scene(borderPane, 600, 400);
        // Scene
        setPopup(new Stage());
        popup.initOwner(getPrimaryStage());
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Delete Version");
        popup.setResizable(false);
        popup.setScene(scene);

        popup.showAndWait();
    }


    public void deleteSyllabus(){
        deletePopup();
    }

    public void exportPopup() {
        BorderPane borderPane = new BorderPane();

        // Merkez
        ChoiceBox<String> langChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> courseChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> versionChoiceBox1 = new ChoiceBox<>();

        VBox centerVBox = new VBox();
        AnchorPane centerAnchorPane = new AnchorPane();
        HBox topHBox = new HBox();
        AnchorPane topAnchorPane = new AnchorPane();
        Text courseText = new Text("Language:");

        langChoiceBox.getItems().addAll("en","tr");
        Label languageLabel = new Label();
        langChoiceBox.setOnAction(event -> {
            String selectedOption = langChoiceBox.getValue();
            languageLabel.setText( selectedOption);
            courseChoiceBox.getItems().clear();
            versionChoiceBox1.getItems().clear();
            try {
                Files.newDirectoryStream(Paths.get("storage/" + selectedOption))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            courseChoiceBox.getItems().add(fileName);
                        });
            } catch (IOException ignore) {

            }
        });

        courseText.setFont(new Font(14.0));
        courseText.setLayoutX(49);
        courseText.setLayoutY(55);
        courseText.setWrappingWidth(64.13671875);

        langChoiceBox.setLayoutX(135.0);
        langChoiceBox.setLayoutY(37.0);
        langChoiceBox.setPrefWidth(150);

        languageLabel.setLayoutX(344);
        languageLabel.setLayoutY(41);
        languageLabel.setPrefWidth(193);
        languageLabel.setPrefHeight(17);


        topAnchorPane.getChildren().addAll(courseText, langChoiceBox, languageLabel);
        topHBox.getChildren().add(topAnchorPane);
        centerAnchorPane.getChildren().add(topHBox);

        VBox vBox2 = new VBox();
        vBox2.setPrefHeight(297);
        vBox2.setPrefWidth(600);
        vBox2.setSpacing(20);

        AnchorPane anchorPane = new AnchorPane();

        HBox hBox2 = new HBox();
        hBox2.setLayoutY(25);
        hBox2.prefHeight(100);
        hBox2.prefWidth(600);

        AnchorPane anchorPane2 = new AnchorPane();
        anchorPane2.prefHeight(100);
        anchorPane2.prefWidth(607);

        Text text1 = new Text("Course: ");
        text1.setFont(new Font(14));
        text1.setLayoutX(51);
        text1.setLayoutY(46);
        text1.setWrappingWidth(88.13671875);

        courseChoiceBox.setLayoutX(135.0);
        courseChoiceBox.setLayoutY(37.0);
        courseChoiceBox.setPrefWidth(150);

        Label courseLabel = new Label();
        courseLabel.setLayoutX(344);
        courseLabel.setLayoutY(42);

        courseChoiceBox.setOnAction(event -> {
            String selectedOption = courseChoiceBox.getValue();
            courseLabel.setText( selectedOption);
            versionChoiceBox1.getItems().clear();
            try {
                Files.newDirectoryStream(Paths.get("storage/" + langChoiceBox.getValue() + "/" + selectedOption))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            versionChoiceBox1.getItems().add(fileName);
                        });
            } catch (IOException ignore) {

            }
        });

        anchorPane2.getChildren().setAll(text1,courseChoiceBox,courseLabel);
        hBox2.getChildren().setAll(anchorPane2);

//        2. hBox

        HBox hBox3 = new HBox();
        hBox3.setLayoutY(100);
        hBox3.prefHeight(100);
        hBox3.prefWidth(600);

        AnchorPane anchorPane3 = new AnchorPane();
        anchorPane3.prefHeight(100);
        anchorPane3.prefWidth(607);

        Text text2 = new Text("Version: ");
        text2.setFont(new Font(14));
        text2.setLayoutX(51);
        text2.setLayoutY(46);
        text2.setWrappingWidth(88.13671875);

        versionChoiceBox1.setLayoutX(135.0);
        versionChoiceBox1.setLayoutY(37.0);
        versionChoiceBox1.setPrefWidth(150);

        Label versionLabel1 = new Label();
        versionLabel1.setLayoutX(344);
        versionLabel1.setLayoutY(42);

        versionChoiceBox1.setOnAction(event -> {
            String selectedOption = versionChoiceBox1.getValue();
            versionLabel1.setText( selectedOption);
        });

        anchorPane3.getChildren().setAll(text2,versionChoiceBox1, versionLabel1);
        hBox3.getChildren().setAll(anchorPane3);

        //        3. hBox

        HBox hBox4 = new HBox();
        hBox4.setLayoutY(180);
        hBox4.prefHeight(100);
        hBox4.prefWidth(600);

        AnchorPane anchorPane4 = new AnchorPane();
        anchorPane4.prefHeight(100);
        anchorPane4.prefWidth(607);

        Text text3 = new Text("File Type: ");
        text3.setFont(new Font(14));
        text3.setLayoutX(51);
        text3.setLayoutY(46);
        text3.setWrappingWidth(90.13671875);

        ChoiceBox<String> typeChoiceBox = new ChoiceBox<>();
        typeChoiceBox.getItems().setAll("json","html","docx");
        typeChoiceBox.setLayoutX(135.0);
        typeChoiceBox.setLayoutY(37.0);
        typeChoiceBox.setPrefWidth(150);

        Label versionLabel2 = new Label();
        versionLabel2.setLayoutX(344);
        versionLabel2.setLayoutY(42);

        typeChoiceBox.setOnAction(event -> {
            String selectedOption = typeChoiceBox.getValue();
            versionLabel2.setText( selectedOption);
        });

        anchorPane4.getChildren().setAll(text3,typeChoiceBox, versionLabel2);
        hBox4.getChildren().setAll(anchorPane4);

        anchorPane.getChildren().setAll(hBox2,hBox3,hBox4);
        vBox2.getChildren().setAll(anchorPane);

        centerVBox.getChildren().setAll(centerAnchorPane, vBox2);


//        centerVBox.getChildren().add(centerAnchorPane);

        // Alt
        AnchorPane bottomAnchorPane = new AnchorPane();
        bottomAnchorPane.setPrefHeight(23);
        bottomAnchorPane.setPrefWidth(600);

        Button exportButton = new Button("Export");
        exportButton.setLayoutX(511.0);
        exportButton.setLayoutY(-12.0);
        exportButton.setPrefHeight(25);
        exportButton.setPrefWidth(69);

        bottomAnchorPane.getChildren().add(exportButton);
        borderPane.setBottom(bottomAnchorPane);
        borderPane.setCenter(centerVBox);

        // Add your other components here
        exportButton.setOnAction(event -> {
            if (versionChoiceBox1.getValue() == null || typeChoiceBox.getValue() == null){
                showAlert("Empty ChoiceBox", "Fill in all ChoiceBoxes!");
            }
            else {
                controllers.fileExport("storage/" + langChoiceBox.getValue() + "/" + courseChoiceBox.getValue() + "/" + versionChoiceBox1.getValue() + "/" + courseChoiceBox.getValue() + ".json",
                        typeChoiceBox.getValue(), langChoiceBox.getValue() + "-" + courseChoiceBox.getValue() + "-" + versionChoiceBox1.getValue());
                popup.close();
            }
        });

        Scene scene = new Scene(borderPane, 600, 400);
        // Scene
        setPopup(new Stage());

        popup.initOwner(getPrimaryStage());
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Export File");
        popup.setResizable(false);
        popup.setScene(scene);

        popup.showAndWait();

    }

    public void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning!");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showFeedback(String header, String content){
        TextInputDialog feedbackDialog = new TextInputDialog();
        feedbackDialog.setTitle("Feedback");
        feedbackDialog.setHeaderText(header);
        feedbackDialog.setContentText(content);

        feedbackDialog.showAndWait();

    }


    public void saveButtonFunctionality(ActionEvent event) {
        syllabusData = new ArrayList<>();

        filterInput(page1);
        filterInput(page2);
        filterInput(page3);
        filterInput(page4);
        filterInput(page5);

        String version = "";

        if (!pathLabel.getText().equals("a")){
            String path = pathLabel.getText();
            int startIndex = path.indexOf("V");
            int endIndex = path.indexOf("\\" ,startIndex);
            version = path.substring(startIndex, endIndex);
        }

        String selectedValue = comboBox.getValue();
        Button saveButton = (Button) event.getSource();
        AnchorPane parent = (AnchorPane) saveButton.getParent();
        CheckBox editLastVersionCheckBox = (CheckBox) parent.getChildren().get(3);
        if (selectedValue.equals("English")) {
            controllers.saveFromUserEntry(syllabusData, "en", editLastVersionCheckBox.isSelected(),descriptionValue.getText(), version);
        }else if (selectedValue.equals("Turkish")){
            controllers.saveFromUserEntry(syllabusData, "tr", editLastVersionCheckBox.isSelected(),descriptionValue.getText(), version);
        }


    }

    public void filterInput(Node node) {


        if (node instanceof TextField) {
            String textValues = ((TextInputControl) node).getText();
            syllabusData.add(textValues);

        } else if (node instanceof CheckBox) {

            String userData = "";
            boolean oneElementIsAlreadySelected = false;
            if (node.getUserData().toString().equals("5") && !((CheckBox) node).isSelected()) {
                HBox grandParent = (HBox) ((AnchorPane) node.getParent()).getParent();

                for (Node parent : grandParent.getChildren()) {
                    AnchorPane anchorPane = (AnchorPane) parent;

                    if (((CheckBox) anchorPane.getChildren().get(0)).isSelected()) {
                        oneElementIsAlreadySelected = true;
                    }
                }
                if (!oneElementIsAlreadySelected) {
                    syllabusData.add(userData);
                }
            }

            if (((CheckBox) node).isSelected()) {
                userData = node.getUserData().toString();
                syllabusData.add(userData);
                // TODO Burada sikinti var
            }


        } else {

            if (node instanceof Parent parent){
                for (Node child : parent.getChildrenUnmodifiable()) {
                    filterInput(child);
                }
            }
        }
    }

    public void passValuesToSyllabusSheet(Node node, ArrayList<String> syllabusData) {

        if (node instanceof TextField) {

            ((TextField) node).setText(syllabusData.get(INDEX_FOR_DATA_PASSING));
            INDEX_FOR_DATA_PASSING++;

        } else if (node instanceof CheckBox) {
            boolean oneElementIsAlreadySelected = false;
            if (ContributionLevelValuesList.contains(node.getUserData().toString())) {
                HBox grandParent = (HBox) ((AnchorPane) node.getParent()).getParent();

                for (Node parent : grandParent.getChildren()) {
                    AnchorPane anchorPane = (AnchorPane) parent;

                    if (((CheckBox) anchorPane.getChildren().get(0)).isSelected()) {
                        oneElementIsAlreadySelected = true;
                        break;
                    }
                }
                if (!oneElementIsAlreadySelected && syllabusData.get(INDEX_FOR_DATA_PASSING).equals(node.getUserData())) {
                    ((CheckBox) node).setSelected(true);
                    INDEX_FOR_DATA_PASSING++;
                    return;
                }
                if (node.getUserData().toString().equals("5") && !oneElementIsAlreadySelected) {
                    INDEX_FOR_DATA_PASSING++;
                    return;
                }
            }
            if (syllabusData.get(INDEX_FOR_DATA_PASSING).equals(node.getUserData()) && !ContributionLevelValuesList.contains(node.getUserData().toString())) {
                ((CheckBox) node).setSelected(true);
                INDEX_FOR_DATA_PASSING++;
            }
        }
        else {
            if (node instanceof Parent) {
                for (Node child: ((Parent) node).getChildrenUnmodifiable()) {
                    passValuesToSyllabusSheet(child, syllabusData);
                }
            }
        }
    }


    public void passValuesToSyllabusSheetCompare(Node node, ArrayList<String> syllabusData, ArrayList<Integer> differences) {

        boolean changeBoolean = true;

        if (node instanceof TextField) {
            textField = (TextField) node;

            if (differences.contains(INDEX_FOR_DATA_PASSING)) {
                textField.setStyle("-fx-text-fill: red;-fx-font-weight: bold;");
                textField.setText(syllabusData.get(INDEX_FOR_DATA_PASSING));


            }else{
                textField.setStyle("-fx-text-fill: green;-fx-font-weight: bold;");
                textField.setText(syllabusData.get(INDEX_FOR_DATA_PASSING));

            }
            INDEX_FOR_DATA_PASSING++;
            textField.setEditable(false);

        } else if (node instanceof CheckBox) {
            checkBox = ((CheckBox) node);
            checkBox.setDisable(true);
            checkBox.setStyle("-fx-opacity: 1; -fx-text-fill: black;");
            boolean oneElementIsAlreadySelected = false;
            if (ContributionLevelValuesList.contains(node.getUserData().toString())) {
                HBox grandParent = (HBox) ((AnchorPane) node.getParent()).getParent();

                for (Node parent : grandParent.getChildren()) {
                    AnchorPane anchorPane = (AnchorPane) parent;

                    if (((CheckBox) anchorPane.getChildren().get(0)).isSelected()) {
                        oneElementIsAlreadySelected = true;
                        break;
                    }
                }
                if (!oneElementIsAlreadySelected && syllabusData.get(INDEX_FOR_DATA_PASSING).equals(node.getUserData())) {
                    if (differences.contains(INDEX_FOR_DATA_PASSING)) {
                        checkBox.getParent().setStyle("-fx-background-color: rgba(245, 7, 7, 0.25);");

                        checkBox.setSelected(true);



                        INDEX_FOR_DATA_PASSING++;
                        return;
                    }else{
                        checkBox.getParent().setStyle("-fx-background-color: rgba(7, 245, 7, 0.25);");
                        checkBox.setSelected(true);
                        INDEX_FOR_DATA_PASSING++;
                    }
                }

                if (node.getUserData().toString().equals("5") && !oneElementIsAlreadySelected) {
                    INDEX_FOR_DATA_PASSING++;
                    return;
                }
            }

            if (syllabusData.get(INDEX_FOR_DATA_PASSING).equals(node.getUserData()) && !ContributionLevelValuesList.contains(node.getUserData().toString())) {
                if (differences.contains(INDEX_FOR_DATA_PASSING)) {
                    checkBox.getParent().setStyle("-fx-background-color: rgba(245, 7, 7, 0.25);");
                    checkBox.setSelected(true);
                    INDEX_FOR_DATA_PASSING++;

                }else{
                    checkBox.getParent().setStyle("-fx-background-color: rgba(7, 245, 7, 0.25);");
                    checkBox.setSelected(true);
                    INDEX_FOR_DATA_PASSING++;
                }
            }
        }
        else {
            if (node instanceof Parent) {
                for (Node child: ((Parent) node).getChildrenUnmodifiable()) {
                    passValuesToSyllabusSheetCompare(child, syllabusData, differences);
                }
            }
        }
    }

    private void passDescription(Node node, ArrayList<String> paths) {

        int index = 0;
        for (Node child : ((Parent) node).getChildrenUnmodifiable()){
            if (child instanceof TextArea){
                try (BufferedReader br = new BufferedReader(new FileReader(paths.get(index)))) {
                    String line = br.readLine();
                    ((TextArea) child).setText(line);
                    index++;
                } catch (IOException ignore) {

                }
            }
        }
    }

    public void onCheckBoxClicked(ActionEvent event) {

        Parent parent = ((AnchorPane) ((CheckBox) event.getSource()).getParent()).getParent();

        if (parent instanceof VBox || parent instanceof HBox || parent instanceof BorderPane) {
            Pane parentPane = (Pane) parent;

            for (Node child : parentPane.getChildren()) {
                if (child instanceof AnchorPane) {
                    for (Node checkBox : ((AnchorPane) child).getChildren()) {
                        if (checkBox instanceof CheckBox) {
                            if (ContributionLevelValuesList.contains(checkBox.getUserData().toString())) {
                                for (Node loop :  ((AnchorPane) child).getChildren()) {
                                    if (loop instanceof CheckBox) {
                                        if (event.getSource() != loop && ((CheckBox) loop).isSelected()) {
                                            ((CheckBox) loop).setSelected(false);
                                        }
                                    }
                                }
                            }
                            else ((CheckBox) checkBox).setSelected(event.getSource() == checkBox);
                        }
                    }
                }
            }
        }
    }
    public void showHelpPopUp() {
        Alert guideAlert = new Alert(Alert.AlertType.INFORMATION);
        guideAlert.getDialogPane().setPrefWidth(600);
        guideAlert.getDialogPane().setPrefHeight(700);
        guideAlert.setTitle("Guide");
        guideAlert.setHeaderText("HOW TO USE SYLLABUS APP");
        guideAlert.setContentText("-OPEN & IMPORT-\n1. To open or import a syllabus file, click on 'Open' in the File Menu.\n2. Direct to the desired JSON File from the File chooser that opens and click on Open.\n3. The desired syllabus can be viewed on the screen. On right, the description of the syllabus is noted." +
                "\n\n-CREATE SYLLABUS & ADD VERSION-\n1. Click on 'New' in the File Menu.\n2. Fill out the syllabus information from the template and add a description.\n3. Select a language (to indicate whether Turkish or English version of the syllabus) from the dropdown menu.\n4. Choose 'Select As New Version' (the default option). If there exists a folder with the same course code it will be added as a new version. Otherwise, it will be added as new syllabus for the entered course code." +
                "\n\n-EDIT SYLLABUS-\n1. After opening a syllabus as stated above, make the necessary changes.\n2. Click on 'Edit Version' on the right panel and click on 'Save' button." +
                "\n\n-COMPARE SYLLABUS VERSIONS-\n1. To compare two versions, click on 'Compare' in the File Menu.\n2. From the pop up that opens, select the language, course name and the versions you want to compare from the dropdown menus.\n3. Click on 'Compare' button. On the screen, both selected versions will be displayed. On the right, the descriptions included while creating syllabi can be checked." +
                "\n\n-DELETE SYLLABUS-\n1. Click on 'Delete' in the File Menu.\n2. Choose language, course code and the version you want to delete and click on 'Delete'." +
                "\n\n-EXPORT SYLLABUS-\n1. To export a syllabus, click on 'Export' in the File Menu.\n2. Select the language, course code, version from the dropdown menu.\n3. Select the file type you want to export as and click on export.\n4. Navigate to the directory you desire to save, and click on 'Save'.");
        guideAlert.showAndWait();
    }
    public void showAboutPopUp() {
        Alert guideAlert = new Alert(Alert.AlertType.INFORMATION);
        guideAlert.getDialogPane().setPrefWidth(600);
        guideAlert.getDialogPane().setPrefHeight(700);
        guideAlert.setTitle("About");
        guideAlert.setHeaderText("Software Development Team");
        guideAlert.setContentText("- Ali Boztepe\n- Beyza Altuner\n- Ege Deniz Yasar\n- Harun Onur\n- Nihan Yuksel" +
                "\n\nThis application is development in the scope of SE 302 - Principles of Software Engineering as the  course project.");
        guideAlert.showAndWait();
    }





    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
    // Todo bunlari dosyala
    public Stage getPopup() {
        return popup;
    }

    public void setPopup(Stage popup) {
        this.popup = popup;
    }

    public Controllers getControllers() {
        return controllers;
    }

    public void setControllers(Controllers controllers) {
        this.controllers = controllers;
    }

    public VersionController getVersionController() {
        return versionController;
    }

    public void setVersionController(VersionController versionController) {
        this.versionController = versionController;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
