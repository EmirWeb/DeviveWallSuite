package com.xtremelabs.imgrec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

class Screen {
	String id;
	MatOfPoint2f contour;
	MatOfPoint2f approxCurve;
	ArrayList<Point> approxPoints;
	double area;
	Point center;
	Point topLeftRelative;
	double widthRelative;
	double heightRelative;

	public Screen(MatOfPoint2f Contour) {
		contour = Contour;
		approxCurve = new MatOfPoint2f();
		approxPoints = new ArrayList<Point>();
		Imgproc.approxPolyDP(contour, approxCurve, 3, true);
		Converters.Mat_to_vector_Point(approxCurve, approxPoints);
		area = Imgproc.contourArea(contour);
	}

	public void sortPoints() {
		center = new Point(0, 0);
		for (int x = 0; x < approxPoints.size(); x++) {
			center.x += approxPoints.get(x).x;
			center.y += approxPoints.get(x).y;
		}
		center.x /= approxPoints.size();
		center.y /= approxPoints.size();
		// System.out.println("Center is "+center);

		final double[] atans = new double[approxPoints.size()];
		for (int x = 0; x < approxPoints.size(); x++) {
			atans[x] = Math.atan2(approxPoints.get(x).y - center.y, approxPoints.get(x).x - center.x);
		}

		Integer[] idx = new Integer[approxPoints.size()];
		for (int i = 0; i < idx.length; i++)
			idx[i] = i;
		Arrays.sort(idx, new Comparator<Integer>() {
			public int compare(Integer i1, Integer i2) {
				return Double.compare(atans[i1], atans[i2]);
			}
		});

		ArrayList<Point> sortedApproxPoints = new ArrayList<Point>();
		int index = 0;
		while (approxPoints.size() != sortedApproxPoints.size()) {
			for (int i = 0; i < idx.length; i++) {
				if (idx[i] == index) {
					sortedApproxPoints.add(approxPoints.get(i));
					index++;
					if (index >= approxPoints.size()) {
						break;
					}
				}
			}
		}
		approxPoints = sortedApproxPoints;
		// System.out.println("sorted points are "+approxPoints);
	}

	public void getRelativeDetails(List<Point> virtualScreen) {
		double virtualScreenWidth = virtualScreen.get(1).x - virtualScreen.get(0).x;
		double virtualScreenHeight = virtualScreen.get(3).y - virtualScreen.get(0).y;

		double tlX = (approxPoints.get(0).x - virtualScreen.get(0).x) / virtualScreenWidth;
		double tlY = (approxPoints.get(0).y - virtualScreen.get(0).y) / virtualScreenHeight;

		topLeftRelative = new Point(tlX, tlY);
		widthRelative = (approxPoints.get(1).x - virtualScreen.get(0).x) / virtualScreenWidth;
		heightRelative = (approxPoints.get(3).y - virtualScreen.get(0).y) / virtualScreenHeight;
	}
}