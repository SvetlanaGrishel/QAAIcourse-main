package org.demo.ui;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Step("Open Homepage")
    public void open() {
        driver.get("https://www.saucedemo.com/");
    }

    @Step("Login with username: {username} and password: {password}")
    public void login(String user, String pass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(user);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(pass);
        driver.findElement(By.id("login-button")).click();
    }

    @Step("Check error message")
    public boolean isErrorVisible() {
        return !driver.findElements(By.cssSelector("[data-test='error']")).isEmpty();
    }

}
