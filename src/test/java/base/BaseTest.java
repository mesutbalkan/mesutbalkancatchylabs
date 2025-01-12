package base;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.ElementInfo;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class BaseTest {

    protected static WebDriver driver;
    protected static Actions actions;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    DesiredCapabilities capabilities;
    ChromeOptions chromeOptions;
    FirefoxOptions firefoxOptions;
    SafariOptions safariOptions;

    String browserName = "chrome";
    String selectPlatform = "mac";
    private static final String DEFAULT_DIRECTORY_PATH = "elementValues";
    ConcurrentMap<String, Object> elementMapList = new ConcurrentHashMap<>();

    @BeforeScenario
    public void setUp() {
        logger.info("************************************  BeforeScenario  ************************************");
        if (StringUtils.isEmpty(System.getenv("key"))) {
            logger.info("Bilgisayarda " + selectPlatform + " ortamında " + browserName + " tarayıcısında test ayağa kalkacak");
            if ("win".equalsIgnoreCase(selectPlatform)) {
                if ("chrome".equalsIgnoreCase(browserName)) {
                    driver = new ChromeDriver(chromeOptions());
                    driver.manage().window().maximize();
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                } else if ("firefox".equalsIgnoreCase(browserName)) {
                    driver = new FirefoxDriver(firefoxOptions());
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                }else if ("safari".equalsIgnoreCase(browserName)) {
                    driver = new SafariDriver(safariOptions());
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                }
            } else if ("mac".equalsIgnoreCase(selectPlatform)) {
                if ("chrome".equalsIgnoreCase(browserName)) {
                    driver = new ChromeDriver(chromeOptions());
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                } else if ("firefox".equalsIgnoreCase(browserName)) {
                    driver = new FirefoxDriver(firefoxOptions());
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                }
                else if ("safari".equalsIgnoreCase(browserName)) {
                    driver = new SafariDriver(safariOptions());
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                }
                actions = new Actions(driver);
            }

        }
    }

    @AfterScenario
    public void tearDown() {
        driver.quit();
    }

    public void initMap(File[] fileList) {
        Type elementType = new TypeToken<List<ElementInfo>>() {
        }.getType();
        Gson gson = new Gson();
        List<ElementInfo> elementInfoList = null;
        for (File file : fileList) {
            try {
                elementInfoList = gson
                        .fromJson(new FileReader(file), elementType);
                elementInfoList.parallelStream()
                        .forEach(elementInfo -> elementMapList.put(elementInfo.getKey(), elementInfo));
            } catch (FileNotFoundException e) {
                logger.warn("{} not found", e);
            }
        }
    }

    public File[] getFileList() {
        File[] fileList = new File(
                this.getClass().getClassLoader().getResource(DEFAULT_DIRECTORY_PATH).getFile())
                .listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".json"));
        if (fileList == null) {
            logger.warn(
                    "File Directory Is Not Found! Please Check Directory Location. Default Directory Path = {}",
                    DEFAULT_DIRECTORY_PATH);
            throw new NullPointerException();
        }
        return fileList;
    }

    /**
     * SAFARİ AYARLARI
     */
    public SafariOptions safariOptions() {
        safariOptions = new SafariOptions();
        capabilities = DesiredCapabilities.chrome();
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        safariOptions.merge(capabilities);
        return safariOptions;
    }

    /**
     * Set Chrome options
     *
     * @return the chrome options
     */
    public ChromeOptions chromeOptions() {
        chromeOptions = new ChromeOptions();
        capabilities = DesiredCapabilities.chrome();
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.addArguments("--start-fullscreen");
        chromeOptions.addArguments("--kiosk");

        System.setProperty("webdriver.chrome.driver", "web_driver/chromedriver");
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);


        chromeOptions.merge(capabilities);
        return chromeOptions;
    }

    /**
     * Set Firefox options
     *
     * @return the firefox options
     */
    public FirefoxOptions firefoxOptions() {
        firefoxOptions = new FirefoxOptions();
        capabilities = DesiredCapabilities.firefox();
        FirefoxProfile profile = new FirefoxProfile();
        firefoxOptions.setBinary("/Applications/Firefox.app/Contents/MacOS/firefox");
        profile.setPreference("general.useragent.override",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        firefoxOptions.addArguments("--kiosk");
        firefoxOptions.addArguments("--disable-notifications");
        firefoxOptions.addArguments("--start-fullscreen");
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        capabilities.setCapability("marionette", true);
        firefoxOptions.merge(capabilities);
        System.setProperty("webdriver.gecko.driver", "web_driver/geckodriver");
        return firefoxOptions;
    }

    public ElementInfo findElementInfoByKey(String key) {
        return (ElementInfo) elementMapList.get(key);
    }

    public void saveValue(String key, String value) {
        elementMapList.put(key, value);
    }

    public String getValue(String key) {
        return elementMapList.get(key).toString();
    }

}