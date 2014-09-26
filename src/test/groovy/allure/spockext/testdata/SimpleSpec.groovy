package allure.spockext.testdata

import ru.yandex.qatools.allure.annotations.Features
import ru.yandex.qatools.allure.annotations.Stories
import spock.lang.Specification
import spock.lang.Unroll

class SimpleSpec extends Specification {

	def "successful test"() {
		given: "input data"
			println("input data")
		when: "do action"
			println("do action")
		then:
			println("success")
			true
	}

	@Unroll
	def "parametrised test"(String input, String excepted) {
		given:
			true
		when:
			true
		then:
			true
		where:
			input 		| excepted
			"foo"		| "bar"
			"foobar" 	| "foobaz"
	}

	def "parametrised test without Unroll"(String input, String excepted) {
		given:
			true
		when:
			true
		then:
			true
		where:
			input 		| excepted
			"foo"		| "bar"
			"foobar" 	| "foobaz"
	}

}