package au.org.ala.dataqualityfilter

import grails.converters.JSON

class BootStrap {
    def init = { servletContext ->
        QualityProfile.withTransaction {
            def qp = QualityProfile.first()
            if (!qp) {
                qp = new QualityProfile(name: 'Default', shortName: 'default', description: 'This is the default profile, it should be edited', contactName: 'Support Email', contactEmail: '', isDefault: true, enabled: true, displayOrder: 1)
                def qcs = QualityCategory.findAll()
                qcs.each { qp.addToCategories(it) }
                qp.save()
            }
        }

        JSON.createNamedConfig('exportProfile') {
            // Set pretty print
            it.setPrettyPrint(true)
            it.registerObjectMarshaller(QualityProfile) { QualityProfile profile ->
                [
                    name        : profile.name,
                    shortName   : profile.shortName,
                    description : profile.description,
                    contactName : profile.contactName,
                    contactEmail: profile.contactEmail,
                    categories  : profile.categories
                ]
            }

            it.registerObjectMarshaller(QualityCategory) { QualityCategory category ->
                [
                    enabled     : category.enabled,
                    name        : category.name,
                    label       : category.label,
                    description : category.description,
                    displayOrder: category.displayOrder,
                    qualityFilters  : category.qualityFilters
                ]
            }

            it.registerObjectMarshaller(QualityFilter) { QualityFilter filter ->
                [
                    enabled     : filter.enabled,
                    description : filter.description,
                    filter      : filter.filter,
                    displayOrder: filter.displayOrder
                ]
            }
        }

        JSON.registerObjectMarshaller(QualityProfile) {
            def output = [:]
            output['id'] = it.id
            output['name'] = it.name
            output['shortName'] = it.shortName
            output['displayOrder'] = it.displayOrder
            output['description'] = it.description
            output['contactName'] = it.contactName
            output['contactEmail'] = it.contactEmail
            output['categories'] = it.categories
            return output;
        }

        JSON.registerObjectMarshaller(QualityCategory) {
            def output = [:]
            output['id'] = it.id
            output['enabled'] = it.enabled
            output['name'] = it.name
            output['label'] = it.label
            output['description'] = it.description
            output['displayOrder'] = it.displayOrder
            output['qualityFilters'] = it.qualityFilters
            return output;
        }

        JSON.registerObjectMarshaller(QualityFilter) {
            def output = [:]
            output['id'] = it.id
            output['enabled'] = it.enabled
            output['description'] = it.description
            output['filter'] = it.filter
            output['displayOrder'] = it.displayOrder
            return output;
        }
    }
    def destroy = {
    }
}

