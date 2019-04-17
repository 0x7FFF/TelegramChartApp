package com.smakhorin.telegramchartapp.charts;


import java.util.List;

/**
 * Line class which contains several JSON parsed parameters such as
 * color, name and a list of y-coord points
 */
public class Line {

    private List<Integer> listOfY;
    private String name;
    private String color;

    List<Integer> getListOfY() {
        return listOfY;
    }

    public void setListOfY(List<Integer> listOfY) {
        this.listOfY = listOfY;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
