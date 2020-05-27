import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import org.mozilla.universalchardet.UniversalDetector;

public class Converter {
	public static final String TXT_EXT = ".txt";
	public static final String MD_EXT = ".md";

	static class Conver {
		String from;
		String to;

		public Conver(String from, String to) {
			this.from = from;
			this.to = to;
		}
	}

	public static final Map<Integer, Conver> convs = new HashMap<>();
	static {
		convs.put(1, new Conver("\r\n\r\n\r\n\r\n\r\n", "@@@"));
		convs.put(2, new Conver("\r\n\r\n\r\n\r\n", "@@@"));
		convs.put(3, new Conver("\r\n\r\n\r\n", "@@@"));
		convs.put(4, new Conver("\r\n\r\n", "@@@"));
		convs.put(5, new Conver("\r\n", "\r\n\r\n"));
		convs.put(6, new Conver("@@@", "\r\n\r\n"));
	}

	public static final int MAX_LENGTH = Short.MAX_VALUE * 2;

	public static String format(String content) {
		if (content == null || content.isEmpty()) {
			return "";
		}
		for (Map.Entry<Integer, Conver> entry : convs.entrySet()) {
			Conver conver = entry.getValue();
			content = content.replaceAll(conver.from, conver.to);
		}
		return content;
	}

	private String mdName(File srcFile) {
		String srcName = srcFile.getName();
		if (!srcName.endsWith(TXT_EXT)) {
			return null;
		}
		return srcFile.getParent() + "/" + srcName.substring(0, srcName.indexOf(TXT_EXT)) + MD_EXT;
	}

	public static String getCharset(File file) {
		UniversalDetector detector = new UniversalDetector(null);
		try {
			FileInputStream fis = new FileInputStream(file);

			long len = file.length();
			int readLen = len > MAX_LENGTH ? MAX_LENGTH : (int) len;

			int n = 0;
			while (n != -1 && !detector.isDone()) {
				byte[] bytes = new byte[readLen];
				n = fis.read(bytes);
				if (n > 0) {
					detector.handleData(bytes, 0, n);
				}
			}
			detector.dataEnd();
			return detector.getDetectedCharset();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			detector.reset();
		}
		return null;
	}

	public void handleFile(File srcFile) {
		FileInputStream fis = null;
		FileWriter writer = null;
		try {
			if (srcFile == null) {
				System.out.println("File is null!");
				return;
			}
			if (srcFile.isDirectory()) {
				handleFolder(srcFile);
			}

			if (!srcFile.exists()) {
				System.out.println("error!!!!!!");
				return;
			}
			if (!srcFile.getName().endsWith(TXT_EXT)) {
				return;
			}
			String charset = getCharset(srcFile);
			fis = new FileInputStream(srcFile);

			String mdName = mdName(srcFile);
			if (mdName == null) {
				System.out.println("Get md name failed, srcFile = " + srcFile.getAbsolutePath());
				return;
			}
			File mdFile = new File(mdName);
			if (mdFile.exists()) {
				if (!mdFile.renameTo(new File(mdName + "_bak_" + System.currentTimeMillis()))) {
					System.out.println("rename" + mdName + " failed!");
					return;
				}
			}
			if (!mdFile.createNewFile()) {
				System.out.println("create " + mdFile.getName() + " failed!");
				return;
			}
			writer = new FileWriter(mdFile);

			long len = srcFile.length();
			int readLen = len > MAX_LENGTH ? MAX_LENGTH : (int) len;

			int n = 0;
			while (n != -1) {
				byte[] bytes = new byte[readLen];
				n = fis.read(bytes);
				if (n > 0) {
					String content;
					if (charset == null) {
						content = new String(bytes);
					} else {
						content = new String(bytes, charset);
					}
					content = format(content);
					writer.write(content);
					writer.flush();
				}
			}

			System.out.println("Convert success! file = " + srcFile.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void handleFolder(File folder) {
		if (folder == null) {
			return;
		}
		if (!folder.isDirectory()) {
			handleFile(folder);
		}
		File[] files = folder.listFiles();
		if (files == null) {
			System.out.println("Empty folder " + folder.getPath());
			return;
		}
		for (File file : files) {
			handleFile(file);
		}
	}

	public void conv(String path) {
		File file = new File(path);
		handleFile(file);
	}
}
