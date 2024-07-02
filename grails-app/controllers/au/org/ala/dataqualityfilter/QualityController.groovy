package au.org.ala.dataqualityfilter

import au.org.ala.plugins.openapi.Path
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.converters.JSON
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

import javax.ws.rs.Produces

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY

//@Api(value = "/api/v1/quality/", tags = ["QualityServiceRPC"], description = "Data Quality web services to support biocache-hubs functions")
class QualityController {

    static responseFormats = ['json']
    static namespace = "v1"

    def qualityService

    @Operation(
            method = "GET",
            tags = "Filters",
            operationId = "getEnabledFiltersByLabel",
            summary = "Get enabled filters, grouped by category label for a given profile name",
            description = "Get enabled filters, grouped by category label for a given profile name",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            example = "ALA General",
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
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
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

    @Operation(
            method = "GET",
            tags = "Filters",
            operationId = "getEnabledQualityFilters",
            summary = "Get Enabled Quality Filters",
            description = "Get Enabled Quality Filters",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            example = "ALA General",
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
                                            array = @ArraySchema(schema = @Schema(implementation = String))
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/getEnabledQualityFilters")
    @Produces("application/json")
    def getEnabledQualityFilters(String profileName) {
        render qualityService.getEnabledQualityFilters(profileName) as JSON
    }

    /** This class is unused other than to provides the type information for the OpenAPI definition */
    @JsonIgnoreProperties('metaClass')
    class GetGroupedEnabledFiltersResponse extends  LinkedHashMap<String, List<QualityFilter>> {}

    @Operation(
            method = "GET",
            tags = "Filters",
            operationId = "getGroupedEnabledFilters",
            summary = "Get Grouped Enabled Filters",
            description = "Get Grouped Enabled Filters",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            example = "ALA General",
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "Group enabled filters",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = GetGroupedEnabledFiltersResponse)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/getGroupedEnabledFilters")
    @Produces("application/json")
    def getGroupedEnabledFilters(String profileName) {
        render qualityService.getGroupedEnabledFilters(profileName) as JSON
    }
    @Operation(
            method = "GET",
            tags = "Categories",
            operationId = "findAllEnabledCategories",
            summary = "Find All Enabled Categories",
            description = "Find All Enabled Categories for a specified profile",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            example =  "ALA General",
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "All enabled Categories",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = QualityCategory)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/findAllEnabledCategories")
    @Produces("application/json")
    def findAllEnabledCategories(String profileName) {
        render qualityService.findAllEnabledCategories(profileName) as JSON
    }

    @Operation(
            method = "GET",
            tags = "Profiles",
            operationId = "activeProfile",
            summary = "Retrieve the data profile for a given profile's short name",
            description = "Retrieve the data profile for a given profile's short name.  If the profile doesn't exist or the short name is omitted then the default profile is returned instead.",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            example = "ALA",
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "Data profile",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = QualityProfile)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/activeProfile")
    @Produces("application/json")
    def activeProfile(String profileName) {
        render qualityService.activeProfile(profileName) as JSON
    }

    @Operation(
            method = "GET",
            tags = "Profiles",
            operationId = "getJoinedQualityFilter",
            summary = "Get the full filter string for a given data profile",
            description = "Get the full filter string for a given data profile",
            parameters = [
                    @Parameter(
                            name = "profileName",
                            in = QUERY,
                            description = "Profile name",
                            schema = @Schema(implementation = String),
                            example = "ALA",
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "Full filter string",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "text/plain",
                                            schema = @Schema(implementation = String)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/getJoinedQualityFilter")
    @Produces("text/plain")
    def getJoinedQualityFilter(String profileName) {
        render qualityService.getJoinedQualityFilter(profileName), contentType: 'text/plain'
    }

    @Operation(
            method = "GET",
            tags = "Categories",
            operationId = "getInverseCategoryFilter",
            summary = "Get the full inverse filter string for a given quality category",
            description = "Get the full inverse filter string for a given quality category. Results for the default profile will be returned if qualityProfileId is omitted",
            parameters = [
                    @Parameter(
                            name = "qualityCategoryId",
                            in = QUERY,
                            description = "Quality Category Id",
                            schema = @Schema(implementation = String),
                            example = "441",
                            required = true
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "Full inverse filter string",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "text/plain",
                                            schema = @Schema(implementation = String)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/getInverseCategoryFilter")
    @Produces("text/plain")
    def getInverseCategoryFilter() {
        def result = qualityService.getInverseCategoryFilter(params.long('qualityCategoryId'))
        if (result == null) {
            render '', contentType: 'text/plain', status: 404
        } else {
            render result, contentType: 'text/plain'
        }
    }

    @Operation(
            method = "GET",
            tags = "Profiles",
            operationId = "getAllInverseCategoryFiltersForProfile",
            summary = "Get all the inverse filter strings for a given data profile",
            description = "Get all the inverse filter strings for a given data profile. Results for the default profile will be returned if qualityProfileId is omitted",
            parameters = [
                    @Parameter(
                            name = "qualityProfileId",
                            in = QUERY,
                            description = "Quality Profile Id",
                            schema = @Schema(implementation = String),
                            example = "441",
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "Full inverse filter string",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = Map)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "string")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "string"))
                            ]
                    )
            ]
    )
    @Path("/api/v1/quality/getAllInverseCategoryFiltersForProfile")
    @Produces("application/json")

    def getAllInverseCategoryFiltersForProfile() {
        def result = qualityService.getAllInverseCategoryFilters(params.long('qualityProfileId'))
        render result as JSON
    }

}
