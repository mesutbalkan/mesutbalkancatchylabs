package step;

import base.BaseTest;
import model.ElementInfo;
import com.thoughtworks.gauge.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class BaseSteps<file> extends BaseTest {
    public static int DEFAULT_MAX_ITERATION_COUNT = 150;
    public static int DEFAULT_MILLISECOND_WAIT_AMOUNT = 100;
    public BaseSteps() throws IOException {
        initMap(getFileList());
    }
    public static int getDefaultMaxIterationCount() {
        return DEFAULT_MAX_ITERATION_COUNT;
    }
    WebElement findElement(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 60);
        WebElement webElement = webDriverWait
                .until(ExpectedConditions.presenceOfElementLocated(infoParam));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})",
                webElement);
        return webElement;
    }
    WebElement findElementByClickable(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 60);
        WebElement webElement = webDriverWait
                .until(ExpectedConditions.elementToBeClickable(infoParam));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})",
                webElement);
        System.out.println("element bulundu");
        return webElement;
    }
    List<WebElement> findElements(String key) {
        return driver.findElements(getElementInfoToBy(findElementInfoByKey(key)));
    }
    public By getElementInfoToBy(ElementInfo elementInfo) {
        By by = null;
        if (elementInfo.getType().equals("css")) {
            by = By.cssSelector(elementInfo.getValue());
        } else if (elementInfo.getType().equals(("name"))) {
            by = By.name(elementInfo.getValue());
        } else if (elementInfo.getType().equals("id")) {
            by = By.id(elementInfo.getValue());
        } else if (elementInfo.getType().equals("xpath")) {
            by = By.xpath(elementInfo.getValue());
        } else if (elementInfo.getType().equals("linkText")) {
            by = By.linkText(elementInfo.getValue());
        } else if (elementInfo.getType().equals(("partialLinkText"))) {
            by = By.partialLinkText(elementInfo.getValue());
        }
        return by;
    }
    private void clickElement(WebElement element) {
        element.click();
    }
    private void clickElementBy(String key) {
        findElement(key).click();
    }
    private void hoverElement(WebElement element) {
        actions.moveToElement(element).build().perform();
    }
    private void hoverElementBy(String key) {
        WebElement webElement = findElement(key);
        actions.moveToElement(webElement).build().perform();
    }
    private void sendKeyESC(String key) {
        findElement(key).sendKeys(Keys.ESCAPE);
    }
    private String getPageSource() {
        return driver.switchTo().alert().getText();
    }
    public WebElement findElementWithKey(String key) {
        return findElement(key);
    }
    public String getElementText(String key) {
        return findElement(key).getText();
    }
    public String getElementAttributeValue(String key, String attribute) {
        return findElement(key).getAttribute(attribute);
    }
    @Step("Print page source")
    public void printPageSource() {
        System.out.println(getPageSource());
    }
    public void javaScriptClicker(WebDriver driver, WebElement element) {
        JavascriptExecutor jse = ((JavascriptExecutor) driver);
        jse.executeScript("var evt = document.createEvent('MouseEvents');"
                + "evt.initMouseEvent('click',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);"
                + "arguments[0].dispatchEvent(evt);", element);
    }
    public void javascriptclicker(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }
    @Step({"Wait <value> seconds",
            "<int> saniye bekle"})
    public void waitBySeconds(int seconds) {
        try {
            logger.info(seconds + " saniye bekleniyor.");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Step({"Wait <value> milliseconds",
            "<long> milisaniye bekle"})
    public void waitByMilliSeconds(long milliseconds) {
        try {
            // logger.info(milliseconds + " milisaniye bekleniyor.");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Step({"Elementine tıkla <key>"})
    public void clickElement(String key) {
        if (!key.isEmpty()) {
            clickElement(findElementByClickable(key));
            logger.info(key + " elementine tıklandı.");
        }
    }
    @Step({"<url> adresine git"})
    public void goToUrl(String url) {
        driver.get(url);
        logger.info(url + " adresine gidiliyor.");
    }
    @Step({"Check if element <key> exists else print message <message>",
            "Element <key> var mı kontrol et yoksa hata mesajı ver <message>"})
    public void getElementWithKeyIfExistsWithMessage(String key, String message) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        By by = getElementInfoToBy(elementInfo);
        int loopCount = 0;
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            if (driver.findElements(by).size() > 0) {
                logger.info(key + " elementi bulundu.");
                return;
            }
            loopCount++;
            waitByMilliSeconds(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        Assertions.fail(message);
    }
    @Step({"Gorunur olmasini bekle <key>"})
    public void gorunurBekle(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 60);
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(infoParam));
    }
    @Step({"Write value <text> to element <key>",
            "<text> textini <key> elemente yaz"})
    public void ssendKeys(String text, String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(text);
            logger.info(key + " elementine " + text + " texti yazıldı.");
        }
    }
    @Step({"Check if element <key> contains text <expectedText>",
            "<key> elementi <text> değerini içeriyor mu kontrol et"})
    public void checkElementContainsText(String key, String expectedText) {
        Boolean containsText = findElement(key).getText().contains(expectedText);
        assertTrue(containsText, "Expected text is not contained");
        logger.info(key + " elementi" + expectedText + "değerini içeriyor.");
    }
    @Step("Makinede <rakam> rakamına tikla")
    public void clickMakineRakam(int rakam) {
        String xpath = "//div[@class='css-146c3p1 r-jwli3a r-adyw6z r-vw2c0b' and text()='" + rakam + "']";
        WebElement element = driver.findElement(By.xpath(xpath));
        element.click();
    }
    @Step("Makinede <islem> butonuna tikla")
    public void clickMakineIslem(int islem) {
        String xpath = "//div[@class='css-146c3p1 r-jwli3a r-adyw6z r-vw2c0b' and text()='" + islem + "']";
        WebElement element = driver.findElement(By.xpath(xpath));
        element.click();
    }
    @Step("Islem sonucunu dogrula <beklenensonuc>")
    public void islemSonucuDogrulama (String beklenensonuc)
    { List<WebElement> resultElements = driver.findElements(By.xpath("//span[contains(@class, 'css-1jxf684')]"));
        String actualResult = ""; for (WebElement element : resultElements) { String text = element.getText().trim();
            if (text.startsWith("=")) { actualResult = text.replace("= ", ""); break; } }
        if (actualResult.equals(beklenensonuc)) { logger.info("Result is correct: " + actualResult); }
        else { logger.error("Result is incorrect. Expected: " + beklenensonuc + ", but got: " + actualResult);
            throw new AssertionError("Result is incorrect.\nExpected: " + beklenensonuc + "\nActual: " + actualResult); }}
}
