package statistics;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.vopen.pal.Constants;

import common.util.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class MobileAgent
{
  private static final MobileAgent agent = new MobileAgent();
  private static final String LOG_TAG = "MobileAgent";
  private static final String PRODUCT_URL = "http://m.analytics.126.net/collector/c";
  private static String bodyName = "com_netease_message_body";
  private static String errorBody = "com_netease_message_error";
  private final Handler hander;
  private static String appid;
  private static String version;
  private static String mid;
  private static boolean isGetInfo = true;
  private Context mContext = null;

  private MobileAgent() { HandlerThread localHandlerThread = new HandlerThread("MobileAgent");
    localHandlerThread.start();
    this.hander = new Handler(localHandlerThread.getLooper()); }

  public static void sessionStart(Context context)
  {
    if (isGetInfo)
    {
      if (!getInfo(context))
      {
        Log.e("MobileAgent", "can't get info...");
      }
    }
    agent.onStartSessionInternal(context);
  }

  public static void sessionEnd(Context context)
  {
    try
    {
      if (context == null) {
        Log.e("MobileAgent", "unexpected null context");
        return;
      }

      agent.onEndSessionInternal(context);
    } catch (Exception e) {
      Log.e("MobileAgent", "Exception occurred in Mobclick.onRause(). ");
    }
  }

  public static void getErrorOnCreate(Context context)
  {
    try
    {
      if (context == null) {
        Log.e("MobileAgent", "unexpected null context");
        return;
      }
      if (getInfo(context))
      {
        isGetInfo = false;
      }
      agent.onErrorInternal(context);
    } catch (Exception e) {
      Log.e("MobileAgent", "Exception occurred in Mobclick.onError()");
    }
  }

  public static void getErrorOnRunning(Context context, String errorMsg)
  {
    try
    {
      if (context == null) {
        Log.e("MobileAgent", "unexpected null context");
        return;
      }
      if ((errorMsg == null) || (errorMsg.length() == 0))
      {
        Log.e("MobileAgent", "unexpected null errorMsg");
        return;
      }
      agent.onErrorInternal(context, errorMsg);
    } catch (Exception e) {
      Log.e("MobileAgent", "Exception occurred in Mobclick.onError()");
    }
  }

  private synchronized void onStartSessionInternal(Context context)
  {
    SharedPreferences state_preferences = getSessionHeader(context);
    if (state_preferences == null) {
      return;
    }
    String session_id = startSession(context, state_preferences);
    Log.i("MobileAgent", "Start new session: " + session_id);
  }

  public synchronized void onEndSessionInternal(Context context)
  {
    if (!setSessionEnd(context))
    {
      Log.e("MobileAgent", "setSessionEnd is false.");
      return;
    }
    SharedPreferences state_preferences = getSessionHeader(context);
    if (state_preferences == null) {
      return;
    }
    endSession(context, state_preferences);
  }

  private synchronized void onErrorInternal(Context context)
  {
    this.mContext = context;
    new Thread() {
      public void run() {
        String error = MobileAgent.CatchLogError(MobileAgent.this.mContext);
        if ((error == "") || (error.length() > 10240)) {
          return;
        }
        MobileAgent.this.emitErrorReport(MobileAgent.this.mContext, error);
      }
    }
    .start();
  }

  private synchronized void onErrorInternal(Context context, String error)
  {
    if (error.length() > 10240) {
      Log.e("MobileAgent", "errorMsg too long.");
      return;
    }
    emitErrorReport(context, error);
  }

  private String startSession(Context context, SharedPreferences state_preferences)
  {
    JSONArray message = getCachedMessageBody(context, bodyName);
    if (message == null)
    {
      message = new JSONArray();
    }
    JSONObject jb = new JSONObject();
    String session_id = state_preferences.getString("session_id", "");
    try {
      jb.put("s", session_id);
      long start_millis = state_preferences.getLong("session_start", 0L);
      String u = state_preferences.getString("u", "");
      String m = state_preferences.getString("m", "");
      String o = state_preferences.getString("o", "");
      String id = state_preferences.getString("id", "");
      String v = state_preferences.getString("v", "");
      String mid = state_preferences.getString("mid", "");
      JSONObject info = new JSONObject();
      info.put("u", u);
      info.put("m", m);
      info.put("o", o);
      info.put("id", id);
      info.put("v", v);
      info.put("mid", mid);
      jb.put("i", info);
      JSONArray event = new JSONArray();
      JSONObject e = new JSONObject();
      e.put("n", "^");
      e.put("t", start_millis);
      event.put(e);
      jb.put("e", event);
      message.put(jb);
    }
    catch (JSONException e) {
      Log.e("MobileAgent", "JSONException", e);
    }

    emitLastEndSessionReport(context, message, bodyName);
    return session_id;
  }

  private void endSession(Context context, SharedPreferences state_preferences)
  {
    String session_id = state_preferences.getString("session_id", null);
    long session_end = state_preferences.getLong("session_end", 0L);
    if (session_id == null)
    {
      Log.e("MobileAgent", "session_id==null");
      return;
    }
    JSONArray message = getCachedMessageBody(context, bodyName);
    if (message == null)
    {
      message = new JSONArray();
      JSONObject jb = new JSONObject();
      JSONArray event = new JSONArray();
      JSONObject e = new JSONObject();
      try {
        jb.put("s", session_id);
        e.put("n", "$");
        e.put("t", session_end);
        event.put(e);
        jb.put("e", event);

        message.put(jb);
      } catch (JSONException ex) {
        Log.e("MobileAgent", "JSONException", ex);
      }

    }
    else
    {
      for (int i = 0; i < message.length(); i++) {
        try
        {
          JSONObject jb = message.getJSONObject(i);
          if ((!jb.has("s")) || (!jb.getString("s").equals(session_id)))
            continue;
          if (jb.has("e"))
          {
            JSONArray event = jb.getJSONArray("e");
            JSONObject e = new JSONObject();
            e.put("n", "$");
            e.put("t", session_end);
            event.put(e);
            jb.put("e", event);
            message.put(i, jb);
          }
          else
          {
            JSONArray event = new JSONArray();
            JSONObject e = new JSONObject();
            e.put("n", "$");
            e.put("t", session_end);
            event.put(e);
            jb.put("e", event);
            message.put(i, jb);
          }
        }
        catch (JSONException e) {
          Log.e("MobileAgent", "JSONException", e);
        }
      }
    }
    deleteCacheFile(context, bodyName);
    cacheJSONArrayBody(context, message, bodyName);
  }

  private void emitErrorReport(Context context, String error)
  {
    JSONArray message = getCachedMessageBody(context, errorBody);

    SharedPreferences state_preferences = getSessionHeader(context);
    if (state_preferences == null) {
      return;
    }
    long start_millis = state_preferences.getLong("session_start", 0L);
    if (start_millis == 0L)
    {
      start_millis = System.currentTimeMillis();
    }
    String session_id = state_preferences.getString("session_id", "");
    if (message == null)
    {
      message = new JSONArray();
    }
    else if (message.toString().length() > 9999)
    {
      message = new JSONArray();
      deleteCacheFile(context, errorBody);
    }
    JSONObject jb = new JSONObject();
    JSONObject info = new JSONObject();
    try {
      jb.put("s", session_id);
      info.put("u", state_preferences.getString("u", ""));
      info.put("m", state_preferences.getString("m", ""));
      info.put("o", state_preferences.getString("o", ""));
      info.put("id", state_preferences.getString("id", ""));
      info.put("v", state_preferences.getString("v", ""));
      info.put("mid", state_preferences.getString("mid", ""));
      jb.put("i", info);
      JSONObject errorMsg = new JSONObject();
      errorMsg.put("t", start_millis);
      errorMsg.put("c", error);
      JSONArray fault = new JSONArray();
      fault.put(errorMsg);
      jb.put("f", fault);
      message.put(jb);
    } catch (JSONException e) {
      Log.e("MobileAgent", "JSONException", e);
    }
    emitLastEndSessionReport(context, message, errorBody);
  }

  private void emitLastEndSessionReport(Context context, JSONArray message, String fileName)
  {
    this.hander.post(new ReportMessageHandler(this, context, message, fileName));
  }

  private void tryToSendMessage(Context context, JSONArray message, String fileName)
  {
    if ((context == null) || (message == null))
    {
      Log.e("MobileAgent", "tryToSendMessage:context==null||message==null");
    }
    if ((isNetworkAvailable(context)) && (sendMessage(message)))
    {
      deleteCacheFile(context, fileName);
    }
    else
    {
      deleteCacheFile(context, fileName);
      cacheJSONArrayBody(context, message, fileName);
    }
  }

  private static boolean sendMessage(JSONArray message)
  {
    HttpPost httppost = new HttpPost("http://m.analytics.126.net/collector/c");
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(params, 3000);
    HttpConnectionParams.setSoTimeout(params, 5000);
    HttpClient httpclient = new DefaultHttpClient(params);
    try {
      StringEntity se = new StringEntity(message.toString(), "UTF-8");
      httppost.setEntity(se);
      HttpResponse response = httpclient.execute(httppost);
      if (response.getStatusLine().getStatusCode() == 200) {
        Log.i("MobileAgent", "Sent message to http://m.analytics.126.net/collector/c");
        return true;
      }
      Log.i("MobileAgent", "Failed to send message.");
      return false;
    } catch (ClientProtocolException e) {
      Log.e("MobileAgent", "ClientProtocolException,Failed to send message.");
      return false;
    } catch (IOException e) {
      Log.e("MobileAgent", message.toString());
      Log.e("MobileAgent", "IOException,Failed to send message.");
    }
    return false;
  }

  public static void setEvent(Context context, String name)
  {
    setEvent(context, name, 1);
  }

  public static void setEvent(Context context, String name, int acc)
  {
    setEvent(context, name, name, acc);
  }

  public static void setEvent(Context context, String name, String tag)
  {
    setEvent(context, name, tag, 1);
  }

  public static void setEvent(Context context, String name, String tag, int num)
  {
    if ((context == null) || (name == null) || (num <= 0))
    {
      Log.e("MobileAgent", "something wrong :context==null||name==null||num<=0");
      return;
    }
    SharedPreferences state_preferences = getSessionHeader(context);
    String sessionid = state_preferences.getString("session_id", null);
    if (sessionid == null)
    {
      Log.e("MobileAgent", "please use sessionStart first.");
      return;
    }

    JSONArray message = getCachedMessageBody(context, bodyName);
    if (message == null)
    {
      message = new JSONArray();
      JSONObject jb = new JSONObject();
      try {
        jb.put("s", sessionid);
        JSONArray event = new JSONArray();
        JSONObject tempJB = new JSONObject();
        tempJB.put("n", name);
        tempJB.put("g", tag);
        tempJB.put("t", num);
        event.put(tempJB);

        jb.put("e", event);
        message.put(jb);
        cacheJSONArrayBody(context, message, bodyName);
      }
      catch (JSONException e) {
        Log.e("MobileAgent", "JSONException", e);
      }

    }
    else
    {
      for (int i = 0; i < message.length(); i++)
      {
        JSONObject jb = null;
        try {
          jb = message.getJSONObject(i);
          if ((!jb.has("s")) || (!jb.getString("s").equals(sessionid)))
            continue;
          if (jb.has("e"))
          {
            JSONArray event = jb.getJSONArray("e");
            JSONObject tempJB = new JSONObject();
            tempJB.put("n", name);
            tempJB.put("g", tag);
            tempJB.put("t", num);
            event.put(tempJB);
            jb.put("e", event);
            message.put(i, jb);
          }
          else
          {
            JSONArray event = new JSONArray();
            JSONObject tempJB = new JSONObject();
            tempJB.put("n", name);
            tempJB.put("g", tag);
            tempJB.put("t", num);
            event.put(tempJB);
            jb.put("e", event);
            message.put(i, jb);
          }
        }
        catch (JSONException e)
        {
          Log.e("MobileAgent", "JSONException", e);
          return;
        }
      }

      deleteCacheFile(context, bodyName);
      cacheJSONArrayBody(context, message, bodyName);
    }
  }

  private static String CatchLogError(Context context)
  {
    String error = "";
    try {
      String packageName = context.getPackageName();
      String log = "";
      boolean ifOurs = false;
      boolean ifException = false;
      ArrayList commandLine = new ArrayList();
      commandLine.add("logcat");
      commandLine.add("-d");
      commandLine.add("-v");
      commandLine.add("raw");
      commandLine.add("-s");
      commandLine.add("AndroidRuntime:E");
      commandLine.add("-p");
      commandLine.add(packageName);

      Process process = Runtime.getRuntime().exec((String[])commandLine.toArray(new String[commandLine.size()]));
      BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(process.getInputStream()), 1024);
      String line = bufferedReader.readLine();

      while (line != null) {
        if (line.indexOf("thread attach failed") < 0)
          log = log + line + '\n';
        if ((!ifException) && (line.toLowerCase().indexOf("exception") >= 0))
          ifException = true;
        if ((!ifOurs) && (line.indexOf(packageName) >= 0))
          ifOurs = true;
        line = bufferedReader.readLine();
      }

      if ((log.length() > 0) && (ifException) && (ifOurs)) {
        error = log;
      }
      try
      {
        Runtime.getRuntime().exec("logcat -c");
      } catch (Exception e) {
        Log.e("MobileAgent", "Failed to clear log");
      }
    } catch (Exception e) {
      Log.e("MobileAgent", "Failed to catch error log");
    }
    return error;
  }

  private static SharedPreferences getSessionHeader(Context context)
  {
    return context.getSharedPreferences("com_netease_session_header", 0);
  }

  private static boolean setSessionEnd(Context context)
  {
    SharedPreferences header_preferences = getSessionHeader(context);
    if (header_preferences == null)
    {
      Log.e("MobileAgent", "header_preferences == null..");
      return false;
    }
    SharedPreferences.Editor editor = header_preferences.edit();
    long end_millis = System.currentTimeMillis();
    editor.putLong("session_end", end_millis);
    editor.commit();
    return true;
  }

  private static boolean getInfo(Context context)
  {
    PackageManager manager = context.getPackageManager();
    try {
      ApplicationInfo info = manager.getApplicationInfo(context.getPackageName(), 128);
      if (info != null)
      {
        appid = info.metaData.get("APPKEY").toString();
        version = Util.getNumberVersion(context);
        mid = Constants.getAppChannelID();
        Log.v("MobileAgent getInfo()", "appid is" + appid  + " version is " + version + " mid is " + mid);
        if (mid == null)
        {
          mid = "netease";
        }
      }
      else
      {
        Log.e("MobileAgent", "info is null.");
        return false;
      }
    }
    catch (PackageManager.NameNotFoundException e) {
      Log.e("MobileAgent", "NameNotFoundException", e);
    }

      TelephonyManager tm = (TelephonyManager)context.getSystemService("phone");
      if (tm == null) {
        Log.w("MobileAgent", "No IMEI.");
        return false;
      }
      String imei = tm.getDeviceId();
      if ((imei == null) || (TextUtils.isEmpty(imei))) {
        Log.w("MobileAgent", "No IMEI..");
        WifiManager wifi = (WifiManager)context.getSystemService("wifi");
        WifiInfo info = wifi.getConnectionInfo();
        imei = info.getMacAddress();
        if ((imei == null) || (TextUtils.isEmpty(imei)))
        {
          Log.w("MobileAgent", "No WIFI MAC..");
          return false;
        }
      }
      SharedPreferences header_preferences = getSessionHeader(context);
      if (header_preferences == null)
      {
        Log.e("MobileAgent", "header_preferences == null.");
        return false;
      }
      SharedPreferences.Editor editor = header_preferences.edit();
      long start_millis = System.currentTimeMillis();
      String session_id = appid + String.valueOf(start_millis);
      editor.putString("session_id", session_id);
      editor.putLong("session_start", start_millis);
      editor.putString("u", imei);
      editor.putString("m", Build.MODEL);
      editor.putString("o", Build.VERSION.RELEASE);
      editor.putString("id", appid);
      editor.putString("v", version);
      editor.putString("mid", mid);
      editor.commit();
    return true;
  }

  private static boolean isNetworkAvailable(Context context)
  {
    ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService("connectivity");
    if (connectivity == null) {
      return false;
    }
    if (connectivity.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED) {
      return true;
    }
    NetworkInfo wifi_network = connectivity.getNetworkInfo(1);
    return wifi_network.getState() == NetworkInfo.State.CONNECTED;
  }

  private static void cacheJSONArrayBody(Context context, JSONArray body, String fileName)
  {
    try
    {
      FileOutputStream output = context.openFileOutput(fileName, 0);
      output.write(body.toString().getBytes());
      output.close();
    } catch (FileNotFoundException e) {
      return;
    } catch (IOException e) {
      return;
    }
  }

  private static JSONArray getCachedMessageBody(Context context, String fileName)
  {
    try
    {
      FileInputStream input = context.openFileInput(fileName);

      String json_text = "";
      byte[] buffer = new byte[16384];
      int read_bytes = 0;
      while ((read_bytes = input.read(buffer)) != -1) {
        json_text = json_text + new String(buffer, 0, read_bytes);
      }

      if (json_text.length() == 0)
      {
        return null;
      }
      try {
        JSONArray localJSONArray = new JSONArray(json_text);
        return localJSONArray;
      } catch (JSONException e) {
        Log.i("MobileAgent", "Fail to construct json message.");
        return null;
      } finally {
        input.close();
      }
    } catch (FileNotFoundException e) {
      Log.i("MobileAgent", "getCachedMessageBody FileNotFoundException");
      return null;
    } catch (IOException e) {
      Log.i("MobileAgent", "getCachedMessageBody IOException");
    }
    return null;
  }

  private static void deleteCacheFile(Context context, String fileName)
  {
    context.deleteFile(fileName);
  }

  private static final class ReportMessageHandler
    implements Runnable
  {
    private static final Object mutex = new Object();
    private MobileAgent _agent;
    private Context _context;
    private JSONArray _message;
    private String _fileName;

    ReportMessageHandler(MobileAgent _agent, Context context, JSONArray message, String fileName)
    {
      this._agent = MobileAgent.agent;
      this._context = context;
      this._message = message;
      this._fileName = fileName;
    }

    public void run() {
      try {
        synchronized (mutex) {
          this._agent.tryToSendMessage(this._context, this._message, this._fileName);
        }
      } catch (Exception e) {
        Log.e("MobileAgent", "Exception occurred when sending message.");
      }
    }
  }
}