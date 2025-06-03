package com.example.mybookshelf;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybookshelf.apis.AiAPI;
import com.example.mybookshelf.dataClass.Book;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BookRecommendationFlow{

    private MainActivity mainActivity;
    private UIMaster uiMaster;
    private RelativeLayout box;
    private ArrayList<String> atributes;
    private AiAPI ai;
    private String bookName;
    private HashSet<String> doNotRecommend;
    private RelativeLayout loadingLayout;
    private List<View> balls = new ArrayList<>();
    private boolean isThinking = false;

    public BookRecommendationFlow(MainActivity mainActivity, UIMaster uiMaster, String bookName) {
        this.mainActivity = mainActivity;
        this.uiMaster = uiMaster;
        this.bookName = bookName;
        atributes = new ArrayList<>();
        ai = new AiAPI();
        doNotRecommend = new HashSet<>();
        List<Book> bookList = mainActivity.getUser().getBookList();
        for (Book book : bookList) {
            doNotRecommend.add(book.getName());
        }
    }

    /**
     * Handles AI logic based on the path type and updates the UI accordingly.
     */
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

    /**
     * Displays buttons for user to choose aspects of a book they liked most.
     */
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

    /**
     * Displays a grid of buttons to choose book genre preferences and navigates to step 3 based on selection.
     */
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

    /**
     * Displays a grid of buttons to select book attributes based on user input.
     */
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

    /**
     * Builds a prompt based on selected attributes and calls the AI to generate a recommendation.
     */
    private void buildAIStringBased(String selectedOption){
        atributes.add(selectedOption);

        String prompt = "I want a book like" + bookName + "with a simular " + atributes.get(0) + "it should have a " + atributes.get(1) + "genre and a "+
                        atributes.get(2) + "feeling. I want only 1 book and only the name of the book, nothing else.";

        callAI(prompt);
    }



    /**
     * Displays a grid of buttons with book genres and handles user selection.
     */
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

    /**
     * Displays a step in the fresh start process with styled buttons.
     */
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

    /**
     * Displays a grid of buttons to select a setting.
     */
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

    /**
     * Displays step 4 of the fresh start process with character type selection options.
     */
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

    /**
     * Displays a grid of buttons for selecting story endings and updates chat messages accordingly.
     */
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


    /**
     * Builds a prompt string based on selected attributes and calls the AI to fetch a book recommendation.
     */
    private void buildAIStringFresh(String selectedOption){
        atributes.add(selectedOption);

        StringBuilder prompt = new StringBuilder();
        prompt.append("I want a " + atributes.get(0) + " book that has a " + atributes.get(1) +
                " vibe and a " + atributes.get(2) + " setting. The story should follow a " +
                atributes.get(3) + " and have a " + atributes.get(4) +
                " ending. I want only 1 book and only the name of the book, nothing else. Never say: ");
        for (String book: doNotRecommend) {
            prompt.append(book).append(", ");
        }

        Log.e("PROMPT", "buildAIStringFresh: ");
        callAI(prompt.toString());
    }

    /**
     * Initiates an AI request based on the given prompt and processes the response.
     */
    private void callAI(String prompt){
        box.removeAllViews();
        showThinkingAnimation();
        AiAPI.fetchResponse(prompt, new ApiResponseCallback() {
            @Override
            /**
             * Handles the successful AI response by updating the UI with parsed data and options.
             */
            public void onSuccess(String response) {
                // Handle the raw response (likely a JSON string)
                Log.d("AI_RESPONSE", response);
                mainActivity.runOnUiThread(() -> {
                    // Update UI if needed

                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(response);
                        String parsedResponse = obj.getJSONObject("result").getString("response");
                        parsedResponse = parsedResponse.replace("\\n", Objects.requireNonNull(System.lineSeparator())).replace("\\\"", "\"");
                        doNotRecommend.add(parsedResponse);
                        hideThinkingAnimation();
                        addChatMessage("Based on your answers we think you will like \"" + parsedResponse + "\"", "question");
                        addChatMessage("Should I suggest you another one?", "question");
                        GridLayout grid = new GridLayout(mainActivity);
                        grid.setColumnCount(2);
                        grid.setLayoutParams(new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        grid.setPadding(40, 40, 40, 40);

                        String finalParsedResponse = parsedResponse;
                        grid.addView(createStyledGridButton("Yes", v -> {
                            addChatMessage("Yes","user");
                            String newPrompt = prompt + "," + finalParsedResponse;
                            callAI(newPrompt);
                        }));
                        grid.addView(createStyledGridButton("No", v -> handleNo()));
                        box.addView(grid);
                    } catch (JSONException e) {
                        hideThinkingAnimation();
                        Toast.makeText(mainActivity, "Something went wronge, please try again", Toast.LENGTH_LONG).show();
                        throw new RuntimeException(e);

                    }
                });
            }

            @Override
            /**
             * Handles failure by logging an error, displaying a toast message, and adding a chat message.
             */
            public void onFailure(String errorMessage) {
                Log.e("AI_ERROR", errorMessage);
                mainActivity.runOnUiThread(() -> {
                    Toast.makeText(mainActivity, "Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    addChatMessage("Something went wrong please try again later", "question");
                });
            }
        });
    }


    /**
     * Creates a styled button with specified text and click listener for use in a grid layout.
     */
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

    /**
     * Handles user response to a question by removing previous views, adding chat messages,
     * and displaying a grid of options for further interaction.
     */
    private void handleNo() {
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews();
            addChatMessage("No", "user");
            addChatMessage("Would you like recommendations based on the book you are currently watching or a completly fresh start?", "question");
            GridLayout grid = new GridLayout(mainActivity);
            grid.setColumnCount(2);
            grid.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            grid.setPadding(40, 40, 40, 40);

            grid.addView(createStyledGridButton("Fresh Start", v -> {
                addChatMessage("Fresh Start", "user");
                handleAI("freshStart");
            }));
            grid.addView(createStyledGridButton("Book Based", v -> {
                addChatMessage("Book Based", "user");
                handleAI("bookBased");
            }));
            box.addView(grid);
        });
    }


    /**
     * Adds a chat message to the UI based on the provided text and sender type.
     * The method creates a TextView for the message, sets its properties such as color,
     * background, and layout, and adds it to the GridLayout in the main activity. It also
     * ensures that the ScrollView scrolls to show the latest message.
     *
     * @param text       The text content of the chat message.
     * @param senderType The type of the sender, either "user" or another identifier for system messages.
     */
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


        ScrollView scrollView = mainActivity.findViewById(R.id.scrollChat);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * Displays a thinking animation with three bouncing balls in a RelativeLayout.
     */
    private void showThinkingAnimation() {
        mainActivity.runOnUiThread(() -> {
            box.removeAllViews(); // Clear box first
            loadingLayout = new RelativeLayout(mainActivity);

            int ballSize = 50;

            for (int i = 0; i < 3; i++) {
                View ball = new View(mainActivity);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ballSize, ballSize);
                params.leftMargin = 200 + (i * 100); // spacing between balls
                params.topMargin = 400;
                ball.setLayoutParams(params);
                ball.setBackgroundResource(R.drawable.circle_background); // We'll create this drawable
                loadingLayout.addView(ball);
                balls.add(ball);

                // Add bouncing animation
                TranslateAnimation bounce = new TranslateAnimation(0, 0, 0, -100);
                bounce.setDuration(500);
                bounce.setRepeatMode(Animation.REVERSE);
                bounce.setRepeatCount(Animation.INFINITE);
                bounce.setStartOffset(i * 200); // delay each ball
                ball.startAnimation(bounce);
            }

            box.addView(loadingLayout);
            isThinking = true;
        });
    }

    /**
     * Hides the thinking animation by clearing animations and resetting views.
     */
    private void hideThinkingAnimation() {
        mainActivity.runOnUiThread(() -> {
            if (isThinking) {
                for (View ball : balls) {
                    ball.clearAnimation();
                }
                balls.clear();
                box.removeAllViews();
                isThinking = false;
            }
        });
    }
}