package com.cloudaward;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * °¢ÀïÂèÂèÍ¼±ê¿âÏÂÔØÆ÷
 * 
 * @author Navy
 *
 */
public class IconfontDownloader {

	public static String urlPrefix = "http://www.iconfont.cn/uploads/fonts/font-";
	public static String urlSuffix = "-.png";
	public static String colorArgs = "color=";
	public static String sizeArgs = "size=";

	public static String outputDir = "iconfont-download";

	public static Map<String, String> sizeNameMap = new HashMap<String, String>();

	static {
		sizeNameMap.put("32", "drawable-mdpi");
		sizeNameMap.put("48", "drawable-hdpi");
		sizeNameMap.put("64", "drawable-xhdpi");
		sizeNameMap.put("96", "drawable-xxhdpi");
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Input argument error, Usage: java -jar IconfontDownloader.jar $id $color [$saveFileName]");
			return;
		}
		long begin = System.nanoTime();
		System.out.println("Download starting...");
		System.out.println("Download directory is " + new File(outputDir).getAbsolutePath());
		if (args.length == 3) {
			download(args[0], args[1], args[2]);
		}else {
			download(args[0], args[1], null);
		}
		System.out.println("Download end, total spent is " + (System.nanoTime() - begin) / 1.0e9 + " second(s)");
	}

	public static void download(String id, String color, String outputFileName) {
		for (String size : sizeNameMap.keySet()) {
			final String strUrl = urlPrefix + id + urlSuffix + "?" + colorArgs
					+ color + "&" + sizeArgs + size;
			String diffSize = sizeNameMap.get(size);
			File dir = new File(outputDir + File.separator + "id_" + id + File.separator + "color_#" + color
					+ File.separator + diffSize);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (outputFileName == null) {
				outputFileName = "ic_" + id + ".png";
			}
			download(strUrl, new File(dir, outputFileName));
			System.out.println("size " + size + " x " + size + " OK!");
		}
	}

	public static void download(String strUrl, File file) {
		HttpURLConnection conn = null;
		InputStream in = null;
		ByteArrayOutputStream out = null;
		FileOutputStream fos = null;
		try {
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(60000);
			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				System.err.println(responseCode + " error, url = " + strUrl);
				return;
			}
			in = new BufferedInputStream(url.openStream());
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			byte[] response = out.toByteArray();
			fos = new FileOutputStream(file);
			fos.write(response);
			fos.flush();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
