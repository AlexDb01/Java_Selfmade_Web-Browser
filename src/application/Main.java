package application;

import java.awt.Insets;

import java.io.File;

import java.io.FileWriter;

import javafx.application.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import web.DocumentHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;


	public class Main extends Application {
		WebView webView = new WebView();
		WebEngine webEngine = webView.getEngine();
		
		
		//Klasse für einen Editor für den html code
		private abstract class Editor {
			SimpleStringProperty html;
			public Editor() {
				html =  new SimpleStringProperty("");
			}
			
			protected abstract Node getNode();
			
			SimpleStringProperty getCodeProperty() {
				return html;
			}
		
			void setCodeProperty(String document) {
				html.setValue(document);
			}
			
			void setCode(String document) {
				setCodeProperty(document);
			}
			
			String getCode() {
				return html.getValue();
			}

		}
	 
	//Einen Text Editor für den Browser aus der Klasse HtmlEditor 
		private class TextEditor extends Editor{
			TextArea textArea; 

			public TextEditor() {
				super();
				textArea = new TextArea();
				textArea.textProperty().bindBidirectional(getCodeProperty());
				addListener();
				
			}
			
			
			protected Node getNode() {
				return textArea;
			}
			protected void addListener() {
				getCodeProperty().addListener(new ChangeListener<String>(){
				public void changed(ObservableValue<? extends String> observable, String oldValue,String newValue) {
					webEngine.loadContent(newValue);
				}
				});
				
			}
			
		}
		
	//JavaScriptEditor
	public class JSEditor extends Editor{
		private WebView webView;
		private WebEngine webEngine;
		private JSObject jsObject;
		
		public JSEditor() {
			super();
			
		}
		
		
		
		protected Node getNode() {
			webView = new WebView();
			webEngine = webView.getEngine();
			webEngine.load(getClass().getResource("ace-builds-master/editor.html").toExternalForm());
			return webView;
		}
		
		protected String getCode() {
			setCodeProperty((String)webEngine.executeScript("editor.getValue();"));
			return super.getCode();
		}
		
		protected void setCode(String content) {
			jsObject.call("setContent", content);
		}
		
		protected void addListener() {
			getCodeProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue,String newValue) {
				jsObject.call("setContent",newValue);
			}
			});
			
		}
		
		
	}
	
	//Edit Button
	public class EditButton extends Button{
		private boolean content = false;
		private String textA;
		private String textB;
		
		public EditButton(String textA, String textB) {
			super(textA);
			this.textA=textA;
			this.textB=textB;
		}
		
	public void setOnAction (EventHandler<ActionEvent> eventA,EventHandler<ActionEvent> eventB) {
		super.setOnAction(actionEvent ->{
			content=!content;
			if(content) {
				setText(textB);
				eventA.handle(actionEvent);
				
			}else {
				setText(textA);
				eventB.handle(actionEvent);
			}
		});
	}
	public boolean isActive() {return content;}
	};
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		WebView webView = new WebView();
		WebEngine webEngine = webView.getEngine();
		WebHistory engineHistory = webEngine.getHistory();

		// UI ELEMENTE
		Button back = new Button("<-");
		Button forward = new Button("->");
		Button reload = new Button("\u2B6E");
		TextField adress = new TextField();
		ProgressBar loadBar = new ProgressBar();
		MenuBar menuBar = new MenuBar();
		BorderPane root = new BorderPane();
		EditButton edit = new EditButton("Edit", "Stop Editing");

		adress.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode().equals(KeyCode.ENTER))
				webEngine.load(adress.getText());
		});

		// meine Kontrolleiste
		root.setTop(menuBar);
		HBox control = new HBox(5);
		control.getChildren().addAll(adress, back, forward, reload, loadBar, edit);

		HBox browserContainer = new HBox();
		browserContainer.getChildren().addAll(webView);
		
		VBox contentPane = new VBox(2);
		contentPane.getChildren().addAll(menuBar, control, browserContainer);

		// Menüleiste
		Menu fileMenu = new Menu("File");
		Menu helpMenu = new Menu("Help");
		MenuItem openItem = new MenuItem("Open");
		MenuItem printItem = new MenuItem("Print");
		MenuItem storeItem = new MenuItem("Store");
		MenuItem exitItem = new MenuItem("Exit");
		MenuItem aboutItem = new MenuItem("About");

		fileMenu.getItems().addAll(storeItem, printItem, openItem, exitItem);
		helpMenu.getItems().addAll(aboutItem);
		menuBar.getMenus().addAll(fileMenu, helpMenu);
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

		// When user click on the Exit item.
		exitItem.setOnAction(actionEvent -> {
			System.exit(0);
		});

		// About geklickt Dialog
		aboutItem.setOnAction(actionEvent -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setHeaderText(null);
			alert.setContentText("This Browser was implemented by Dobozanov");

			alert.showAndWait();
		});

		// Drucken
		printItem.setOnAction(actionEvent -> {
			PrinterJob job = PrinterJob.createPrinterJob();
			if (job != null) {
				if (job.showPrintDialog(primaryStage)) {
					webEngine.print(job);
					job.endJob();
				}
			}
		});

		// Open File
		openItem.setOnAction(actionEvent -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Resource File");
			fileChooser.getExtensionFilters().addAll(
					new ExtensionFilter("Text Files", "*.txt", "*.htm", "*.html", "*.pdf"),
					new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				webEngine.load(selectedFile.toURI().toString());
			}
		});

		// Save File
		storeItem.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            try{
            	FileWriter writer = new FileWriter(selectedFile.toString());
            	writer.write((String)webEngine.executeScript("document.documentElement.outerHTML;"));
            	writer.close(); 
            	}
            catch(Exception e) {
				Alert alertHTML = new Alert(AlertType.ERROR);
				alertHTML.setTitle("ERROR SAVING HTML");
				alertHTML.setHeaderText("The File coud not be saved");
				alertHTML.setContentText("Try saving a different File");
				alertHTML.showAndWait();
            	}
			
		});
		
		// edit Button
		
		TextEditor editor = new TextEditor();
		JSEditor jsEditor = new JSEditor();
		
		Button updateButton = new Button("update Browser");
		updateButton.setOnAction(actionEvent->{
			webEngine.loadContent(jsEditor.getCode(), "text/html");
		});
		
		edit.setOnAction(actionEvent->{
			jsEditor.setCode((String)webEngine.executeScript("document.documentElement.outerHTML;"));
			control.getChildren().add(updateButton);
			browserContainer.getChildren().add(jsEditor.getNode());
			adress.setDisable(true);
			
		},
				actionEvent->{
					browserContainer.getChildren().remove(1,2);
					control.getChildren().remove(6);
					adress.setDisable(false);
				}
				);

		// UI ELEMENTE bearbeiten
		control.setHgrow(adress, Priority.ALWAYS);
		back.setMinWidth(30);
		forward.setMinWidth(30);
		reload.setMinWidth(30);

		// Button funktionen
		forward.setOnAction(actionEvent -> {
			JSObject history = (JSObject) webEngine.executeScript("history");
			history.call("forward");
		});

		back.setOnAction(actionEvent -> {
			JSObject history = (JSObject) webEngine.executeScript("history");
			history.call("back");
		});

		reload.setOnAction(actionEvent -> {
			webEngine.reload();
		});
		

		// Lade Balken bearbeitung
		loadBar.progressProperty().bind(webView.getEngine().getLoadWorker().progressProperty());

		loadBar.visibleProperty()
				.bind(Bindings.when(loadBar.progressProperty().lessThan(0).or(loadBar.progressProperty().isEqualTo(1)))
						.then(false).otherwise(true));

		loadBar.managedProperty().bind(loadBar.visibleProperty());

		loadBar.setMaxWidth(Double.MAX_VALUE);

		// Browserleiste nach der webseite bennen und buttons sperren bzw. zulassen
		Worker<Void> loadWorker = webEngine.getLoadWorker();
		loadBar.progressProperty().bind(loadWorker.progressProperty());
		loadWorker.progressProperty().addListener((ObservableValue, oldValue, newValue) -> {

			String url = webEngine.getLocation();
			String pageName = webEngine.getTitle();

			if (loadWorker.getWorkDone() != loadWorker.getTotalWork()) {
				reload.setDisable(true);
			} else if (loadWorker.getWorkDone() == loadWorker.getTotalWork()) {
				primaryStage.setTitle(pageName);
				adress.setText(url);

				int index = engineHistory.getEntries().size();
				int current = engineHistory.getCurrentIndex();

				if (index > 1 && current >= 1)
					back.setDisable(false);
				else
					back.setDisable(true);

				if (current + 1 < index)
					forward.setDisable(false);
				else
					forward.setDisable(true);

				reload.setDisable(false);
			} else {
				primaryStage.setTitle(url + "konnte nicht geladen werden");
			}
		});
		
		


		webEngine.load("https://youtube.com/");

		Scene scene = new Scene(contentPane);
		primaryStage.setScene(scene);

		primaryStage.show();

	}


	
	public static void main(String[] args) {
		launch(args);
	}
	

}
