package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import server.Request;
import server.RequestType;
import tables.ExaminationCard;
import tables.User;
import util.Connection;

public class ClientViewController implements Initializable {

	@FXML
	private Button orderTermButton;

	@FXML
	private Button startExamButton;

	@FXML
	private Button browseExamsButton;
	
	private User user;

	public void initUser(User user) {
		this.user = user;
	}

	@FXML
	void orderTermButtonClicked(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("OrderTermView.fxml"));
		Parent orderTermView = loader.load();
		Scene orderTermScene = new Scene(orderTermView);

		OrderTermController orderTermController = loader.getController();
		orderTermController.initUser(user);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(orderTermScene);
		window.show();
	}
	
	@FXML
	void browseExamsButtonClicked(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("BrowsingExamsView.fxml"));
		Parent browsingExamsView = loader.load();
		Scene browsingExamsScene = new Scene(browsingExamsView);

		BrowsingExamsController browsingExamsController = loader.getController();
		browsingExamsController.initUser(user);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(browsingExamsScene);
		window.show();
	}

	@FXML
	void startExamButtonClicked(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("TheoreticalExam.fxml"));
		Parent theoreticalExamView = loader.load();
		Scene theoreticalExamScene = new Scene(theoreticalExamView);
		
		TheoreticalExamController theoreticalExamController = loader.getController();
		theoreticalExamController.initUser(user);
		
		try {
			Connection.request = new Request();
			Connection.request.setType(RequestType.START_THEORETICAL_EXAM);
			Connection.request.setUser(user);
			Connection.getOutput().writeObject(Connection.request);
			Request received = new Request();
			received = (Request) Connection.getInput().readObject();
			if (received.getResult()) {
				theoreticalExamController.initQuestions(received.getQuestions());
				theoreticalExamController.initExam(received.getTheoreticalExam());
				Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
				window.setScene(theoreticalExamScene);
				window.show();
			} else {
				AlertBox.display("Error", received.getMessage());
			}
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

}
