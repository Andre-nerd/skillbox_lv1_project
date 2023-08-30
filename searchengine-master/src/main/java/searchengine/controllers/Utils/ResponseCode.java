package searchengine.controllers.Utils;

public class ResponseCode {
    public static final int TOO_MANY_REQUEST = 429;
    public static final int ACCEPTED = 202;
    public static final int EMPTY_SEARCH_TERM_CODE = 403;
    public static final int PAGE_NOT_FOUND_CODE = 404;

    public static final String INDEXING_ALREADY_STARTED ="Индексация уже запущена";
    public static final String  ERROR_WHILE_CRAWLING ="Error while crawling site pages";

    public static final String INDEXING_NOT_LAUNCH = "Индексация не запущена";

    public static final String URL_INVALIDATE  ="URL invalidate | site not included in configuration file";

    public static final String EMPTY_SEARCH_TERM ="An empty search term has been specified";
    public static final String PAGE_NOT_FOUND ="Specified page not found";
}
