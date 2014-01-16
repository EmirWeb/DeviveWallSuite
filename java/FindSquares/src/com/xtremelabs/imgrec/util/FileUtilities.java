package com.xtremelabs.imgrec.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtilities {

	public static void writeJsonToOutputFile(String json, String filePath){
		FileWriter fr = null;
		BufferedWriter br = null;
		
		try {
			fr = new FileWriter(new File(filePath));
			br = new BufferedWriter(fr);
			if (json != null) {
				br.write(json);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreams(br, fr);
		}
	}
	

	private static void closeStreams(Closeable... streams) {
		for (Closeable closeable : streams) {
			try {
				if (closeable != null) {
					closeable.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
