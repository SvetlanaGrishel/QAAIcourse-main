package org.demo.ui;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class CartPage {

    private final WebDriver driver;

    public CartPage(WebDriver driver) {
        this.driver = driver;
    }

    @Step("Open cart page")
    public void open() {
        driver.get("https://www.saucedemo.com/cart.html");
    }

    @Step("Get names of items in cart")
    public List<String> getItemNames() {
        return driver.findElements(By.cssSelector(".cart_item .inventory_item_name"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    @Step("Get prices of items in cart")
    public List<String> getItemPrices() {
        return driver.findElements(By.cssSelector(".cart_item .inventory_item_price"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    @Step("Get total number of items in cart")
    public int getTotalItems() {
        return driver.findElements(By.cssSelector(".cart_item")).size();
    }

    @Step("Remove item from cart by name: {name}")
    public void removeItemByName(String name) {
        List<WebElement> items = driver.findElements(By.cssSelector(".cart_item"));
        for (WebElement item : items) {
            WebElement titleEl = item.findElement(By.cssSelector(".inventory_item_name"));
            if (titleEl.getText().equals(name)) {
                // try common remove button selectors inside the cart item
                item.findElement(By.cssSelector("button.btn_secondary, button.btn_inventory, button.remove-button")).click();
                return;
            }
        }
    }

    @Step("Remove first item from cart")
    public void removeFirstItem() {
        List<WebElement> items = driver.findElements(By.cssSelector(".cart_item"));
        if (!items.isEmpty()) {
            WebElement item = items.get(0);
            item.findElement(By.cssSelector("button.btn_secondary, button.btn_inventory, button.remove-button")).click();
        }
    }

    @Step("Go to checkout")
    public void checkout() {
        driver.findElement(By.cssSelector("button#checkout, .checkout_button, button.checkout")).click();
    }

    @Step("Open item details from cart by name: {name}")
    public void goToItemDetailsByName(String name) {
        List<WebElement> items = driver.findElements(By.cssSelector(".cart_item"));
        for (WebElement item : items) {
            WebElement titleEl = item.findElement(By.cssSelector(".inventory_item_name"));
            if (titleEl.getText().equals(name)) {
                titleEl.click();
                return;
            }
        }
    }

    @Step("Get cart badge count")
    public int getCartBadgeCount() {
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        if (badges.isEmpty()) return 0;
        try {
            return Integer.parseInt(badges.get(0).getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Step("Is cart visible")
    public boolean isVisible() {
        return !driver.findElements(By.cssSelector(".cart_list, .cart_contents_container")).isEmpty();
    }
}
