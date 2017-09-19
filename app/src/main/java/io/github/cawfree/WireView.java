package io.github.cawfree;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

/** All credit to the-graph for these beautiful and playful little wires. https://github.com/flowhub/the-graph/blob/master/the-graph/the-graph-edge.js */
public class WireView extends View {

    /* Static Declarations. */
//    private static final float EPSILON          = 0.1f;

    /** Base implementation for a Wire. */
    public static class Wire {
        /* Member Variables. */
        private final Point mStart;
        private final Point mFinish;
        private final int   mColor;
        /** Constructor. */
        public Wire(final Point pStart, final Point pFinish, final int pColor) {
            // Initialize Member Variables.
            this.mStart  = pStart;
            this.mFinish = pFinish;
            this.mColor  = pColor;
        }
        /* Getters. */
        public final Point getStart() {
            return this.mStart;
        }
        public final Point getFinish() {
            return this.mFinish;
        }
        public final int   getColor() {
            return this.mColor;
        }
    }

    /** Returns a point on a cubic bezier curve. Reference: http://en.wikipedia.org/wiki/File:Bezier_3_big.gif */
    public static final float[] findPointOnCubicBezier(final float[] pBuffer, final float p, final float sx, final float sy, final float c1x, final float c1y, final float c2x, final float c2y, final float ex, final float ey) {
        // p is percentage from 0 to 1
        float op = 1 - p;
        // 3 green points between 4 points that define curve
        float g1x = sx * p + c1x * op;
        float g1y = sy * p + c1y * op;
        float g2x = c1x * p + c2x * op;
        float g2y = c1y * p + c2y * op;
        float g3x = c2x * p + ex * op;
        float g3y = c2y * p + ey * op;
        // 2 blue points between green points
        float b1x = g1x * p + g2x * op;
        float b1y = g1y * p + g2y * op;
        float b2x = g2x * p + g3x * op;
        float b2y = g2y * p + g3y * op;
        // Point on the curve between blue points
        float x = b1x * p + b2x * op;
        float y = b1y * p + b2y * op;
        // Return the Point.
        pBuffer[0] = x;
        pBuffer[1] = y;
        // Return the Buffer.
        return pBuffer;
    }

    /** Fetches the shifted point along the Cubic Bezier Curve. An estimate of perpendicularity. */
    public static final float[] getShiftedPoint(final float[] pBuffer, final float epsilon, float sx, float sy, float c1x, float c1y, float c2x, float c2y, float ex, float ey) {
        // Return the point along the cubic Bezier curve.
        return WireView.findPointOnCubicBezier(pBuffer, 0.5f + epsilon, sx, sy, c1x, c1y, c2x, c2y, ex, ey);
    }

    /** Finds the point on a line using the straight-line equation. (y = mx + c) */
    public static final float[] getLinePoint(final float[] pBuffer, final float x, final float y, final float m, final float b, final float pOffset, final float pIsFlipped) { // no flip, use 1
        // Declare the base parameterization.
        final float z = (float)Math.sqrt(1 + m*m);
        final float x1 = x + pOffset/z;
        final float y1;
        if(Math.abs(m) == Float.POSITIVE_INFINITY || Math.abs(m) == Float.NEGATIVE_INFINITY) {
            y1 = y + (pIsFlipped) * pOffset;
        }
        else {
            y1 = (m * x1) + b;
        }
        // Update the Buffer.
        pBuffer[0] = x1;
        pBuffer[1] = y1;
        // Return the Buffer.
        return pBuffer;
    }

    /** Returns points of the getPerpendicular line of length l, centered at (x,y). */
    public static final void getPerpendicular(final float[] pBufferS, final float[] pBufferF, float x, float y, float oldM, float l) {
        // Calculate line metrics.
        float m = -1f/oldM;
        float b = y - m*x;
        // Return the Perpendicular Line.
        WireView.getLinePoint(pBufferS, x, y, m, b, l/2, 1.0f);
        WireView.getLinePoint(pBufferF, x, y, m, b, l/-2, 1.0f);
    }

    /* Member Variables. */
    private List<Wire> mWires;
    private Paint      mWirePaint;

    /** Constructor. */
    public WireView(final Context pContext) {
        // Implement the Parent.
        super(pContext);
        // Initialize the WireView.
         this.init(pContext, null, -1);
    }

    /** Constructor. */
    public WireView(final Context pContext, final AttributeSet pAttributeSet) {
        // Implement the Parent.
        super(pContext, pAttributeSet);
        // Initialize the WireView.
        this.init(pContext, pAttributeSet, -1);
    }

    /** Constructor. */
    public WireView(final Context pContext, final AttributeSet pAttributeSet, final int pDefStyle) {
        // Implement the Parent.
        super(pContext, pAttributeSet, pDefStyle);
        // Initialize the WireView.
        this.init(pContext, pAttributeSet, pDefStyle);
    }

    /** Look and feel constants. */
    protected final float getReverseTolerance() { return 5.0f;           }
    protected final float getCurveRadius()      { return 72.0f / 200.0f; }
    protected final float getTweakRadius()      { return 72.0f / 2;      }
    protected final float getArrowLength()      { return 12.0f;          }
    protected final float getWireWidth()        { return 3.0f;           }
    protected final float getEpsilon()          { return 0.1f;           }

    /** Initialization Method. */
    @SuppressWarnings("unused") private void init(final Context pContext, final AttributeSet pAttributeSet, final int pDefStyle) {
        // Allocate the Wires.
        this.mWires     = new ArrayList<>();
        // Allocate the Paint.
        this.mWirePaint = new Paint();
        // Assert that we'll be using a Stroke.
        this.getWirePaint().setStyle(Paint.Style.STROKE);
        // Define the stroke width.
        this.getWirePaint().setStrokeWidth(this.getWireWidth());
        // Use anti-aliasing.
        this.getWirePaint().setAntiAlias(true);
    }

    /** Define the dimensions of the View. */
    @Override protected final void onMeasure(final int pWidthMeasureSpec, final int pHeightMeasureSpec) {
        // Define the MeasuredDimension using the supplied parameters.
        setMeasuredDimension(MeasureSpec.getSize(pWidthMeasureSpec), MeasureSpec.getSize(pHeightMeasureSpec));
    }

    /** The drawing operation; where the real meat of the View lies. */
    @Override protected final void onDraw(final Canvas pCanvas) {
        // Define rendering buffers.
        final float[] lCentre = new float[2];
        final float[] lPlus   = new float[2];
        final float[] lMinus  = new float[2];
        // Declare the Path we'll be rendering.
        final Path lPath = new Path();
        // Iterate the Wires.
        for(final Wire lWire : this.getWires()) {
            // Apply the Wire Color.
            this.getWirePaint().setColor(lWire.getColor());
            // Organic / curved edge
            float c1X, c1Y, c2X, c2Y;
            // Are we handling a reversed Wire?
            if(lWire.getFinish().x - this.getReverseTolerance() < lWire.getStart().x) {
                // Calculate the CurveFactor for a reverse wire.
                final float lCurveFactor = (lWire.getStart().x - lWire.getFinish().x) * this.getCurveRadius();
                // Determine whether the points are going to begin.
                if(Math.abs(lWire.getFinish().y-lWire.getStart().y) < this.getTweakRadius()) {
                    // Define the loop-back co-ordinates.
                    c1X =  lWire.getStart().x + lCurveFactor;
                    c1Y =  lWire.getStart().y - lCurveFactor;
                    c2X = lWire.getFinish().x - lCurveFactor;
                    c2Y = lWire.getFinish().y - lCurveFactor;
                }
                else {
                    // Stick out a little.
                    c1X =  lWire.getStart().x + lCurveFactor;
                    c1Y =  lWire.getStart().y + (lWire.getFinish().y > lWire.getStart().y ? lCurveFactor : -lCurveFactor);
                    c2X = lWire.getFinish().x - lCurveFactor;
                    c2Y = lWire.getFinish().y + (lWire.getFinish().y > lWire.getStart().y ? -lCurveFactor : lCurveFactor);
                }
            }
            else {
                // Define the control points for the Cubic Bezier.
                c1X = lWire.getStart().x + (lWire.getFinish().x - lWire.getStart().x)/2;
                c1Y = lWire.getStart().y;
                c2X = c1X;
                c2Y = lWire.getFinish().y;
            }
            // Reset the path.
            lPath.reset();
            // Move to the beginning of the Wire.
            lPath.moveTo(lWire.getStart().x, lWire.getStart().y);
            // Buffer the cubic curve.
            lPath.cubicTo(c1X, c1Y, c2X, c2Y, lWire.getFinish().x, lWire.getFinish().y);
            // Render the Path.
            pCanvas.drawPath(lPath, this.getWirePaint());
            // Find the center of the bezier curve. (Use a progression of 50%.)
            WireView.findPointOnCubicBezier(lCentre, 0.5f, lWire.getStart().x, lWire.getStart().y, c1X, c1Y, c2X, c2Y, lWire.getFinish().x, lWire.getFinish().y);
            // Find offsets.
            WireView.getShiftedPoint(lPlus, this.getEpsilon(), lWire.getStart().x, lWire.getStart().y, c1X, c1Y, c2X, c2Y, lWire.getFinish().x, lWire.getFinish().y);
            WireView.getShiftedPoint(lMinus, -this.getEpsilon(), lWire.getStart().x, lWire.getStart().y, c1X, c1Y, c2X, c2Y, lWire.getFinish().x, lWire.getFinish().y);
            // Calculate line variables.
            final float   lM     = 1 * (lPlus[1] - lMinus[1]) / (lPlus[0] - lMinus[0]);
            final float   lB     = lCentre[1] - (lM * lCentre[0]);
            // Declare the ArrowLength.
            float lArrowLength = this.getArrowLength(); /** TODO: Dip. */
            // Which direction should arrow point?
            if(lPlus[0] > lMinus[0]) {
                // Reflect the arrow.
                lArrowLength *= -1;
            }
            // Enabled flip.
            WireView.getLinePoint(lCentre, lCentre[0], lCentre[1], lM, lB, -1*lArrowLength/2, 1);
            // If there's no gradient, define whether to point up or down.
            final float   lFlip     = lPlus[1] > lMinus[1] ? -1 : 1;
            // Fetch the Perpendicular points. (Re-use the Plus/Minus.)
            WireView.getPerpendicular(lPlus, lMinus, lCentre[0], lCentre[1], lM, lArrowLength * 0.9f);
            // Buffer the ArrowTip into the Centre.
            WireView.getLinePoint(lCentre, lCentre[0], lCentre[1], lM, lB, lArrowLength, lFlip);
            // Render the ArrowHead.
            lPath.moveTo(  lPlus[0],   lPlus[1]);
            lPath.lineTo(lCentre[0], lCentre[1]);
            lPath.lineTo( lMinus[0],  lMinus[1]);
            // Draw the Wire.
            pCanvas.drawPath(lPath, this.getWirePaint());
        }

    }

    /* Getters. */
    public final List<Wire> getWires() {
        return this.mWires;
    }

    private final Paint getWirePaint() {
        return this.mWirePaint;
    }
 
}