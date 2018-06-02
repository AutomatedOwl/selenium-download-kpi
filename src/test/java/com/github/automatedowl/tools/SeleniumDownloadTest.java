package com.github.automatedowl.tools;

import com.github.automatedowl.tools.pages.AdamInternetPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/** Class that contains download tests in Selenium session.
 *  It would use SeleniumDownloadKPI object to download files in different configurations. */
public class SeleniumDownloadTest {

    private WebDriver driver;
    private AdamInternetPage adamInternetPage;
    private SeleniumDownloadKPI seleniumDownloadKPI;

    // Define custom timeout of one minute in milliseconds.
    private final int CUSTOM_DOWNLOAD_TIMEOUT = 60000;

    // Define timeout before closing browser after test.
    private final int BROWSER_WAIT_MILLISECONDS = 4000;

    /** Static method that sets Chromedriver executable as system property. */
    @BeforeAll
    static void setChromeDriver() {
        System.setProperty("webdriver.chrome.driver",
                System.getProperty("user.dir")
                        + "/src/main/java/com/github/automatedowl" +
                        "/tools/drivers/webdriver/chromedriver");
    }

    /** Before each Chromedriver session, define default download directory,
     *  as well as driver and page objects. */
    @BeforeEach
    void setUpTest() {
        seleniumDownloadKPI =
                new SeleniumDownloadKPI("/tmp/");
        ChromeOptions chromeOptions =
                seleniumDownloadKPI.generateDownloadFolderCapability();
        driver = new ChromeDriver(chromeOptions);
        adamInternetPage = new AdamInternetPage(driver);
    }

    /** Test method.
     *  It uses SeleniumDownloadKPI object to download 16MB file and attach KPI to Allure report.
     *  */
    @Test
    void downloadAttachTest() throws InterruptedException {
        adamInternetPage.navigateToPage(driver);
        seleniumDownloadKPI.fileDownloadKPI(
                adamInternetPage.getFileDownloadLink(), "SpeedTest_16MB.dat");
        waitBeforeClosingBrowser();
    }

    /** Test method.
     *  It uses SeleniumDownloadKPI object to download 16MB file and attach KPI to Allure report.
     *  The test also uses custom download timeout.
     *  */
    @Test
    void downloadAttachCustomTimeoutTest() throws InterruptedException {
        adamInternetPage.navigateToPage(driver);
        seleniumDownloadKPI.fileDownloadKPI(
                adamInternetPage.getFileDownloadLink(),
                "SpeedTest_16MB.dat", CUSTOM_DOWNLOAD_TIMEOUT);
        waitBeforeClosingBrowser();
    }

    /** Test method.
     *  It uses SeleniumDownloadKPI object to download 16MB file and attach KPI to Allure report.
     *  The test also perform a comparison of download bandwidth to expected threshold.
     *  */
    @Test
    void downloadAssertTest() throws InterruptedException {
        adamInternetPage.navigateToPage(driver);
        seleniumDownloadKPI.fileDownloadAssertKPI(
                adamInternetPage.getFileDownloadLink(),
                "SpeedTest_16MB.dat", 5);
        waitBeforeClosingBrowser();
    }

    /** Test method.
     *  It uses SeleniumDownloadKPI object to download 16MB file and attach KPI to Allure report.
     *  The test also perform a comparison of download bandwidth to expected threshold,
     *  as well as custom download timeout.
     *  */
    @Test
    void downloadAssertCustomTimeoutTest() throws InterruptedException {
        adamInternetPage.navigateToPage(driver);
        seleniumDownloadKPI.fileDownloadAssertKPI(
                adamInternetPage.getFileDownloadLink(),
                "SpeedTest_16MB.dat", 5, CUSTOM_DOWNLOAD_TIMEOUT);
        waitBeforeClosingBrowser();
    }

    @AfterEach
    void closeDriver() {
        driver.quit();
    }

    private void waitBeforeClosingBrowser() throws InterruptedException {
        Thread.sleep(BROWSER_WAIT_MILLISECONDS);
    }
}
