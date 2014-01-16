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

import com.google.gson.Gson;
import com.xtremelabs.imgrec.model.DeviceScreen;
import com.xtremelabs.imgrec.model.VirtualScreen;
import com.xtremelabs.imgrec.test.TestOutput;
import com.xtremelabs.imgrec.util.FileUtilities;

public class DetectScreens {

	private static final int THRESHOLD = 50;
	private static final int N = 11;

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("File needed");
			System.exit(0);
		}

		String filename = args[0];
		File imageFile = new File(filename);
		if (imageFile.exists()) {
			System.out.println("File " + imageFile.getName() + " exists, Processing...");
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			processImage(imageFile);
		} else {
			System.out.println("File does not exist");
			System.exit(0);
		}
		
		TestOutput.testOutput();
	}

	private static void processImage(File imageFile) {
		long startTime = System.currentTimeMillis();
		Mat deviceWallPink = Highgui.imread(imageFile.getAbsolutePath());
		Mat sharpenedImage = deviceWallPink.clone();

		sharpenImage(deviceWallPink, sharpenedImage);

		List<Screen> screens = new ArrayList<Screen>();
		findSquares(sharpenedImage, screens);
		System.out.println("Screen Count: " + screens.size());

		List<Point> virtualScreen = virtualScreen(screens);
		System.out.println("Virtual Screen is: " + virtualScreen);

		List<MatOfPoint> virtualScreenList = new ArrayList<MatOfPoint>();
		MatOfPoint virtualScreenBox = new MatOfPoint();
		virtualScreenBox.fromList(virtualScreen);
		virtualScreenList.add(virtualScreenBox);
		
		Mat virtualScreenImage = sharpenedImage.clone();
		Core.polylines(virtualScreenImage, virtualScreenList, true, new Scalar(0, 255, 0));
		String virtualScreenfilename = "virtualScreen.png";
		System.out.println("Writing " + virtualScreenfilename);
		Highgui.imwrite(virtualScreenfilename, virtualScreenImage);

		List<String> screenIdList = new ArrayList<String>();
		for (int i = 0; i < screens.size(); i++) {
			screenIdList.add(getIdFromScreen(screens.get(i), sharpenedImage, i));
		}

		drawSquares(sharpenedImage, screens, true);
		
		String json = getJsonString(virtualScreen, screens, screenIdList);
		
		FileUtilities.writeJsonToOutputFile(json, "squares_data2.json");
		
		printReport(startTime);
	}
	
	private static void printReport(long startTime) {
		long completionTime = System.currentTimeMillis() - startTime;
		System.out.println("Screen Detection and OCR Completed in " + completionTime + "ms");
	}

	private static String getJsonString(List<Point> virtualScreen, List<Screen> screens, List<String> screenIdList) {
		if (virtualScreen == null || screens == null)
			return "";
		if (virtualScreen.size() != 4)
			return "";

		double virtualScreenWidth = virtualScreen.get(1).x - virtualScreen.get(0).x;
		double virtualScreenHeight = virtualScreen.get(3).y - virtualScreen.get(0).y;

		VirtualScreen vScreen = new VirtualScreen();
		vScreen.setWidth(virtualScreenWidth);
		vScreen.setHeight(virtualScreenHeight);

		for (int i = 0; i < screens.size(); i++) {
			String id = screenIdList.get(i);

			int screenId = id.length() > 0 ? Integer.parseInt(screenIdList.get(i)) : 0;
			Screen screen = screens.get(i);
			screen.getRelativeDetails(virtualScreen);
			
			DeviceScreen deviceScreen = new DeviceScreen();
			deviceScreen.setId(screenId);
			deviceScreen.setxRelative(screen.topLeftRelative.x);
			deviceScreen.setyRelative(screen.topLeftRelative.y);
			deviceScreen.setWidthRelative(screen.widthRelative);
			deviceScreen.setHeightRelative(screen.heightRelative);
			vScreen.addScreen(deviceScreen);
		}
		
		Gson gson = new Gson();
		return gson.toJson(vScreen, VirtualScreen.class);
	}


	public static void drawSquares(Mat image, final List<Screen> screens, boolean fillSquares) {

		ArrayList<MatOfPoint> squares = new ArrayList<MatOfPoint>();
		for (int i = 0; i < screens.size(); i++) {
			squares.add(new MatOfPoint(screens.get(i).contour.toArray()));
		}

		if (squares != null) {
			ArrayList<MatOfPoint> p = new ArrayList<MatOfPoint>(squares);
			if (fillSquares)
				Imgproc.drawContours(image, p, -1, new Scalar(0, 255, 0), -1);
			else
				Imgproc.drawContours(image, p, -1, new Scalar(0, 255, 0), 0);

			String filename = "squares.png";
			System.out.println(String.format("Writing %s", filename));
			Highgui.imwrite(filename, image);
		}
	}

	private static String getIdFromScreen(Screen screen, Mat srcImage, int index) {

		Mat aScreen = new Mat(srcImage, new Rect(screen.approxPoints.get(0), screen.approxPoints.get(2)));
		String filename = "screen" + index + ".png";
		System.out.println(String.format("Writing %s", filename));
		Highgui.imwrite(filename, aScreen);

		System.out.println("Converting " +filename + " to tiff format for ID detection");
		System.out.println("output" + index + ".tif");
		String cropCommand = "/usr/local/bin/convert " + filename + " -crop " + (aScreen.size().width - 50) + "x" + (aScreen.size().height - 50) + "+25+25 output" + index + ".tif";
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
			System.out.println("Detected ID: " + sb.toString());
			return (sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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

	private static void findSquares(Mat image, List<Screen> screens) {

		screens.clear();
		Size sz = new Size(image.width() & -2, image.height() & -2);
		Mat pyr = new Mat(new Size(image.width() / 2, image.height() / 2), image.channels());
		Mat timg = image.clone();
		Mat gray = new Mat(sz, 1);

		Imgproc.pyrDown(timg, pyr);
		Imgproc.pyrUp(pyr, timg);

		String filename = "timg.png";
		System.out.println("Writing " + filename);
		Highgui.imwrite(filename, timg);

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
				System.out.println("Writing " + filename);
				Highgui.imwrite(filename, channels.get(c));
				findScreensInChannel(gray, channels.get(c), screens);
			}

		} else {
			findScreensInChannel(gray, timg.clone(), screens);
		}
	}

	private static void findScreensInChannel(Mat gray, Mat tgray, List<Screen> screens) {
		long startTime = System.currentTimeMillis();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		for (int i = 0; i < N; i++) {
			if (i == 0) {
				Imgproc.Canny(tgray, gray, 0, THRESHOLD);
				Imgproc.dilate(gray, gray, new Mat());

				String filename = "gray.png";
				System.out.println("Writing " + filename);
				Highgui.imwrite(filename, gray);
			} else {
				Imgproc.threshold(tgray, gray, (i + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);
			}

			Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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
											System.out.println("\tFound more accurate square: Removing " + currScreen.approxPoints + " and adding " + screen.approxPoints + " with area " + screen.area + " and center " + screen.center);
											screens.remove(x);
											screens.add(screen);
										}
										seenThisSquare = true;
										break;
									}
								}
							}

							if (!seenThisSquare) {
								System.out.println("\tHaven't seen this square, adding it: " + screen.approxPoints + " with area " + screen.area + " and center " + screen.center);

								screens.add(screen);
							}
						}
					}
				}
			}
			contours = new ArrayList<MatOfPoint>();
		}
		System.out.println("findScreensInChannel completed in : " + (System.currentTimeMillis() - startTime));
	}

	public static double angle(Point pt1, Point pt2, Point pt0) {
		double dx1 = pt1.x - pt0.x;
		double dy1 = pt1.y - pt0.y;
		double dx2 = pt2.x - pt0.x;
		double dy2 = pt2.y - pt0.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
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

	private static void sharpenImage(Mat deviceWallPink, Mat sharpenedImage) {
		Imgproc.GaussianBlur(deviceWallPink, sharpenedImage, new Size(0, 0), 3);
		Core.addWeighted(deviceWallPink, 1.5, sharpenedImage, -0.5, 0, sharpenedImage);

		String sharpenedImageFileName = "sharpened.png";
		System.out.println(String.format("Writing %s", sharpenedImageFileName));
		Highgui.imwrite(sharpenedImageFileName, sharpenedImage);
	}

}
