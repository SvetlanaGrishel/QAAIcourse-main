import model.TestCase;
import model.TestSuite;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestGenerator {

    public static void generate() {

        TestSuite suite = TestcasesParser.parse("generated/testcases.json");

        StringBuilder code = new StringBuilder();

        code.append("""
                package org.demo.generated;
                import org.demo.ui.BaseUiTest;
                import org.demo.ui.LoginPage;
                import org.demo.ui.CartPage;
                import org.demo.ui.CheckoutPage;
                import org.demo.ui.ItemDetailsPage;
                import org.testng.Assert;
                import org.testng.annotations.Test;
                public class GeneratedLoginTest extends org.demo.ui.BaseUiTest {
                """);

        for (TestCase tc : suite.testcases) {
            code.append(generateTest(tc));
        }

        code.append("}");

        FilesUtil.write(
                "src/test/java/org/demo/generated/GeneratedLoginTest.java",
                code.toString()
        );

        System.out.println("Autotests generated.");
    }

    private static String generateTest(TestCase tc) {

        String methodName = tc.id.toLowerCase() + "_" +
                tc.title.replaceAll("[^a-zA-Z]", "_");

        String assertion =
                tc.type.equalsIgnoreCase("positive")
                        ? "Assert.assertTrue(driver.getCurrentUrl().contains(\"inventory\"));"
                        : "Assert.assertTrue(page.isErrorVisible());";

        String username = "";
        String password = "";
        Pattern pattern = Pattern.compile("\\((.*?)\\)");

        for (String step : tc.steps) {
            Matcher matcher = pattern.matcher(step);
            if (step.contains("userlogin") && matcher.find()) {
                username = matcher.group(1);
            } else if (step.contains("password") && matcher.find()) {
                password = matcher.group(1);
            }
        }

        return """
                
                    @Test
                    public void %s() {
                        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
                        page.open();
                        page.login("%s", "%s");
                        %s
                    }
                """.formatted(methodName, username, password, assertion);
    }
}
