package ab.demo;

import ab.demo.Pigs_position;
import ab.demo.Action_reward_Q;

import java.awt.Point;
import java.util.ArrayList;

class RLState
{
    private RLState parent;
	private ArrayList<Pigs_position> pigs_position;
	private ArrayList<RLState> children;
	private ArrayList<Action_reward_Q> child_states;
	private boolean visited;
	//private boolean lost;
	
	/*public RLState ()
	{
	}
	
	public RLState (RLState x)
	{
		this.parent = x.parent;
		this.pigs_position = x.pigs_position;
		this.children = x.children;
		this.child_states = x.child_states;
		this.visited = x.visited;
		this.lost = x.lost;
	}*/
	public ShootSequence getPigPosforMaxQ()
	{
		ShootSequence ss = new ShootSequence();
		int numChildStates = child_states.size();
		int max = child_states.get(0).getQ();
		int index = 0;
		for(int i = 1;i<numChildStates;i++)
		{
			int QVal = child_states.get(i).getQ();
			if(QVal > max)
			{
				max = QVal;
				index = i;
			}
		}
		//Return pigs_position corresponding to index 
		int pigNumberToShoot = child_states.get(index).getPignumber();
		ss.pTarget = pigs_position.get(pigNumberToShoot - 1).getPosition();
		ss.maxChildIndex = index;
		ss.releasePoint = child_states.get(index).getReleasePoint();
		return ss;
	}
	
	public void setParent(RLState parent)
	{
		this.parent = parent;
	}

	public RLState getParent()
	{
		return parent;
	}
	
	public void setPigs_position(ArrayList<Pigs_position> pigs_position)
	{
		this.pigs_position = pigs_position;
	}

	public ArrayList<Pigs_position> getPigs_position()
	{
		return pigs_position;
	}
	
	public void setChildren(ArrayList<RLState> children)
	{
		this.children = children;
	}

	public ArrayList<RLState> getChildren()
	{
		return children;
	}
	
	public void setChild_states(ArrayList<Action_reward_Q> child_states)
	{
		this.child_states = child_states;
	}

	public ArrayList<Action_reward_Q> getChild_states()
	{
		return child_states;
	}
	
	public void setVisited(boolean visited)
	{
		this.visited = visited;
	}

	public boolean getVisited()
	{
		return visited;
	}
	
	/*public void setLost(boolean lost)
	{
		this.lost = lost;
	}

	public boolean getLost()
	{
		return lost;
	}*/
}
