package au.org.ala.dataqualityfilter

class AdminDataQualityInterceptor {
    AdminDataQualityInterceptor() {
        match(controller: "adminDataQuality", action: "saveProfile")
        match(controller: "adminDataQuality", action: "importProfile")
        match(controller: "adminDataQuality", action: "enableQualityProfile")
        match(controller: "adminDataQuality", action: "setDefaultProfile")
        match(controller: "adminDataQuality", action: "exportProfile")
        match(controller: "adminDataQuality", action: "deleteQualityProfile")
    }

    def userService

    boolean before() {
        // several things are checked here
        // 1. user must be logged in
        // 2. only admin can edit/export public profiles
        // 3. non-admin user can only edit/export his own profile
        while (true) {
            def loggedInUserId = userService.getLoggedInUserId()

            // if not logged in
            if (!loggedInUserId) {
                flash.message = 'you need to login to make changes to the profiles'
                break;
            }

            def requestUserId = null

            if (params.action in ['saveProfile', 'enableQualityProfile', 'setDefaultProfile', 'exportProfile', 'deleteQualityProfile']) {
                requestUserId = params.userId ?: null
            } else if (params.action == 'importProfile') {
                requestUserId = request.getParameter('userId') ?: null
            }

            // if current logged in is not admin but save a public profile, fail it
            if (!requestUserId && !userService.isLoggedInAdmin()) {
                flash.message = 'you need to admin to edit/export public profiles'
                break
            }
            // we can only save using the logged in id
            if (requestUserId != null && requestUserId != loggedInUserId) {
                flash.message = 'you can only edit/export your own profiles'
                break
            }
            return true
        }

        redirect(controller: "adminDataQuality", action: "data-profiles")
    }
}