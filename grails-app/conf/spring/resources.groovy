import au.org.ala.dataqualityfilter.CustomResourceBundleMessageSource

beans = {
    messageSource(CustomResourceBundleMessageSource) {
        // The standard messageSource will already use "WEB-INF/grails-app/i18n/messages"
        // ExtendedPluginAwareResourceBundleMessageSource uses messageSource as an additional backing message source
        //basename = "${application.config.biocache.baseUrl}/facets/i18n"

        basenames = [
                "WEB-INF/grails-app/i18n/messages",
                "classpath:messages",
                "${application.config.biocache.baseUrl}/facets/i18n"
        ] as String[]

        cacheSeconds = (60 * 60 * 6) // 6 hours
        useCodeAsDefaultMessage = false
    }
}