package com.incipientinfo.loginwithlinkedin;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Linkedin {

    private static Context mcontext;
    private static String mapiKey;
    private static String msecretKey;
    private static String mredirectUrl;
    private static Dialog dialog;

    private static String PROFILE_URL = "https://api.linkedin.com/v2/people/~";
    private static String proUrl="https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))";
    private static String emailUrl="https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";
    private static String OAUTH_ACCESS_TOKEN_PARAM = "oauth2_access_token";

    private static String AUTHORIZATION_URL = "https://www.linkedin.com/uas/oauth2/authorization";
    private static String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken";
    private static String SECRET_KEY_PARAM = "client_secret";
    private static String RESPONSE_TYPE_PARAM = "response_type";
    private static String GRANT_TYPE_PARAM = "grant_type";
    private static String GRANT_TYPE = "authorization_code";
    private static String RESPONSE_TYPE_VALUE = "code";
    private static String CLIENT_ID_PARAM = "client_id";
    private static String STATE_PARAM = "state";
    private static String REDIRECT_URI_PARAM = "redirect_uri";
    private static String QUESTION_MARK = "?";
    private static String AMPERSAND = "&";
    private static String EQUALS = "=";
    private static String STATE = "E3ZYKC1T6H2yP4z";
    private static String accessToken="";

    public static onLinkedinResponce listner;
    private static JSONObject responce;

    public interface onLinkedinResponce {
        public void onSuccess(JSONObject responce);
    }

    public static void init(Context context, String apiKey, String secretKey, String redirectUrl){
        mcontext=context;
        mapiKey=apiKey;
        msecretKey=secretKey;
        mredirectUrl=redirectUrl;
        responce=new JSONObject();
        listner=(onLinkedinResponce)context;
    }

    public static void login(){

        dialog=new Dialog(mcontext);
        dialog.setContentView(R.layout.webview_dialog);
        final WebView webview=dialog.findViewById(R.id.webview);
        final ProgressBar progressBar=dialog.findViewById(R.id.progressBar);

        webview.requestFocus(View.FOCUS_DOWN);

        progressBar.setVisibility(View.VISIBLE);
        webview.setWebViewClient(new WebViewClient(){

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {

                progressBar.setVisibility(View.GONE);
                if (authorizationUrl.startsWith(mredirectUrl)) {
                    Log.i("Authorize", "");
                    Uri uri = Uri.parse(authorizationUrl);

                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(STATE)) {
                        Log.e("Authorize", "State token doesn't match");
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        return true;
                    }

                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        Log.i("Authorize", "The user doesn't allow authorization.");
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        return true;
                    }
                    Log.i("Authorize", "Auth token received: "+authorizationToken);

                    String accessTokenUrl = getAccessTokenUrl(authorizationToken);

                    postRequest(accessTokenUrl);

                } else {
                    Log.i("Authorize", "Redirecting to: "+authorizationUrl);
                    webview.loadUrl(authorizationUrl);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        String authUrl = getAuthorizationUrl();
        Log.i("Authorize", "Loading Auth Url: "+authUrl);
        //Load the authorization URL into the webView
        webview.loadUrl(authUrl);
        //dialog=alertDialog.show()
        dialog.show();

    }

    private static void postRequest(String url) {

        StringRequest  req=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("resp", response);
                try {
                    JSONObject jobject = new JSONObject(response);
                    if (jobject.has("access_token")) {
                        accessToken = jobject.getString("access_token");
                        //String profileUrl = getProfileUrl(accessToken);
                        getUserData(proUrl);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("resp"," "+error);
            }
        });

        Volley.newRequestQueue(mcontext).add(req);

    }

    private static void getUserData(String url){
        Log.d("resp"," "+url);//object:
        StringRequest  req= new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jobject = new JSONObject(response);
                    String firstName = jobject.getJSONObject("firstName").getJSONObject("localized").getString("en_US");
                    String lastName = jobject.getJSONObject("lastName").getJSONObject("localized").getString("en_US");


                    String image = jobject.getJSONObject("profilePicture").getJSONObject("displayImage~")
                            .getJSONArray("elements").getJSONObject(0).getJSONArray("identifiers")
                            .getJSONObject(0).getString("identifier");
                    makeResponce("firstName",firstName);
                    makeResponce("lastName",lastName);
                    makeResponce("profileImage",image);

                    getEmail();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("resp"," "+error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers =new  HashMap<String, String>();
                headers.put("Authorization","Bearer "+accessToken);

                return headers;
            }
        };

        Volley.newRequestQueue(mcontext).add(req);
    }

    private static void getEmail(){
        StringRequest req=new StringRequest(Request.Method.GET, emailUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jobject = new JSONObject(response);

                    JSONObject elements = jobject.getJSONArray("elements").getJSONObject(0);
                    if (elements.has("handle~")) {
                        String email = elements.getJSONObject("handle~").getString("emailAddress");
                        makeResponce("userEmail",email);
                        listner.onSuccess(responce);
                    }


                    if (dialog != null) {
                        dialog.dismiss();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("resp"," "+error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers =new  HashMap<String, String>();
                headers.put("Authorization","Bearer "+accessToken);

                return headers;
            }
        };

        Volley.newRequestQueue(mcontext).add(req);
    }

    private static void makeResponce(String key, String value) throws JSONException {
        responce.put(key,value);
    }

    private static String  getProfileUrl(String accessToken) {
        return (PROFILE_URL
                + QUESTION_MARK
                + OAUTH_ACCESS_TOKEN_PARAM + EQUALS + accessToken+AMPERSAND+"format=json");
    }

    private static String getAccessTokenUrl(String authorizationToken) {
        return (ACCESS_TOKEN_URL
                + QUESTION_MARK
                + GRANT_TYPE_PARAM + EQUALS + GRANT_TYPE
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken
                + AMPERSAND
                + CLIENT_ID_PARAM + EQUALS + mapiKey
                + AMPERSAND
                + REDIRECT_URI_PARAM + EQUALS + mredirectUrl
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + msecretKey);
    }

    private static String getAuthorizationUrl() {
        return (AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + mapiKey
                + AMPERSAND + STATE_PARAM + EQUALS + STATE
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + mredirectUrl
                + AMPERSAND+"scope=r_liteprofile,r_emailaddress");
    }

}
