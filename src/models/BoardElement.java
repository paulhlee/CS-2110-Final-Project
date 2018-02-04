package models;

/** An instance represents a viewable entity attached to a board. */
public interface BoardElement {

	/** Return the name this Object has when drawn on the board */
	public String getName();

	/** Return the x coordinate of this BoardElement. */
	public int getX();

	/** Return the y coordinate of this BoardElement. */
	public int getY();
}
