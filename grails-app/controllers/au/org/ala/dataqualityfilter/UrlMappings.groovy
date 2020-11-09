package au.org.ala.dataqualityfilter

class UrlMappings {
	static mappings = {
        "/"(redirect: "/data-profiles")

        "/$action?/$id?(.$format)?"(controller:'adminDataQuality')

        "/api(.$format)?"(controller: 'apiDoc', action: 'getDocuments')
        "/api/v1/quality/$action?/$id?(.$format)?"(controller: 'quality', namespace: 'v1')
        "/api/v1/profiles"(redirect: [uri: '/api/v1/data-profiles'])
        "/api/v1/data-profiles"(resources: 'qualityProfile', includes:['index', 'show']) {
            "/categories"(resources:"qualityCategory", includes:['index', 'show']) {
                "/filters"(resources:"qualityFilter", includes:['index', 'show'])
            }
        }

        "/$controller/$action?/$id?(.$format)?"{ }
	}
}
