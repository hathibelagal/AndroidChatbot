package com.tutsplus.chatbottutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CHATBOTTUTORIAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ConversationService myConversationService =
                new ConversationService(
                        "2017-05-26",
                        getString(R.string.username),
                        getString(R.string.password)
                );

        final TextView conversation = (TextView)findViewById(R.id.conversation);

        final EditText userInput = (EditText)findViewById(R.id.user_input);
        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView tv, int action, KeyEvent keyEvent) {
                if(action == EditorInfo.IME_ACTION_DONE) {

                    final String inputText = userInput.getText().toString();
                    conversation.append(
                            Html.fromHtml("<p><b>You:</b> " + inputText + "</p>")
                    );

                    // Clear edittext
                    userInput.setText("");

                    MessageRequest request = new MessageRequest.Builder()
                            .inputText(inputText)
                            .build();

                    myConversationService
                            .message(getString(R.string.workspace), request)
                            .enqueue(new ServiceCallback<MessageResponse>() {
                                @Override
                                public void onResponse(MessageResponse response) {
                                    final String outputText = response.getText().get(0);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            conversation.append(
                                                    Html.fromHtml("<p><b>Bot:</b> " +
                                                            outputText + "</p>")
                                            );
                                        }
                                    });

                                    if(response.getIntents().get(0).getIntent().endsWith("RequestQuote")) {
                                        Fuel.get("https://api.forismatic.com/api/1.0/?method=getQuote&format=text&lang=en")
                                                .responseString(new Handler<String>() {
                                                    @Override
                                                    public void success(Request request, Response response, String quote) {
                                                        conversation.append(
                                                                Html.fromHtml("<p><b>Bot:</b> " +
                                                                        quote + "</p>")
                                                        );
                                                    }

                                                    @Override
                                                    public void failure(Request request, Response response, FuelError fuelError) {
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            });
                }
                return false;
            }
        });
    }
}
