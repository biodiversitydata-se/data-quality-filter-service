package au.org.ala.dataqualityfilter

import grails.converters.JSON
import grails.plugin.cache.Cacheable
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.springframework.web.client.RestClientException
import org.supercsv.cellprocessor.ift.CellProcessor
import org.supercsv.io.CsvListReader
import org.supercsv.io.ICsvListReader
import org.supercsv.prefs.CsvPreference

/**
 * Service to perform web service DAO operations
 */
class WebServicesService {

    def grailsApplication

    def JSONObject getRecord(String id, Boolean hasClubView) {
        def url = "${grailsApplication.config.biocache.baseUrl}/occurrence/${id.encodeAsURL()}"
        if (hasClubView) {
            url += "?apiKey=${grailsApplication.config.biocache.apiKey?:''}"
        }
        getJsonElements(url)
    }

    /**
     * Get the CSV for ALA data quality checks meta data
     *
     * @return
     */
    @Cacheable('longTermCache')
    def String getDataQualityCsv() {
        String url = grailsApplication.config.dataQualityChecksUrl ?: "https://docs.google.com/spreadsheet/pub?key=0AjNtzhUIIHeNdHJOYk1SYWE4dU1BMWZmb2hiTjlYQlE&single=true&gid=0&output=csv"
        getText(url)
    }

    @Cacheable('longTermCache')
    def JSONArray getLoggerReasons() {
        def url = "${grailsApplication.config.logger.baseUrl}/logger/reasons"
        def jsonObj = getJsonElements(url)
        jsonObj.findAll { !it.deprecated } // skip deprecated reason codes
    }

    @Cacheable('longTermCache')
    def JSONArray getLoggerSources() {
        def url = "${grailsApplication.config.logger.baseUrl}/logger/sources"
        try {
            getJsonElements(url)
        } catch (Exception ex) {
            log.error "Error calling logger service: ${ex.message}", ex
        }
    }

    @Cacheable('longTermCache')
    def JSONArray getAssertionCodes() {
        def url = "${grailsApplication.config.biocache.baseUrl}/assertions/codes"
        return getJsonElements(url)
    }

    /**
     * Perform HTTP GET on a JSON web service
     *
     * @param url
     * @return
     */
    JSONElement getJsonElements(String url) {
        log.debug "(internal) getJson URL = " + url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            return JSON.parse(conn.getInputStream(), "UTF-8")
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error error
            throw new RestClientException(error, e)
        }
    }

    /**
     * Perform HTTP GET on a text-based web service
     *
     * @param url
     * @return
     */
    String getText(String url) {
        log.debug "(internal text) getText URL = " + url
        def conn = new URL(url).openConnection()

        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            def text = conn.content.text
            return text
        } catch (Exception e) {
            def error = "Failed to get text from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error error
            //return null
            throw new RestClientException(error, e) // exception will result in no caching as opposed to returning null
        }
    }

    def getAllOccurrenceFields() {
        def url = "${grailsApplication.config.biocache.baseUrl}/index/fields"
        return getJsonElements(url)?.collect {it.name}
    }

    def getMessagesPropertiesFile() {
        def url = "${grailsApplication.config.biocache.baseUrl}/facets/i18n"

        def map = [:]
        def lineContent
        // split text to get lines
        def lines = getText(url).split("\\r?\\n")
        lines?.each {
            // if not comment
            if (!it.startsWith('#')) {
                lineContent = it.split('=')
                if (lineContent.length == 2) {
                    map[lineContent[0]] = lineContent[1]
                }
            }
        }
        return map
    }

    def getAssertionCodeMap() {
        JSONArray codes = getAssertionCodes() // code <-> name
        Map dataQualityCodes = getAllCodes()  // code -> detail

        // convert to name -> detail
        return codes.findAll{dataQualityCodes.containsKey(String.valueOf(it.code))}.collectEntries{[(it.name) : dataQualityCodes.get(String.valueOf(it.code))]}
    }

    def getAllCodes() {
        Map dataQualityCodes = [:]
        String dataQualityCsv = getDataQualityCsv() // cached
        ICsvListReader listReader = null

        try {
            listReader = new CsvListReader(new StringReader(dataQualityCsv), CsvPreference.STANDARD_PREFERENCE)
            listReader.getHeader(true) // skip the header (can't be used with CsvListReader)
            final CellProcessor[] processors = getProcessors()

            List<Object> dataQualityList
            while ((dataQualityList = listReader.read(processors)) != null) {
                //log.debug("row: " + StringUtils.join(dataQualityList, "|"));
                Map<String, String> dataQualityEls = new HashMap<String, String>();
                if (dataQualityList.get(1) != null) {
                    dataQualityEls.put("name", (String) dataQualityList.get(1));
                }
                if (dataQualityList.get(3) != null) {
                    dataQualityEls.put("description", (String) dataQualityList.get(3));
                }
                if (dataQualityList.get(4) != null) {
                    dataQualityEls.put("wiki", (String) dataQualityList.get(4));
                }
                if (dataQualityList.get(0) != null) {
                    dataQualityCodes.put((String) dataQualityList.get(0), dataQualityEls);
                }
            }
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
        dataQualityCodes
    }

    /**
     * CellProcessor method as required by SuperCSV
     *
     * @return
     */
    private static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = [
                null, // col 1
                null, // col 2
                null, // col 3
                null, // col 4
                null, // col 5
                null, // col 6
                null, // col 7
                null, // col 8
                null, // col 9
                null, // col 10
                null, // col 11
                null, // col 12
                null, // col 13
                null, // col 14
                null, // col 15
        ]

        return processors
    }
}
