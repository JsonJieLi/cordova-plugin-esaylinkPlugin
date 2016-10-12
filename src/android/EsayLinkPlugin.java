package hhmao.plugin.esaylink;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.payeco.android.plugin.PayecoPluginLoadingActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * This class echoes a string called from JavaScript.
 */
@SuppressWarnings("deprecation")
public class EsayLinkPlugin extends CordovaPlugin{

	//商户服务器下单地址，此地址为商户平台测试环境下单地址，商户接入需改为商户自己的服务器下单地址
	private String payUrl = "";
	//通知商户地址，建议在接收到支付成功结果时，通知商户服务器
	private String notifyUrl = "";
	// 00: 测试环境, 01: 生产环境
	private String environment = "";
	private CallbackContext callBackContext;
	//广播地址，用于接收易联支付插件支付完成之后回调客户端
	private final static String BROADCAST_PAY_END="com.merchant.demo.broadcast";
	// ~ Fields
	// ================================================================================
	/**
	 * @Fields payecoPayBroadcastReceiver : 易联支付插件广播
	 */
	private BroadcastReceiver payecoPayBroadcastReceiver;
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callBackContext = callbackContext;
		if (action.equals("pay")) {
			this.pay(args);
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		unRegisterPayecoPayBroadcastReceiver();
		super.onDestroy();
	}



	/**
	 * @Title registerPayecoPayBroadcastReceiver 
	 * @Description 注册广播接收器
	 */
	private void registerPayecoPayBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_PAY_END);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		cordova.getActivity().registerReceiver(payecoPayBroadcastReceiver, filter);
	}

	/**	
	 * @Title unRegisterPayecoPayBroadcastReceiver 
	 * @Description 注销广播接收器
	 */
	private void unRegisterPayecoPayBroadcastReceiver() {

		if (payecoPayBroadcastReceiver != null) {
			cordova.getActivity().unregisterReceiver(payecoPayBroadcastReceiver);
			payecoPayBroadcastReceiver = null;
		}
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		// TODO Auto-generated method stub
		super.initialize(cordova, webView);
		this.payUrl = webView.getPreferences().getString("payUrl", "");
		this.notifyUrl = webView.getPreferences().getString("notifyUrl", "");
		this.environment = webView.getPreferences().getString("environment", "");
		//初始化支付结果广播接收器
		this.initPayecoPayBroadcastReceiver();
		//注册支付结果广播接收器
		this.registerPayecoPayBroadcastReceiver();
	}
	
	/**
	 * @Title pay 
	 * @Description 下单支付按扭点击事件，支付流程 
	 * @param view
	 * @throws JSONException 
	 */
	public void pay(JSONArray args) throws JSONException{
		//		Toast.makeText(this.getServiceName().,"正在下单，请稍等...", Toast.LENGTH_LONG).show();
		//callbackContext.success(i.getStringExtra(Intent.EXTRA_TEXT));
		//callbackContext.error("");
		Toast.makeText(cordova.getActivity(), "正在下单，请稍等...", Toast.LENGTH_LONG).show();
		JSONObject jsonObject = (JSONObject) args.get(0);
		final String orderId = jsonObject.getString("orderId");
		if (orderId == null || "".equals(orderId)) {
			Toast.makeText(cordova.getActivity(), "订单ID不能为空！", Toast.LENGTH_SHORT).show();
			return ;
		}
		// 使用异步通讯
				new AsyncTask<Void, Void, String>(){
					@Override
					protected String doInBackground(Void... params) {
						//组织参数，用于向商户服务器下单的参数
						ArrayList<NameValuePair> reqParams = new ArrayList<NameValuePair>();
						reqParams.add(new BasicNameValuePair("orderId", orderId));//商品编号
//						reqParams.add(new BasicNameValuePair("Amount", amount));//商品数量
						//以上参数根据实际需要来组织
						
						//用于接收通讯响应的内容
						String respString = null;
						
						//请求商户服务器下单地址
						try {
							Log.i("test", "正在请求："+payUrl);
							respString = httpComm(payUrl, reqParams);
							
						} catch (Exception e) {
							Log.e("test", "下单失败，通讯发生异常", e);
							e.printStackTrace();
						}
						return respString;
					}

					@Override
					protected void onPostExecute(String result) {
						super.onPostExecute(result);
						
						if (result == null) {
							Log.e("test", "下单失败！");
							return ;
						}
						
						Log.i("test", "响应数据："+result);
						
						try {
							//解析响应数据
							JSONObject json = new JSONObject(result);
							//响应示例如下，其中RetCode、RetMsg用于告诉客户端请求是否成功，其它参数可直接传递给易联支付插件
							//{
							//	"RetCode": "0000", 
							//	"RetMsg": "", 
							//	"Version": "2.0.0", 
							//	"MerchOrderId": "1408006824547", 
							//	"MerchantId": "302020000058", 
							//	"Amount": "5.00", 
							//	"TradeTime": "20140814170024", 
							//	"OrderId": "302014081400038872", 
							//	"Sign": "QBOiI4xl1CgWNHt+8KTyVR2c9bAGNMMkXTHsYhJrmr9QPuHhRe1CiPGu+beOiayQTGGigTJEzUm23q0lAnDoXcnmwt7bsyG+UOwl3m9OKUd8o+SP741OOJxXHK884OXWuygMXkczK+TvYhNv/RLYKgAVSG6qN0lmsc2lek+cxqo="
							//}
							//校验返回结果
							if (!json.has("RetCode") || !"0000".equals(json.getString("RetCode"))) {
								if (json.has("RetMsg")) {
									Toast.makeText(cordova.getActivity(), json.getString("RetMsg"), Toast.LENGTH_LONG).show();
									Log.e("test", json.getString("RetMsg"));
								}else{
									Toast.makeText(cordova.getActivity(), "返回数据有误:"+result, Toast.LENGTH_LONG).show();
									Log.e("test", "返回数据有误:"+result);
								}
								
								return;
							}
							
							//组织参数用于跳转至易联支付插件，示例如下
							//{
							//	"Version": "2.0.0", 
							//	"MerchOrderId": "1408006824547", 
							//	"MerchantId": "302020000058", 
							//	"Amount": "5.00", 
							//	"TradeTime": "20140814170024", 
							//	"OrderId": "302014081400038872", 
							//	"Sign": "QBOiI4xl1CgWNHt+8KTyVR2c9bAGNMMkXTHsYhJrmr9QPuHhRe1CiPGu+beOiayQTGGigTJEzUm23q0lAnDoXcnmwt7bsyG+UOwl3m9OKUd8o+SP741OOJxXHK884OXWuygMXkczK+TvYhNv/RLYKgAVSG6qN0lmsc2lek+cxqo="
							//}
							json.remove("RetCode");//RetCode参数不需要传递给易联支付插件
							json.remove("RetMsg");//RetMsg参数不需要传递给易联支付插件
							
							String upPayReqString = json.toString();
					//		String upPayReqString ="%7b%0d%0a++++++++++++++++++++%22Version%22%3a%222.0.0%22%2c%0d%0a++++++++++++++++++++%22MerchOrderId%22%3a%221502121011538122392o203001%22%2c%0d%0a++++++++++++++++++++%22MerchantId%22%3a%22502050001611%22%2c%0d%0a++++++++++++++++++++%22Amount%22%3a%224.00%22%2c%0d%0a++++++++++++++++++++%22TradeTime%22%3a%2220150212101544%22%2c%0d%0a++++++++++++++++++++%22OrderId%22%3a%22502015021269015161%22%2c%0d%0a++++++++++++++++++++%22Sign%22%3a%22j9bZdM4g8x9fSebRVenMMPBOc5N25xU%2bqavG4MN9hAa5mHN4Wm6HqkV3raUtcwkJvrW7Y3K1behxeOT%2bdvhRzv7Miz5uLtEHj5qsNyogozs4qEIIIdp7wrcOiMpUGwgYR1M9yTD128g4hoe4regXG9CmGryMYr669rmkHpjNyfM%3d%22%0d%0a++++++++++++++++%7d ";
							Log.i("test", "请求易联支付插件，参数："+upPayReqString);
							
							//跳转至易联支付插件
							Intent intent = new Intent(cordova.getActivity(),PayecoPluginLoadingActivity.class);
							intent.putExtra("upPay.Req", upPayReqString);
							intent.putExtra("Broadcast", BROADCAST_PAY_END); //广播接收地址
							intent.putExtra("Environment",environment); // 00: 测试环境, 01: 生产环境
							cordova.getActivity().startActivity(intent);
							
						} catch (JSONException e) {
							Log.e("test", "解析处理失败！", e);
							e.printStackTrace();
						}
					}
				}.execute();
	}

	//http通讯
	private String httpComm(String reqUrl, ArrayList<NameValuePair> reqParams) throws UnsupportedEncodingException,
	IOException, ClientProtocolException {
		String respString = null;
		HttpPost httpPost = new HttpPost(reqUrl);
		HttpEntity entity = new UrlEncodedFormEntity(reqParams, HTTP.UTF_8);
		httpPost.setEntity(entity);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResp = httpClient.execute(httpPost);
		int statecode = httpResp.getStatusLine().getStatusCode();
		if (statecode == 200) {
			respString = EntityUtils.toString(httpResp.getEntity());
		}else{
			Log.e("test", "通讯发生异常，响应码["+statecode+"]");
		}
		return respString;
	}
	//初始化支付结果广播接收器
	private void initPayecoPayBroadcastReceiver() {
		payecoPayBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//接收易联支付插件的广播回调
				String action = intent.getAction();
				if (!BROADCAST_PAY_END.equals(action)) {
					Log.e("test", "接收到广播，但与注册的名称不一致["+action+"]");
					return ;
				}	
				//商户的业务处理
				String result = intent.getExtras().getString("upPay.Rsp");
				Log.i("test", "接收到广播内容："+result);
				final String notifyParams = result;
				// 使用异步通讯
				new AsyncTask<Void, Void, String>(){
					@Override
					protected String doInBackground(Void... params) {
						//用于接收通讯响应的内容
						String respString = null;
						//通知商户服务器
						try {
							JSONObject reqJsonParams = new JSONObject(notifyParams);
							ArrayList<NameValuePair> reqParams = new ArrayList<NameValuePair>();
							@SuppressWarnings("unchecked")
							Iterator<String> keys = reqJsonParams.keys();
							while (keys.hasNext()) {
								String key = keys.next();
								String value = reqJsonParams.getString(key);
								reqParams.add(new BasicNameValuePair(key, value));
							}

							Log.i("test", "正在请求："+notifyUrl);
							respString = httpComm(notifyUrl, reqParams);
						} catch (JSONException e) {
							Log.e("test", "解析处理失败！", e);
							e.printStackTrace();
						} catch (Exception e) {
							Log.e("test", "通知失败，通讯发生异常", e);
							e.printStackTrace();
						}
						return respString;
					}

					@Override
					protected void onPostExecute(String result) {
						super.onPostExecute(result);
						if (result == null) {
							Log.e("test", "通知失败！");
							return ;
						}
						Log.i("test", "响应数据："+result);
						try {
							//解析响应数据
							JSONObject json = new JSONObject(result);
							//校验返回结果
							if (!json.has("RetMsg")) {
								Toast.makeText(cordova.getActivity(), "返回数据有误:"+result, Toast.LENGTH_LONG).show();
								Log.e("test", "返回数据有误:"+result);
								return ;
							}
							Toast.makeText(cordova.getActivity(), json.getString("RetMsg"), Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							Log.e("test", "解析处理失败！", e);
							e.printStackTrace();
						}
					}
				}.execute();
				try {
					JSONObject jsoR = new JSONObject(result);
					String code = jsoR.getString("respCode");
					JSONObject resp = new JSONObject();
					resp.put("respCode", code);
					resp.put("respDesc", jsoR.getString("respDesc"));
					resp.put("orderId",jsoR.getString("OrderId"));
					if (null != jsoR & "0000".equals(code)) {
						callBackContext.success(resp);
					}else{
						callBackContext.error(resp);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
}
