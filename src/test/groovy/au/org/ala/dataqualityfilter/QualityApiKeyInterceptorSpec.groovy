package au.org.ala.dataqualityfilter

import grails.testing.gorm.DataTest
import grails.testing.web.interceptor.InterceptorUnitTest
import org.grails.web.util.GrailsApplicationAttributes
import spock.lang.Specification

class QualityApiKeyInterceptorSpec extends Specification implements InterceptorUnitTest<QualityApiKeyInterceptor>, DataTest {

    static final String API_KEY_HEADER_NAME = 'apiKey'
    static final int STATUS_UNAUTHORISED = 403

    QualityProfile qp;

    def setup() {
        mockDomains(QualityCategory, QualityFilter, QualityProfile)
        qp = new QualityProfile(name: 'name', shortName: 'name', enabled: true, isDefault: true, displayOrder: 1, userId: '123').save(flush: true)

        interceptor.qualityService = Mock(QualityService)
        interceptor.qualityService.findProfileById(_) >> qp
        interceptor.apiKeyService = Mock(ApiKeyService)
        interceptor.apiKeyService.checkApiKey(_) >> { String key -> [valid: (key == "valid")] }
    }

    def cleanup() {
    }

    void setParams(params, action) {
        if (action == 'getDefaultProfile') {
            params.userId = '123'
        } else if (action == 'getAllInverseCategoryFiltersForProfile') {
            params.qualityProfileId = qp.id
        } else {
            params.profileName = 'name'
        }
    }

    void "Test invalid api key on QualityController"() {
        when:
        request.addHeader(API_KEY_HEADER_NAME, "invalid")
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE, 'quality')
        request.setAttribute(GrailsApplicationAttributes.ACTION_NAME_ATTRIBUTE, action)
        setParams(params, action)

        withRequest(controller: "quality", action: action)
        def result = interceptor.before()

        then:
        result == before
        response.status == responseCode

        where:
        action    | responseCode | before
        'getEnabledFiltersByLabel' | STATUS_UNAUTHORISED | false
        'getEnabledQualityFilters' | STATUS_UNAUTHORISED | false
        'getGroupedEnabledFilters' | STATUS_UNAUTHORISED | false
        'findAllEnabledCategories' | STATUS_UNAUTHORISED | false
        'activeProfile' | STATUS_UNAUTHORISED | false
        'getDefaultProfile' | STATUS_UNAUTHORISED | false
        'getJoinedQualityFilter' | STATUS_UNAUTHORISED | false
        'getAllInverseCategoryFiltersForProfile' | STATUS_UNAUTHORISED | false
    }

    void "Test valid api key on QualityController"() {
        when:
        request.addHeader(API_KEY_HEADER_NAME, "valid")
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE, 'quality')
        request.setAttribute(GrailsApplicationAttributes.ACTION_NAME_ATTRIBUTE, action)
        setParams(params, action)

        withRequest(controller: "quality", action: action)
        def result = interceptor.before()

        then:
        result == before

        where:
        action    |  before
        'getEnabledFiltersByLabel' | true
        'getEnabledQualityFilters' | true
        'getGroupedEnabledFilters' | true
        'findAllEnabledCategories' | true
        'activeProfile' | true
        'getDefaultProfile' | true
        'getJoinedQualityFilter' | true
        'getAllInverseCategoryFiltersForProfile' | true
    }

    void "Test api key on QualityProfileController"() {
        when:
        request.addHeader(API_KEY_HEADER_NAME, apikey)
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE, 'qualityProfile')
        request.setAttribute(GrailsApplicationAttributes.ACTION_NAME_ATTRIBUTE, action)
        params.id = qp.id

        withRequest(controller: "qualityProfile", action: action)
        def result = interceptor.before()

        then:
        result == before

        where:
        action  | apikey    | before
        'show'  | 'invalid' | false
        'show'  | 'valid'   | true
    }

    void "Test api key on QualityCategoryController"() {
        when:
        request.addHeader(API_KEY_HEADER_NAME, apikey)
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE, 'qualityCategory')
        request.setAttribute(GrailsApplicationAttributes.ACTION_NAME_ATTRIBUTE, action)
        params.qualityProfileId = qp.id

        withRequest(controller: "qualityCategory", action: action)
        def result = interceptor.before()

        then:
        result == before

        where:
        action  | apikey    | before
        'index' | 'invalid' | false
        'index' | 'valid'   | true
        'show'  | 'invalid' | false
        'show'  | 'valid'   | true
    }

    void "Test api key on QualityFilterController"() {
        when:
        request.addHeader(API_KEY_HEADER_NAME, apikey)
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE, 'qualityFilter')
        request.setAttribute(GrailsApplicationAttributes.ACTION_NAME_ATTRIBUTE, action)
        params.qualityProfileId = qp.id

        withRequest(controller: "qualityFilter", action: action)
        def result = interceptor.before()

        then:
        result == before

        where:
        action  | apikey    | before
        'index' | 'invalid' | false
        'index' | 'valid'   | true
        'show'  | 'invalid' | false
        'show'  | 'valid'   | true
    }
}
