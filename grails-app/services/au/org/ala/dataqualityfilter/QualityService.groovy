package au.org.ala.dataqualityfilter

import grails.core.GrailsApplication
import grails.plugin.cache.Cacheable
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TermRangeQuery
import org.grails.web.json.JSONArray
import org.springframework.context.MessageSource
import org.springframework.web.util.UriComponentsBuilder

@Transactional
class QualityService {

    GrailsApplication grailsApplication
    MessageSource messageSource
    def webServicesService

    def createOrUpdateCategory(QualityCategory qualityCategory) {
        if (qualityCategory.displayOrder == null) {
            qualityCategory.displayOrder = (QualityCategory.selectMaxDisplayOrder().get() ?: 0) + 1
        }

        qualityCategory.save(validate: true, failOnError: true)
    }

    def createOrUpdateFilter(QualityFilter qualityFilter) {
        if (qualityFilter.displayOrder == null) {
            def maxDisplayOrder = QualityFilter.withCriteria {
                qualityCategory {
                    eq('id', qualityFilter.qualityCategory.id)
                }
                projections {
                    max('displayOrder')
                }
            }

            qualityFilter.displayOrder = (maxDisplayOrder.get(0) ?: 0) + 1
        }
        qualityFilter.save(validate: true, failOnError: true)
    }

    void deleteFilter(Long id) {
        QualityFilter.get(id)?.delete()
    }

    void deleteCategory(QualityCategory qualityCategory) {
        qualityCategory.delete()
    }

    QualityProfile getProfile(String profileName) {
        return QualityProfile.findByShortName(profileName)    
    }

    @Transactional(readOnly = true)
    Map<String, String> getEnabledFiltersByLabel(String profileName) {
        getGroupedEnabledFilters(profileName).collectEntries { [(it.key): it.value*.filter.join(' AND ')] } as Map<String, String>
    }

    @Transactional(readOnly = true)
    List<String> getEnabledQualityFilters(String profileName) {
        QualityProfile qp = getProfile(profileName)
        if (qp) {
            return QualityFilter.withCriteria {
                eq('enabled', true)
                qualityCategory {
                    qualityProfile {
                        eq('id', qp.id)
                    }
                    eq('enabled', true)
                }
                projections {
                    property('filter')
                }
                order('displayOrder')
            } as List<String>
        }
        []
    }

    @Transactional(readOnly = true)
    Map<String, List<QualityFilter>> getGroupedEnabledFilters(String profileName) {
        QualityProfile qp = getProfile(profileName)

        if (qp) {
            return QualityFilter.withCriteria {
                eq('enabled', true)
                qualityCategory {
                    qualityProfile {
                        eq('id', qp.id)
                    }
                    eq('enabled', true)
                }
                order('displayOrder')
            }.groupBy { QualityFilter qualityFilter ->
                qualityFilter.qualityCategory.label
            }.collectEntries { label, filters ->
                [(label): filters]
            }
        }
        [:]
    }

    @Transactional(readOnly = true)
    Map<QualityCategory, List<QualityFilter>> getEnabledCategoriesAndFilters(String profileName) {
        QualityProfile qp = getProfile(profileName)
        if (qp) {
            return QualityFilter.withCriteria {
                eq('enabled', true)
                qualityCategory {
                    qualityProfile {
                        eq('id', qp.id)
                    }
                    eq('enabled', true)
                }
            }.groupBy {
                (it.qualityCategory)
            }
        }
        [:]
    }

    @Transactional(readOnly = true)
    List<QualityCategory> findAllEnabledCategories(String profileName) {
        QualityProfile qp = getProfile(profileName)
        QualityCategory.findAllByQualityProfileAndEnabled(qp, true).findAll { category -> category.qualityFilters?.findAll { it.enabled }?.size() > 0 }
    }


    /**
     * Get the default profile. If userId provided, return the default profile for the user. Otherwise return the default public profile.
     * @param userId
     * @return
     */

    QualityProfile getDefaultProfile(String userId = null) {
        if (userId) {
            // if userId specified, return his profile if enabled, one person can have 1 profile now
            def qp = QualityProfile.findByUserId(userId)
            if (qp?.enabled) {
                return qp
            }
        }

        QualityProfile.findByIsDefault(true)
    }

    String getJoinedQualityFilter(String profileName) {
        getEnabledQualityFilters(profileName).join(' AND ')
    }

    @Transactional(readOnly = true)
    String getInverseCategoryFilter(Long categoryId) {
        def qc = QualityCategory.get(categoryId)
        qc ? getInverseCategoryFilter(qc) : null
    }

    @Transactional(readOnly = true)
    Map<String, String> getAllInverseCategoryFilters(Long profileId) {
        def qp = QualityProfile.get(profileId)
        qp ? qp?.getCategories()?.collectEntries { qc ->
            [(qc.label): getInverseCategoryFilter(qc)]
        } : [:]
    }

    @Transactional(readOnly = true)
    String getInverseCategoryFilter(QualityCategory category) {
        PrecedenceQueryParser qp = new PrecedenceQueryParser()
        qp.setAllowLeadingWildcard(true)
        TermQuery
        def filters = category.qualityFilters.findAll { it.enabled }*.filter.collect { processFilter(it) }
        def filter = filters.join(' AND ')
        if (!filter) return ''
        Query query = qp.parse(filter, '')
        String inverseQuery
        switch (query) {
            case BooleanQuery:
                inverseQuery = inverseBooleanQuery(query, filter)
                break
            default:
                inverseQuery = inverseOtherQuery(query)
                break
        }

        return inverseQuery
    }

    private def processFilter(filter) {
        def key = filter.substring(0, filter.indexOf(':') + 1)
        def val = filter.substring(filter.indexOf(':') + 1)

        // If the value is surrounded by "", when it's passed into QueryParser, they will be removed. So in inversed query there's no ""
        // for example -license:"CC-BY-NC" AND -license:"CC-BY-ND 4.0 (Int)" after inversing will be license:CC-BY-NC license:CC-BY-ND 4.0 (Int)
        // To retain the "" in inversed query, we escape the original surrounding ""
        // details can be found at https://stackoverflow.com/questions/63276920/
        //
        // we don't care about " inside the query becasue biocache-service will handle them.

        if (val.startsWith('"') && val.endsWith('"')) {
            val = '"\\"' + val.substring(1, val.length() - 1) + '\\""'
        }

        return key + val
    }

    private def inverseOtherQuery(Query query) {
        return new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST_NOT).build().toString()
    }

    private def inverseBooleanQuery(BooleanQuery booleanQuery, String originalQuery) {
        def clauses = booleanQuery.clauses()
        if ( clauses.size() == 1) {
            def first = clauses.first()
            if (first.prohibited) {
                return first.query.toString()
            } else {
                return inverseOtherQuery(first.query)
            }
        }

        // SOLR will return different results for:
        // 1) geospatial_kosher:"false" assertions:"habitatMismatch" -coordinate_uncertainty:[0+TO+10000]
        // 2) geospatial_kosher:"false" OR assertions:"habitatMismatch" OR -coordinate_uncertainty:[0+TO+10000]
        // 3) -(-geospatial_kosher:"false" -assertions:"habitatMismatch" +coordinate_uncertainty:[0+TO+10000])
        // 4) -(-geospatial_kosher:"false" AND -assertions:"habitatMismatch" AND coordinate_uncertainty:[0+TO+10000])
        //
        // 4) gives the correct results for inverting
        // -geospatial_kosher:"false" AND -assertions:"habitatMismatch" AND coordinate_uncertainty:[0+TO+10000]
        // so if the boolean contains a should or must range query then we just wrap the original query in an exclude
        if (clauses.any { clause -> clause.query instanceof TermRangeQuery && clause.occur != BooleanClause.Occur.MUST_NOT }) {
            return "-(${originalQuery})"
        } else {
            def bqb = new BooleanQuery.Builder()
            clauses.each { clause ->
                bqb.add(clause.query, clause.prohibited ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST_NOT)
            }
            return bqb.build().toString()
        }
    }

    @Transactional
    void createOrUpdateProfile(QualityProfile qualityProfile) {
        // if you are creating a private profile
        if (!qualityProfile.id && qualityProfile.userId) {
            List<QualityCategory> existingProfiles = QualityProfile.findAllByUserId(qualityProfile.userId) as List<QualityCategory>
            if (existingProfiles.size() >= 1) {
                throw new MaximumRecordNumberReachedException('You can only have 1 private profile')
            }
        }

        if (qualityProfile.displayOrder == null) {
            qualityProfile.displayOrder = (QualityProfile.selectMaxDisplayOrder().get() ?: 0) + 1
        }
        qualityProfile.save(validate: true, failOnError: true)
    }

    @Transactional
    void setDefaultProfile(Long id) {
        def qp = QualityProfile.get(id)
        if (qp) {
            qp.isDefault = true
            qp.save()
            def others = QualityProfile.findAllByIdNotEqual(id)
            others.each { it.isDefault = false }
            QualityProfile.saveAll(others)
        }
    }

    @Transactional
    void deleteProfile(QualityProfile qualityProfile) {
        qualityProfile.delete()
    }

    @NotTransactional
    String getFieldDescription(boolean include, String field, String value, Locale locale) {
        def cleanedValue = dequote(value)
        boolean isAssertion = field == 'assertions'
        def result

        def description
        if (isAssertion) {
            def assertionsMap = webServicesService.getAssertionCodeMap()
            description = assertionsMap[dequote(cleanedValue)]?.description
        }
        if (!description) {
            def biocacheField = getBiocacheField(field)
            description = biocacheField?.description
        }
        if (!description) {
            def props = webServicesService.getMessagesPropertiesFile()
            description = props["$field.$cleanedValue"] ?: props[cleanedValue] ?: props[field]
        }

        if (description && isAssertion) {
            if (include) {
                result = messageSource.getMessage('field.description.assertion.include', [description].toArray(), 'Include only records where {0}', locale)
            } else {
                result = messageSource.getMessage('field.description.assertion.exclude', [description].toArray(), 'Exclude all records where {0}', locale)
            }
        } else if (description) {
            if (include) {
                result = messageSource.getMessage('field.description.include', [description, value].toArray(), 'Include only records where {0} is "{1}"', locale)
            } else {
                result = messageSource.getMessage('field.description.exclude', [description, value].toArray(), 'Exclude all records where {0} is "{1}"', locale)
            }
        }

        return result
    }

    private String dequote(String string) {
        String retVal
        if (string == '"') {
            retVal = string
        } else if (string.startsWith('"') && string.endsWith('"')) {
            retVal = string.substring(1, string.length() - 1)
        } else {
            retVal = string
        }
        return retVal
    }

    /**
     * Get fields info from http://biocache.ala.org.au/ws/index/fields.
     * Example record:
     *
     * {
     *   dwcTerm: "basisOfRecord",
     *   downloadDescription: "Basis Of Record - processed",
     *   indexed: true,
     *   stored: true,
     *   downloadName: "basisOfRecord.p",
     *   multivalue: false,
     *   classs: "Record",
     *   description: "Basis Of Record - processed",
     *   dataType: "string",
     *   name: "basis_of_record"
     * }
     *
     * @return fields (List)
     */
    @Cacheable('longTermCache')
    @NotTransactional
    List getBiocacheFields()  {
        List fields
        def url = grailsApplication.config.getProperty('biocache.indexedFieldsUrl', String)
        def resp = webServicesService.getJsonElements(url)

        resp
    }

    /**
     * Get field info from http://biocache.ala.org.au/ws/index/fields?fl=<field>.
     * Example record:
     *
     * {
     *   dwcTerm: "basisOfRecord",
     *   downloadDescription: "Basis Of Record - processed",
     *   indexed: true,
     *   stored: true,
     *   downloadName: "basisOfRecord.p",
     *   multivalue: false,
     *   classs: "Record",
     *   description: "Basis Of Record - processed",
     *   dataType: "string",
     *   name: "basis_of_record"
     * }
     *
     */
    @NotTransactional
    def getBiocacheField(String field) {
        def baseUrl = grailsApplication.config.getProperty('biocache.indexedFieldsUrl', String)
        def url = UriComponentsBuilder.fromUriString(baseUrl).queryParam('fl', field).toUriString()
        def resp = webServicesService.getJsonElements(url)
        if (resp instanceof JSONArray && resp.length() > 0) resp = resp[0]
        resp
    }

    /**
     * Get the profile by id or a given short name
     * @param profileId The profile id or short name to lookup
     * @return The profile that matches or null if no matching found
     */

    QualityProfile findProfileById(Serializable profileId) {
        def qp
        if (profileId instanceof Long || profileId instanceof Integer) {
            qp = QualityProfile.get(profileId)
        } else if (profileId instanceof String && profileId.isLong()) {
            qp = QualityProfile.get(profileId.toLong())
        } else if (profileId instanceof String) {
            qp = QualityProfile.findByShortName(profileId)
        }
        return qp
    }

    List<QualityCategory> findCategoriesByProfile(Serializable profileId) {
        def qp = findProfileById(profileId)
        qp?.categories as List
    }

    QualityCategory findCategoryByProfileAndId(Serializable profileId, Serializable id) {
        def qp = findProfileById(profileId)
        QualityCategory.findByQualityProfileAndId(qp, id)
    }

    List<QualityFilter> findFiltersByProfileAndCategory(Serializable profileId, Serializable categoryId) {
        def qc = findCategoryByProfileAndId(profileId, categoryId)
        qc?.qualityFilters as List
    }

    QualityFilter findFilterByProfileAndCategoryAndId(Serializable profileId, Serializable categoryId, Serializable id) {
        def qc = findCategoryByProfileAndId(profileId, categoryId)
        QualityFilter.findByQualityCategoryAndId(qc, id)
    }

    List<QualityProfile> queryProfiles(Map map) {
        def c = QualityProfile.createCriteria()
        c.list(map) {
            if (map.containsKey('enabled')) {
                eq('enabled', map.boolean('enabled'))
            }
            if (map.containsKey('name')) {
                ilike('name', map.name)
            }
            if (map.containsKey('shortName')) {
                ilike('shortName', map.shortName)
            }

            if (map.containsKey('userId')) {
                or {
                    eq('userId', map.userId)
                    isNull('userId')
                }
            } else {
                isNull('userId')
            }
        } as List<QualityProfile>
    }
}
