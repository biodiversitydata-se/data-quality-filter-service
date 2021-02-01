/*
 * Copyright (C) 2020 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
package au.org.ala.dataqualityfilter

import au.org.ala.web.CASRoles
import com.google.gson.Gson
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.web.servlet.mvc.SynchronizerTokensHolder

class AdminDataQualityController {

    public static final String DATA_PROFILES_ACTION_NAME = 'data-profiles'
    def qualityService
    def webServicesService
    def authService

    def filters() {
        def qp = QualityProfile.get(params.long('id'))
        if (qp) {
            respond QualityCategory.findAllByQualityProfile(qp, [sort: 'id', lazy: false]), model: ['qualityFilterStrings': qualityService.getEnabledFiltersByLabel(qp.shortName), 'errors': flash.errors, 'options': webServicesService.getAllOccurrenceFields(), 'profile': qp]
        }
    }

    def profiles() {
        redirect action: DATA_PROFILES_ACTION_NAME
    }

    def 'data-profiles'() {
        def model = [:]
        // if admin, add public profiles
        if (request.isUserInRole(CASRoles.ROLE_ADMIN)) {
            model.put('publicProfiles', QualityProfile.findAll(sort:"id") {
                isPublic == true
            })
        }
        // always add private profiles
        model.put('privateProfiles', QualityProfile.findAll(sort:"id") {
            userId == authService?.getUserId()
        })
        model.put('userId', authService?.getUserId())
        model.put('errors', flash.errors)
        model
    }

    def saveProfile(QualityProfile qualityProfile) {
        preprocessProfile(qualityProfile)
        def invalidReq = false
        withForm {
            try {
                qualityService.createOrUpdateProfile(qualityProfile)
            } catch (ValidationException e) {
                flash.errors = e.errors
            } catch (MaximumRecordNumberReachedException e) {
                flash.message = e.message
            }
        }.invalidToken {
            invalidReq = true
            log.debug("ignore duplicate save profile request. name:{}, shortName:{}", qualityProfile.name, qualityProfile.shortName)
        }

        withFormat {
            html {
                redirect(action: DATA_PROFILES_ACTION_NAME)
            }
            json {
                if (flash.errors || invalidReq) {
                    render {} as JSON
                } else {
                    def rslt = [:]
                    rslt.profile = QualityProfile.findById(qualityProfile.id)
                    rslt.token = SynchronizerTokensHolder.store(session).generateToken(params.SYNCHRONIZER_URI)
                    render rslt as JSON
                }
            }
        }
    }

    def enableQualityProfile() {
        def qp = QualityProfile.get(params.long('id'))
        if (processAllowed(qp)) {
            withForm {
                qp.enabled = params.boolean('enabled', false)
                qp.save(flush: true)
            }.invalidToken {
                // bad request
                log.debug("ignore duplicate enable category request")
            }
            redirect(action: DATA_PROFILES_ACTION_NAME)
        }
    }

    def setDefaultProfile() {
        if (processAllowed(QualityProfile.get(params.long('id')))) {
            withForm {
                qualityService.setDefaultProfile(params.long('id'))
            }.invalidToken {
                log.debug('set default profile invalid token')
            }
            redirect(action: DATA_PROFILES_ACTION_NAME)
        }
    }

    def deleteQualityProfile(QualityProfile qualityProfile) {
        if (processAllowed(qualityProfile)) {
            withForm {
                qualityService.deleteProfile(qualityProfile)
            }.invalidToken {
                // bad request
                log.debug("ignore duplicate delete profile request. name: {}, shortname: {}", qualityProfile.name, qualityProfile.shortName)
            }
            redirect(action: DATA_PROFILES_ACTION_NAME)
        }
    }

    def saveQualityCategory(QualityCategory qualityCategory) {
        def invalidReq = false
        withForm {
            try {
                qualityService.createOrUpdateCategory(qualityCategory)
            } catch (ValidationException e) {
                flash.errors = e.errors
            }
        }.invalidToken {
            invalidReq = true
            log.debug("ignore duplicate save category request. name:{}, label:{}", qualityCategory.name, qualityCategory.label)
        }

        withFormat {
            html {
                redirect(action: 'filters', id: qualityCategory.qualityProfile.id)
            }
            json {
                if (flash.errors || invalidReq) {
                    render { } as JSON
                } else {
                    def rslt = [:]
                    rslt.category = QualityCategory.findById(qualityCategory.id)
                    rslt.token = SynchronizerTokensHolder.store(session).generateToken(params.SYNCHRONIZER_URI)
                    render rslt as JSON
                }
            }
        }
    }

    def enableQualityCategory() {
        def qc = QualityCategory.get(params.long('id'))
        withForm {
            qc.enabled = params.boolean('enabled', false)
            qc.save(flush: true)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate enable category request")
        }
        redirect(action: 'filters', id: qc.qualityProfile.id)
    }

    def deleteQualityCategory(QualityCategory qualityCategory) {
        withForm {
            qualityService.deleteCategory(qualityCategory)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate delete category request. name:{}, label:{}", qualityCategory.name, qualityCategory.label)
        }
        redirect(action: 'filters', id: qualityCategory.qualityProfile.id)
    }

    def saveQualityFilter(QualityFilter qualityFilter) {
        def profileid = qualityFilter.qualityCategory.qualityProfile.id

        withFormat {
            html {
                withForm {
                    try {
                        qualityService.createOrUpdateFilter(qualityFilter)
                    } catch (ValidationException e) {
                        flash.errors = e.errors
                    } catch (IllegalStateException e) {
                        return render(status: 400, text: 'invalid params')
                    }
                    redirect(action: 'filters', id: profileid)
                }.invalidToken {
                    // bad request
                    log.debug("ignore duplicate save filter request. description:{}, filter:{}", qualityFilter.description, qualityFilter.filter)
                    redirect(action: 'filters', id: profileid)
                }
            }
            json {
                def succeed = false
                withForm {
                    try {
                        qualityService.createOrUpdateFilter(qualityFilter)
                        succeed = true
                    } catch (ValidationException e) {
                        flash.errors = e.errors
                    }
                }.invalidToken {
                    // bad request
                    log.debug("ignore duplicate save filter request. description:{}, filter:{}", qualityFilter.description, qualityFilter.filter)
                }

                if (succeed) {
                    def rslt = [:]
                    rslt.filter = QualityFilter.findById(qualityFilter.id)
                    rslt.token = SynchronizerTokensHolder.store(session).generateToken(params.SYNCHRONIZER_URI)
                    render rslt as JSON
                } else {
                    render { } as JSON
                }
            }
        }
    }

    def deleteQualityFilter() {
        def id = params.long('id')
        def profileId = params.long('profileId')
        withForm {
            if (!id) {
                return render(status: 404, text: 'filter not found')
            }
            qualityService.deleteFilter(id)
        }.invalidToken {
            log.debug("ignore duplicate delete filter request")
        }
        redirect(action: 'filters', id: profileId)
    }

    def enableQualityFilter() {
        def qf = QualityFilter.get(params.long('id'))
        withForm {
            qf.enabled = params.boolean('enabled', false)
            qf.save(flush: true)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate enable filter request")
        }
        redirect(action: 'filters', id: qf.qualityCategory.qualityProfile.id)
    }

    def exportProfile() {
        QualityProfile profile = QualityProfile.get(params.long('profileId'))

        if (profile && processAllowed(profile)) {
            if (profile) {
                String fileName = 'profile_' + profile.shortName
                response.setHeader('Content-Disposition', 'attachment; filename=' + fileName + '.json')
                response.setContentType("text");

                JSON.use('exportProfile', {
                    render profile as JSON
                })
            }
        }
    }

    @Transactional
    def importProfile() {
        while (true) {
            def f = request.getFile('filejson')
            // if file not selected or empty
            if (f == null || f.empty) {
                flash.message = 'File selected is empty'
                break
            }

            QualityProfile profile = null
            try {
                // convert json to QualityProfile
                profile = new Gson().fromJson(new InputStreamReader(f.getInputStream()), QualityProfile.class);
            } catch (e) {
                flash.message = e.getLocalizedMessage()
                break
            }

            preprocessProfile(profile)

            try {
                // set profile display order, there may already have existing profiles, so new display order = current max + 1
                profile.displayOrder = (QualityProfile.selectMaxDisplayOrder().get() ?: 0) + 1

                // setup mapping
                // since we are creating a new profile, displayOrders of categories within it should start from 1
                Long categoryDisplayOrder = 1
                for (QualityCategory category : profile.categories) {
                    category.qualityProfile = profile
                    if (category.displayOrder == null) {
                        category.displayOrder = categoryDisplayOrder++
                    }

                    // within a category, filter displayOrder starts from 1
                    Long filterDisplayOrder = 1;
                    for (QualityFilter filter : category.qualityFilters) {
                        filter.qualityCategory = category
                        if (filter.displayOrder == null) {
                            filter.displayOrder = filterDisplayOrder++;
                        }
                    }
                }

                // validate profile
                if (!profile.validate()) {
                    flash.errors = profile.errors
                    break;
                }

                // safe whole profile, if any filed fails validation an exception will be thrown
                qualityService.createOrUpdateProfile(profile)
            } catch (ValidationException e) {
                flash.errors = e.errors
            } catch (MaximumRecordNumberReachedException e) {
                flash.message = e.message
            }
            break
        }
        redirect(action: DATA_PROFILES_ACTION_NAME)
    }

    def fieldDescription(String field, String include, String value) {
        boolean isInclude = include == 'Include'
        def locale = request.locale
        def description = qualityService.getFieldDescription(isInclude, field, value, locale)
        if (description) {
            render description
        } else {
            render status: 404
        }
    }

    private boolean processAllowed(qualityProfile) {
        while (true) {
            // if processing public profile, you need to be an admin
            if (!qualityProfile.userId) {
                if (!request.isUserInRole(CASRoles.ROLE_ADMIN)) {
                    flash.message = 'you need to be an administrator to edit/export public profiles'
                    break;
                }
            } else { // if processing private profile, you need to be the owner of the profile
                if (qualityProfile.userId != authService?.getUserId()) {
                    flash.message = 'you can only edit/export your own profiles'
                    break;
                }
            }

            return true
        }
        redirect(controller: "adminDataQuality", action: "data-profiles")
    }

    private void preprocessProfile(qualityProfile) {
        qualityProfile.userId = params.isPublicProfile == 'true' ? null : authService?.getUserId()
    }
}
