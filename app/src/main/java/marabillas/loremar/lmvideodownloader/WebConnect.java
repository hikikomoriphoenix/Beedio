package marabillas.loremar.lmvideodownloader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;

class WebConnect {
    private EditText textBox;
    private Activity activity;

    WebConnect(EditText textBox, Activity activity) {
        this.textBox = textBox;
        this.activity = activity;
    }

    void connect() {
        String text = textBox.getText().toString();
        if(Patterns.WEB_URL.matcher(text).matches()){
            if(!text.startsWith("http://")){
                text = "http://" + text;
            }
            openPage(text);
        }
        else{
            text = "https://google.com/search?q="+text;
            openPage(text);
        }
    }

    private void openPage(String url){
        Bundle data = new Bundle();
        data.putString("url", url);
        BrowserWindow browser = new BrowserWindow();
        browser.setArguments(data);
        activity.getFragmentManager().beginTransaction()
                .replace(android.R.id.content, browser, null)
                .addToBackStack(null)
                .commit();
    }
}
