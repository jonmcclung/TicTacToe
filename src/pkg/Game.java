package pkg;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;


public class Game extends JPanel{
	
	Player[] allPlayers = new Player[2];
	Player activePlayer, otherPlayer;
	int turn = 0, defaultOption = 0;
	public Tile[][] map = new Tile[3][3];
	public static int height = 640, width = 640;
	Random rand = new Random();
	
	Game() {
		setPreferredSize(new Dimension(width, height));	
		setVisible(true);
	}
	
	public void addComponents() {
		setLayout(new MigLayout("wrap 3, gap 12, ins 0", "", ""));
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				map[x][y] = new Tile(this, new Position(x, y));
				add(map[x][y]);
			}
		}
		revalidate();
		reset();
	}
	
	public void reset() {
		turn = 0;
		
		String[] options = {"Friend", "Computer"};
		String message = "Who would you like to play against?";
		int response = JOptionPane.showOptionDialog(Main.frame, message, "New Game Setup", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[defaultOption]);	
		defaultOption = response;
		if (response == 1 && allPlayers[0] != null && allPlayers[0].type.equals("ai")) {
			allPlayers[0] = new Player("human", "blue");
			allPlayers[1] = new Player("ai", "pink");				
		}
		else if (response == 1) {
			allPlayers[1] = new Player("human", "blue");
			allPlayers[0] = new Player("ai", "pink");			
		}
		else if (response == 0) {
			allPlayers[0] = new Player("human", "blue");
			allPlayers[1] = new Player("human", "pink");				
		}
		else if (response == JOptionPane.CLOSED_OPTION) {
			System.exit(0);
		}
		for (Player p : allPlayers) {
			p.marks.clear();
		}
		for (Tile[] T : map) {
			for (Tile t : T) {
				t.full = false;				
			}
		}
		activePlayer = allPlayers[0];
		otherPlayer = allPlayers[1];
		if (activePlayer.type.equals("ai")) {
			// AI chooses this if given enough time, but it takes a few seconds
			// better to hardcode it so that it can be fast from the get-go.
			takeAITurn(new Position(0, 0));
		}
	}
	
	public void toggleActivePlayer() {
		otherPlayer = allPlayers[turn%2];
		turn++;
		activePlayer = allPlayers[turn%2];
		if (activePlayer.type.equals("ai")) {
			takeAITurn();
		}
	}
	
	public int minimax(State state, int alpha, int beta) {
		ArrayList<Position> possibleMoves = state.getPossibleMoves();
		int result = -1001;
		for (Position p : possibleMoves) {
			State s = new State(p, state.otherPlayer, state.activePlayer);
			result = minimax(s, beta*-1, alpha*-1);
			if (result > beta) {
//				System.out.println(p.print() + " is really good because " + result + " > " + beta);
//				s.drawState();
				return result*-1;
			}
			else if (result > alpha) {
//				System.out.println(p.print() + " is okay because " + result + " > " + alpha);
//				s.drawState();
				alpha = result;
			}
			else {
//				System.out.println(p.print() + " is not good because " + result + " < " + alpha);
			}
		}
		if (possibleMoves.size() == 0) {
			return state.getTilt();
		}
		return alpha*-1;
	}

	public void takeAITurn(Position choice) {
		if (choice != null) {
		//System.out.println("choosing "+choice.print());
			map[choice.x][choice.y].full = true;
			doAction(map[choice.x][choice.y]);
		}
		else {
			throw new IllegalArgumentException("Choice mustn't be null.");
		}

	}
	
	public void takeAITurn() {
//		System.out.println("current state: ");
		State state = new State(activePlayer, otherPlayer);
//		state.drawState();
			Position choice = null;
			ArrayList<Position> possibleMoves = state.getPossibleMoves();
			int bestValue = -1001;
			for (Position p : possibleMoves) {
//				System.out.println("What about " + p.print() + "?");
				State s = new State(p, state.activePlayer, state.otherPlayer);
				if (s.getTilt() >= 1000) {
					choice = p;
					break;
				}
				int result = minimax(s, -1001, 1001);
//				System.out.println("position " + p.print() + ": " + result);
				if (result > bestValue) {
					bestValue = result;
					choice = p;
				}
			}
			takeAITurn(choice);
		}
	
	public void checkWin() {
		State state = new State(activePlayer, otherPlayer);
		if (Math.abs(state.getTilt()) == 1000) {
			String message = "";
			if (activePlayer.type.equals("human")) {
				message = "You won!";
			}
			else {
				message = "You lost!";
			}
			JOptionPane.showMessageDialog(Main.frame, message);
			reset();
		}
		else if (turn < 9-1) {
			toggleActivePlayer();
		}
		else {
			JOptionPane.showMessageDialog(Main.frame, "Cat!");
			reset();
		}
	}
	
	public void doAction(Tile t) {
	//System.out.println(activePlayer.type+" plays "+t.p.print());
		activePlayer.marks.add(new Mark(t.p, activePlayer));	
		repaint();
		checkWin();
	}
	
	public void render() {
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(Main.bg, 0, 0, null);
		for (Player p : allPlayers) {
			if (p != null) {
				g.setColor(p.color);
				g.setFont(Main.font.deriveFont(256f));
				for (Mark m : p.marks) {
					g.drawString(p.letter, (int)(m.p.x*216)+40, (int)((m.p.y+1)*216)-60);
				}				
			}
		}
	}
}
