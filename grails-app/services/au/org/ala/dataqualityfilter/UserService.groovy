package au.org.ala.dataqualityfilter

import grails.gorm.transactions.Transactional
import au.org.ala.web.CASRoles

@Transactional
class UserService {
    def authService

    def getLoggedInUserId() {
        authService?.getUserId()
    }

    def isLoggedInAdmin() {
        isAdmin(getLoggedInUserId())
    }

    private def isAdmin(String userId) {
        authService?.getUserForUserId(userId)?.hasRole(CASRoles.ROLE_ADMIN)
    }
}
