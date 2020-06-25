package au.org.ala.dataqualityfilter

class UrlMappings {
	static mappings = {
        "/occurrences/search"(controller: 'occurrence', action: 'list')
        "/"(redirect: "/profiles")
        "/$action?/$id?(.$format)?"(controller:'adminDataQuality')

        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
	}
}
