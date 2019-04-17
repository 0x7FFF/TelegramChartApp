package com.smakhorin.telegramchartapp.charts;


import java.util.ArrayList;
import java.util.List;

public class Followers {

    private List<Long> listOfX;
    private final List<Line> lines = new ArrayList<>();

    List<Long> getListOfX() {
        return listOfX;
    }

    public void setListOfX(List<Long> listOfX) {
        this.listOfX = listOfX;
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    List<Line> getLines() {
        return lines;
    }
}
