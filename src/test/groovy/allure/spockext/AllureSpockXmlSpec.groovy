package allure.spockext

import allure.spockext.testdata.SimpleSpec
import org.junit.runner.JUnitCore
import ru.yandex.qatools.allure.commons.AllureFileUtils
import ru.yandex.qatools.allure.utils.AllureResultsUtils
import spock.lang.Specification

class AllureSpockXmlSpec extends Specification {
	File tmpFolder
	JUnitCore core

	def setup() {
		tmpFolder = File.createTempDir()
		AllureResultsUtils.setResultsDirectory(tmpFolder);
		core = new JUnitCore();
	}

	def "generating xml report simple test"() {
		when:
			core.run(SimpleSpec)
		then:
			AllureFileUtils.listTestSuiteFiles(tmpFolder).size() == 1
	}

}
