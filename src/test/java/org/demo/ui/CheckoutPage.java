// java
package org.demo.ui;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class CheckoutPage {

    private final WebDriver driver;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
    }

    @Step("Open checkout step one page")
    public void openStepOne() {
        driver.get("https://www.saucedemo.com/checkout-step-one.html");
    }

    @Step("Fill first name: {firstName}")
    public void setFirstName(String firstName) {
        WebElement el = driver.findElement(By.cssSelector("input#first-name, input[name='firstName']"));
        el.clear();
        el.sendKeys(firstName);
    }

    @Step("Fill last name: {lastName}")
    public void setLastName(String lastName) {
        WebElement el = driver.findElement(By.cssSelector("input#last-name, input[name='lastName']"));
        el.clear();
        el.sendKeys(lastName);
    }

    @Step("Fill postal code: {postalCode}")
    public void setPostalCode(String postalCode) {
        WebElement el = driver.findElement(By.cssSelector("input#postal-code, input[name='postalCode']"));
        el.clear();
        el.sendKeys(postalCode);
    }

    @Step("Submit checkout information (Continue)")
    public void continueFromStepOne() {
        driver.findElement(By.cssSelector("input#continue, button#continue, button[name='continue']")).click();
    }

    @Step("Cancel checkout")
    public void cancelCheckout() {
        driver.findElement(By.cssSelector("button#cancel, a#cancel, button[name='cancel']")).click();
    }

    @Step("Complete checkout (Finish)")
    public void finishCheckout() {
        // assumes on checkout overview page
        driver.findElement(By.cssSelector("button#finish, .checkout_button, button.finish")).click();
    }

    @Step("Get checkout error message")
    public String getErrorMessage() {
        List<WebElement> errors = driver.findElements(By.cssSelector("h3[data-test='error'], .error-message-container, .checkout_info > h3"));
        if (errors.isEmpty()) return "";
        return errors.get(0).getText().trim();
    }

    @Step("Get item names on overview")
    public List<String> getItemNames() {
        return driver.findElements(By.cssSelector(".cart_item .inventory_item_name, .cart_list .inventory_item_name"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    @Step("Get item prices on overview")
    public List<String> getItemPrices() {
        return driver.findElements(By.cssSelector(".cart_item .inventory_item_price, .cart_list .inventory_item_price"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    @Step("Get subtotal label")
    public String getSubtotal() {
        List<WebElement> els = driver.findElements(By.cssSelector(".summary_subtotal_label, .summary_subtotal"));
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }

    @Step("Get tax label")
    public String getTax() {
        List<WebElement> els = driver.findElements(By.cssSelector(".summary_tax_label, .summary_tax"));
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }

    @Step("Get total label")
    public String getTotal() {
        List<WebElement> els = driver.findElements(By.cssSelector(".summary_total_label, .summary_total"));
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }

    @Step("Is checkout page visible")
    public boolean isVisible() {
        return !driver.findElements(By.cssSelector(".checkout_info, .checkout_summary_container, .checkout_complete_container")).isEmpty();
    }

    @Step("Is checkout complete visible")
    public boolean isCompleteVisible() {
        return !driver.findElements(By.cssSelector(".checkout_complete_container, .complete-header")).isEmpty();
    }

    @Step("Get checkout complete header")
    public String getCompleteHeader() {
        List<WebElement> els = driver.findElements(By.cssSelector(".complete-header, .checkout_complete_container .complete-header"));
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }

    @Step("Get checkout complete message")
    public String getCompleteMessage() {
        List<WebElement> els = driver.findElements(By.cssSelector(".complete-text, .checkout_complete_container .complete-text"));
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }
}
