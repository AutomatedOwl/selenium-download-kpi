package com.github.automatedowl.tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * <ul>
 *     <li> Class that contains methods for download files in Selenium session.
 *          Using Chromedriver, it would allow you:</li>
 *         <li>To define your download folder both globally and locally in one code line.</li>
 *         <li>Given a file name, web element and timeout,
 *             you would be able to download a file and verify succession.</li>
 *         <li>Logging the download bandwidth in Mbps and attaching it to Allure report.</li>
 *         <li>Assertion of download bandwidth,
 *             where you define a numeric threshold for passing/failing the test.</li>
 * </ul>
 */
public class SeleniumDownloadKPI {

    private Logger logger = Logger.getGlobal();
    private String downloadFolder;
    private Path downloadPath;
    private final String DOWNLOAD_TIMEOUT_MESSAGE =
            "Download timeout of $X milliseconds has expired.";
    private final String DOWNLOAD_BANDWIDTH_MESSAGE =
            "Download bandwidth of $X : $Y Mbps.";
    private final String DOWNLOAD_BANDWIDTH_ERROR =
            "Download bandwidth of $X is under threshold of $Y Mbps.";

    // Define a delay before deleting downloaded file.
    private final int DELAY_BEFORE_DELETE = 2000;

    // Define one second polling in milliseconds.
    private final long ONE_SECOND_POLLING = 1000;

    // Define default download timeout of 5 minutes in milliseconds.
    private final long DEFAULT_DOWNLOAD_TIMEOUT = 360000;

    public SeleniumDownloadKPI(String downloadFolder) {
        this.downloadFolder = downloadFolder;
        this.downloadPath = Paths.get(downloadFolder);
    }

    public void fileDownloadKPI(WebElement element, String fileName, boolean deleteFile)
            throws InterruptedException {
        clickDownloadButton(element);
        double downloadMbps = waitForFileDownload(fileName, DEFAULT_DOWNLOAD_TIMEOUT, deleteFile);
        downloadMbps = adjustDecimals(downloadMbps);
        logBandwidth(fileName, downloadMbps);
        attachDownloadBandwidth(getBandwidthMessage(fileName, downloadMbps));
    }

    public void fileDownloadKPI(WebElement element, String fileName, long downloadTimeout, boolean deleteFile)
            throws InterruptedException {
        clickDownloadButton(element);
        double downloadMbps = waitForFileDownload(fileName, downloadTimeout, deleteFile);
        downloadMbps = adjustDecimals(downloadMbps);
        logBandwidth(fileName, downloadMbps);
        attachDownloadBandwidth(getBandwidthMessage(fileName, downloadMbps));
    }

    public void fileDownloadAssertKPI(WebElement element, String fileName, long mbpsThreshold, boolean deleteFile)
            throws InterruptedException {
        clickDownloadButton(element);
        double downloadMbps = waitForFileDownload(fileName, DEFAULT_DOWNLOAD_TIMEOUT, deleteFile);
        downloadMbps = adjustDecimals(downloadMbps);
        logBandwidth(fileName, downloadMbps);
        attachDownloadBandwidth(getBandwidthMessage(fileName, downloadMbps));
        if (downloadMbps < mbpsThreshold) {
            throw new WebDriverException(DOWNLOAD_BANDWIDTH_ERROR
                    .replace("$X", fileName)
                    .replace("$Y", Long.toString(mbpsThreshold)));
        }
    }

    public void fileDownloadAssertKPI(
            WebElement element, String fileName, long mbpsThreshold, long downloadTimeout, boolean deleteFile)
            throws InterruptedException {
        clickDownloadButton(element);
        double downloadMbps = waitForFileDownload(fileName, downloadTimeout, deleteFile);
        downloadMbps = adjustDecimals(downloadMbps);
        logBandwidth(fileName, downloadMbps);
        attachDownloadBandwidth(getBandwidthMessage(fileName, downloadMbps));
        if (downloadMbps < mbpsThreshold) {
            throw new WebDriverException(DOWNLOAD_BANDWIDTH_ERROR
                    .replace("$X", fileName)
                    .replace("$Y", Long.toString(mbpsThreshold)));
        }
    }

    @Step("Download of {0} finished successfully")
    private void logBandwidth(String fileName, double downloadMbps) {
        logger.info(getBandwidthMessage(fileName, downloadMbps));
    }

    @Step("Clicking download button")
    private void clickDownloadButton(WebElement element) {
        element.click();
    }

    /** Perform download file using polling in 'while' statement.
     * @param fileName contains the full file name with its extension.
     * @param downloadTimeout contain the download timeout in milliseconds.
     * */
    @Step("Waiting for download of {0} to finish")
    private double waitForFileDownload(String fileName, long downloadTimeout, boolean deleteFile)
            throws InterruptedException {
        File downloadFile = downloadPath.resolve(fileName).toFile();
        long downloadStartTime = System.currentTimeMillis();
        while (!downloadFile.exists() &
                (System.currentTimeMillis() - downloadStartTime) < downloadTimeout) {
            if (((System.currentTimeMillis() - downloadStartTime) > downloadTimeout - 1000) &&
                    !downloadFile.exists()) {
                throw new TimeoutException(DOWNLOAD_TIMEOUT_MESSAGE.replace(
                        "$X", Long.toString(downloadTimeout)));
            } else {
                Thread.sleep(ONE_SECOND_POLLING);
            }
        }

        // Save file size before deleting.
        long fileSize = downloadFile.length();

        // Check for deletion flag.
        if (deleteFile) {
            waitBeforeFileDelete();
            downloadFile.delete();
        }

        // Return download bandwidth in Mbps.
        return calculateMbps(fileSize, downloadStartTime);
    }

    /** Attach download bandwidth to Allure report. */
    @Attachment(value = "{0}", type = "text/plain")
    private static String attachDownloadBandwidth(String message) {
        return message;
    }

    /** @return ChromeOptions object that contained default download folder.  */
    public ChromeOptions generateDownloadFolderCapability() {
        HashMap<String, Object> chromeAdditionalOptions =
                new HashMap<>();
        chromeAdditionalOptions.put("download.default_directory", downloadFolder);
        chromeAdditionalOptions.put("download.prompt_for_download", false);
        chromeAdditionalOptions.put("download.directory_upgrade", true);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromeAdditionalOptions);
        return options;
    }

    /** Calculate Mbps value from elapsed time and file size.  */
    private double calculateMbps(long fileSize, long startTime) {
        return (double)
                (8 * fileSize / 1024 / 1024) /
                ((System.currentTimeMillis() - startTime) / 1000);
    }

    /** Adjust Mbps value to three digits after the decimal point. */
    private double adjustDecimals(double downloadMbps) {
        return Math.floor(downloadMbps * 1000) / 1000.0;
    }

    private String getBandwidthMessage(String fileName, double downloadMbps) {
        return DOWNLOAD_BANDWIDTH_MESSAGE
                .replace("$X", fileName)
                .replace("$Y", Double.toString(downloadMbps));
    }

    private void waitBeforeFileDelete() throws InterruptedException {
        Thread.sleep(DELAY_BEFORE_DELETE);
    }
}
