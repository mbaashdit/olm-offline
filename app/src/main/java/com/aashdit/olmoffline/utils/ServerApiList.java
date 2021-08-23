package com.aashdit.olmoffline.utils;

public class ServerApiList {

    public static final String BASE_URL = "http://209.97.136.18:8080/DAS_Jajpur/";
//    public static final String BASE_URL = "http://192.168.1.102:3030/DAS_Jajpur/";
    //"http://distdms-jajpur.com/";
    //"http://209.97.136.18:8080/DAS_Jajpur/";//209.97.136.18:8080  //192.168.43.215:3030 - sujit

    //login
    public static final String LOGIN_URL = BASE_URL + "api/userLogin";
    public static final String CHANGEPASSWORD_URL = BASE_URL + "api/changePassword";
    public static final String GETPROJECTINITIATIONTENDERLIST = BASE_URL + "api/getTendersForInspection";
    public static final String GETPROJECTCLOSURETENDERLIST = BASE_URL + "api/getTendersForClosure";
    public static final String GETPROJECTINTIATIONPHOTOUPLOAD = BASE_URL + "api/tenderInitialInspectionUpload";
    public static final String GETPROJECTCLOSUREPHOTOUPLOAD = BASE_URL + "api/tenderClosureInspectionUpload";
    public static final String GETPROJECTTENDERINTIALUPLOADEDTENDER = BASE_URL + "api/getInitialUploadedTenders";
    public static final String GETPROJECTCLOSUREUPLOADEDTENDER = BASE_URL + "api/getClosureUploadedTenders";
    public static final String SHOWFILE=BASE_URL+"api/showFile";
}
