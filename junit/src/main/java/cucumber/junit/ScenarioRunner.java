package cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

class ScenarioRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final List<String> extraCodePaths;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;

    public ScenarioRunner(Runtime runtime, List<String> extraCodePaths, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.runtime = runtime;
        this.extraCodePaths = extraCodePaths;
        this.cucumberScenario = cucumberScenario;
        this.jUnitReporter = jUnitReporter;
    }

    @Override
    public String getName() {
        Scenario scenario = cucumberScenario.getScenario();
        return scenario.getKeyword() + ": " + scenario.getName();
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberScenario.getSteps();
    }

    @Override
    protected Description describeChild(Step step) {
        // use scenario and step as class and method names (in order to generate useable JUnit reports)
        String className = getName();
        String methodName = step.getKeyword() + step.getName();
        String formattedDescription = String.format("%s(%s)", methodName, className);
        return Description.createSuiteDescription(formattedDescription);
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.setRunner(this, notifier);
        try {
            runtime.prepareAndFormat(cucumberScenario, jUnitReporter, extraCodePaths);
        } catch (CucumberException e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }
        super.run(notifier);
        try {
            runtime.dispose();
        } catch (CucumberException e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        runtime.runStep(cucumberScenario.getUri(), step, jUnitReporter, cucumberScenario.getLocale());
    }
}
