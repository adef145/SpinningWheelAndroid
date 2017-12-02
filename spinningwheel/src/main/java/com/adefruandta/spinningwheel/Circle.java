package com.adefruandta.spinningwheel;

import android.graphics.Matrix;
import android.graphics.Point;

/**
 * Created by adefruandta on 3/12/17.
 */

class Circle {

    private float cx;

    private float cy;

    private float radius;

    private Matrix matrix;

    public Circle() {
        matrix = new Matrix();
    }

    public Circle(float width, float height) {
        this();

        cx = width / 2f;
        cy = height / 2f;
        radius = Math.min(cx, cy);
    }

    public float getCx() {
        return cx;
    }

    public float getCy() {
        return cy;
    }

    public float getRadius() {
        return radius;
    }

    public boolean contains(float x, float y) {
        x = cx - x;
        y = cy - y;
        return x * x + y * y <= radius * radius;
    }

    public Point rotate(float angle, float x, float y) {
        // This is to onRotate about the Rectangles center
        matrix.setRotate(angle, cx, cy);

        // Create new float[] to hold the rotated coordinates
        float[] pts = new float[2];

        // Initialize the array with our Coordinate
        pts[0] = x;
        pts[1] = y;

        // Use the Matrix to map the points
        matrix.mapPoints(pts);

        // NOTE: pts will be changed by transform.mapPoints call
        // after the call, pts will hold the new cooridnates

        // Now, create a new Point from our new coordinates
        return new Point((int) pts[0], (int) pts[1]);
    }
}
