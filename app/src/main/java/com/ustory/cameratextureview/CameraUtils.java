package com.ustory.cameratextureview;

import android.hardware.Camera;

import java.util.Comparator;

public class CameraUtils {

    public static Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    };


}
