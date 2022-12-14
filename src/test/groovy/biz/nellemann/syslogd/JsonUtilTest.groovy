package biz.nellemann.syslogd

import biz.nellemann.syslogd.parser.JsonUtil
import spock.lang.Specification

class JsonUtilTest extends Specification {

    def "test short decode"() {
        setup:
        def testShort = 'Eating a piece of \u03c0 (pi)'

        when:
        def result = JsonUtil.decode(testShort)

        then:
        result == 'Eating a piece of π (pi)'
    }

    def "test long decode"() {
        setup:
        def testLong = 'I stole this guy from wikipedia: \ud83d\ude02' // emoji "face with tears of joy"

        when:
        def result = JsonUtil.decode(testLong)

        then:
        result == 'I stole this guy from wikipedia: 😂'
    }


    def "test quotes decode"() {
        setup:
        def testQuote = 'here it comes \" to wreck the day...'

        when:
        def result = JsonUtil.decode(testQuote)

        then:
        result == 'here it comes " to wreck the day...'
    }

    def "test newline decode"() {
        setup:
        def testQuote = 'here it comes \n to wreck the day...'

        when:
        def result = JsonUtil.decode(testQuote)

        then:
        result == 'here it comes \n to wreck the day...'
    }
}
