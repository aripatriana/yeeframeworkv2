package com.yeeframework.automate.screen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.yeeframework.automate.Constants;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.io.FileImageIO;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.util.DateUtils;
import com.yeeframework.automate.util.IDUtils;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.util.SimpleEntry;
import com.yeeframework.automate.util.StringUtils;

public class WindowScreen {
	
	private final static String REMARK_FAILED = "failed";
	public final static int CAPTURE_CURRENT_WINDOW = 0;
	public final static int CAPTURE_FULL_WINDOW = 1;
	
	private final static int SNAPSHOT_TEMP = 0;
	private final static int SNAPSHOT_FINAL = 1;
	
	@PropertyValue("{tmp_dir}")
	private String tmpDir;
	
	@PropertyValue("{report_dir}")
	private String testCaseDir;
	
	@PropertyValue("generate.output.image")
	private String generateOutputImage;
	
	@PropertyValue(Constants.CURRENT_TESTCASE_ID)
	private String targetFolder;
	
	@PropertyValue(Constants.CURRENT_TESTSCEN_ID)
	private String prefixFileName;
	
	@PropertyValue(Constants.START_TIME_MILIS_ID)
	private String startTimeMilis;
	
	private Scrolling scrolling;
	private WebDriver webDriver;
	
	@PropertyValue("current_session_id")
	private String sessionId;
	
	private String remark;
	
	public WindowScreen(WebDriver webDriver) {
		this.webDriver = webDriver;
	}
	
	public String getTargetFolder() {
		return targetFolder;
	}
	
	public void setTargetFolder(String targetFolder) {
		this.targetFolder = targetFolder;
	}
	
	public String getPrefixFileName() {
		return prefixFileName;
	}
	
	public void setPrefixFileName(String prefixFileName) {
		this.prefixFileName = prefixFileName;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public boolean checkIsScrolling() {
		Integer scrollHeight = scrolling.getScrollHeight();
		Integer clientHeight = scrolling.getClientHeight();
		return !clientHeight.equals(scrollHeight);
	}
	
	public void setScrolling(Scrolling scrolling) {
		this.scrolling = scrolling;
	}
	
	private void capture(int captureType, Scrolling scrolling) throws IOException {
		if ("true".equalsIgnoreCase(generateOutputImage)) {
			File outputFile = null;
			
			setScrolling(scrolling);
			
			LinkedHashMap<String, SimpleEntry<PositionPixel, File>> images = new LinkedHashMap<String, SimpleEntry<PositionPixel, File>>();
			if (checkIsScrolling() && (captureType == 1)) {
				
				scrolling.scrollToDown();
				while (true) {
					int pixelRelative = getPixelRelative();
					File file = snapshot(WindowScreen.SNAPSHOT_TEMP);
					images.put(file.getName(), new SimpleEntry<PositionPixel, File>(scrolling.getPosition(), file));
					
					if (!scrolling.isPixelOrigin()) {
						scrolling.moveUp(pixelRelative);	
					} else {
						break;
					}				
				}
				
				outputFile = putToFile(FileImageIO.combineImage(scrolling.getClientWidth(), scrolling.getScrollHeight(), scrolling.getClientHeight(), images));
			} else {
				outputFile = snapshot(WindowScreen.SNAPSHOT_FINAL);
			}	
			
			ReportMonitor.logSnapshotEntry(targetFolder, prefixFileName, sessionId, outputFile.getAbsolutePath(), 
					(REMARK_FAILED.equals(remark) ? ReportManager.FAILED : ReportManager.PASSED));
		}
		resetRemark();
	}

	public void resetRemark() {
		setRemark(null);
	}

	
	public void capture(int captureType) throws IOException {
		capture(captureType, new WindowScrolling((JavascriptExecutor) webDriver));	
	}
	
	public void capture(int captureType, String elementId) throws IOException {
		capture(captureType, new ModalScrolling((JavascriptExecutor) webDriver, elementId));
	}

	public int getPixelRelative() {
		Integer clientHeight =  scrolling.getClientHeight();
		Integer scrollHeight = scrolling.getScrollHeight();
		PositionPixel position = scrolling.getPosition();
		int relative = scrollHeight/clientHeight;
		if (relative < 1) {
			relative = scrollHeight%clientHeight;
		} else {
			relative = position.getY()/clientHeight;
			if (relative < 1) {
				relative = position.getY()%clientHeight;
			} else {
				relative = 500;				
			}
		}
		return relative;
	}
	
	public File snapshot(int snapshotType) throws IOException {
		 TakesScreenshot scrShot =((TakesScreenshot)webDriver);
		 File sourceFile = scrShot.getScreenshotAs(OutputType.FILE);
		 if (snapshotType == WindowScreen.SNAPSHOT_TEMP) {
			 return putToTempFile(sourceFile);
		 } else {
			 return putToFile(sourceFile);	 
		 }
	}
	
	public File normalizeFile(File sourceFile) throws IOException {
		BufferedImage buf = FileImageIO.resizeImage(ImageIO.read(sourceFile), scrolling.getClientWidth(), scrolling.getClientHeight());
		ImageIO.write(buf, "png", sourceFile);
		return sourceFile;
	}
	
	public File putToTempFile(File sourceFile) throws IOException {
		sourceFile = normalizeFile(sourceFile);
		File destFile=new File(StringUtils.path(tmpDir, IDUtils.getRandomId() + ".png"));
		FileUtils.copyFile(sourceFile, destFile);
		return destFile;
	}
	
	public File putToFile(File sourceFile) throws IOException {
		sourceFile = normalizeFile(sourceFile);
		File destFile=new File(constructOutputFileName());
		FileUtils.copyFile(sourceFile, destFile);
		return destFile;
	}
	
	public File putToFile(BufferedImage bufferedImg) throws IOException {
		File destFile=new File(constructOutputFileName());
		destFile.mkdirs();
		ImageIO.write(bufferedImg, "png", destFile);
		return destFile;
	}
	
	private String constructOutputFileName() {
		String targetFileName = StringUtils.path(testCaseDir, DateUtils.format(Long.valueOf(startTimeMilis)));
		if (targetFolder != null)
			targetFileName = StringUtils.path(targetFileName, targetFolder, prefixFileName.replace(targetFolder + "_", ""));
		if (prefixFileName != null)
			targetFileName = StringUtils.path(targetFileName, prefixFileName + "_");
		
		if (remark != null) {
			targetFileName += IDUtils.getRandomId() + "_" + remark + ".png";
		} else {
			targetFileName += IDUtils.getRandomId() + ".png";
		}
		return targetFileName;
	}
	
	public static void main1(String[] args) throws IOException {
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("{tmp_dir}", System.getProperty("user.dir") + "\\tmp");
		metadata.put("{log_dir}", System.getProperty("user.dir") + "\\log");
		metadata.put("{config_dir}", System.getProperty("user.dir") + "\\config");
		metadata.put("{keyfile_dir}", System.getProperty("user.dir") + "\\keyfile");
		metadata.put("{testcase_dir}", System.getProperty("user.dir") + "\\testcase");
		
		WebDriver wd = DriverManager.getDefaultDriver();
		wd.get("https://www.telkomsigma.co.id/");
		WindowScreen ws = new WindowScreen(wd);
		InjectionUtils.setObjectWithCustom(ws, metadata);
		ws.capture(1);
	}
}
