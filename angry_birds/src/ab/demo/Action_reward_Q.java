package ab.demo;

import java.awt.Point;

class Action_reward_Q
{
    	private int pignumber;
	private Point releasePoint;
	private boolean highangle;  
	// if it is high angle it is set to true
	private int reward;
	private int Q;

	public void setHighangle(boolean highangle)
	{
		this.highangle = highangle;
	}

	public boolean getHighangle()
	{
		return highangle;
	}
	
	public void setReleasePoint(Point releasePoint)
	{
		this.releasePoint = releasePoint;
	}

	public Point getReleasePoint()
	{
		return releasePoint;
	}
	
	public void setPignumber(int pignumber)
	{
		this.pignumber = pignumber;
	}

	public int getPignumber()
	{
		return pignumber;
	}

	public void setReward(int reward)
	{
		this.reward = reward;
	}

	public int getReward()
	{
		return reward;
	}

	public void setQ(int Q)
	{
		this.Q = Q;
	}

	public int getQ()
	{
		return Q;
	}
}
