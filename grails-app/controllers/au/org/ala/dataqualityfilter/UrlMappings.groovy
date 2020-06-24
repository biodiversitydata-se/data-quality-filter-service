package au.org.ala.dataqualityfilter

class UrlMappings {
	static mappings = {
        "/"(redirect: "/profiles")
        "/$action?/$id?(.$format)?"(controller:'adminDataQuality')

        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
	}
}
