package com.paytm.framework.ui.element;

import com.paytm.framework.core.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class UIElements {

	public static List<UIElement> getMultiple(By by, String pageName, String elementName) {
		int index = 0;
		List<UIElement> list = new ArrayList<>();
		WebDriver driver = DriverManager.getDriver();
		List<WebElement> elements = driver.findElements(by);
		for (WebElement ele : elements) {
			list.add(new UIElement(ele, pageName, elementName + "[" + index + "]") {
				@Override
				public void waitUntilClickable() {
					String debugMsg = "Wait until [" + this.getElementName() + "] is clickable on [" + this.getPageName() + "]";
					DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.elementToBeClickable(this.getWrappedElement()));
				}

				@Override
				public void waitUntilVisible() {
					String debugMsg = "Wait until [" + this.getElementName() + "] is visible " + "on [" + this.getPageName() + "]";
					DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.visibilityOf(this.getWrappedElement()));
				}
			});
		}
		return list;
	}

	public static List<Button> getButtons(By by, String pageName, String elementName) {
		int index = 0;
		List<Button> list = new ArrayList<>();
		WebDriver driver = DriverManager.getDriver();
		List<WebElement> elements = driver.findElements(by);
		for (WebElement ele : elements) {
			list.add(new Button(ele, pageName, elementName + "[" + index + "]"));
		}
		return list;
	}

	public static List<CheckBox> getCheckBoxes(By by, String pageName, String elementName) {
		int index = 0;
		List<CheckBox> list = new ArrayList<>();
		WebDriver driver = DriverManager.getDriver();
		List<WebElement> elements = driver.findElements(by);
		for (WebElement ele : elements) {
			list.add(new CheckBox(ele, pageName, elementName + "[" + index + "]"));
		}
		return list;
	}

	public static List<RadioButton> getRadioButton(By by, String pageName, String elementName) {
		int index = 0;
		List<RadioButton> list = new ArrayList<>();
		WebDriver driver = DriverManager.getDriver();
		List<WebElement> elements = driver.findElements(by);
		for (WebElement ele : elements) {
			list.add(new RadioButton(ele, pageName, elementName + "[" + index + "]"));
		}
		return list;
	}

}