package allure.spockext

import org.junit.internal.AssumptionViolatedException
import org.junit.runner.Description
import org.spockframework.runtime.IRunListener
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.SpecInfo
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events.ClearStepStorageEvent
import ru.yandex.qatools.allure.events.TestCaseCanceledEvent
import ru.yandex.qatools.allure.events.TestCaseFailureEvent
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent
import ru.yandex.qatools.allure.events.TestCaseStartedEvent
import ru.yandex.qatools.allure.events.TestSuiteFinishedEvent
import ru.yandex.qatools.allure.events.TestSuiteStartedEvent
import ru.yandex.qatools.allure.utils.AnnotationManager
import spock.lang.Unroll

class AllureSpockListener implements IRunListener {

	private Allure lifecycle = Allure.LIFECYCLE;

	private String suiteGuid

	@Override
	void beforeSpec(SpecInfo specInfo) {
		println("beforeSpec ${specInfo.name}")

		suiteGuid = generateSuiteUid(specInfo.name);

		TestSuiteStartedEvent event = new TestSuiteStartedEvent(suiteGuid, specInfo.name);
		AnnotationManager am = new AnnotationManager(specInfo.description.annotations);
		am.update(event);

//		event.withLabels(AllureModelUtils.createTestFrameworkLabel("Spock"), AllureModelUtils.createLabel(LANGUAGE, "Spock"));

		getLifecycle().fire(event);
	}

	@Override
	void beforeFeature(FeatureInfo featureInfo) {
		println("beforeFeature ${featureInfo.name}")

		if  (!isDataTest(featureInfo.description)) {
			TestCaseStartedEvent event = new TestCaseStartedEvent(suiteGuid, featureInfo.name);
			AnnotationManager am = new AnnotationManager(featureInfo.description.getAnnotations());

			am.update(event);

			//fireClearStepStorage();
			getLifecycle().fire(event);
		}
	}

	private static boolean isDataTest(Description description) {
		new AnnotationManager(description.getAnnotations()).isAnnotationPresent(Unroll)
	}

	@Override
	void beforeIteration(IterationInfo iterationInfo) {
		println("beforeIteration ${iterationInfo.name}")

		if (isDataTest(iterationInfo.description)) {
			TestCaseStartedEvent event = new TestCaseStartedEvent(suiteGuid, iterationInfo.name);
			AnnotationManager am = new AnnotationManager(iterationInfo.description.getAnnotations());

			am.update(event);

			//fireClearStepStorage();
			getLifecycle().fire(event);
		}
	}

	@Override
	void afterIteration(IterationInfo iterationInfo) {
		println("afterIteration ${iterationInfo.name}")

		if (isDataTest(iterationInfo.description)) {
			getLifecycle().fire(new TestCaseFinishedEvent());
		}
	}

	@Override
	void afterFeature(FeatureInfo featureInfo) {
		println("afterFeature ${featureInfo.name}")
		if  (!isDataTest(featureInfo.description)) {
			getLifecycle().fire(new TestCaseFinishedEvent());
		}
	}

	@Override
	void afterSpec(SpecInfo specInfo) {
		println("afterSpec ${specInfo.name}")
		getLifecycle().fire(new TestSuiteFinishedEvent(suiteGuid));
	}

	@Override
	void error(ErrorInfo errorInfo) {
		println("errorInfo ${errorInfo}")
		if (errorInfo.exception instanceof AssumptionViolatedException) {
			getLifecycle().fire(new TestCaseCanceledEvent().withThrowable(errorInfo.exception));
		} else {
			getLifecycle().fire(new TestCaseFailureEvent().withThrowable(errorInfo.exception));
		}
	}

	@Override
	void specSkipped(SpecInfo specInfo) {

	}

	@Override
	void featureSkipped(FeatureInfo featureInfo) {

	}

	public void fireClearStepStorage() {
		getLifecycle().fire(new ClearStepStorageEvent());
	}

	private static String generateSuiteUid(String suiteName) {
		UUID.randomUUID().toString();
	}

	Allure getLifecycle() {
		return lifecycle
	}

	void setLifecycle(Allure lifecycle) {
		this.lifecycle = lifecycle
	}
}
