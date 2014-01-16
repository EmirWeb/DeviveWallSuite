package com.xtremelabs.imgrec.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.xtremelabs.imgrec.model.VirtualScreen;

public class TestOutput {

	/**
	 * @param args
	 */
	public static void testOutput(){
//	public static void main(String[] args) {
		String file1 = readFile("squares_data.json");
		String file2 = readFile("squares_data2.json");
		
		Gson gson = new Gson();
		VirtualScreen virtualScreen1 = gson.fromJson(file1, VirtualScreen.class);
		VirtualScreen virtualScreen2 = gson.fromJson(file2, VirtualScreen.class);
		
		if(virtualScreen1.equals(virtualScreen2)){
			System.out.println("outputs match");
		}
		else{
			System.out.println("outputs dont match");
		}
		
	}

	private static String readFile(String file) {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			
			fr = new FileReader(new File(file));
			br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line.trim());
				line = br.readLine();
			}
			sb.trimToSize();
			
			return (sb.toString());
		} catch (IOException e) {
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

}
