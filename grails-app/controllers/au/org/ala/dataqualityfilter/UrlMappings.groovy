package au.org.ala.dataqualityfilter

class UrlMappings {
	static mappings = {
        "/"(redirect: "/profiles")

        "/$action?/$id?(.$format)?"(controller:'adminDataQuality')

        "/api(.$format)?"(controller: 'apiDoc', action: 'getDocuments')
        "/api/v1/quality/$action?/$id?(.$format)?"(controller: 'quality', namespace: 'v1')
        "/api/v1/profiles"(resources: 'qualityProfile', includes:['index', 'show']) {
            "/categories"(resources:"qualityCategory", includes:['index', 'show']) {
                "/filters"(resources:"qualityFilter", includes:['index', 'show'])
            }
        }

        "/$controller/$action?/$id?(.$format)?"{ }
	}
}
