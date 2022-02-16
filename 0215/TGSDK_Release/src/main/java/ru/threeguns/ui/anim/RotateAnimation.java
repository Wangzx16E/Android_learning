package ru.threeguns.ui.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class RotateAnimation extends Animation {
	public static final int LEFT_IN = 0;
	public static final int LEFT_OUT = 1;
	public static final int RIGHT_IN = 2;
	public static final int RIGHT_OUT = 3;
	public static final boolean ROTATE_INCREASE = false;
	public static final float DEPTH_Z = 400.0f;
	public static final long DURATION = 350;
	private final int type;
	private final float centerX;
	private final float centerY;
	private Camera camera;

	public RotateAnimation(float cX, float cY, int type) {
		centerX = cX;
		centerY = cY;
		this.type = type;
		setDuration(DURATION);
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		camera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation transformation) {

		float from = 0.0f, to = 0.0f;
		float depth = 0f;
		switch (type) {
		case LEFT_IN:
			from = 90f;
			to = 0f;
			depth = (1.0f - interpolatedTime) * DEPTH_Z;
			break;
		case LEFT_OUT:
			from = 0f;
			to = -90f;
			depth = interpolatedTime * DEPTH_Z;
			break;
		case RIGHT_IN:
			from = -90f;
			to = 0f;
			depth = (1.0f - interpolatedTime) * DEPTH_Z;
			break;
		case RIGHT_OUT:
			from = 0f;
			to = 90f;
			depth = interpolatedTime * DEPTH_Z;
			break;
		}
		float degree = from + (to - from) * interpolatedTime;
		final Matrix matrix = transformation.getMatrix();

		camera.save();
		camera.translate(0f, 0f, depth);
		camera.rotateY(degree);
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);

	}

}