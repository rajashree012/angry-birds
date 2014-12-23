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
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

public class NaiveAgentFinal implements Runnable {

	private int focus_x;
	private int focus_y;

	public ActionRobot ar;
	public int currentLevel = 1;
	TrajectoryPlanner tp;

	private boolean firstShot;
	private Point prevTarget;

	// a standalone implementation of the Naive Agent
	public NaiveAgentFinal() {
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
	
	RLState root = new RLState();
	RLState mainstate = root;
	RLState childs=null;	
	Action_reward_Q childs_state=null;
	int index1=0;
	// run the client
	public void run() 
	{
		root.setParent(null);
		root.setVisited(false);
		root.setChildren(null);
		root.setChild_states(null);
		ar.loadLevel(currentLevel);
		while (true) 
		{
			System.out.println("*****************************  starting **********************************");
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
					//currentLevel = currentLevel + 1;
					//root.setParent(null);
					//root.setVisited(false);
					//root.setChildren(null);
					//root.setChild_states(null);
					//root.setPigs_position(null);
					//mainstate = root;
				    break;
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
					//currentLevel = currentLevel + 1;
					//root.setParent(null);
					//root.setVisited(false);
					//root.setChildren(null);
					//root.setChild_states(null);
					//root.setPigs_position(null);
					//mainstate = root;
					break;
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
		
		for(int iLoop =1;iLoop <= 20;iLoop++)
		{
			calculateQValues_dfs(mainstate);
			System.out.print("Hello");
		}
		RLState state = mainstate;
		try {
			//FileOutputStream fos = new FileOutputStream(new File("out.txt"));
			PrintWriter out = new PrintWriter(new File("out"+currentLevel+".txt"));
			while(state.getChild_states() != null)
			{
				ShootSequence ss = state.getPigPosforMaxQ();
				//out.println(ss.pTarget + "," + ss.releasePoint);
				out.println(Integer.toString(ss.pTarget.x)+","+Integer.toString(ss.pTarget.y)+":"+Integer.toString(ss.releasePoint.x)+","+Integer.toString(ss.releasePoint.y));
				state = state.getChildren().get(ss.maxChildIndex);
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try {
			 StringOutputStream fos = new StringOutputStream();
			XMLEncoder xenc = new XMLEncoder(fos);
			   xenc.writeObject(mainstate);
			   xenc.close();
			   PrintWriter out = new PrintWriter(new File("out.xml"));
			   out.println(fos.getString());
			   out.close();
			   
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		
	}
	private class StringOutputStream extends OutputStream {
		  StringBuilder mBuf = new StringBuilder();
		  public void write(int bytes) throws IOException {mBuf.append((char) bytes);}
		  public String getString() {return mBuf.toString();}
		}
	private void calculateQValues_dfs(RLState root)
	{
		if(root.getChild_states() == null)
			return;
		int iNumChild = root.getChild_states().size();
		if(iNumChild == 0)
			return;
		for(int i = 0;i < iNumChild;i++)
		{
			RLState child = root.getChildren().get(i);
			int max = 0;
			if(child.getChild_states() != null)
			{
				for(int j = 0;j<child.getChild_states().size();j++)
				{
					int qvalue = child.getChild_states().get(j).getQ();
					if(qvalue > max)
						max = qvalue;
				}
				
			}
			int reward = root.getChild_states().get(i).getReward();
			root.getChild_states().get(i).setQ((int) (reward + (0.8 * max)));
			
		}
		for(int k = 0;k<iNumChild;k++)
		{
			calculateQValues_dfs(root.getChildren().get(k));
		}
	}

	private double distance(Point p1, Point p2) 
	{
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()
	{
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		Rectangle sling = vision.findSlingshot();

		while (sling == null && ar.checkState() == GameState.PLAYING) 
		{
			System.out.println("no slingshot detected. Please remove pop up or zoom out");
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

		System.out.println("...found " + pigs.size() + " pigs and "+ bird_count + " birds and");
		GameState state = ar.checkState();
	
		// if there is a sling, then play, otherwise just skip.
		if (sling != null) 
		{
			ar.fullyZoom();
			if (!pigs.isEmpty()) 
			{
				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint = null;
				Point _tpt = null;
				int pignum = 0;
				
				// random pick up a pig
					Random r = new Random();
					
				{
					if (root.getPigs_position() == null)
					{
					    	root.setPigs_position(new ArrayList<Pigs_position>());
						for(int index = 0; index < pigs.size(); index++)
						{
							Rectangle pig = pigs.get(index);
							_tpt = new Point((int) pig.getCenterX(),(int) pig.getCenterY());
							root.getPigs_position().add(new Pigs_position(index,_tpt));
						}
					}
					
					if (root.getChildren() == null && root.getChild_states() == null)
					{
						root.setChildren(new ArrayList<RLState>());
						root.setChild_states(new ArrayList<Action_reward_Q>());
						for(int index = 0; index < pigs.size(); index++)
						{
							ArrayList<Point> pts = tp.estimateLaunchPoint(sling, root.getPigs_position().get(index).getPosition());
							RLState child = new RLState();
							child.setParent(root);
							child.setVisited(false);
							root.getChildren().add(child);
							Action_reward_Q child_state = new Action_reward_Q();
							child_state.setPignumber(index+1);
							child_state.setReleasePoint(pts.get(0));
							child_state.setHighangle(false);
							child_state.setQ(0);
							root.getChild_states().add(child_state);
							if (pts.size() > 1)
							{
								child = new RLState();
								child_state = new Action_reward_Q();
								child.setParent(root);
								child.setVisited(false);
								root.getChildren().add(child);
								child_state.setPignumber(index+1);
								child_state.setHighangle(true);
								child_state.setReleasePoint(pts.get(1));
								child_state.setQ(0);
								root.getChild_states().add(child_state);
							}
							else
							{
								child = new RLState();
								child_state = new Action_reward_Q();
								child.setParent(root);
								child.setVisited(false);
								root.getChildren().add(child);
								child_state.setPignumber(index+1);
								child_state.setHighangle(true);
								child_state.setReleasePoint(pts.get(0));
								child_state.setQ(0);
								root.getChild_states().add(child_state);
							}
						}
					}					
					for(index1 = 0; index1 < root.getChildren().size(); index1++)
					{
						childs = root.getChildren().get(index1);
						childs_state = root.getChild_states().get(index1);
						if(!childs.getVisited())
						{
						    	pignum = root.getChild_states().get(index1).getPignumber();
						    	_tpt = root.getPigs_position().get(pignum-1).getPosition();
							releasePoint = root.getChild_states().get(index1).getReleasePoint();
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
					if (releasePoint != null) 
					{
						double releaseAngle = tp.getReleaseAngle(sling,releasePoint);
						System.out.println(" The release angle is : "+ Math.toDegrees(releaseAngle));
						int base = 0;
						if (releaseAngle > Math.PI / 4)
							base = 1400;
						else
							base = 550;
						int tap_time = (int) (base + Math.random() * 1500);						
						shots.add(new Shot(focus_x, focus_y, (int) releasePoint.getX() - focus_x, (int) releasePoint.getY()
								- focus_y, 0, tap_time));
					} 
					else
						System.err.println("Out of Knowledge"); 
				}

				// check whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					ar.fullyZoom();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshot();
					if (sling.equals(_sling)) 
					{
						state = ar.shootWithStateInfoReturned(shots);
						// update parameters after a shot is made
						if (state == GameState.PLAYING) 
						{
							screenshot = ActionRobot.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;							
						}
					} 
					else
						System.out.println("scale is changed, can not execute the shot, will re-segement the image");
				}
				
				// This is to get scores in the middle of game after each shoot is executed
				/*int score = -2;
				while (score != StateUtil.checkCurrentScore(ar.proxy)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					score = StateUtil.checkCurrentScore(ar.proxy);
				}
				// setting the reward for this state
				childs_state.setReward(score);
				System.out.println("###### The intermediate game score is " + score + "########");
				System.out.println("###### The reward for this actio is " + childs_state.getReward() + "########");*/
			}
			
		}
		if (state == GameState.WON || state == GameState.LOST)
		{
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
			// setting the reward for this state
			childs_state.setReward(score);
			System.out.println("###### The intermediate game score is " + score + "########");
			System.out.println("###### The reward for this actio is " + childs_state.getReward() + "########");
			root=childs;
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
			// setting the reward for this state
			childs_state.setReward(0);
			System.out.println("###### The intermediate game score is " + score + "########");
			System.out.println("###### The reward for this actio is " + childs_state.getReward() + "########");
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

		/*Point p1 = new Point(5,6);
		Point p2 = new Point(7,8);
		Point p3 = new Point(9,2);
		Point p4 = new Point(1,8);
		try {
			PrintWriter pw = new PrintWriter(new File("abc.txt"));
			pw.println(Integer.toString(p1.x)+","+Integer.toString(p1.y)+":"+Integer.toString(p2.x)+","+Integer.toString(p2.y));
			pw.println(Integer.toString(p3.x)+","+Integer.toString(p3.y)+":"+Integer.toString(p4.x)+","+Integer.toString(p4.y));
			//pw.write(p3.toString()+p4.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		NaiveAgentFinal na = new NaiveAgentFinal();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.currentLevel = 4;
		na.run();

	}
}
