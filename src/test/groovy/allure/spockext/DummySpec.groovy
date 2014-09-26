package allure.spockext

import spock.lang.Specification

/**
 * Created by twuser on 9/22/2014.
 */
class DummySpec extends Specification {

	def "foo"() {
		given:
			true
		when:
			true
		then:
			true
	}
}
