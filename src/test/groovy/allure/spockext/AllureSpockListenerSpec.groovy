package allure.spockext

import allure.spockext.testdata.SimpleAnnotatedSpec
import allure.spockext.testdata.SimpleSpec
import org.junit.runner.Description
import org.spockframework.runtime.JUnitDescriptionGenerator
import org.spockframework.runtime.SpecInfoBuilder
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.SpecInfo
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent
import ru.yandex.qatools.allure.events.TestCaseStartedEvent
import ru.yandex.qatools.allure.events.TestSuiteStartedEvent
import ru.yandex.qatools.allure.model.LabelName
import spock.lang.Specification

class AllureSpockListenerSpec extends Specification {
	Allure allure
	AllureSpockListener listener

	def setup() {
		allure = Mock(Allure)
		listener = new AllureSpockListener()
		listener.setLifecycle(allure)
	}

	def "TestSuiteStartedEvent is fired before Spec started"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

		when:
			listener.beforeSpec(spec)

		then:
			1 * allure.fire({ TestSuiteStartedEvent event ->
				assert event.name == "SimpleSpec"
				assert event.title == null
				assert event.description == null
				true
			})
	}

	def "TestSuiteStartedEvent is enriched with annotations"() {
		given:
			def spec = specificationInfoFrom(SimpleAnnotatedSpec)

		when:
			listener.beforeSpec(spec)

		then:
			1 * allure.fire({ TestSuiteStartedEvent event ->
				assert event.name == "SimpleAnnotatedSpec"
				assert event.title == "Simple Specification"
				assert event.description.value == "Simple Specification used for allure sock extension"
				true
			})
	}


	def "TestCaseStartedEvent is fired before Feature started"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

		when:
			listener.beforeFeature(spec.features.find({ it.name == spec.toFeatureName("successful test") }))

		then:
			1 * allure.fire({ TestCaseStartedEvent event ->
				assert event.name == "successful test"
				true
			})
	}

	def "TestCaseStartedEvent is enriched with annotation"() {
		given:
			def spec = specificationInfoFrom(SimpleAnnotatedSpec)

		when:
			listener.beforeFeature(spec.features.find({ it.name == spec.toFeatureName("successful test") }))

		then:
			1 * allure.fire({ TestCaseStartedEvent event ->
				assert event.name == "successful test"
				assert event.labels.find({ it.value == "My Feature" && it.name == LabelName.FEATURE.value() })
				assert event.labels.find({ it.value == "Story1" && it.name == LabelName.STORY.value() })
				assert event.labels.find({ it.value == "Story2" && it.name == LabelName.STORY.value() })
				true
			})
	}

	def "TestCaseStartedEvent is not fired for data-driven feature when beforeFeature"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

		when:
			listener.beforeFeature(spec.features.find({ it.name == spec.toFeatureName("parametrised test") }))
		then:
			0 * allure.fire(_)
	}

	def "TestCaseStartedEvent is fired for data-driven test beforeIteration"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

			FeatureInfo dataDrivenFeature = spec.features.find({ it.name == spec.toFeatureName("successful test") })
			def iteration = new IterationInfo(dataDrivenFeature, new Object[0], 1)

			Description description = Description.createTestDescription(spec.getReflection(),
					"iterationName", dataDrivenFeature.getFeatureMethod().getReflection().getAnnotations());
			iteration.setDescription(description);
			iteration.setName("successful test")

		when:
			listener.beforeIteration(iteration)
		then:
			0 * allure.fire(_)
	}

	def "TestCaseStartedEvent is not fired when regular feature when beforeIteration"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

			FeatureInfo dataDrivenFeature = spec.features.find({ it.name == spec.toFeatureName("parametrised test") })
			def iteration = new IterationInfo(dataDrivenFeature, new Object[0], 1)

			Description description = Description.createTestDescription(spec.getReflection(),
					"iterationName", dataDrivenFeature.getFeatureMethod().getReflection().getAnnotations());
			iteration.setDescription(description);
			iteration.setName("parametrised test[0]")

		when:
			listener.beforeIteration(iteration)
		then:
			1 * allure.fire({ TestCaseStartedEvent event ->
				assert event.name == "parametrised test[0]"
				true
			})
	}

	def "TestCaseFinishedEvent is fired for regular feature afterFeature"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

		when:
			listener.afterFeature(spec.features.find({ it.name == spec.toFeatureName("successful test") }))

		then:
			1 * allure.fire(_ as TestCaseFinishedEvent )
	}

	def "TestCaseFinishedEvent is not fired for data-driven feature when afterFeature"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

			FeatureInfo dataDrivenFeature = spec.features.find({ it.name == spec.toFeatureName("parametrised test") })

		when:
			listener.afterFeature(dataDrivenFeature)
		then:
			0 * allure.fire(_)
	}

	def "TestCaseFinishedEvent is not fired for regular feature afterIteration"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

			FeatureInfo dataDrivenFeature = spec.features.find({ it.name == spec.toFeatureName("successful test") })
			def iteration = new IterationInfo(dataDrivenFeature, new Object[0], 1)

			Description description = Description.createTestDescription(spec.getReflection(),
					"iterationName", dataDrivenFeature.getFeatureMethod().getReflection().getAnnotations());
			iteration.setDescription(description);
			iteration.setName("successful test")

		when:
			listener.afterIteration(iteration)
		then:
			0 * allure.fire(_)
	}

	def "TestCaseFinishedEvent is fired for data-driven feature afterIteration"() {
		given:
			def spec = specificationInfoFrom(SimpleSpec)

			FeatureInfo dataDrivenFeature = spec.features.find({ it.name == spec.toFeatureName("parametrised test") })
			def iteration = new IterationInfo(dataDrivenFeature, new Object[0], 1)

			Description description = Description.createTestDescription(spec.getReflection(),
					"iterationName", dataDrivenFeature.getFeatureMethod().getReflection().getAnnotations());
			iteration.setDescription(description);
			iteration.setName("parametrised test[0]")

		when:
			listener.afterIteration(iteration)
		then:
			1 * allure.fire(_ as TestCaseFinishedEvent)
	}

	private SpecInfo specificationInfoFrom(Class<?> specClass) {
		def spec = new SpecInfoBuilder(specClass).build()
		new JUnitDescriptionGenerator(spec).attach()
		return spec
	}
}