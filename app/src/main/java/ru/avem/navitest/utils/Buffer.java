package ru.avem.navitest.utils;

import java.util.Arrays;

public class Buffer {
    private int mSize = 3;
    private double[] mElements;
    private int mPointer;
    private double mAverage;
    private double mMax;

    public Buffer() {
        mElements = new double[mSize];
    }

    public Buffer(int size) {
        mSize = size;
        mElements = new double[mSize];
    }

    public synchronized void add(double newElement) {
        if (mPointer < mSize) {
            mElements[mPointer++] = newElement;
            double sum = 0;
            mMax = -10000;
            for (double element : mElements) {
                sum += element;
                if (element > mMax) {
                    mMax = element;
                }
            }
            mAverage = sum / mPointer;
        }
    }

    public double getAverage() {
        return mAverage;
    }

    public double getMax() {
        return mMax;
    }

    public boolean isEmpty() {
        return mPointer != mSize;
    }

    public String getElements() {
        return Arrays.toString(mElements);
    }
}
