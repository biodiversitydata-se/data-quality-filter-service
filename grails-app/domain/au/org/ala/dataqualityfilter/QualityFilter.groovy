package au.org.ala.dataqualityfilter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes = ['enabled', 'description', 'filter'])
@ToString(includes = ['enabled', 'description', 'filter'])
class QualityFilter {

    Long id

    boolean enabled = true

    String description
    String filter

    Date dateCreated
    Date lastUpdated

    static belongsTo = [ qualityCategory: QualityCategory ]

    static constraints = {
        filter blank: false, unique: ['qualityCategory']
    }

    static mapping = {
        enabled defaultValue: 'true', index: 'quality_filter_enabled_idx'
        dateCreated index: 'quality_filter_date_created_idx'
        sort 'dateCreated'
    }

}
