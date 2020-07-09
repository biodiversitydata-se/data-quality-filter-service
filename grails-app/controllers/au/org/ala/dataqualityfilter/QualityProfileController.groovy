package au.org.ala.dataqualityfilter

import grails.rest.RestfulController
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static javax.servlet.http.HttpServletResponse.SC_OK

@Api(value = "/api/v1/", tags = ["profiles"], description = "Data Quality RESTful API for Quality Profiles")
class QualityProfileController extends RestfulController<QualityProfile> {

    static responseFormats = ['json']

    def qualityService

    QualityProfileController() {
        super(QualityProfile, true)
    }

    @Override
    protected QualityProfile queryForResource(Serializable id) {
        return qualityService.findQualityProfileById(id)
    }

    @ApiOperation(
            value = "List all quality profiles",
            nickname = "profiles",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Maximum results to return", dataType = 'integer')
    ])
    def index(Integer max) {
        super.index(max)
    }

    @ApiOperation(
            value = "List all quality profiles",
            nickname = "profiles/{id}",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = SC_OK, message = "OK", response = QualityProfile, responseContainer = "List")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = false, value = "The id or short name for the quality profile or default for the default profile", dataType = 'string'),
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Maximum results to return", dataType = 'integer')
    ])
    def show() {
        super.show()
    }
}
