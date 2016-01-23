package org.wso2.appmanager.ui.integration.test.cases.store.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appmanager.ui.integration.test.dto.WebApp;
import org.wso2.appmanager.ui.integration.test.pages.LoginPage;
import org.wso2.appmanager.ui.integration.test.pages.PublisherCreateWebAppPage;
import org.wso2.appmanager.ui.integration.test.pages.PublisherWebAppsListPage;
import org.wso2.appmanager.ui.integration.test.pages.StoreHomePage;
import org.wso2.appmanager.ui.integration.test.utils.AppManagerIntegrationTest;

import java.util.Set;

public class StoreAnonymousAppAccessTestCase extends AppManagerIntegrationTest {
    private static final String TEST_DESCRIPTION = "Verify Anonymous application access";
    private static final String TEST_ANONYMOUS_APP_NAME_1 = "Test_anonymous_app_1";
    private static final String TEST_ANONYMOUS_APP_NAME_2 = "Test_anonymous_app_2";
    private static final String TEST_NON_ANONYMOUS_APP_NAME = "Test_non_anonymous_app_1";

    private static final Log log = LogFactory.getLog(StoreAnonymousAppAccessTestCase.class);

    private PublisherWebAppsListPage webAppsListPage;
    private PublisherCreateWebAppPage createWebAppPage;

    private String anonymous_app1_id;
    private String anonymous_app2_id;
    private String non_anonymous_app_id;
    WebDriverWait wait;

    @BeforeClass(alwaysRun = true)
    public void startUp() throws Exception {
        super.init();
        wait = new WebDriverWait(driver, 120);

        //login to publisher
        webAppsListPage = (PublisherWebAppsListPage) login(driver, LoginPage.LoginTo.PUBLISHER);

        //create anonymous and non anonymous apps
        createApps();

        //submit,approve and publish created apps
        manageLifeCycle();

    }

    @Test(groups = TEST_GROUP, description = TEST_DESCRIPTION)
    public void testAnonymousApplicationAccess() throws Exception {

        //Access anonymous allowed web app (by resources) using an anonymous user
        accessApps(true, anonymous_app1_id, TEST_ANONYMOUS_APP_NAME_1);

        //Access anonymous allowed web app (by flag) using an anonymous user
        accessApps(true, anonymous_app2_id, TEST_ANONYMOUS_APP_NAME_2);

        //Access anonymous disallowed web app using an anonymous user
        accessApps(false, non_anonymous_app_id, "authenticationendpoint");

    }


    private void createApps() throws Exception {

        //create anonymous app using making all resources anonymous
        createWebAppPage = webAppsListPage.gotoCreateWebAppPage();
        createWebAppPage.createAnonymousWebAppUsingResources(new WebApp(TEST_ANONYMOUS_APP_NAME_1,
                                                                        TEST_ANONYMOUS_APP_NAME_1,
                                                                        TEST_ANONYMOUS_APP_NAME_1,
                                                                        "1.0", "http://wso2.com",
                                                                        "http"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 +
                        "'][data-action='Submit for Review']")));

        //create anonymous app selecting anonymous flag
        createWebAppPage = webAppsListPage.gotoCreateWebAppPage();
        createWebAppPage.createAnonymousWebAppUsingFlag(new WebApp(TEST_ANONYMOUS_APP_NAME_2,
                                                                   TEST_ANONYMOUS_APP_NAME_2,
                                                                   TEST_ANONYMOUS_APP_NAME_2,
                                                                   "1.0", "http://wso2.com",
                                                                   "http"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 +
                        "'][data-action='Submit for Review']")));


        //create non anonymous app
        createWebAppPage = webAppsListPage.gotoCreateWebAppPage();
        webAppsListPage = createWebAppPage.createWebApp(new WebApp(TEST_NON_ANONYMOUS_APP_NAME,
                                                                   TEST_NON_ANONYMOUS_APP_NAME,
                                                                   TEST_NON_ANONYMOUS_APP_NAME,
                                                                   "1.0", "http://wso2.org",
                                                                   "http"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME +
                        "'][data-action='Submit for Review']")));
    }

    private void manageLifeCycle() throws Exception {
        //Set app id's
        anonymous_app1_id = driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 + "'][data-action='Submit for Review']"))
                .getAttribute("data-app");
        anonymous_app2_id = driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 + "'][data-action='Submit for Review']"))
                .getAttribute("data-app");
        non_anonymous_app_id = driver.findElement(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME +
                        "'][data-action='Submit for Review']"))
                .getAttribute("data-app");


        //anonymous app1: click submit button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 +
                        "'][data-action='Submit for Review']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 + "'][data-action='Submit for Review']"))
                .click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        //anonymous app1: click approve button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 + "'][data-action='Approve']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 + "'][data-action='Approve']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        //anonymous app1: click publish button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 + "'][data-action='Publish']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_1 + "'][data-action='Publish']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        driver.navigate().refresh();
        //anonymous app2: click submit button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 +
                        "'][data-action='Submit for Review']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 + "'][data-action='Submit for Review']"))
                .click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        //anonymous app2: click approve button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 + "'][data-action='Approve']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 + "'][data-action='Approve']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        //anonymous app2: click publish button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 + "'][data-action='Publish']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_ANONYMOUS_APP_NAME_2 + "'][data-action='Publish']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        driver.navigate().refresh();
        //non anonymous app1: click submit button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME +
                        "'][data-action='Submit for Review']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME +
                        "'][data-action='Submit for Review']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        //non anonymous app1: click approve button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME + "'][data-action='Approve']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME + "'][data-action='Approve']"))
                .click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();

        //non anonymous app1: click publish button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME + "'][data-action='Publish']")));
        driver.findElement(By.cssSelector(
                "[data-name='" + TEST_NON_ANONYMOUS_APP_NAME + "'][data-action='Publish']"))
                .click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-dismiss='modal']")));
        driver.findElement(By.cssSelector("[data-dismiss='modal']")).click();
    }

    private void accessApps(Boolean isAnonymousApp, String appId, String redirectedURL)
            throws Exception {
        String exceptionMsg;

        Thread.sleep(1200);

        driver.get(appMServer.getContextUrls().getWebAppURLHttps() + "/store");
        StoreHomePage.getPage(driver, appMServer);


        if (isAnonymousApp) {
            exceptionMsg = "Anonymous App URL is invalid";
        } else {
            exceptionMsg = "Non Anonymous Apps do not get redirected to login page";
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "a[href*='/store/assets/webapp/" + appId + "']")));
        driver.findElement(By.cssSelector(
                "a[href*='/store/assets/webapp/" + appId + "']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("gatewayURL")));
        driver.findElement(By.id("gatewayURL")).click();

        Set<String> afterPopup = driver.getWindowHandles();
        if (afterPopup.size() > 1) {
            if (driver.switchTo().window((String) afterPopup.toArray()[1]).getCurrentUrl()
                    .contains(redirectedURL) == false) {
                throw new Exception(exceptionMsg);
            }
            driver.switchTo().window((String) afterPopup.toArray()[1]).close();
            driver.switchTo().window((String) afterPopup.toArray()[0]);
        }
    }


    @AfterClass(alwaysRun = true)
    public void closeDown() throws Exception {
        closeDriver(driver);
    }
}
