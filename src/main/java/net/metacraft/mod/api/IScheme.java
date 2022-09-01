package net.metacraft.mod.api;

public interface IScheme {

    SchemePreResponse processRequest(String url);
    void getResponseHeaders(ISchemeResponseHeaders resp);
    boolean readResponse(ISchemeResponseData data);

}
