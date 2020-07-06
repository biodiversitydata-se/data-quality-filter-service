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

@Api(value = "/api/v1/quality/", tags = ["QualityServiceRPC"], description = "Data Quality web services to support biocache-hubs directly")
class QualityController {

    static responseFormats = ['json']
    static namespace = "v1"

    def qualityService

    @ApiOperation(
            value = "Get enabled filters, grouped by category label",
            nickname = "getEnabledFiltersByLabel",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String, responseContainer = "Map")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def getEnabledFiltersByLabel(String profileName) {
        render qualityService.getEnabledFiltersByLabel(profileName) as JSON
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
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def getEnabledQualityFilters(String profileName) {
        render qualityService.getEnabledQualityFilters(profileName) as JSON
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
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def getGroupedEnabledFilters(String profileName) {
        render qualityService.getGroupedEnabledFilters(profileName) as JSON
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
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def findAllEnabledCategories(String profileName) {
        render qualityService.findAllEnabledCategories(profileName) as JSON
    }

    @ApiOperation(
            value = "Retrieve the filter qualityProfile for a given qualityProfile name",
            nickname = "activeProfile",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def activeProfile(String profileName) {
        render qualityService.activeProfile(profileName) as JSON
    }

    @ApiOperation(
            value = "Get the full filter string for a given quality qualityProfile",
            nickname = "getJoinedQualityFilter",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def getJoinedQualityFilter(String profileName) {
        render qualityService.getJoinedQualityFilter(profileName)
    }

    @ApiOperation(
            value = "Get the full inverse filter string for a given quality qualityProfile",
            nickname = "getInverseCategoryFilter",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = String)
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
    ])
    def getInverseCategoryFilter(QualityCategory category) {
        render qualityService.getInverseCategoryFilter(category)
    }

}
