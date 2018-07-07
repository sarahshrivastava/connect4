package com.internshala.connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int columns = 7;
	private static final int rows = 6;
	private static final int diameter = 80;
	private static  String player1 = "Player 1";
	private static  String player2 = "Player 2";
	private static final String disc1 = "#24303e";
	private static final String disc2 = "4caa88";

	private boolean isPlayer1turn = true;

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertPane;

	@FXML
	public Label playerNameLabel;

	private boolean isInsertAllowed = true;

	private Disc[][] insertdiscsArr = new Disc[rows][columns];


	public void createPlayground(){
		Shape rectangle = new Rectangle((columns + 1)*diameter,(rows+1)*diameter);

		for (int row = 0; row<rows;row++)
		{
			for (int column = 0; column<columns;column++){
				Circle circle = new Circle();
				circle.setRadius(diameter/2);
				circle.setCenterX(diameter/2);
				circle.setCenterY(diameter/2 + 10);
				circle.setSmooth(true);

				circle.setTranslateX(column*(diameter+5) + 20);
				circle.setCenterY(row*(diameter+5) + 60);

				rectangle = Shape.subtract(rectangle,circle);

			}

		}

		rectangle.setFill(Color.WHITE);
		rootGridPane.add(rectangle,0,1);

		List<Rectangle> rectangleList = createClick();

		for (Rectangle rectangle1:rectangleList){
			rootGridPane.add(rectangle1,0,1);
		}
	}

	private List<Rectangle> createClick(){

		List<Rectangle> rectangleList = new ArrayList<>();

		for(int col = 0 ; col<columns;col ++){

			Rectangle rectangle = new Rectangle(diameter,(rows +1)*diameter);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col*(diameter+5) +20);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			 final int column = col;
			rectangle.setOnMouseClicked(event -> {

				if(isInsertAllowed) {

					isInsertAllowed = false;

					insertDisc(new Disc(isPlayer1turn), column);

				}
			});

			rectangleList.add(rectangle);


		}

		return rectangleList;
	}

	private  void insertDisc(Disc disc,int column){

		int row = rows-1;
		while (row>=0) {
			if (getDiscIfPresent(row,column) == null)
				break;

			row--;
		}
			if(row<0)
				return;



		insertdiscsArr[row][column]=disc;
		insertPane.getChildren().add(disc);
		disc.setTranslateX(column*(diameter+5) +20);
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row*(diameter+5) + 22);

		int currentRow = row;
		translateTransition.setOnFinished(event -> {

			isInsertAllowed = true;
			
			if(gameEnded(currentRow,column)){
				gameOver();
				return;
			}
			isPlayer1turn = !isPlayer1turn;
			playerNameLabel.setText(isPlayer1turn? player1:player2);

		});

		translateTransition.play();


	}

	private boolean gameEnded(int row, int column) {

		List<javafx.geometry.Point2D> verticalPoints = IntStream.rangeClosed(row-3,row+3)
				.mapToObj(r-> new javafx.geometry.Point2D(r,column))
				.collect(Collectors.toList());

		

		List<javafx.geometry.Point2D> horizontalPoints = IntStream.rangeClosed(column-3,column+3)
				.mapToObj(col-> new javafx.geometry.Point2D(row,col))
				.collect(Collectors.toList());

		javafx.geometry.Point2D startPoint1 = new javafx.geometry.Point2D(row - 3,column+3);
		List<javafx.geometry.Point2D> diagonalPoints = IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint1.add(i,-i))
				.collect(Collectors.toList());

		javafx.geometry.Point2D startPoint2 = new javafx.geometry.Point2D(row-3,column-3);
		List<javafx.geometry.Point2D> diagonalPoints2 = IntStream.rangeClosed(0,6)
				.mapToObj(j->startPoint2.add(j,j))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints)||checkCombinations(horizontalPoints)||
							checkCombinations(diagonalPoints)||checkCombinations(diagonalPoints2);
		return isEnded;

			/*@Override
			public double getY() {
				return 0;
			}

			@Override
			public void setLocation(double x, double y) {

			}
		})*/

	}

	private boolean checkCombinations(List<javafx.geometry.Point2D> points) {

		int chain = 0;
		for(javafx.geometry.Point2D point:points){



			int rowIndexForArray = (int) point.getX();
			int colIndexForArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexForArray,colIndexForArray);
			if(disc!=null && disc.isPlayeroneMove==isPlayer1turn){
				chain++;
				if(chain==4)
					return true;
			}
			else {
				chain=0;
			}

		}

			return false;
	}

	private Disc getDiscIfPresent(int row,int col){
		if(row>=rows||row<0||col>=columns||col<0) {
			return null;
		}
			else
				return insertdiscsArr[row][col];


	}

	private void gameOver() {

		String winner = isPlayer1turn?player1:player2;
		System.out.println("Winner : "+winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("GAME OVER!!");
		alert.setHeaderText("The winner is "+winner);
		alert.setContentText("Wanna play again??");

		ButtonType yesbtn = new ButtonType("Yes");


		ButtonType nobtn = new ButtonType("No");
		alert.getButtonTypes().setAll(yesbtn,nobtn);

		Platform.runLater(()->{

			Optional<ButtonType> btnclicked = alert.showAndWait();

			if(btnclicked.isPresent()&&btnclicked.get()==yesbtn){
				resetgame();
			}
			else {
				exitGame();
			}


		});


	}

	private void exitGame() {
		Platform.exit();
		System.exit(0);

	}

	public void resetgame() {

		insertPane.getChildren().clear();

		for(int row = 0;row<rows;row++)
			for (int col = 0;col<columns;col++)
				insertdiscsArr[row][col]=null;

		isPlayer1turn = true;

		playerNameLabel.setText(player1);

		createPlayground();


	}

	private static class Disc extends Circle{

		private final boolean isPlayeroneMove ;

		public Disc(boolean isPlayeroneMove){
			this.isPlayeroneMove = isPlayeroneMove;

			setFill(isPlayeroneMove?Color.valueOf(disc1):Color.valueOf(disc2));
			setRadius(diameter/2);
			setCenterX(diameter/2);
			setCenterY(diameter/2);
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
