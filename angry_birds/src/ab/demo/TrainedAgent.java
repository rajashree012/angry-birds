/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.demo.util.StateUtil;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class TrainedAgent implements Runnable {

	private int focus_x;
	private int focus_y;

	private ActionRobot ar;
	public int currentLevel = 1;
	TrajectoryPlanner tp;

	private boolean firstShot;
	private Point prevTarget;
	
	private ArrayList<Point> targetPoints;
	private ArrayList<Point> releasePoints;
	
	int global_index = 0;

	// a standalone implementation of the Naive Agent
	public TrainedAgent() {
		ar = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	public int getCurrent_level() {
		return currentLevel;
	}

	public void setCurrent_level(int current_level) {
		this.currentLevel = current_level;
	}

	// run the client
	public void run() {

		ar.loadLevel(currentLevel);
		while (true) {
			targetPoints = new ArrayList<Point>();
			releasePoints = new ArrayList<Point>();
			try {
				FileInputStream fis = new FileInputStream("out"+currentLevel+".txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String strLine;
				while ((strLine = br.readLine()) != null)   {
					//Format::targetx,targety:releasex,releasey
					int index1 = strLine.indexOf(',');
					int index2 = strLine.indexOf(':');
					int index3 = strLine.lastIndexOf(',');
					int x1 = Integer.parseInt(strLine.substring(0, index1));
					int y1 = Integer.parseInt(strLine.substring(index1+1, index2));
					int x2 = Integer.parseInt(strLine.substring(index2+1, index3));
					int y2 = Integer.parseInt(strLine.substring(index3+1));
					targetPoints.add(new Point(x1,y1));
					releasePoints.add(new Point(x2,y2));
					}
					br.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = -2;
				while (score != StateUtil.checkCurrentScore(ar.proxy)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					score = StateUtil.checkCurrentScore(ar.proxy);
				}
				System.out.println("###### The game score is " + score
						+ "########");
				global_index = 0;
				ar.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("restart");
				ar.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
						.println("unexpected level selection page, go to the lasts current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
						.println("unexpected episode menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()

	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		Rectangle sling = vision.findSlingshot();

		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out
					.println("no slingshot detected. Please remove pop up or zoom out");
			ar.fullyZoom();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshot();
		}

		//List<Rectangle> red_birds = vision.findRedBirds();
		//List<Rectangle> blue_birds = vision.findBlueBirds();
		//List<Rectangle> yellow_birds = vision.findYellowBirds();
		//List<Rectangle> pigs = vision.findPigs();
		//int bird_count = 0;
		//bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		//System.out.println("...found " + pigs.size() + " pigs and "
			//	+ bird_count + " birds");
		GameState state = ar.checkState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ar.fullyZoom();
		 

				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();

				//	int index = r.nextInt(pigs.size());
					//Rectangle pig = pigs.get(index);
					Point _tpt = targetPoints.get(global_index);

					System.out.println("the target point is " + _tpt);

					// if the target is very close to before, randomly choose a
					// point near it
					//if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						//double _angle = r.nextDouble() * Math.PI * 2;
						//_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						//_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						//System.out.println("Randomly changing to " + _tpt);
					//}

					//prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate
					// velocity
					//if (firstShot && pts.size() > 1) {
						//releasePoint = pts.get(1);
					//} else if (pts.size() == 1)
						//releasePoint = pts.get(0);
					//else {
						// System.out.println("first shot " + firstShot);
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						//if (r.nextInt(6) == 0)
							//releasePoint = pts.get(1);
						//else
							//releasePoint = pts.get(0);
					//}
					releasePoint = releasePoints.get(global_index);
					global_index++;
					Point refPoint = tp.getReferencePoint(sling);
					/* Get the center of the active bird */
					focus_x = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getX() : refPoint.x);
					focus_y = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getY() : refPoint.y);
					System.out.println("the release point is: " + releasePoint);
					/*
					 * =========== Get the release point from the trajectory
					 * prediction module====
					 */
					System.out.println("Shoot!!");
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println(" The calculated release angle is : "
								+ Math.toDegrees(releaseAngle));
						int base = 0;
						if (releaseAngle > Math.PI / 4)
							base = 1400;
						else
							base = 550;
						int tap_time = (int) (base + Math.random() * 1500);
						
						
						
						shots.add(new Shot(focus_x, focus_y, (int) releasePoint
								.getX() - focus_x, (int) releasePoint.getY()
								- focus_y, 0, tap_time));
					} else
						System.err.println("Out of Knowledge");
				}

				// check whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					ar.fullyZoom();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshot();
					if (sling.equals(_sling)) {
						state = ar.shootWithStateInfoReturned(shots);
						// update parameters after a shot is made
						if (state == GameState.PLAYING) {
							screenshot = ActionRobot.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;
							
						}
					} else
						System.out
								.println("scale is changed, can not execute the shot, will re-segement the image");
				}

			

		}
		return state;
	}

	public static void main(String args[]) {

		TrainedAgent ta = new TrainedAgent();
		if (args.length > 0)
			ta.currentLevel = Integer.parseInt(args[0]);
		ta.run();

	}
}
