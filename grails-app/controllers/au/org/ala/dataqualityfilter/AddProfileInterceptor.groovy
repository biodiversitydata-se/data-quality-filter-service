package au.org.ala.dataqualityfilter

class AddProfileInterceptor {
    AddProfileInterceptor() {
        match(controller: "adminDataQuality", action: "saveProfile")
        match(controller: "adminDataQuality", action: "importProfile")
    }

    // 1 user can only have 1 private profile, checks are done here
    boolean before() {
        while (true) {

            def requestUserId = null
            def profileId = null

            if (params.action in ['saveProfile']) {
                requestUserId = params.userId ?: null
                profileId = params.id ?: null
            } else if (params.action in ['importProfile']) {
                requestUserId = request.getParameter('userId') ?: null
            }

            // if public profile, no limit
            if (requestUserId == null) return true

            // must be private profile here
            List<QualityCategory> existingProfiles = QualityProfile.findAllByUserId(requestUserId)
            if (existingProfiles.size() > 1) {
                break
            } else if (existingProfiles.size() == 1 && profileId == null) { // profileId == null means create new profile
                break
            }
            return true
        }
        flash.message = 'you can only have 1 private profile'
        redirect(controller: "adminDataQuality", action: "data-profiles")
    }
}