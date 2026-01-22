import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PipelineMain {

    public static void main(String[] args) {

        System.out.println("=== AI QA PIPELINE STARTED ===");

        // STAGE 1. BUILD PROMPT FROM CHECKLIST
        String prompt = PromptEngine.buildPrompt(
                "prompts/01_scenarios_from_checklist.txt",
                "checklist.txt"
        );
        FilesUtil.write("generated/final_prompt.txt", prompt);

        // STAGE 2. PII SCAN & MASK
        PiiReport report = PiiScanner.scan(prompt);
        FilesUtil.write("generated/pii_report.txt", report.toText());

        String finalPrompt = prompt;

        if (report.hasFindings()) {
            System.out.println("PII detected. Masking input.");
            finalPrompt = PiiMasker.mask(prompt);
            FilesUtil.write("generated/prompt_masked.txt", finalPrompt);
        }

        // STAGE 3. GENERATE SCENARIOS
        String rawScenarios = MistralClient.call(finalPrompt);
        FilesUtil.write("generated/scenarios_raw.json", rawScenarios);

        String scenarios = extractAssistantContent(rawScenarios);
        FilesUtil.write("generated/ai_output.txt", scenarios);

        // ================================
        // STAGE 4. GENERATE JSON TESTCASES
        String jsonPrompt = FilesUtil.read("prompts/02_testcases_json.txt")
                .replace("{{SCENARIOS}}", scenarios);

        FilesUtil.write("generated/testcases_prompt.txt", jsonPrompt);

        String rawJson = MistralClient.call(jsonPrompt);
        FilesUtil.write("generated/testcases_raw.json", rawJson);

        String llmJsonText = extractAssistantContent(rawJson);
        FilesUtil.write("generated/testcases_llm.txt", llmJsonText);

        String pureJson = JsonExtractor.extractJson(llmJsonText);
        FilesUtil.write("generated/testcases.json", pureJson);

        System.out.println("Testcases generated: generated/testcases.json");

        // STAGE 5. GENERATE AUTOTESTS
        TestGenerator.generate();
        System.out.println("Autotests generated.");

        // STAGE 5.5. LINTING / CODE STYLE CHECK
        runLinter();

        String testLogs = runTests();
        FilesUtil.write("generated/test_logs.txt", testLogs);

        generateAllureReport();

        // STAGE 6. AI CODE REVIEW
        String generatedTest = FilesUtil.read(
                "src/test/java/org/demo/generated/GeneratedLoginTest.java"
        );

        String reviewPrompt = FilesUtil.read("prompts/03_code_review.txt")
                .replace("{{CODE}}", generatedTest);

        FilesUtil.write("generated/code_review_prompt.txt", reviewPrompt);

        String rawReview = MistralClient.call(reviewPrompt);
        FilesUtil.write("generated/code_review_raw.json", rawReview);

        String review = extractAssistantContent(rawReview);
        FilesUtil.write("generated/code_review.txt", review);

        System.out.println("AI code review saved: generated/code_review.txt");

        // STAGE 7. AI QA SUMMARY
        String allureSummary = FilesUtil.read("target/site/allure-maven-plugin/widgets/summary.json");
        String summaryPrompt = FilesUtil.read("prompts/05_qa_summary.txt")
                .replace("{{LOGS}}", testLogs)
                .replace("{{REPORT}}", allureSummary);
        FilesUtil.write("generated/qa_summary_prompt.txt", summaryPrompt);

        String rawQaSummary = MistralClient.call(summaryPrompt);
        FilesUtil.write("generated/qa_summary_raw.json", rawQaSummary);

        String qaSummary = extractAssistantContent(rawQaSummary);
        FilesUtil.write("generated/qa_summary.txt", qaSummary);

        System.out.println("QA summary saved: generated/qa_summary.txt");


        // STAGE. AI BUG REPORT (DESIGN-TIME)
        String checklist = FilesUtil.read("checklist.txt");
        String testcases = FilesUtil.read("generated/testcases.json");
        String codeReview = FilesUtil.read("generated/code_review.txt");

        String bugPrompt = FilesUtil.read("prompts/04_bug_report.txt")
                .replace("{{CHECKLIST}}", checklist)
                .replace("{{TESTCASES}}", testcases)
                .replace("{{REVIEW}}", codeReview);

        FilesUtil.write("generated/bug_report_prompt.txt", bugPrompt);

        String rawBug = MistralClient.call(bugPrompt);
        FilesUtil.write("generated/bug_report_raw.json", rawBug);

        String bugText = extractAssistantContent(rawBug);
        FilesUtil.write("generated/bug_report_llm.txt", bugText);

        String pureBugJson = JsonExtractor.extractJson(bugText);
        FilesUtil.write("generated/bug_report.json", pureBugJson);

        System.out.println("Bug report saved: generated/bug_report.json");

        System.out.println("=== AI QA PIPELINE FINISHED ===");
    }

    private static void runLinter() {
        System.out.println("=== RUNNING CODE STYLE CHECK ===");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "validate");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with error code : " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("Code style check failed. See output for details.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run linter", e);
        }
    }

    private static String runTests() {
        System.out.println("=== RUNNING AUTOTESTS ===");
        StringBuilder logs = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "test");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                logs.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with error code : " + exitCode);

            if (exitCode != 0) {
                // We don't throw an exception here because we want to analyze the report even if tests fail
                System.out.println("Autotests failed. See output for details.");
            }
            // Truncate logs to avoid exceeding LLM token limit
            if (logs.length() > 10000) {
                logs.delete(0, logs.length() - 10000);
                logs.insert(0, "...\n[TRUNCATED LOGS]\n");
            }
            return logs.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to run tests", e);
        }
    }

    private static void generateAllureReport() {
        System.out.println("=== GENERATING ALLURE REPORT ===");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "allure:report");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with error code : " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("Allure report generation failed. See output for details.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Allure report", e);
        }
    }

    private static String extractAssistantContent(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(rawJson)
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract assistant content from LLM response", e);
        }
    }
}
