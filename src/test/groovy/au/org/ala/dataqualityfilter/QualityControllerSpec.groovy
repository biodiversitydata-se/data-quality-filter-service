package au.org.ala.dataqualityfilter

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class QualityControllerSpec extends Specification implements ControllerUnitTest<QualityController>, DataTest {

    def setup() {
        def qualityService = Mock(QualityService)
        controller.qualityService = qualityService

        qualityService.getEnabledFiltersByLabel(_) >> {
            String name -> name == 'invalid' ? null : [:]
        }

        qualityService.getEnabledQualityFilters(_) >> {
            String name -> name == 'invalid' ? null : []
        }

        qualityService.getGroupedEnabledFilters(_) >> {
            String name -> name == 'invalid' ? null : [:]
        }

        qualityService.findAllEnabledCategories(_) >> {
            String name -> name == 'invalid' ? null : []
        }

        qualityService.getJoinedQualityFilter(_) >> {
            String name -> name == 'invalid' ? null : ''
        }

        qualityService.getInverseCategoryFilter(_ as Long) >> {
            Long id -> id == -1 ? null : ''
        }

        qualityService.getAllInverseCategoryFilters(_ as Long) >> {
            Long id -> id == -1 ? null : [:]
        }
    }


    def 'test getEnabledFiltersByLabel'() {
        when:
        params.profileName = profileName
        controller.getEnabledFiltersByLabel()

        then:
        response.status == code

        where:
        profileName | code
        'invalid'   | 404
        'valid'     | 200
    }

    def 'test getEnabledQualityFilters'() {
        when:
        params.profileName = profileName
        controller.getEnabledQualityFilters()

        then:
        response.status == code

        where:
        profileName | code
        'invalid'   | 404
        'valid'     | 200
    }

    def 'test getGroupedEnabledFilters'() {
        when:
        params.profileName = profileName
        controller.getGroupedEnabledFilters()

        then:
        response.status == code

        where:
        profileName | code
        'invalid'   | 404
        'valid'     | 200
    }

    def 'test findAllEnabledCategories'() {
        when:
        params.profileName = profileName
        controller.findAllEnabledCategories()

        then:
        response.status == code

        where:
        profileName | code
        'invalid'   | 404
        'valid'     | 200
    }

    def 'test getJoinedQualityFilter'() {
        when:
        params.profileName = profileName
        controller.getJoinedQualityFilter()

        then:
        response.status == code

        where:
        profileName | code
        'invalid'   | 404
        'valid'     | 200
    }

    def 'test getInverseCategoryFilter'() {
        when:
        params.qualityCategoryId = categoryId
        controller.getInverseCategoryFilter()

        then:
        response.status == code

        where:
        categoryId | code
        -1         | 404
        1          | 200
    }

    def 'test getAllInverseCategoryFiltersForProfile' () {
        when:
        params.qualityProfileId = qualityProfileId
        controller.getAllInverseCategoryFiltersForProfile()

        then:
        response.status == code

        where:
        qualityProfileId | code
        -1         | 404
        1          | 200
    }
}