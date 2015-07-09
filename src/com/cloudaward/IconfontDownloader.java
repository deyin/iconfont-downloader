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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * °¢ÀïÂèÂèÍ¼±ê¿âÏÂÔØÆ÷
 * 
 * @author Navy
 *
 */
public class IconfontDownloader {

  public static String colors[] = new String[] {"00bb9c", "11cd6e", "56abe4", "9d55b8", "33475f",
      "f4c600", "ea8010", "eb4f38", "ecf0f1", "a9b7b7", "000000"};

  public static Map<String, String> sizeNameMap = new HashMap<String, String>();

  static {
    sizeNameMap.put("16", "drawable-ldpi");
    sizeNameMap.put("32", "drawable-mdpi");
    sizeNameMap.put("64", "drawable-hdpi");
    sizeNameMap.put("128", "drawable-xhdpi");
  }

  public static String urlPrefix = "http://www.iconfont.cn/uploads/fonts/font-";
  public static String urlSuffix = "-.png";
  public static String colorArgs = "color=";
  public static String sizeArgs = "size=";

  public static String baseDir = "iconfont-extracted";

  private static int threshold = 10; // default

  public static void main(String[] args) {
    long begin = System.nanoTime();
    System.out.println("Start to download...");
    if (args.length < 1) {
      System.err.println("Error usage!");
      System.out.println("Usage: Extractor $idList");
      System.out.println("Usage: Extractor $startId $endId [threshold]");
    } else if (args.length == 1) {
      String[] split = args[0].split(",");
      for (String str : split) {
        int id = Integer.parseInt(str);
        download(id);
      }
    } else if (args.length > 1) {
      int start = Integer.parseInt(args[0]);
      if (start < 0) {
        throw new IllegalArgumentException("StartId value must be positive number");
      }
      int end = Integer.parseInt(args[1]);
      if (end < 0) {
        throw new IllegalArgumentException("EndId value must be positive number");
      }
      if (args.length == 3) {
        threshold = Integer.parseInt(args[2]);
        if (threshold < 0) {
          throw new IllegalArgumentException("Threshold value must be positive number");
        }
      }
      int count = end - start;
      if (count < 0) {
        throw new IllegalArgumentException("EndId value must greater than StartId");
      }
      if (count < threshold) { // sequence
        System.out.println("Count < " + threshold + ", using sequence download!");
        sequenceDownload(start, end);
      } else { // concurrent
        System.out.println("Count = " + count + " > " + threshold + ", using concurrent downlaod!");
        concurrentDownoad(start, end);
      }
    }
    System.out.println("End to download...");
    System.out
        .println("Download total spent " + (System.nanoTime() - begin) / 1.0e9 + " second(s)");
  }

  private static void concurrentDownoad(int start, int end) {
    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
    for (int i = start; i <= end; i++) {
      final int id = i;
      tasks.add(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          download(id);
          return null;
        }
      });
    }
    try {
      pool.invokeAll(tasks);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      pool.shutdown();
    }
  }

  private static void sequenceDownload(int start, int end) {
    for (int i = start; i <= end; i++) {
      download(i);
    }
  }

  public static void download(int id) {
    for (String color : colors) {
      for (String size : sizeNameMap.keySet()) {
        File dir = new File(baseDir + "\\" + "color_" + color, sizeNameMap.get(size));
        if (!dir.exists()) {
          dir.mkdirs();
        }
        String url = urlPrefix + id + urlSuffix + "?" + colorArgs + color + "&" + sizeArgs + size;
        download(url, new File(dir, "ic_" + id + ".png"));
      }
    }
    System.out.println("Downlod id = " + id + " image OK!");
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
        System.err.println(responseCode + " not ok, url = " + strUrl);
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
