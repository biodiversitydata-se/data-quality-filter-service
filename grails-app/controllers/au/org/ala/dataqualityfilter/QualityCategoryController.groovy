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

class QualityCategoryController extends RestfulController<QualityCategory> {

    static responseFormats = ['json']
    def qualityService

    QualityCategoryController() {
        super(QualityCategory, true)
    }

    @Override
    protected QualityCategory queryForResource(Serializable id) {
        return qualityService.findCategoryByProfileAndId(params.qualityProfileId, id)
    }

    @Override
    protected List<QualityCategory> listAllResources(Map params) {
        return qualityService.findCategoriesByProfile(params.qualityProfileId)
    }

    @Operation(
            method = "GET",
            tags = "categories",
            operationId = "getQualityCategories",
            summary = "List all quality categories",
            description = "List all quality categories",
            parameters = [
                    @Parameter(
                            name = "profileId",
                            in = PATH,
                            description = "The id or short name for the quality profile or default for the default profile",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
                    @Parameter(
                            name = "max",
                            in = QUERY,
                            description = "Maximum results to return",
                            schema = @Schema(implementation = Boolean),
                            required = false
                    )
            ],
            responses = [
                    @ApiResponse(
                            description = "List of quality categories",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = QualityCategory))
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
    @Path("/api/v1/data-profiles/{profileId}/categories")
    @Produces("application/json")
    def index() {
        render listAllResources(params) as JSON
    }

    @Operation(
            method = "GET",
            tags = "categories",
            operationId = "getQualityCategory",
            summary = "Retrieve a single quality category",
            description = "Retrieve a single quality category",
            parameters = [
                    @Parameter(
                            name = "profileId",
                            in = PATH,
                            description = "The id or short name for the quality profile or default for the default profile",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
                    @Parameter(
                            name = "id",
                            in = PATH,
                            description = "The id for the quality category",
                            schema = @Schema(implementation = String),
                            required = true
                    ),
            ],
            responses = [
                    @ApiResponse(
                            description = "A quality category",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = QualityProfile)
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
    @Path("/api/v1/data-profiles/{profileId}/categories/{id}")
    @Produces("application/json")
    def show() {
        render queryForResource(params.id) as JSON
    }
}
