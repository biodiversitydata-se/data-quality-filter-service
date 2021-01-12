package au.org.ala.dataqualityfilter

import grails.converters.JSON
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import static javax.servlet.http.HttpServletResponse.SC_OK

@Api(value = "/api/v1/quality/", tags = ["QualityServiceRPC"], description = "Data Quality web services to support biocache-hubs functions")
class QualityController {

    static responseFormats = ['json']
    static namespace = "v1"
    static type_profile = 'profile'
    static type_category = 'category'
    static id_type_name = 'name'
    static id_type_id = 'id'

    def qualityService

    private processResult(rslt, resourceType, resourceId, resourceValue, contentType = null) {
        if (rslt == null) {
            render "Can't find " + resourceType + ' with ' + resourceId + ' [' + resourceValue + ']', contentType: 'text/plain', status: 404
        } else {
            if (contentType) {
                render rslt, contentType: contentType
            } else {
                render rslt as JSON
            }
        }
    }

    @ApiOperation(
            value = "Get enabled filters, grouped by category label for a given profile name",
            nickname = "getEnabledFiltersByLabel",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String, responseContainer = "Map")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = true, value = "Profile name", dataType = 'string')
    ])
    def getEnabledFiltersByLabel(String profileName) {
        processResult(qualityService.getEnabledFiltersByLabel(profileName), type_profile, id_type_name, profileName)
    }
    final class GetEnabledFiltersByLabelResponse extends LinkedHashMap<String, String> {}

    @ApiOperation(
            value = "Get Enabled Quality Filters",
            nickname = "getEnabledQualityFilters",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = true, value = "Profile name", dataType = 'string')
    ])
    def getEnabledQualityFilters(String profileName) {
        processResult(qualityService.getEnabledQualityFilters(profileName), type_profile, id_type_name, profileName)
    }

    /** This class is unused other than to provides the type information for the OpenAPI definition */
    final class GetGroupedEnabledFiltersResponse extends LinkedHashMap<String, List<QualityFilter>> {}

    @ApiOperation(
            value = "Get Grouped Enabled Filters",
            nickname = "getGroupedEnabledFilters",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = GetGroupedEnabledFiltersResponse) // , responseContainer = "Map"
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = true, value = "Profile name", dataType = 'string')
    ])
    def getGroupedEnabledFilters(String profileName) {
        processResult(qualityService.getGroupedEnabledFilters(profileName), type_profile, id_type_name, profileName)
    }

    @ApiOperation(
            value = "Find All Enabled Categories",
            nickname = "findAllEnabledCategories",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityCategory, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = true, value = "Profile name", dataType = 'string')
    ])
    def findAllEnabledCategories(String profileName) {
        processResult(qualityService.findAllEnabledCategories(profileName), type_profile, id_type_name, profileName)
    }

    @ApiOperation(
            value = """Retrieve the data profile for a given profile's short name. If the profile doesn't exist or the short name is omitted then get the default profile of the specified user. 
                    If no profile found or no userId specified, return the default public profile""",
            nickname = "activeProfile",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "The profile short name", dataType = 'string'),
            @ApiImplicitParam(name = "userId", paramType = "query", required = false, value = "the userId used to get active profile in case profile name is not provided", dataType = 'string')
    ])
    def activeProfile(String profileName, String userId) {
        if (profileName) {
            def qp = qualityService.getProfile(profileName)
            if (qp) {
                render qp as JSON
            }
        }

        render qualityService.getDefaultProfile(userId) as JSON
    }

    @ApiOperation(
            value = "Retrieve the default data profile. If the userId is provided, return the default profile for the user. Otherwise return the default public profile",
            nickname = "getDefaultProfile",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "userId", paramType = "query", required = false, value = "The userId used to retrieve the default profile", dataType = 'string')
    ])
    def getDefaultProfile(String userId) {
        render qualityService.getDefaultProfile(userId) as JSON
    }

    @ApiOperation(
            value = "Get the full filter string for a given data profile",
            nickname = "getJoinedQualityFilter",
            produces = "text/plain",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = true, value = "Profile name", dataType = 'string')
    ])
    def getJoinedQualityFilter(String profileName) {
        processResult(qualityService.getJoinedQualityFilter(profileName), type_profile, id_type_name, profileName,'text/plain')
    }

    @ApiOperation(
            value = "Get the full inverse filter string for a given quality category",
            nickname = "getInverseCategoryFilter",
            produces = "text/plain",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "qualityCategoryId", paramType = "query", required = false, value = "Quality Category Id", dataType = 'integer')
    ])
    def getInverseCategoryFilter() {
        def categoryId = params.long('qualityCategoryId')
        processResult(qualityService.getInverseCategoryFilter(categoryId), type_category, id_type_id, categoryId, 'text/plain')
    }

    @ApiOperation(
            value = "Get all the inverse filter strings for a given data profile",
            nickname = "getAllInverseCategoryFiltersForProfile",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String, responseContainer = "Map")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "qualityProfileId", paramType = "query", required = true, value = "Quality Profile Id", dataType = 'integer')
    ])
    def getAllInverseCategoryFiltersForProfile() {
        def profileId = params.long('qualityProfileId')
        processResult(qualityService.getAllInverseCategoryFilters(profileId), type_profile, id_type_id, profileId)
    }
}
