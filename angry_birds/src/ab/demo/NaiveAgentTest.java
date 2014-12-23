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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.demo.util.StateUtil;
import ab.demo.RLState;
import ab.demo.Pigs_position;
import ab.demo.Action_reward_Q;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgentTest implements Runnable {

	private int focus_x;
	private int focus_y;

	public ActionRobot ar;
	public int currentLevel = 1;
	TrajectoryPlanner tp;

	private boolean firstShot;
	private Point prevTarget;

	// a standalone implementation of the Naive Agent
	public NaiveAgentTest() {
	//	ar = new ActionRobot();
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
	
	/*public void rewardcalcinner(RLState root)
	{
		run();
		if(root.getPigs_position().size() == 0 || root.getLost())
			return
		for(int i=0;i<root.getChildren().size();i++)
		{
			rewardcalcinner(root.getChildren().get(i));
		}
		root.setVisited() = true;
	}*/
	RLState root = new RLState();
	RLState mainstate = root;
	RLState childs=null;	
		Action_reward_Q childs_state=null;
		int index1=0;
	// run the client
	public void run() 
	{
	    System.out.println("222222222222222222");
		root.setParent(null);
		root.setVisited(false);
		root.setChildren(null);
		root.setChild_states(null);
		ar.loadLevel(currentLevel);
		System.out.println("TTTTTTTTTTTTTTTTT");
		while (true) {
			System.out.println("*****************************  starting **********************************");
			if (root.getChildren() == null)
			System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
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
				if (mainstate.getVisited())
				{
				    System.out.println("ZZZZZZZZZZZZZZZZZZZZZ");
					currentLevel = currentLevel + 1;
					root.setParent(null);
					root.setVisited(false);
					root.setChildren(null);
					root.setChild_states(null);
					root.setPigs_position(null);
					mainstate = root;
				}
				ar.loadLevel(currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("restart");
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
				if (mainstate.getVisited())
				{
				    System.out.println("ZZZZZZZZZZZZZZZZZZZZZ");
					currentLevel = currentLevel + 1;
					root.setParent(null);
					root.setVisited(false);
					root.setChildren(null);
					root.setChild_states(null);
					root.setPigs_position(null);
					mainstate = root;
				}
				ar.loadLevel(currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
				//ar.restartLevel();
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
										
		List<Rectangle> red_birds = vision.findRedBirds();
		List<Rectangle> blue_birds = vision.findBlueBirds();
		List<Rectangle> yellow_birds = vision.findYellowBirds();
		List<Rectangle> pigs = vision.findPigs();
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds and ");
		GameState state = ar.checkState();
	
		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ar.fullyZoom();
			if (!pigs.isEmpty()) {

				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint = null;
				Point _tpt = null;
				int pignum = 0;
				{
					// random pick up a pig
					Random r = new Random();

					//int index = r.nextInt(pigs.size());
					if (root.getPigs_position() == null)
					{
					    root.setPigs_position(new ArrayList<Pigs_position>());
						for(int index = 0; index < pigs.size(); index++)
						{
							Rectangle pig = pigs.get(index);
							_tpt = new Point((int) pig.getCenterX(),
								(int) pig.getCenterY());
							root.getPigs_position().add(new Pigs_position(index,_tpt));
						}
						System.out.println("$$$$$$$$$$  "+root.getPigs_position().size()+"  $$$$$$$$$$$$");
					}
					
					if (root.getChildren() == null && root.getChild_states() == null)
					{
						root.setChildren(new ArrayList<RLState>());
						root.setChild_states(new ArrayList<Action_reward_Q>());
						int location = 0;
						for(int index = 0; index < pigs.size(); index++)
						{
							ArrayList<Point> pts = tp.estimateLaunchPoint(sling, root.getPigs_position().get(index).getPosition());
							System.out.println("*******"+pts.size());
							System.out.println(index);
							location = index;
							RLState child = new RLState();
							child.setParent(root);
							child.setVisited(false);
							root.getChildren().add(child);
							Action_reward_Q child_state = new Action_reward_Q();
							child_state.setPignumber(index+1);
							child_state.setReleasePoint(pts.get(0));
							child_state.setHighangle(false);
							root.getChild_states().add(child_state);
							if (pts.size() > 1)
							{
								child = new RLState();
								child_state = new Action_reward_Q();
								location++;
								child.setParent(root);
								child.setVisited(false);
								root.getChildren().add(child);
								child_state.setPignumber(index+1);
								child_state.setHighangle(true);
								child_state.setReleasePoint(pts.get(1));
								root.getChild_states().add(child_state);
							}
							else
							{
								child = new RLState();
								child_state = new Action_reward_Q();
								location++;
								child.setParent(root);
								child.setVisited(false);
								root.getChildren().add(child);
								child_state.setPignumber(index+1);
								child_state.setHighangle(true);
								child_state.setReleasePoint(pts.get(0));
								root.getChild_states().add(child_state);
							}
							location++;
							System.out.println("@@@@@@@@  "+ root.getChild_states().size() + root.getChildren().size());
						}
						//System.out.println("@@@@@@@@  "+ root.getChild_states().size() + root.getChildren().size());
					//	System.out.println("@@@@@@@@  "+ root.getChild_states().get(0).getReleasePoint() + root.getChild_states().get(1).getReleasePoint());
					}
	
					
					for(index1 = 0; index1 < root.getChildren().size(); index1++)
					{
						//System.out.println("@@@@@@@@  ");
						childs = root.getChildren().get(index1);
						childs_state = root.getChild_states().get(index1);
						if(!childs.getVisited())
						{
						  //  System.out.println("@@@@@@@@  ");
						    pignum = root.getChild_states().get(index1).getPignumber();
						//	System.out.println("@@@@@@@@  "+pignum);
							_tpt = root.getPigs_position().get(pignum-1).getPosition();
						//	System.out.println("@@@@@@@@  "+_tpt);
							releasePoint = root.getChild_states().get(index1).getReleasePoint();
						//	System.out.println("@@@@@@@@  "+releasePoint +index1);
							break;
						}
					}
					System.out.println("the index is " + index1);
					System.out.println("the target point is " + _tpt);
					System.out.println("the release point is " + releasePoint);

					// if the target is very close to before, randomly choose a
					// point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = r.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
						root.getPigs_position().get(pignum-1).setPosition(_tpt);
					}
					
					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					//ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate
					// velocity
					/*if (firstShot && pts.size() > 1) {
						releasePoint = pts.get(1);
					} else if (pts.size() == 1)
						releasePoint = pts.get(0);
					else {
						// System.out.println("first shot " + firstShot);
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						if (r.nextInt(6) == 0)
							releasePoint = pts.get(1);
						else
							releasePoint = pts.get(0);
					}*/
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
						System.out.println(" The release angle is : "
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
				
				// This is to get scores in the middle of game after each shoot is executed
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
				childs_state.setReward(score);
				System.out.println("###### The intermediate game score is " + score
						+ "########");

			}
			
		}
		if (state == GameState.WON || state == GameState.LOST)
			{
			root=childs;
				System.out.println("YYYYYYYYYYYYYYY");
			System.out.println("..........................................."+root.getParent().getChildren().size());
			
			/*	if (root.getChildren() == null)
			System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
			if (root.getChildren() != null)
			System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEE");
			if (childs == null)
			 System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
			 else
			 System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");*/
				root.setVisited(true); 
				
				if (index1 >= root.getParent().getChildren().size()-1)
				{
					System.out.println("%%%%%%%%%%%%%%%%%%%%");
					root.getParent().setVisited(true);
					visit(root.getParent());
				}
					root = mainstate;
			}
			else
			{
				root = childs;
			}
		System.out.println(state+"SSSSSSSSSSSSSSSSSSS");
		return state;
	}
	
	public void visit(RLState node)
	{
		if (node == mainstate)
			return;
		else
		{
			if (node == node.getParent().getChildren().get(node.getParent().getChildren().size()-1))
			{
				node.getParent().setVisited(true);
				visit(node.getParent());
			}
			return;
		}
	}

	public static void main(String args[]) {

		NaiveAgentTest na = new NaiveAgentTest();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
