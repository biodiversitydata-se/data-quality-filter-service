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

import grails.util.Environment
import groovy.xml.MarkupBuilder
import org.springframework.web.servlet.support.RequestContextUtils

/**
 * Custom taglib for biocache-hubs
 *
 */
class OccurrenceTagLib {
    // injected beans
    def messageSourceCacheService

    //static defaultEncodeAs = 'html'
    //static encodeAsForTags = [tagName: 'raw']
    static returnObjectForTags = ['getLoggerReasons','message','createFilterItemLink']
    static namespace = 'alatag'     // namespace for headers and footers

    /**
     * Alternative to g.message(code:'foo.bar')
     *
     * @see org.grails.plugins.web.taglib.ValidationTagLib
     *
     * @attr code REQUIRED
     * @attr default
     */
    def message = { attrs ->
        def code = attrs.code?.toString() // in case a G-sting
        def output = ""

        if (code) {
            String defaultMessage
            if (attrs.containsKey('default')) {
                defaultMessage = attrs['default']?.toString()
            } else {
                defaultMessage = code
            }

            Map messagesMap = messageSourceCacheService.getMessagesMap(RequestContextUtils.getLocale(request)) // g.message too slow so we use a Map instead
            def message = messagesMap.get(code)
            output = message ?: defaultMessage
        }

        return output
    }

    /**
     * Output the meta tags (HTML head section) for the build meta data in application.properties
     * E.g.
     * <meta name="svn.revision" content="${g.meta(name:'svn.revision')}"/>
     * etc.
     *
     * Updated to use properties provided by build-info plugin
     */
    def addApplicationMetaTags = { attrs ->
        // def metaList = ['svn.revision', 'svn.url', 'java.version', 'java.name', 'build.hostname', 'app.version', 'app.build']
        def metaList = ['app.version', 'app.grails.version', 'build.date', 'scm.version', 'environment.BUILD_NUMBER', 'environment.BUILD_ID', 'environment.BUILD_TAG', 'environment.GIT_BRANCH', 'environment.GIT_COMMIT']
        def mb = new MarkupBuilder(out)

        mb.meta(name:'grails.env', content: "${Environment.current}")
        metaList.each {
            mb.meta(name:it, content: g.meta(name: it)?: '' )
        }
        mb.meta(name:'java.version', content: "${System.getProperty('java.version')}")
    }

    /**
     * Remove any text containing the apiKey value from the UI
     *
     * @attr message REQUIRED
     */
    def stripApiKey = { attrs, body ->
        String message = attrs.message
        String output = message.replaceAll(/apiKey=[a-z0-9_\-]*/, "")
        log.warn "input = ${message}"
        log.warn "output = ${output}"
        out << output
    }
}
