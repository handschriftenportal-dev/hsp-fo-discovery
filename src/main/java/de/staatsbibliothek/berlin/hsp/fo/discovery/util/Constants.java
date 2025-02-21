package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

public class Constants {

  private Constants() {
  }

  public static final String API_PARAM_DESCRIPTION_EXTENDED = "Furthermore, by using the flag <i>isExtended</i>, a query in <a href=\"https://github.com/jirutka/rsql-parser\">RSQL syntax</a> can be used, ";
  public static final String API_PARAM_DESCRIPTION_QUERY = "A search term or phrase. If quotation marks are used (surrounding the whole phrase or a single word)," +
      "an exact search is performed and wildcards can be used within by adding <i>?</i> (representing exact one character) or <i>*</i> (representing zero or more sequential characters). ";
  public static final String API_PARAM_DESCRIPTION_QUERY_WITH_EXTENDED = API_PARAM_DESCRIPTION_QUERY + API_PARAM_DESCRIPTION_EXTENDED;

  public static final String API_PARAM_DESCRIPTION_QUERY_FIELDS = "Comma separated list of fields, on which the search should be performed on";
  public static final String API_PARAM_DESCRIPTION_FACET_FIELDS = "Comma separated list of fields, on which the result should be faceted on";
  public static final String API_PARAM_DESCRIPTION_FILTER_QUERY = "A filter query as JSON object. The JSON object key correspond with facet field names.\n" +
      " Entry values may have two forms: <br>\n" +
      " - For multivalued string filters, JSON values are string arrays, e.g. {" +
      " \"settlement-facet\": [\"Berlin\", \"Leipzig\"] }<br>\n" +
      " - For range filters, JSON values are object containing minimum and maximum value as well\n" +
      " as a flag for including documents without this filter value, e.g. { \"width-facet\": {" + " \"from\": 10, \"to\": 20, \"missing\": false }} <br>\n\n" +
      " For origin dates, the filter query is a special case of the range filter: <br>\n" +
      " - name / key is \"orig-date-facet\", filter queries include the two fields\n"
      + " orig-date-from-facet and orig-date-to-facet<br>\n"
      + " - filter has an addition boolean field \"exact\" that indicates that both\n"
      + " \"orig-date-from-facet\" and \"orig-date-to-facet\" of a document have values between \"from\"\n"
      + " and \"to\"";
  public static final String API_PARAM_DESCRIPTION_HIGHLIGHT = "Indicates whether the term should be highlighted in the response documents.";
  public static final String API_PARAM_DESCRIPTION_START = "Start index for the response documents.";
  public static final String API_PARAM_DESCRIPTION_ROWS = "Maximum number of response documents.";
  public static final String API_PARAM_EXAMPLE_QUERY = "\"\\\"Herzo? August Biblioth*\\\"\"";
  public static final String API_PARAM_EXAMPLE_FILTER_QUERY = "{ \"settlement-facet\": [\"Wolfenbüttel\", \"Berlin\"], \"repository-facet\": [\"Herzog August Bibliothek Wolfenbüttel\"] }";
}
