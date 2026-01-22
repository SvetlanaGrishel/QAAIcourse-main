package org.demo.ui;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ItemDetailsPage {

    private final WebDriver driver;

    public ItemDetailsPage(WebDriver driver) {
        this.driver = driver;
    }

    @Step("Open item details page by id: {itemId}")
    public void open(String itemId) {
        driver.get("https://www.saucedemo.com/inventory-item.html?id=" + itemId);
    }

    @Step("Get item title")
    public String getTitle() {
        return driver.findElement(By.cssSelector(".inventory_details_name")).getText();
    }

    @Step("Get item description")
    public String getDescription() {
        return driver.findElement(By.cssSelector(".inventory_details_desc")).getText();
    }

    @Step("Get item price")
    public String getPrice() {
        return driver.findElement(By.cssSelector(".inventory_details_price")).getText();
    }

    @Step("Add item to cart")
    public void addToCart() {
        WebElement addBtn = driver.findElement(By.cssSelector("button.btn_primary, button.btn_inventory"));
        addBtn.click();
    }

    @Step("Remove item from cart")
    public void removeFromCart() {
        WebElement removeBtn = driver.findElement(By.cssSelector("button.btn_secondary, button.btn_inventory"));
        removeBtn.click();
    }

    @Step("Back to products")
    public void backToProducts() {
        driver.findElement(By.cssSelector(".inventory_details_back_button")).click();
    }

    @Step("Is item details visible")
    public boolean isVisible() {
        return !driver.findElements(By.cssSelector(".inventory_details_container")).isEmpty();
    }
}
