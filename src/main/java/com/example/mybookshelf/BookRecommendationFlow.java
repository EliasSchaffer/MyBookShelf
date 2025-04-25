package com.example.mybookshelf;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybookshelf.apis.AiAPI;

import java.util.ArrayList;

public class BookRecommendationFlow{

    private MainActivity mainActivity;
    private UIMaster uiMaster;
    private RelativeLayout box;
    private ArrayList<String> atributes;
    private AiAPI ai;
    private String bookName;

    public BookRecommendationFlow(MainActivity mainActivity, UIMaster uiMaster, String bookName) {
        this.mainActivity = mainActivity;
        this.uiMaster = uiMaster;
        this.bookName = bookName;
        atributes = new ArrayList<>();
        ai = new AiAPI();
    }

    public void handleAI(String pathType) {
        atributes.clear();
        box = this.mainActivity.findViewById(R.id.inputBox);
        RelativeLayout box = mainActivity.findViewById(R.id.inputBox);
        box.removeAllViews();  // Clear previous content

        // UI changes must run on the UI thread
        mainActivity.runOnUiThread(() -> {

            if (pathType.equals("bookBased")) {
                showBookBasedStep1();
            } else if (pathType.equals("freshStart")) {
                showFreshStartStep1();
            }

            // Add question and grid to box

        });
    }

    private void showBookBasedStep1() {
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("\"What did you like most about the book?", "question");
            grid.addView(createStyledGridButton("Characters", v -> {
                addChatMessage("Characters", "user");
                showBookBasedStep2("Characters");
            }));
            grid.addView(createStyledGridButton("Plot", v -> {
                addChatMessage("Plot", "user");
                showBookBasedStep2("Plot");
            }));
            grid.addView(createStyledGridButton("Pacing", v -> {
                addChatMessage("Pacing", "user");
                showBookBasedStep2("Pacing");
            }));
            grid.addView(createStyledGridButton("Writing style", v -> {
                addChatMessage("Writing style", "user");
                showBookBasedStep2("Writing style");
            }));
            box.addView(grid);
        });
    }

    private void showBookBasedStep2(String selectedOption) {
        atributes.add(selectedOption);

        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("Do you want something with the same genre or are you open to others?", "question");
            grid.addView(createStyledGridButton("Same", v -> {
                addChatMessage("Same", "user");
                showBookBasedStep3("Same");
            }));
            grid.addView(createStyledGridButton("slightly different", v -> {
                addChatMessage("slightly different", "user");
                showBookBasedStep3("slightly different");
            }));
            grid.addView(createStyledGridButton("totally different", v -> {
                addChatMessage("totally different", "user");
                showBookBasedStep3("totally different");
            }));

            box.addView(grid);
        });
    }

    private void showBookBasedStep3(String selectedOption) {
        atributes.add(selectedOption);

        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("How heavy or light do you want it to be?", "question");
            grid.addView(createStyledGridButton("Light/funny", v -> {
                addChatMessage("Light/funny", "user");
                buildAIStringBased("Light/funny");
            }));
            grid.addView(createStyledGridButton("Emotional/deep", v -> {
                addChatMessage("Emotional/deep", "user");
                buildAIStringBased("Emotional/deep");
            }));
            grid.addView(createStyledGridButton("Dark/twisty", v -> {
                addChatMessage("Dark/twisty", "user");
                buildAIStringBased("Dark/twisty");
            }));

            box.addView(grid);
        });

    }

    private void buildAIStringBased(String selectedOption){
        atributes.add(selectedOption);

        String prompt = "I want a book like" + bookName + "with a simular " + atributes.get(0) + "it should have a " + atributes.get(1) + "genre and a "+
                        atributes.get(2) + "feeling. I want only 1 book and only the name of the book, nothing else.";

        callAI(prompt);
    }



    private void showFreshStartStep1() {
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("What kind of books are you into right now?", "question");
            grid.addView(createStyledGridButton("Romance", v -> {
                addChatMessage("Romance", "user");
                showFreshStartStep2("Romance");
            }));
            grid.addView(createStyledGridButton("Fantasy", v -> {
                addChatMessage("Fantasy", "user");
                showFreshStartStep2("Fantasy");
            }));
            grid.addView(createStyledGridButton("Sci-Fi", v -> {
                addChatMessage("Sci-Fi", "user");
                showFreshStartStep2("Sci-Fi");
            }));
            grid.addView(createStyledGridButton("Drama", v -> {
                addChatMessage("Drama", "user");
                showFreshStartStep2("Drama");
            }));
            box.addView(grid);
        });
    }

    private void showFreshStartStep2(String selectedOption) {
        atributes.add(selectedOption);
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("Pick a vibe:", "question");
            grid.addView(createStyledGridButton("Light & feel-good", v -> {
                addChatMessage("Light & feel-good", "user");
                showFreshStartStep3("Light & feel-good");
            }));
            grid.addView(createStyledGridButton("Emotional & deep", v -> {
                addChatMessage("Emotional & deep", "user");
                showFreshStartStep3("Emotional & deep");
            }));
            grid.addView(createStyledGridButton("Epic & adventurous", v -> {
                addChatMessage("Epic & adventurous", "user");
                showFreshStartStep3("Epic & adventurous");
            }));
            grid.addView(createStyledGridButton("Complex & thought-provoking", v -> {
                addChatMessage("Complex & thought-provoking", "user");
                showFreshStartStep3("Complex & thought-provoking");
            }));
            box.addView(grid);
        });
    }

    private void showFreshStartStep3(String selectedOption) {
        atributes.add(selectedOption);
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("Any setting you’re drawn to?", "question");
            grid.addView(createStyledGridButton("Modern day", v -> {
                addChatMessage("Modern day", "user");
                showFreshStartStep4("Modern day");
            }));
            grid.addView(createStyledGridButton("Historical", v -> {
                addChatMessage("Historical", "user");
                showFreshStartStep4("Historical");
            }));
            grid.addView(createStyledGridButton("Futuristic", v -> {
                addChatMessage("Futuristic", "user");
                showFreshStartStep4("Futuristic");
            }));
            grid.addView(createStyledGridButton("Dystopian", v -> {
                addChatMessage("Dystopian", "user");
                showFreshStartStep4("Dystopian");
            }));
            box.addView(grid);
        });
    }

    private void showFreshStartStep4(String selectedOption) {
        atributes.add(selectedOption);
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("Would you like to follow a specific kind of character?", "question");
            grid.addView(createStyledGridButton("Royalty ", v -> {
                addChatMessage("Royalty ", "user");
                showFreshStartStep5("Royalty ");
            }));
            grid.addView(createStyledGridButton("Hero", v -> {
                addChatMessage("Hero", "user");
                showFreshStartStep5("Hero");
            }));
            grid.addView(createStyledGridButton("Antihero ", v -> {
                addChatMessage("Antihero ", "user");
                showFreshStartStep5("Antihero ");
            }));
            grid.addView(createStyledGridButton("Teen", v -> {
                addChatMessage("Teen", "user");
                showFreshStartStep5("Teen");
            }));
            box.addView(grid);
        });
    }

    private void showFreshStartStep5(String selectedOption) {
        atributes.add(selectedOption);
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();  // Clear previous content
            // Create a GridLayout for buttons
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            addChatMessage("How do you want the story to feel at the end?", "question");
            grid.addView(createStyledGridButton("Uplifted  ", v -> {
                addChatMessage("Uplifted  ", "user");
                buildAIStringFresh("Uplifted  ");
            }));
            grid.addView(createStyledGridButton("Shocked ", v -> {
                addChatMessage("Shocked ", "user");
                buildAIStringFresh("Shocked ");
            }));
            grid.addView(createStyledGridButton("Moved  ", v -> {
                addChatMessage("Moved  ", "user");
                buildAIStringFresh("Moved  ");
            }));
            grid.addView(createStyledGridButton("Mind-blown", v -> {
                addChatMessage("Mind-blown", "user");
                buildAIStringFresh("Mind-blown");
            }));
            box.addView(grid);
        });
    }


    private void buildAIStringFresh(String selectedOption){
        atributes.add(selectedOption);

        String prompt = "I want a " + atributes.get(0) + " book that has a " + atributes.get(1) +
                " vibe and a " + atributes.get(2) + " setting. The story should follow a " +
                atributes.get(3) + " and have a " + atributes.get(4) +
                " ending. I want only 1 book and only the name of the book, nothing else.";
        callAI(prompt);
    }

    private void callAI(String prompt){
        AiAPI.fetchResponse(prompt, new ApiResponseCallback() {
            @Override
            public void onSuccess(String response) {
                // Handle the raw response (likely a JSON string)
                Log.d("AI_RESPONSE", response);
                mainActivity.runOnUiThread(() -> {
                    // Update UI if needed
                    //TODO Add parsing for the AI response
                    Toast.makeText(mainActivity, "AI Suggestion: " + response, Toast.LENGTH_LONG).show();
                    addChatMessage(response, "question");

                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("AI_ERROR", errorMessage);
                mainActivity.runOnUiThread(() -> {
                    Toast.makeText(mainActivity, "Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    addChatMessage("Something went wrong please try again later", "question");
                });
            }
        });
    }


    private Button createStyledGridButton(String text, View.OnClickListener listener) {
        Button btn = new Button(mainActivity);
        btn.setText(text);
        btn.setBackgroundColor(Color.parseColor("#6200EE"));
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(16f);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(20, 20, 20, 20);

        btn.setLayoutParams(params);
        btn.setPadding(30, 30, 30, 30);
        btn.setOnClickListener(listener);
        return btn;
    }

    private void handleAnswer(String selectedOption) {
        Log.d("AIInput", "User chose: " + selectedOption);
        mainActivity.runOnUiThread(() -> {
            addChatMessage(selectedOption, "user");
            // Optional: show next step here
        });
    }


    public void addChatMessage(String text, String senderType) {
        GridLayout chat = mainActivity.findViewById(R.id.grdChat);

        // Create the message TextView
        TextView messageView = new TextView(mainActivity);
        messageView.setText(text);
        messageView.setTextColor(senderType.equals("user") ? Color.BLACK : Color.WHITE);

        // Set max width and layout
        messageView.setMaxWidth(500);  // Set max width if needed

        // Set background color and padding
        GradientDrawable background = new GradientDrawable();
        background.setColor(senderType.equals("user") ? Color.WHITE : Color.BLUE);
        background.setCornerRadius(16f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            background.setPadding(20, 20, 20, 20);
        }
        messageView.setBackground(background);

        // Use a new row for each message
        int row = chat.getChildCount(); // Each message gets its own row

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(row); // unique row per message

        // Determine column (user on right, system/question on left)
        if (senderType.equals("user")) {
            params.columnSpec = GridLayout.spec(1);
        } else {
            params.columnSpec = GridLayout.spec(0);
        }

        chat.addView(messageView, params);
    }

}