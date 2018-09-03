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
		    return false;// 返回false
		    }
		});
		
	        // 让WebView能够执行javaScript
	        webSettings.setJavaScriptEnabled(true);
	        // 让JavaScript可以自动打开windows
	        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	        // 设置缓存
	        webSettings.setAppCacheEnabled(true);
	        // 设置缓存模式,一共有四种模式
	        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	        // 设置缓存路径
//	        webSettings.setAppCachePath("");
	        // 支持缩放(适配到当前屏幕)
	        webSettings.setSupportZoom(true);
	        // 将图片调整到合适的大小
	        webSettings.setUseWideViewPort(true);
	        // 支持内容重新布局,一共有四种方式
	        // 默认的是NARROW_COLUMNS
	        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
	        // 设置可以被显示的屏幕控制
	        webSettings.setDisplayZoomControls(true);
	        // 设置默认字体大小
	
		myWebView.loadUrl(url);
	}

	
}
