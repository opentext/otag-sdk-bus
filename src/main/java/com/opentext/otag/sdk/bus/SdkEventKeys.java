package com.opentext.otag.sdk.bus;

/**
 * The SDK API's available methods.
 */
public class SdkEventKeys {

    public static final String AUTH_API = "auth:";
    public static final String AUTH_GET_TOKEN_FOR_USER =  AUTH_API + "getUserForToken";
    public static final String AUTH_LIST_GROUP_USER_IDS =  AUTH_API + "listGroupIdsForUser";
    public static final String AUTH_GET_USER_PROFILE =  AUTH_API + "getUserProfile";
    public static final String AUTH_REGISTER_AUTH_HANDLERS =  AUTH_API + "registerAuthHandlers";

    public static final String CONNECTOR_API = "connectors:";
    public static final String GET_EIM_CONNECTORS = CONNECTOR_API + "getEIMConnectors";
    public static final String REGISTER_EIM_CONNECTOR = CONNECTOR_API + "registerEIMConnector";

    public static final String SERVICE_MGMT_API = "deploymentManagement:";
    public static final String SERVICE_MGMT_REGISTER_LOCAL_APP = SERVICE_MGMT_API + "registerLocalApp";
    public static final String SERVICE_MGMT_COMPLETE_DEPLOYMENT = SERVICE_MGMT_API + "completeDeployment";
    public static final String SERVICE_MGMT_IS_APP_ENABLED = SERVICE_MGMT_API + "isAppEnabled";
    public static final String SERVICE_MGMT_ZIP_DL_PERMITTED = SERVICE_MGMT_API + "isMobileZipDownloadPermitted";

    public static final String MAIL_API = "mail:";
    public static final String SEND_MAIL = MAIL_API + "sendMail";
    public static final String SEND_IMPORTANT_MAIL = MAIL_API + "sendMailImmediately";

    public static final String WEB_NOTIFICATIONS_API = "webNotifications:";
    public static final String NOTIFICATIONS_SEND_WEB =  WEB_NOTIFICATIONS_API + "sendToClientsAndUsers";
    public static final String NOTIFICATIONS_GET_NOTIF_SEQ_BOUNDS =  WEB_NOTIFICATIONS_API + "getNotificationSeqBounds";

    public static final String PUSH_NOTIFICATIONS_API = "pushNotifications:";
    public static final String NOTIFICATIONS_PUSH =  PUSH_NOTIFICATIONS_API + "sendPushNotification";

    public static final String RUNTIMES_API = "runtimes:";
    public static final String GET_RUNTIMES = RUNTIMES_API + "getRuntimes";

    public static final String SETTINGS_API = "settings:";
    public static final String SETTINGS_ADD_SETTING = SETTINGS_API + "addSetting";
    public static final String SETTINGS_UPDATE_SETTING = SETTINGS_API + "updateSetting";
    public static final String SETTINGS_REGISTER_FOR_SETTING_UPDATES = SETTINGS_API + "register";
    public static final String SETTINGS_GET_SETTINGS = SETTINGS_API + "getSettings";
    public static final String SETTINGS_GET_SETTING = SETTINGS_API + "getSetting";
    public static final String SETTINGS_REMOVE_SETTING = SETTINGS_API + "removeSetting";

    public static final String TRUSTED_PROVIDER_API = "trustedProviders:";
    public static final String PROVIDER_LIST_PROVIDERS = TRUSTED_PROVIDER_API + "listProviders";
    public static final String PROVIDER_GET_OR_CREATE = TRUSTED_PROVIDER_API + "getOrCreate";

}
