package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification;
import testannotations.ArrayAnnotation;
import testannotations.Truth;

class ArrayAttributeAnnotations extends AbstractPlasticSpecification {

    def "handling of array attribute defaults"() {
        when:
        def pc = mgr.getPlasticClass("testsubjects.AnnotationSubject")

        def a = pc.getAnnotation(ArrayAnnotation.class)

        then:
        a.numbers().length == 0
        a.strings().length == 0
        a.types().length == 0
        a.annotations().length == 0
    }

    def "explicit values for array attributes"() {
        when:
        def pc = mgr.getPlasticClass("testsubjects.ArrayAttributesSubject")
        def a = pc.getAnnotation(ArrayAnnotation.class)

        then:

        a.numbers() == [5]

        a.strings() == ["frodo", "sam"]

        a.types() == [Runnable.class]

        a.annotations().length == 2
        a.annotations()[0].value() == Truth.YES
        a.annotations()[1].value() == Truth.NO
    }

    def "handling of explicitly empty array attributes"() {
        when:
        def pc = mgr.getPlasticClass("testsubjects.ExplicityEmptyArrayAttributesSubject")

        def a = pc.getAnnotation(ArrayAnnotation.class)

        then:
        a.numbers().length == 0
        a.strings().length == 0
        a.types().length == 0
        a.annotations().length == 0
    }
}
