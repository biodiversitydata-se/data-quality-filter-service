package au.org.ala.dataqualityfilter

import au.org.ala.plugins.openapi.Path
import grails.converters.JSON
import grails.rest.RestfulController
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

class QualityFilterController extends RestfulController<QualityFilter> {

    static responseFormats = ['json']
    def qualityService

    QualityFilterController() {
        super(QualityFilter, true)
    }

    @Override
    protected QualityFilter queryForResource(Serializable id) {
        return qualityService.findFilterByProfileAndCategoryAndId(params.qualityProfileId, params.qualityCategoryId, id)
    }

    @Override
    protected List<QualityFilter> listAllResources(Map params) {
        return qualityService.findFiltersByProfileAndCategory(params.qualityProfileId, params.qualityCategoryId)
    }

    @Operation(
            method = "GET",
            tags = "filters",
            operationId = "getQualityFilters",
            summary = "List all quality filters",
            description = "List all quality filters",
            parameters = [
                    @Parameter(
                            name = "profileId",
                            in = PATH,
                            description = "The id or short name for the quality profile or default for the default profile",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
                    @Parameter(
                            name = "categoryId",
                            in = PATH,
                            description = "The id for the quality category",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
                    @Parameter(
                            name = "max",
                            in = QUERY,
                            description = "Maximum results to return",
                            schema = @Schema(implementation = Integer),
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "List of quality filters",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = QualityFilter))
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
    @Path("/api/v1/data-profiles/{profileId}/categories/{categoryId}/filters")
    @Produces("application/json")
    def index() {
        render listAllResources(params) as JSON
    }

    @Operation(
            method = "GET",
            tags = "filters",
            operationId = "getQualityFilter",
            summary = "Retrieve a single quality filter",
            description = "Retrieve a single quality filter",
            parameters = [
                    @Parameter(
                            name = "profileId",
                            in = PATH,
                            description = "The id or short name for the quality profile or default for the default profile",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
                    @Parameter(
                            name = "categoryId",
                            in = PATH,
                            description = "The id for the quality category",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
                    @Parameter(
                            name = "id",
                            in = PATH,
                            description = "The id for the quality filter",
                            schema = @Schema(implementation = String),
                            required = true
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "A quality filter",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = QualityFilter)
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
    @Path("/api/v1/data-profiles/{profileId}/categories/{categoryId}/filters/{id}")
    @Produces("application/json")
    def show() {
       render queryForResource(params.id) as JSON
    }
}
