package com.xtremelabs.imgrec;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

//
// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
//
class DetectFaceDemo {

	static int thresh = 50;
	static int N = 11;// 11;

	public void run(File file) {

		Mat deviceWallPink = Highgui.imread("/Users/devfloater56/Development/xl-device_wall-suite/java/FindSquares/resources/image10.jpg");
		Mat sharpenedImage = deviceWallPink.clone();
		Imgproc.GaussianBlur(deviceWallPink, sharpenedImage, new Size(0, 0), 3);
		Core.addWeighted(deviceWallPink, 1.5, sharpenedImage, -0.5, 0, sharpenedImage);

		String filename = "sharpened.png";
		System.out.println(String.format("Writing %s", filename));
		Highgui.imwrite(filename, sharpenedImage);

		List<Screen> screens = new ArrayList<Screen>();
		DetectFaceDemo.findSquares(sharpenedImage, screens, false);
		System.out.println("Screen count: " + screens.size());

		List<Point> virtualScreen = DetectFaceDemo.virtualScreen(screens);
		System.out.println("Virtual screen is " + virtualScreen);
		
		List<MatOfPoint> virtualScreenList = new ArrayList<MatOfPoint>();
		MatOfPoint virtualScreenBox = new MatOfPoint();
		virtualScreenBox.fromList(virtualScreen);
		virtualScreenList.add(virtualScreenBox);
		
		Mat virtualScreenImage = sharpenedImage.clone();
		Core.polylines(virtualScreenImage, virtualScreenList, true, new Scalar(0, 255, 0));
		
		filename = "virtualScreen.png";
		System.out.println(String.format("Writing %s", filename));
		Highgui.imwrite(filename, virtualScreenImage);

		List<String> screenIdList = new ArrayList<String>();
		// Deskew a screen before OCR
		for (int i = 0; i < screens.size(); i++) {
			screenIdList.add(DetectFaceDemo.getIdFromScreen(screens.get(i), sharpenedImage, i));
		}

		DetectFaceDemo.drawSquares(sharpenedImage, screens, true);
		System.out.println(DetectFaceDemo.getJsonString(virtualScreen, screens, screenIdList));
		FileWriter fr = null;
		BufferedWriter br = null;
		String json = DetectFaceDemo.getJsonString(virtualScreen, screens, screenIdList);
		try {
			fr = new FileWriter(new File("squares_data.json"));
			br = new BufferedWriter(fr);
			if(json != null){
				br.write(json);
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			closeStreams(br,fr);
		}
		System.out.println(json);
		// Deskew based on virtual corners and run again for more straightened
		// rectangles?
		// Write images of each screen to disk as pngs, run tesseract on them
		// (no need to convert to tif)

		// Next steps:
		// If you find extra rectangles due to high N, find out how to reduce
		// them to minimum rectangles (compare top left corners with each other)
		// Pick one gray to focus on (7gray?) and that'll be default. But if it
		// doesn't catch all rectangles, then we can fall back to N = 11 and
		// know how to parse out extras
		// Straighten image
		// Throw away rectangles whose average colour isn't pure red
		// Throw away duplicate contours. Need only 1 per screen. If two
		// contours have close top left corner, take first.
		// Find top left corner of rectangles relative to virtual top left
		// corner of them all together
		// OCR! Use numbers, not letters

		// Test:
		// Devices at various angles together
		// An actual picture from a camera, with some noise
		// Low lighting
		// Blurry photo
		// If only some squares detected, then if I manually draw on the photo
		// some solid lines on the missed screens, are they detected?
		System.out.println("Done");
	}

	private static void closeStreams(Closeable ... streams){
		  for (Closeable closeable : streams) {
			try {
				if(closeable != null){
					closeable.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	  }
	public static String getJsonString(List<Point> virtualScreen, List<Screen> screens, List<String> screenIdList) {
		if (virtualScreen == null || screens == null)
			return "";
		if (virtualScreen.size() != 4)
			return "";

		double virtualScreenWidth = virtualScreen.get(1).x - virtualScreen.get(0).x;
		double virtualScreenHeight = virtualScreen.get(3).y - virtualScreen.get(0).y;

		String jsonString = "{";
		jsonString += "\"width\": " + virtualScreenWidth + ", ";
		jsonString += "\"height\": " + virtualScreenHeight + ", ";
		jsonString += "\"screens\": [";

		// int screenId = 0; // When we can detect the numbers on screens, use
		// that instead
		for (int i = 0; i < screens.size(); i++) {
			String sId = screenIdList.get(i);

			int screenId = sId.length() > 0 ? Integer.parseInt(screenIdList.get(i)) : 0;
			Screen screen = screens.get(i);
			screen.getRelativeDetails(virtualScreen);
			if (i != 0)
				jsonString += ",";
			jsonString += "{\"id\": " + screenId + ", ";
			jsonString += "\"xRelative\": " + screen.topLeftRelative.x + ", ";
			jsonString += "\"yRelative\": " + screen.topLeftRelative.y + ", ";
			jsonString += "\"widthRelative\": " + screen.widthRelative + ", ";
			jsonString += "\"heightRelative\": " + screen.heightRelative + "}";
		}

		jsonString += "]}";
		return jsonString;
	}

	public static String getIdFromScreen(Screen screen, Mat srcImage, int index) {

		Mat aScreen = new Mat(srcImage, new Rect(screen.approxPoints.get(0), screen.approxPoints.get(2)));
		String filename = "screen" + index + ".png";
		System.out.println(String.format("Writing %s", filename));
		Highgui.imwrite(filename, aScreen);

		String cropCommand = "/usr/local/bin/convert " + filename + " -crop " + (aScreen.size().width - 50) + "x" + (aScreen.size().height - 50) + "+25+25 output" + index + ".tif";
		System.out.println(cropCommand);
		Runtime runtime = Runtime.getRuntime();
		FileReader fr = null;
		BufferedReader br = null;

		try {
			Process process = runtime.exec(cropCommand);
			int returnCode = process.waitFor();
			if (returnCode != 0)
				System.out.println("error with convert command: " + returnCode);

			String ocrCommand = "/usr/local/bin/tesseract output" + index + ".tif output digits";
			process = runtime.exec(ocrCommand);
			returnCode = process.waitFor();
			if (returnCode != 0)
				System.out.println("error with tesseract command: " + returnCode);

			fr = new FileReader(new File("output.txt"));
			br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line.trim());
				line = br.readLine();
			}
			sb.trimToSize();
			System.out.println(sb.toString());
			return (sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return "";
	}

	public static List<Point> virtualScreen(final List<Screen> screens) {
		List<Point> result = new ArrayList<Point>();
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = 0, maxY = 0;
		for (int i = 0; i < screens.size(); i++) {
			List<Point> approxPoints = screens.get(i).approxPoints;
			for (int j = 0; j < approxPoints.size(); j++) {
				double currX = approxPoints.get(j).x;
				double currY = approxPoints.get(j).y;
				if (currX < minX)
					minX = currX;
				if (currX > maxX)
					maxX = currX;
				if (currY < minY)
					minY = currY;
				if (currY > maxY)
					maxY = currY;
			}
		}
		// Find smallest and largest x and y. With that you can generate the 4
		// corners.
		result.add(new Point(minX, minY));
		result.add(new Point(maxX, minY));
		result.add(new Point(maxX, maxY));
		result.add(new Point(minX, maxY));
		return result;
	}

	// the function draws all the squares in the image
	public static void drawSquares(Mat image, final List<Screen> screens, boolean fillSquares) {
		// Mat imageBackup = image.clone();
		List<MatOfPoint> squares = new ArrayList<MatOfPoint>();
		for (int i = 0; i < screens.size(); i++) {
			squares.add(new MatOfPoint(screens.get(i).contour.toArray()));
		}

		if (squares != null) {
			List<MatOfPoint> p = new ArrayList<MatOfPoint>(squares);
			if (fillSquares)
				Imgproc.drawContours(image, p, -1, new Scalar(0, 255, 0), -1);
			else
				Imgproc.drawContours(image, p, -1, new Scalar(0, 255, 0), 0);

			String filename = "squares.png";
			System.out.println(String.format("Writing %s", filename));
			Highgui.imwrite(filename, image);
		}
	}

	// helper function:
	// finds a cosine of angle between vectors
	// from pt0->pt1 and from pt0->pt2
	public static double angle(Point pt1, Point pt2, Point pt0) {
		double dx1 = pt1.x - pt0.x;
		double dy1 = pt1.y - pt0.y;
		double dx2 = pt2.x - pt0.x;
		double dy2 = pt2.y - pt0.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}

	public static void findSquares(Mat image, List<Screen> screens, boolean fastSearch) {
		screens.clear();

		Mat pyr = null;
		Mat timg = null;
		Mat gray = null;
		timg = image.clone();

		Size sz = new Size(image.width() & -2, image.height() & -2);
		gray = new Mat(sz, 1);
		pyr = new Mat(new Size(image.width() / 2, image.height() / 2), image.channels());

		// This isn't necessary?
		Imgproc.pyrDown(timg, pyr);
		Imgproc.pyrUp(pyr, timg);
		String filename = "timg.png";
		System.out.println(String.format("Writing %s", filename));
		Highgui.imwrite(filename, timg);

		// String filename;
		System.out.println("image.channels() = " + timg.channels());
		if (timg.channels() > 1) {
			List<Mat> channels = new ArrayList<Mat>();
			for (int c = 0; c < timg.channels(); c++) {
				channels.add(new Mat(sz, 1));
			}
			Core.split(timg, channels);

			// 0 = blue, 1 = green, 2 = red
			for (int c = 0; c < timg.channels(); c++) {
				filename = "channels" + c + ".png";
				System.out.println(String.format("Writing %s", filename));
				Highgui.imwrite(filename, channels.get(c));
				if (!fastSearch)
					findScreensInChannel(gray, channels.get(c), screens);
			}
			if (fastSearch)
				findScreensInChannel(gray, channels.get(0), screens);
			// tgray = channels.get(2);

		} else {
			findScreensInChannel(gray, timg.clone(), screens);
			// tgray = timg.clone();
		}

	}

	private static void findScreensInChannel(Mat gray, Mat tgray, List<Screen> screens) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		for (int i = 0; i < N; i++) {
			if (i == 0) {
				Imgproc.Canny(tgray, gray, 0, thresh);

				Mat kernel = new Mat();
				Imgproc.dilate(gray, gray, kernel);
				String filename = "gray.png";
				System.out.println(String.format("Writing %s", filename));
				Highgui.imwrite(filename, gray);
			} else {
				Imgproc.threshold(tgray, gray, (i + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);

			}

			Mat hierarchy = new Mat();
			Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

			for (int j = 0; j < contours.size(); j++) {
				if (contours.get(j) != null) {
					MatOfPoint2f contour = new MatOfPoint2f(contours.get(j).toArray());
					Screen screen = new Screen(contour);

					if (screen.approxCurve.total() == 4 && screen.area > 10000) {
						double maxCosine = 0;

						for (int k = 2; k < 5; k++) {
							double cosine = Math.abs(angle(screen.approxPoints.get(k % 4), screen.approxPoints.get(k - 2), screen.approxPoints.get(k - 1)));
							maxCosine = Math.max(maxCosine, cosine);
						}
						if (maxCosine < 0.2) {
							boolean seenThisSquare = false;
							screen.sortPoints();
							for (int x = 0; x < screens.size(); x++) {
								Screen currScreen = screens.get(x);
								if (screen.approxPoints.size() == currScreen.approxPoints.size()) {

									if ((Math.abs(screen.center.x - currScreen.center.x) + Math.abs(screen.center.y - currScreen.center.y)) < 5) {

										if (screen.area > currScreen.area) {
											System.out.println("  Found more accurate square: Removing " + currScreen.approxPoints + " and adding " + screen.approxPoints + " with area " + screen.area + " and center " + screen.center);
											screens.remove(x);
											screens.add(screen);
										}
										seenThisSquare = true;
										break;
									}
								}
							}

							if (!seenThisSquare) {
								System.out.println("  Haven't seen this square, adding it: " + screen.approxPoints + " with area " + screen.area + " and center " + screen.center);

								screens.add(screen);
							}
						}
					}
				}
			}
			contours = new ArrayList<MatOfPoint>();
		}
	}
}

public class FindSquaresCleanup {
	public static void main(String[] args) {
		if(args.length == 0){
			System.out.println("file needed");
			return;
		}
		
		String fileName = args[0];
		
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new DetectFaceDemo().run(new File(fileName));
	}
}
