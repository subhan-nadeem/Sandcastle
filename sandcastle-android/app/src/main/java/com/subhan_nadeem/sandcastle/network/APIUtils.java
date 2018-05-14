package com.subhan_nadeem.sandcastle.network;

/**
 * Created by Subhan Nadeem on 2017-10-10.
 */
public class APIUtils {
    public static final String BASE_URL = "http://ec2-18-191-55-141.us-east-2.compute.amazonaws.com";
    //  private static final String BASE_URL = "http://localhost:3000";

    public static APIService getAPIService() {
        return APIServiceClient.getClient(BASE_URL).create(APIService.class);
    }

}
