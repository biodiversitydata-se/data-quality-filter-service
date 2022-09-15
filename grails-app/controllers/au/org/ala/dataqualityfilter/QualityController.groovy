package au.org.ala.dataqualityfilter

import au.org.ala.plugins.openapi.Path
import grails.converters.JSON
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

import javax.ws.rs.Produces

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY

//@Api(value = "/api/v1/quality/", tags = ["QualityServiceRPC"], description = "Data Quality web services to support biocache-hubs functions")
class QualityController {

    static responseFormats = ['json']
    static namespace = "v1"

    def qualityService

    @Operation(
            method = "GET",
            tags = "QualityServiceRPC",
            operationId = "getEnabledFiltersByLabel",
            summary = "Get enabled filters, grouped by category label for a given profile name",
            description = "Get enabled filters, grouped by category label for a given profile name",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "Enabled filters",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = Object)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/getEnabledFiltersByLabel")
    @Produces("application/json")
    def getEnabledFiltersByLabel(String profileName) {
        render qualityService.getEnabledFiltersByLabel(profileName) as JSON
    }
    final class GetEnabledFiltersByLabelResponse extends LinkedHashMap<String, String> {}

//    @ApiOperation(
//            value = "Get Enabled Quality Filters",
//            nickname = "getEnabledQualityFilters",
//            produces = "application/json",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = String, responseContainer = "List")
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
//    ])
    def getEnabledQualityFilters(String profileName) {
        render qualityService.getEnabledQualityFilters(profileName) as JSON
    }

    /** This class is unused other than to provides the type information for the OpenAPI definition */
    final class GetGroupedEnabledFiltersResponse extends LinkedHashMap<String, List<QualityFilter>> {}

//    @ApiOperation(
//            value = "Get Grouped Enabled Filters",
//            nickname = "getGroupedEnabledFilters",
//            produces = "application/json",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = GetGroupedEnabledFiltersResponse) // , responseContainer = "Map"
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
//    ])
    def getGroupedEnabledFilters(String profileName) {
        render qualityService.getGroupedEnabledFilters(profileName) as JSON
    }
//
//    @ApiOperation(
//            value = "Find All Enabled Categories",
//            nickname = "findAllEnabledCategories",
//            produces = "application/json",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = QualityCategory, responseContainer = "List")
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
//    ])
    def findAllEnabledCategories(String profileName) {
        render qualityService.findAllEnabledCategories(profileName) as JSON
    }

//    @ApiOperation(
//            value = "Retrieve the data profile for a given profile's short name.  If the profile doesn't exist or the short name is omitted then the default profile is returned instead.",
//            nickname = "activeProfile",
//            produces = "application/json",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile)
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "The profile short name", dataType = 'string')
//    ])
    def activeProfile(String profileName) {
        render qualityService.activeProfile(profileName) as JSON
    }

//    @ApiOperation(
//            value = "Get the full filter string for a given data profile",
//            nickname = "getJoinedQualityFilter",
//            produces = "text/plain",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = String)
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "profileName", paramType = "query", required = false, value = "Profile name", dataType = 'string')
//    ])
    def getJoinedQualityFilter(String profileName) {
        render qualityService.getJoinedQualityFilter(profileName), contentType: 'text/plain'
    }
//
//    @ApiOperation(
//            value = "Get the full inverse filter string for a given quality category",
//            nickname = "getInverseCategoryFilter",
//            produces = "text/plain",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = String)
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "qualityCategoryId", paramType = "query", required = false, value = "Quality Category Id", dataType = 'integer')
//    ])
    def getInverseCategoryFilter() {
        def result = qualityService.getInverseCategoryFilter(params.long('qualityCategoryId'))
        if (result == null) {
            render '', contentType: 'text/plain', status: 404
        } else {
            render result, contentType: 'text/plain'
        }
    }

//    @ApiOperation(
//            value = "Get all the inverse filter strings for a given data profile",
//            nickname = "getAllInverseCategoryFiltersForProfile",
//            produces = "application/json",
//            httpMethod = "GET"
//    )
//    @ApiResponses([
//            @ApiResponse(code = SC_OK, message = "OK", response = String, responseContainer = "Map")
//    ])
//    @ApiImplicitParams([
//            @ApiImplicitParam(name = "qualityProfileId", paramType = "query", required = false, value = "Quality Profile Id", dataType = 'integer')
//    ])
    def getAllInverseCategoryFiltersForProfile() {
        def result = qualityService.getAllInverseCategoryFilters(params.long('qualityProfileId'))
        render result as JSON
    }

}
