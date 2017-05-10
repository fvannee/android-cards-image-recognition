package com.fnee.carddetector.algorithm;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;

import com.fnee.carddetector.common.Card;
import com.fnee.carddetector.common.Rank;
import com.fnee.carddetector.common.Suit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.opencv.ml.Ml.ROW_SAMPLE;

public class CardDetector {
    private KNearest modelRed, modelBlack;
    private Size smallSize;

    private Mat subImg = new Mat();
    private Mat smallImg = new Mat();
    private Mat smallImgF32 = new Mat();
    private Mat results = new Mat();
    private Mat neighborResponses = new Mat(), dist = new Mat();

    public Mat getDebugImg() {
        return debugImg;
    }

    public void setDebugImg(Mat debugImg) {
        this.debugImg = debugImg;
    }

    private Mat debugImg = null;

    static final int MATCH_FOUND_PARAM = 7000;
    static final int INTERESTING_AREA_PARAM = 6000;
    static final double MIN_DIM = 3.5;
    static final double MIN_SUIT_DIM = 2;
    static final String CARDS = "23456789TJQKA";
    static final String SUITS = "CDHS";

    public CardDetector(Mat redSamples, Mat redResponses, Mat blackSamples, Mat blackResponses)
    {
        this.modelRed = KNearest.create();
        this.modelBlack = KNearest.create();
        int size = (int)Math.sqrt(redSamples.cols());
        this.smallSize = new Size(size, size);
        this.modelRed.train(redSamples, ROW_SAMPLE, redResponses);
        this.modelBlack.train(blackSamples, ROW_SAMPLE, blackResponses);
    }

    private static double angle_p(double x, double y)
    {
        double rads = Math.atan2(-y, x);
        rads %= 2 * Math.PI;
        return rads;
    }

    public static void subImage(Mat bwImg, Mat dstImg, Point center, double theta, Size size)
    {
        theta *= Math.PI / 180.0;
        double v_x_0 = Math.cos(theta);
        double v_x_1 = Math.sin(theta);
        double v_y_0 = -Math.sin(theta);
        double v_y_1 = Math.cos(theta);
        double s_x = center.x - v_x_0 * (size.width / 2.0) - v_y_0 * (size.height / 2.0);
        double s_y = center.y - v_x_1 * (size.width / 2.0) - v_y_1 * (size.height / 2.0);

        Mat mapping = new Mat(2, 3, CvType.CV_32F);
        mapping.put(0, 0, v_x_0);
        mapping.put(0, 1, v_y_0);
        mapping.put(0, 2, s_x);
        mapping.put(1, 0, v_x_1);
        mapping.put(1, 1, v_y_1);
        mapping.put(1, 2, s_y);

        Imgproc.warpAffine(bwImg, dstImg, mapping, size, Imgproc.WARP_INVERSE_MAP, Core.BORDER_REPLICATE, new Scalar(0));
    }

    private static void rot90(RotatedRect rectangle, int k)
    {
        boolean odd = k % 2 != 0;
        if (odd) {
            double tmp = rectangle.size.height;
            rectangle.size.height = rectangle.size.width;
            rectangle.size.width = tmp;
        }
        rectangle.angle = (rectangle.angle + k * 90) % 360;
    }

    public static Set<Card> CardsToSet(List<CardInImage> cardsInImage)
    {
        Set<Card> cards = new TreeSet<Card>();
        for (CardInImage card : cardsInImage)
        {
            cards.add(card.getCard());
        }
        return cards;
    }

    public static void CardsToSet(List<CardInImage> cardsInImage, Set<Card> cards)
    {
        for (CardInImage card : cardsInImage)
        {
            cards.add(card.getCard());
        }
    }

    private static boolean isInterestingRectangle(Mat bwImage, RotatedRect rectangle, int interestingAreaParam, double minDimension)
    {
        int min_area = bwImage.width() * bwImage.height() / interestingAreaParam;
        int area = (int)(rectangle.size.width * rectangle.size.height);
        if (area > min_area)
        {
            double dim = rectangle.size.width / rectangle.size.height;
            return dim < minDimension && dim > (1/minDimension);
        }
        {
            return false;
        }
    }

    private CardFeatureInImage getCardFeatureFromSubImage(Mat bwImage, RotatedRect rectangle, int featureColor, int requiredAccuracy)
    {
        Imgproc.resize(bwImage, smallImg, smallSize);
        double bestDist = 1e9f;
        double bestVal = 0;
        for (int i = 0; i < 2; i++)
        {
            Mat reshaped = smallImg.reshape(0, 1);
            //String dump = reshaped.dump();
            reshaped.convertTo(smallImgF32, CvType.CV_32F);
            if (featureColor == Color.RED)
                this.modelRed.findNearest(smallImgF32, 1, results, neighborResponses, dist);
            else
                this.modelBlack.findNearest(smallImgF32, 1, results, neighborResponses, dist);
            if (dist.get(0, 0)[0] < bestDist)
            {
                bestDist = dist.get(0,0)[0];
                bestVal = results.get(0,0)[0];
                if (i == 1)
                {
                    rectangle.angle = (rectangle.angle + 180) % 360;
                }
            }
            if (i < 1)
            {
                //String before = smallImg.dump();
                Core.flip(smallImg, smallImg, -1);
                //String after = smallImg.dump();
            }
        }
        boolean match_found = bestDist < requiredAccuracy * smallSize.area();
        if (match_found)
        {
            char feat = (char)((int)bestVal);
            /*if (feat == 'S' && featureColor == Color.RED)
                feat = 'H';
            else if (feat == 'H' && featureColor == Color.BLACK)
                feat = 'S';
            else if (feat == 'D' && featureColor == Color.BLACK)
                feat = 'S';*/

            double dim = rectangle.size.height / rectangle.size.width;
            if (!SUITS.contains("" + feat) || dim < MIN_SUIT_DIM) {
                return new CardFeatureInImage(feat, rectangle);
            }
            return null;
        }
        return null;
    }

    private CardFeatureInImage getCardFeatureFromContour(Mat bwImage, MatOfPoint contour, Mat colorImg)
    {
        MatOfPoint2f dst = new MatOfPoint2f();
        contour.convertTo(dst, CvType.CV_32F);
        RotatedRect rect = Imgproc.minAreaRect(dst);
        if (isInterestingRectangle(bwImage, rect, INTERESTING_AREA_PARAM, MIN_DIM))
        {
            if (rect.size.width > rect.size.height)
            {
                rot90(rect, 1);
            }
            if (debugImg != null)
            {
                Point[] points = new Point[4];
                rect.points(points);
                for(int i=0; i<4; ++i){

                    Imgproc.line(debugImg, points[i], points[(i+1)%4], new Scalar(0,0,255), 3);
                }
            }
            subImage(bwImage, subImg, rect.center, rect.angle, rect.size);
            if (rect.size.area() > 2500 && rect.size.area() < 100000) {
                //String dump = subImg.dump();
                //Log.d("Detector", dump);
            }
            double[] pixel = colorImg.get((int)rect.center.y, (int)rect.center.x);
            int color = isRedOrBlack(pixel);
            CardFeatureInImage feature = getCardFeatureFromSubImage(subImg, rect, color, MATCH_FOUND_PARAM);
            if (debugImg != null)
            {
                Imgproc.putText(debugImg, feature == null ? "-" : "" + feature.getFeature(), rect.center, 0, 2, new Scalar(0, 255, 75), 5, Core.LINE_AA, false);
            }
            return feature;
        }
        return null;
    }

    private double colorDistance(double[] e1, double[] e2)
    {
        double rmean = ( e1[0] + e2[0] ) / 2;
        double r = e1[0] - e2[0];
        double g = e1[1] - e2[1];
        double b = e1[2] - e2[2];
        return Math.sqrt((((512+rmean)*r*r)/256) + 4*g*g + (((767-rmean)*b*b)/256));
    }

    private static double[][] colors = new double[][] { { 0, 0, 0}, { 255, 0, 0 }, { 255, 128, 0 }};
    private static int[] colorMapping = new int[] { Color.BLACK, Color.RED, Color.RED };

    static {
        for (int i = 0; i < colors.length; i++)
        {
            ColorUtils.RGBToLAB((int)colors[i][0], (int)colors[i][1], (int)colors[i][2], colors[i]);
        }
    }

    public static double ciede2000(double[] x, double[] y) {
        // adapted from Sharma et al's MATLAB implementation at
        //  http://www.ece.rochester.edu/~gsharma/ciede2000/

        // parametric factors, use defaults
        double kl = 1, kc = 1, kh = 1;

        // compute terms
        double pi = Math.PI,
                L1 = x[0], a1 = x[1], b1 = x[2], Cab1 = Math.sqrt(a1*a1 + b1*b1),
                L2 = y[0], a2 = y[1], b2 = y[2], Cab2 = Math.sqrt(a2*a2 + b2*b2),
                Cab = 0.5*(Cab1 + Cab2),
                G = 0.5*(1 - Math.sqrt(Math.pow(Cab,7)/(Math.pow(Cab,7)+Math.pow(25,7)))),
                ap1 = (1+G) * a1,
                ap2 = (1+G) * a2,
                Cp1 = Math.sqrt(ap1*ap1 + b1*b1),
                Cp2 = Math.sqrt(ap2*ap2 + b2*b2),
                Cpp = Cp1 * Cp2;

        // ensure hue is between 0 and 2pi
        double hp1 = Math.atan2(b1, ap1); if (hp1 < 0) hp1 += 2*pi;
        double hp2 = Math.atan2(b2, ap2); if (hp2 < 0) hp2 += 2*pi;

        double dL = L2 - L1,
                dC = Cp2 - Cp1,
                dhp = hp2 - hp1;

        if (dhp > +pi) dhp -= 2*pi;
        if (dhp < -pi) dhp += 2*pi;
        if (Cpp == 0) dhp = 0;

        // Note that the defining equations actually need
        // signed Hue and chroma differences which is different
        // from prior color difference formulae
        double dH = 2 * Math.sqrt(Cpp) * Math.sin(dhp/2);

        // Weighting functions
        double Lp = 0.5 * (L1 + L2),
                Cp = 0.5 * (Cp1 + Cp2);

        // Average Hue Computation
        // This is equivalent to that in the paper but simpler programmatically.
        // Average hue is computed in radians and converted to degrees where needed
        double hp = 0.5 * (hp1 + hp2);
        // Identify positions for which abs hue diff exceeds 180 degrees
        if (Math.abs(hp1-hp2) > pi) hp -= pi;
        if (hp < 0) hp += 2*pi;

        // Check if one of the chroma values is zero, in which case set
        // mean hue to the sum which is equivalent to other value
        if (Cpp == 0) hp = hp1 + hp2;

        double Lpm502 = (Lp-50) * (Lp-50),
                Sl = 1 + 0.015*Lpm502 / Math.sqrt(20+Lpm502),
                Sc = 1 + 0.045*Cp,
                T = 1 - 0.17*Math.cos(hp - pi/6)
                        + 0.24*Math.cos(2*hp)
                        + 0.32*Math.cos(3*hp+pi/30)
                        - 0.20*Math.cos(4*hp - 63*pi/180),
                Sh = 1 + 0.015 * Cp * T,
                ex = (180/pi*hp-275) / 25,
                delthetarad = (30*pi/180) * Math.exp(-1 * (ex*ex)),
                Rc =  2 * Math.sqrt(Math.pow(Cp,7) / (Math.pow(Cp,7) + Math.pow(25,7))),
                RT = -1 * Math.sin(2*delthetarad) * Rc;

        dL = dL / (kl*Sl);
        dC = dC / (kc*Sc);
        dH = dH / (kh*Sh);

        // The CIE 00 color difference
        return Math.sqrt(dL*dL + dC*dC + dH*dH + RT*dC*dH);
    }

    private int isRedOrBlack(double[] pixel) {
        double[] lab = new double[3];
        ColorUtils.RGBToLAB((int)pixel[0], (int)pixel[1], (int)pixel[2], lab);

        int best_match = -1;
        double best_value = 1e9;
        for (int i = 0; i < colors.length; i++) {
            //double dist = colorDistance(pixel, colors[i]);
            //double dist = ColorUtils.distanceEuclidean(lab, colors[i]);
            double dist = ciede2000(lab, colors[i]);
            if (dist < best_value) {
                best_value = dist;
                best_match = i;
            }
        }
        return colorMapping[best_match];
    }

    private List<CardFeatureInImage> collectFeaturesInImage(Mat bwImage, Mat colorImg)
    {
        List<CardFeatureInImage> results = new ArrayList<CardFeatureInImage>();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(bwImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : contours)
        {
            CardFeatureInImage feature = getCardFeatureFromContour(bwImage, contour, colorImg);
            if (feature != null)
            {
                results.add(feature);
            }
        }
        return results;
    }

    public List<CardInImage> detectCardsInImage(Mat bwImage, Mat colorImg)
    {
        List<CardFeatureInImage> features = collectFeaturesInImage(bwImage, colorImg);
        List<CardInImage> cards = featuresToCards(features);
        return cards;
    }

    private List<CardInImage> featuresToCards(List<CardFeatureInImage> features)
    {
        ArrayList<CardInImage> results = new ArrayList<CardInImage>();
        for (CardFeatureInImage feature : features)
        {
            if (CARDS.contains("" + feature.getFeature()))
            {
                Point p1 = feature.getRectangle().center;
                double m = feature.getRectangle().size.height * feature.getRectangle().size.height;
                double best_angle = 180;
                Point best_point = null;
                Card best_card = null;
                for (CardFeatureInImage feature2 : features)
                {
                    if (SUITS.contains("" + feature2.getFeature()))
                    {
                        Point p2 = feature2.getRectangle().center;
                        if ((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) < 1.3 * m)
                        {
                            double line_angle = angle_p(p2.x - p1.x, p1.y - p2.y) / Math.PI * 180;
                            double angle_diff = ((line_angle + 360 - feature.getRectangle().angle) % 360);
                            double ANGLE_ERROR = 35;
                            double angle_error = Math.min(Math.abs(angle_diff - 90), Math.abs(angle_diff - 270));
                            if (angle_error < ANGLE_ERROR && angle_error < best_angle) {
                                char s = feature.getFeature();
                                char s2 = feature2.getFeature();
                                if (s == '9' && angle_diff > 180)
                                {
                                        s = '6';
                                }
                                best_card = Card.getCard(Suit.getSuit(s2), Rank.getRank(s));
                                best_point = feature2.getRectangle().center;
                                best_angle = angle_error;
                            }
                        }
                    }
                }
                if (best_card != null)
                {
                    results.add(new CardInImage(best_card, best_point));
                }
            }
        }
        return results;
    }

}
