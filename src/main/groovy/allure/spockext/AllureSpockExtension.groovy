package allure.spockext

import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

class AllureSpockExtension implements IGlobalExtension {


	@Override
	void visitSpec(SpecInfo spec) {

		println("Adding AllureSpockExtension to specification ${spec.name}")
		spec.addListener(new AllureSpockListener())

	}
}
