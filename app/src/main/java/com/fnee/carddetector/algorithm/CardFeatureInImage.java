package com.fnee.carddetector.algorithm;

import org.opencv.core.RotatedRect;

/**
 * Created by Floris on 24-3-2017.
 */

public class CardFeatureInImage {
    private char feature;

    public char getFeature() {
        return feature;
    }

    public RotatedRect getRectangle() {
        return rectangle;
    }

    private RotatedRect rectangle;

    public CardFeatureInImage(char feature, RotatedRect rect)
    {
        this.feature = feature;
        this.rectangle = rect;
    }
}
