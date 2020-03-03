package stageunimib.federicoragusa816623.shimmersensing.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

//import com.github.lzyzsd.circleprogress.ArcProgress;
//
//public class ArcAngleAnimation extends Animation {
//
//    private ArcProgress arcView;
//
//    private float oldAngle;
//    private float newAngle;
//
//    public ArcAngleAnimation(ArcProgress arcView, int newAngle) {
//        this.oldAngle = arcView.getArcAngle();
//        this.newAngle = newAngle;
//        this.arcView = arcView;
//    }
//
//    @Override
//    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
//        float angle = 0 + ((newAngle - oldAngle) * interpolatedTime);
//
//        arcView.setArcAngle(angle);
//        arcView.requestLayout();
//    }
//}