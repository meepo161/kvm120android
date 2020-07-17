package ru.avem.navitest.utils;

public class AutoQueue {
    private int mSize = 3;
    private double[] mElements;
    private int mPointer = -1;
    private double mAverage;

    public AutoQueue() {
        mElements = new double[mSize];
    }

    public AutoQueue(int size) {
        mSize = size;
        mElements = new double[mSize];
    }

    public synchronized void add(double newElement) {
        mPointer++;
        mPointer %= mSize;
        mElements[mPointer] = newElement;
        double sum = 0;
        for (double element : mElements) {
            sum += element;
        }
        mAverage = sum / mElements.length;
    }

    public double getAverage() {
        return mAverage;
    }
}
