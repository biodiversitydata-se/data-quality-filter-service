package au.org.ala.dataqualityfilter

class QualityApiKeyInterceptor {
    def qualityService
    def apiKeyService

    static final String API_KEY_HEADER_NAME = "apiKey"
    static final int STATUS_UNAUTHORISED = 403

    QualityApiKeyInterceptor() {
        match(controller: "quality")
        match(controller: "qualityProfile")
        match(controller: "qualityCategory")
        match(controller: "qualityFilter")
    }

    boolean before() {
        boolean checkApiKey = false
        // if controllerName == 'quality' look for profileName, userId, profileId from query params
        QualityProfile qp = null
        if (controllerName == 'quality') {
            if (actionName == 'activeProfile') { // activeProfile provides both profileName and userId
                qp = qualityService.findProfileById(params.profileName)
                if (!qp && params.userId) {
                    checkApiKey = true
                }
            } else if (params.profileName) {
                qp = qualityService.findProfileById(params.profileName)
            } else if (params.qualityProfileId) {
                qp = qualityService.findProfileById(params.profileName)
            } else if (params.userId) {
                checkApiKey = true
            } else if (params.qualityCategoryId) {
                def qc = QualityCategory.get(params.qualityCategoryId)
                qp = qc?.qualityProfile
            }
        } else { // else profileId is in the path
            def profileId = (controllerName == 'qualityProfile') ? params.id : params.qualityProfileId
            qp = qualityService.findProfileById(profileId)
        }

        checkApiKey = checkApiKey || (qp && !qp.isPublic)

        if (checkApiKey) {
            def apiKey = request.getHeader(API_KEY_HEADER_NAME)
            boolean keyOk = apiKeyService.checkApiKey(apiKey).valid
            if (!keyOk) {
                log.warn("No valid api key for ${controllerName}/${actionName}")
                response.status = STATUS_UNAUTHORISED
                response.sendError(STATUS_UNAUTHORISED, "Forbidden")
                return false
            }
        }

        true
    }
}
