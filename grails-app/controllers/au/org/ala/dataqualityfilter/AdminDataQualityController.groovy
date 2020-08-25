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

import com.google.gson.Gson
import grails.converters.JSON
import grails.transaction.Transactional
import grails.validation.ValidationException

class AdminDataQualityController {

    def qualityService
    def webServicesService

    def filters() {
        def qp = QualityProfile.get(params.long('id'))
        respond QualityCategory.findAllByQualityProfile(qp, [sort: 'id', lazy: false]), model: [ 'qualityFilterStrings' : qualityService.getEnabledFiltersByLabel(qp.shortName), 'errors': flash.errors, 'options': webServicesService.getAllOccurrenceFields(), 'profile': qp ]
    }

    def profiles() {
        respond QualityProfile.list(sort: 'id'), model: ['errors': flash.errors]
    }

    def saveProfile(QualityProfile qualityProfile) {
        withForm {
            saveProfileImpl(qualityProfile)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate save profile request. name:{}, shortName:{}", qualityProfile.name, qualityProfile.shortName)
        }
        redirect(action: 'profiles')
    }

    def saveProfileImpl(QualityProfile qualityProfile) {
        try {
            qualityService.createOrUpdateProfile(qualityProfile)
        } catch (ValidationException e) {
            flash.errors = e.errors
        }
    }

    def saveProfileViaPost(QualityProfile qualityProfile) {
        saveProfileImpl(qualityProfile)
        render QualityProfile.findById(qualityProfile.id) as JSON
    }

    def enableQualityProfile() {
        withForm {
            def qp = QualityProfile.get(params.long('id'))
            qp.enabled = params.boolean('enabled', false)
            qp.save(flush: true)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate enable category request")
        }
        redirect(action: 'profiles')
    }

    def setDefaultProfile() {
        withForm {
            qualityService.setDefaultProfile(params.long('id'))
        }.invalidToken {
            log.debug('set default profile invalid token')
        }
        redirect(action: 'profiles')
    }

    def deleteQualityProfile(QualityProfile qualityProfile) {
        withForm {
            qualityService.deleteProfile(qualityProfile)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate delete profile request. name: {}, shortname: {}", qualityProfile.name, qualityProfile.shortName)
        }
        redirect(action: 'profiles')
    }

    def saveQualityCategory(QualityCategory qualityCategory) {
        withForm {
            saveQualityCategoryImpl(qualityCategory)
        }.invalidToken {
            // bad request
            log.debug("ignore duplicate save category request. name:{}, label:{}", qualityCategory.name, qualityCategory.label)
        }
        redirect(action: 'filters', id: qualityCategory.qualityProfile.id)
    }

    def saveQualityCategoryImpl(QualityCategory qualityCategory) {
        try {
            qualityService.createOrUpdateCategory(qualityCategory)
        } catch (ValidationException e) {
            flash.errors = e.errors
        }
    }

    def saveQualityCategoryViaPost(QualityCategory qualityCategory) {
        saveQualityCategoryImpl(qualityCategory)
        render QualityCategory.findById(qualityCategory.id) as JSON
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

    def saveFilterImpl(QualityFilter qualityFilter) {
        try {
            qualityService.createOrUpdateFilter(qualityFilter)
        } catch (ValidationException e) {
            flash.errors = e.errors
        } catch (IllegalStateException e) {
            return render(status: 400, text: 'invalid params')
        }
    }

    def saveFilterViaPost(QualityFilter qualityFilter) {
        saveFilterImpl(qualityFilter)
        render QualityFilter.findById(qualityFilter.id) as JSON
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
        QualityProfile profile = QualityProfile.get(params.long('id'))

        if (profile) {
            String fileName = 'profile_' + params.long('id')
            response.setHeader('Content-Disposition', 'attachment; filename=' + fileName + '.json')
            response.setContentType("text");

            JSON.use('exportProfile', {
                render profile as JSON
            })
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
            }
            break
        }

        redirect(action: 'profiles')
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
}
