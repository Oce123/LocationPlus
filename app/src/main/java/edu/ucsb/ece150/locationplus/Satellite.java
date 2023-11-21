package edu.ucsb.ece150.locationplus;

/*
 * This class is provided as a way for you to store information about a single satellite. It can
 * be helpful if you would like to maintain the list of satellites using an ArrayList (i.e.
 * ArrayList<Satellite>). As in Homework 3, you can then use an Adapter to update the list easily.
 *
 * You are not required to implement this if you want to handle satellite information in using
 * another method.
 */
public class Satellite {

    // [TODO] Define private member variables
    private int satelliteType;
    private int satellitePrn;
    private float satelliteCn0;

    // [TODO] Write the constructor
    public Satellite(int type, int prn, float cn0) {
        this.satelliteType = type;
        this.satellitePrn = prn;
        this.satelliteCn0 = cn0;
    }

    // [TODO] Implement the toString() method. When the Adapter tries to assign names to items
    // in the ListView, it calls the toString() method of the objects in the ArrayList
    @Override
    public String toString() {
        return "Type: " + satelliteType + ", PRN: " + satellitePrn + ", C/N0: " + satelliteCn0;
    }
}
