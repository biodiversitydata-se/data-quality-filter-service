/*
 * Copyright (C) 2014 Atlas of Living Australia
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

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONObject

/**
 * Controller for occurrence searches and records
 */
@Slf4j
class OccurrenceController {
    def webServicesService, facetsCacheService, postProcessingService, authService

    QualityService qualityService

    /**
     * Perform a full text search
     *
     * @param requestParams
     * @return
     */
    def list(SpatialSearchRequestParams requestParams) {
        def start = System.currentTimeMillis()

        requestParams.fq = params.list("fq") as String[] // override Grails binding which splits on internal commas in value

        if (!params.pageSize) {
            requestParams.pageSize = 20
        }

        if (!params.sort && !params.dir) {
            requestParams.sort = "first_loaded_date"
            requestParams.dir = "desc"
        }

        log.debug "requestParams = ${requestParams}"

        List taxaQueries = (ArrayList<String>) params.list("taxa") // will be list for even one instance
        log.debug "skin.useAlaBie = ${grailsApplication.config.skin.useAlaBie}"
        log.debug "taxaQueries = ${taxaQueries} || q = ${requestParams.q}"

        if (grailsApplication.config.skin.useAlaBie?.toString()?.toBoolean() &&
                grailsApplication.config.bie.baseUrl && taxaQueries && taxaQueries[0]) {
            // check for list with empty string
            // taxa query - attempt GUID lookup
            List guidsForTaxa = webServicesService.getGuidsForTaxa(taxaQueries)
            def additionalQ = (params.q) ? " AND " + params.q : "" // advanced search form can provide both taxa and q params
            requestParams.q = postProcessingService.createQueryWithTaxaParam(taxaQueries, guidsForTaxa) + additionalQ
        } else if (!params.q && taxaQueries && taxaQueries[0]) {
            // Bypass BIE lookup and pass taxa query in as text
            List emptyGuidList = taxaQueries.clone().collect { it = ""} // list of empty strings, of equal size to taxaQueries
            requestParams.q = postProcessingService.createQueryWithTaxaParam(taxaQueries, emptyGuidList)
        }

        if (!requestParams.q) {
            requestParams.q = "*:*"
        }

        try {
            //the configured grouping
            Map configuredGroupedFacets = webServicesService.getGroupedFacets()
            List listOfGroupedFacets = postProcessingService.getListFromGroupedFacets(configuredGroupedFacets)
            Map defaultFacets = postProcessingService.getAllFacets(listOfGroupedFacets)
            String[] userFacets = postProcessingService.getFacetsFromCookie(request)
            String[] filteredFacets = postProcessingService.getFilteredFacets(defaultFacets)

            final facetsDefaultSelectedConfig = grailsApplication.config.facets.defaultSelected
            if (!userFacets && facetsDefaultSelectedConfig) {
                userFacets = facetsDefaultSelectedConfig.trim().split(",")
                log.debug "facetsDefaultSelectedConfig = ${facetsDefaultSelectedConfig}"
                log.debug "userFacets = ${userFacets}"
                def facetKeys = defaultFacets.keySet()
                facetKeys.each {
                    defaultFacets.put(it, false)
                }
                userFacets.each {
                    defaultFacets.put(it, true)
                }
            }

            List dynamicFacets = []

            String[] requestedFacets = userFacets ?: filteredFacets

            if (grailsApplication.config.facets.includeDynamicFacets?.toString()?.toBoolean()) {
                // Sandbox only...
                dynamicFacets = webServicesService.getDynamicFacets(requestParams.q)
                requestedFacets = postProcessingService.mergeRequestedFacets(requestedFacets as List, dynamicFacets)
            }

            requestParams.facets = requestedFacets

            def wsStart = System.currentTimeMillis()
            JSONObject searchResults = webServicesService.fullTextSearch(requestParams)
            def wsTime = (System.currentTimeMillis() - wsStart)

            // If there's an error, treat it as an exception so error page can be shown
            if (searchResults.status == 'ERROR') {
                throw new Exception(searchResults.errorMessage)
            }

            //create a facet lookup map
            Map groupedFacetsMap = postProcessingService.getMapOfFacetResults(searchResults.facetResults)

            //grouped facets
            Map groupedFacets = postProcessingService.getAllGroupedFacets(configuredGroupedFacets, searchResults.facetResults, dynamicFacets)

            //remove qc from active facet map
            if (params?.qc) {
                def qc = params.qc
                if (searchResults?.activeFacetMap) {
                    def remove = null
                    searchResults?.activeFacetMap.each { k, v ->
                        if (k + ':' + v?.value == qc) {
                            remove = k
                        }
                    }
                    if (remove) searchResults?.activeFacetMap?.remove(remove)
                }

                if (searchResults?.activeFacetObj) {
                    def removeKey = null
                    def removeIdx = null
                    searchResults?.activeFacetObj.each { k, v ->
                        def idx = v.findIndexOf { it.value == qc }
                        if (idx > -1) {
                            removeKey = k
                            removeIdx = idx
                        }
                    }
                    if (removeKey && removeIdx != null) {
                        searchResults.activeFacetObj[removeKey].remove(removeIdx)
                    }
                }

            }

            def hasImages = postProcessingService.resultsHaveImages(searchResults)
            if(grailsApplication.config.alwaysshow.imagetab?.toString()?.toBoolean()){
                hasImages = true
            }

            def qualityCategories = qualityService.findAllEnabledCategories(requestParams.qualityProfile)
            def qualityFiltersByLabel = qualityService.getEnabledFiltersByLabel(requestParams.qualityProfile)
            def qualityExcludeCount = qualityService.getExcludeCount(qualityCategories, requestParams)
            def qualityTotalCount = qualityService.countTotalRecords(requestParams)
            def qualityFilterDescriptionsByLabel = qualityService.getGroupedEnabledFilters(requestParams.qualityProfile).collectEntries {[(it.key) : it.value*.description.join(' and ')]}

            def (userFqInteractDQNames, dqInteractFQs, UserFQColors, DQColors) = postProcessingService.processUserFQInteraction(requestParams, searchResults?.activeFacetObj)

            def translatedFilterMap = postProcessingService.translateValues(qualityService.getGroupedEnabledFilters(requestParams.qualityProfile), webServicesService.getMessagesPropertiesFile(), webServicesService.getAssertionCodeMap())

            log.debug "defaultFacets = ${defaultFacets}"

            def json = [
                    sr: searchResults,
                    searchRequestParams: requestParams,
                    defaultFacets: defaultFacets,
                    groupedFacets: groupedFacets,
                    groupedFacetsMap: groupedFacetsMap,
                    dynamicFacets: dynamicFacets,
                    translatedFilterMap: translatedFilterMap,
                    selectedDataResource: getSelectedResource(requestParams.q),
                    hasImages: hasImages,
                    showSpeciesImages: false,
                    overlayList: postProcessingService.getListOfLayers(requestParams),
                    sort: requestParams.sort,
                    dir: requestParams.dir,
                    userId: authService?.getUserId(),
                    userEmail: authService?.getEmail(),
                    processingTime: (System.currentTimeMillis() - start),
                    wsTime: wsTime,
                    qualityCategories: qualityCategories,
                    qualityExcludeCount: qualityExcludeCount,
                    qualityFiltersByLabel: qualityFiltersByLabel,
                    qualityTotalCount: qualityTotalCount,
                    userFqInteractDQNames: userFqInteractDQNames,
                    qualityFilterDescriptionsByLabel: qualityFilterDescriptionsByLabel,
                    dqInteractFQs: dqInteractFQs,
                    UserFQColors: UserFQColors,
                    DQColors: DQColors,
                    activeProfile: qualityService.activeProfile(requestParams.qualityProfile),
                    qualityProfiles: QualityProfile.findAllByEnabled(true)
            ] as JSON

            json.prettyPrint = true
            json.render response

        } catch (Exception ex) {
            def error = "Error getting search results: $ex.message"
            flash.message = "${ex.message}"
            render error
        }
    }

    private def getSelectedResource(query){
        if(query.contains("data_resource_uid:")){
            query.replace("data_resource_uid:","")
        } else {
            ""
        }
    }
}
