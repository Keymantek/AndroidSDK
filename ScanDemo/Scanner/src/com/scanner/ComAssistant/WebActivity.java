package com.scanner.ComAssistant;

 
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.scanner.ScanAssistant.R;

public class WebActivity extends Activity {

	private String url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		Intent in = getIntent();
		url = in.getStringExtra("url");
		
		WebView myWebView = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = myWebView .getSettings();
		myWebView.setWebViewClient(new WebViewClient(){
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    return false;// ����false
		    }
		});
		
	        // ��WebView�ܹ�ִ��javaScript
	        webSettings.setJavaScriptEnabled(true);
	        // ��JavaScript�����Զ���windows
	        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	        // ���û���
	        webSettings.setAppCacheEnabled(true);
	        // ���û���ģʽ,һ��������ģʽ
	        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	        // ���û���·��
//	        webSettings.setAppCachePath("");
	        // ֧������(���䵽��ǰ��Ļ)
	        webSettings.setSupportZoom(true);
	        // ��ͼƬ���������ʵĴ�С
	        webSettings.setUseWideViewPort(true);
	        // ֧���������²���,һ�������ַ�ʽ
	        // Ĭ�ϵ���NARROW_COLUMNS
	        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
	        // ���ÿ��Ա���ʾ����Ļ����
	        webSettings.setDisplayZoomControls(true);
	        // ����Ĭ�������С
	
		myWebView.loadUrl(url);
	}

	
}
