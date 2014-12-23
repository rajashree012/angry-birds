package ab.demo;

import java.awt.Point;

class Pigs_position
{
	private int pigno;
	private Point position;
	
	public Pigs_position(int pigno, Point position)
	{
		this.pigno = pigno;
		this.position = position;
	}

	public void setPigno(int pigno)
	{
		this.pigno = pigno;
	}

	public int getPigno()
	{
		return pigno;
	}
	
	public void setPosition(Point position)
	{
		this.position = position;
	}

	public Point getPosition()
	{
		return position;
	}
}
