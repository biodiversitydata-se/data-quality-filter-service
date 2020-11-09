package au.org.ala.dataqualityfilter

import grails.rest.RestfulController
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static javax.servlet.http.HttpServletResponse.SC_OK

@Api(value = "/api/v1/data-profiles/{profileId}/", tags = ["categories"], description = "Data Quality RESTful API for Quality Categories")
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

    @ApiOperation(
            value = "List all quality categories",
            nickname = "categories",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityCategory, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileId", paramType = "path", required = false, value = "The id or short name for the quality profile or default for the default profile", dataType = 'string'),
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Maximum results to return", dataType = 'integer')
    ])
    def index(Integer max) {
        super.index(max)
    }

    @ApiOperation(
            value = "Retrieve a single quality category",
            nickname = "categories/{id}",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileId", paramType = "path", required = false, value = "The id or short name for the quality profile or default for the default profile", dataType = 'string'),
            @ApiImplicitParam(name = "id", paramType = "path", required = false, value = "The id for the quality category", dataType = 'integer')
    ])
    def show() {
        super.show()
    }
}
