package com.example.mymessenger;


/**
 * ALPHA VERSION 1.0
 *
 * This class initializes the login activity which is the starting activity then the user starts the application.
 * Within you need to insert manually the username and password.
 *
 * NOT COMPLETED
 * in development is the user registration field
 */



import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Login extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    Context cont;
    boolean loggedIn = false;
    SSLContext mySSL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        cont = getApplication();
    }

    public void loginToMsg(View view) {

        EditText eUname = findViewById(R.id.username);
        EditText ePwd = findViewById(R.id.password);

        // get content of both edit fields
        String username = eUname.getText().toString();
        String password = ePwd.getText().toString();

        //check that username and pwd is not empty otherwise showalert()
        if(!username.equals("") && !password.equals("")) {
            new DoConnect().execute(username, password);
            for(int i = 0; i<1000; i++) {
                if(loggedIn) {
                    ToastMessage toastmsg = new ToastMessage(cont, "Login successful", 2000);
                    Toast toaster = toastmsg.getToast();
                    toaster.show();
                    break;
                }
            }
        }
        else
            showAlert();
    }

    private void showAlert() {
        Log.d(TAG, "Username or password is empty!");
        ToastMessage toastmsg = new ToastMessage(cont, "Username or password is empty!", 2000);

        Toast toaster = toastmsg.getToast();
        toaster.show();
    }


    // Async process as inner class for ejab connection
    @SuppressLint("StaticFieldLeak")
    private class DoConnect extends AsyncTask<String, Void, Boolean> {

        private String TAG = this.getClass().getSimpleName();

        @Override
        protected Boolean doInBackground(String... strings) {
            String username = strings[0];
            String password = strings[1];
            DomainBareJid serviceName;

            Log.d(TAG, "within sendMessage");

            try {
                serviceName = JidCreate.domainBareFrom("hackstation");
            } catch (XmppStringprepException exc) {
                exc.printStackTrace();
                return false;
            }

            InetAddress service = null;
            try {
                service = InetAddress.getByName("192.168.178.32");
            } catch(UnknownHostException exc) {
                exc.printStackTrace();
            }

            HostnameVerifier verifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(username, password)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                        .setHostAddress(service)
                        .setServiceName(serviceName)
                        .setPort(5222)
                        .setCompressionEnabled(false);


            // create the ssl socket
            try {
                createSSLSocket(getApplicationContext());
            } catch (KeyManagementException exc) {
                Log.e(TAG, "KeyManagementException: " + exc.getMessage());;
                exc.printStackTrace();
            } catch (NoSuchAlgorithmException exc) {
                Log.e(TAG, "NoSuchAlgorithmException: " + exc.getMessage());;
                exc.printStackTrace();
            } catch (IOException exc) {
                Log.e(TAG, "IOException: " + exc.getMessage());;
                exc.printStackTrace();
            }

            configBuilder.setCustomSSLContext(mySSL);
            configBuilder.setHostnameVerifier(verifier);
            XMPPTCPConnectionConfiguration config = configBuilder.build();


            XMPPTCPConnection connect = new XMPPTCPConnection(config);


            try {
                connect.connect();
                Log.d(TAG, connect.getHost());
                connect.login();
                Log.d(TAG, connect.getUser().toString());
                Log.d(TAG, "Successfully connected!");
                return true;
            } catch(InterruptedException exc) {
                exc.printStackTrace();
            } catch(IOException exc) {
                exc.printStackTrace();
            } catch(XMPPException exc) {
                System.out.println("XMPPException: " + exc.getMessage());
                Toast failed = new ToastMessage(getApplicationContext(), "Login failed", 2000)
                        .getToast();
                failed.show();
                exc.printStackTrace();
            } catch(SmackException.ConnectionException exc) {
                System.out.println("SmackException.ConnectionException: " + exc.getFailedAddresses());
                Log.d(TAG, "Following caused the exception:" + exc.getFailedAddresses());
                exc.printStackTrace();
            } catch(SmackException exc) {
                exc.printStackTrace();
            }

            return false;
        }


        // after successful login set var loggedIn to loginRes bool from doInBackground
        protected void onPostExecute(Boolean loginRes) {
            loggedIn = loginRes;
        }


        // assigns all values and init ssl custom socket
        private void createSSLSocket(Context context) throws
                KeyManagementException, NoSuchAlgorithmException, IOException, MalformedURLException {

            // NOT SECURE SOLUTION --> accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // protocol in use for usual TLSv.1.2 otherwise protocol not found exception in log
            mySSL = SSLContext.getInstance("TLSv1.2");

            // init socket
            mySSL.init(null, trustAllCerts, new SecureRandom());
        }

     }
}
