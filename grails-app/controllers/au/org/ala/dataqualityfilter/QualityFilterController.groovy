package au.org.ala.dataqualityfilter

import grails.rest.RestfulController
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static javax.servlet.http.HttpServletResponse.SC_OK

@Api(value = "/api/v1/profiles/{profileId}/categories/{categoryId}/", tags = ["filters"], description = "Data Quality RESTful API for Quality Categories")
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

    @ApiOperation(
            value = "List all quality filters",
            nickname = "filters",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityFilter, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileId", paramType = "path", required = true, value = "The id or short name for the quality profile or default for the default profile", dataType = 'string'),
            @ApiImplicitParam(name = "categoryId", paramType = "path", required = true, value = "The id for the quality category", dataType = 'string'),
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Maximum results to return", dataType = 'integer')
    ])
    def index(Integer max) {
        super.index(max)
    }

    @ApiOperation(
            value = "List all quality profiles",
            nickname = "filters/{id}",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityFilter, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "profileId", paramType = "path", required = true, value = "The id or short name for the quality profile or default for the default profile", dataType = 'string'),
            @ApiImplicitParam(name = "categoryId", paramType = "path", required = true, value = "The id for the quality category", dataType = 'string'),
            @ApiImplicitParam(name = "id", paramType = "path", required = false, value = "The id for the quality category", dataType = 'integer'),
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Maximum results to return", dataType = 'integer')
    ])
    def show() {
        super.show()
    }
}
