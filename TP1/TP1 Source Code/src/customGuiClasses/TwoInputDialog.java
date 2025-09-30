package customGuiClasses;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;


public class TwoInputDialog extends Application{

	private Dialog<TwoStringResults> dialog = new Dialog<>();
	private TextField tField1 = new TextField();
	private TextField tField2 = new TextField();
	
	
	@Override
	public void start(Stage primaryStage) 
	{
		DialogPane dialogPane = dialog.getDialogPane();
		
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialogPane.setContent(new VBox(10, tField1, tField2));
		dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new TwoStringResults(tField1.getText(), tField2.getText());
            }
            return null;
        });
		
        dialog.setOnShown(e -> tField1.requestFocus());
        dialog.showAndWait().ifPresent(res -> {
            System.out.println("First: " + res.text1);
            System.out.println("Second: " + res.text2);
        });
        
        dialog.showAndWait();
	}
	
	
	public Dialog<TwoStringResults> getDialog() 
	{
		return dialog;
	}
	
	public TextField getTextField1() 
	{
		return tField1;
	}
	
	public TextField getTextField2() 
	{
		return tField2;
	}
	
	private static class TwoStringResults
	{
		String text1;
		String text2;
		
		public TwoStringResults(String t1, String t2) 
		{
			text1 = t1;
			text2 = t2;
		}
	}
	
	public static void main(String[] args) {
	    launch(args);
	}
}
