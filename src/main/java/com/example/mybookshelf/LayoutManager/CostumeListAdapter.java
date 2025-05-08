package com.example.mybookshelf.LayoutManager;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybookshelf.DataBaseConnection;
import com.example.mybookshelf.MainActivity;
import com.example.mybookshelf.UIMaster;
import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.User;

import java.sql.SQLException;
import java.util.List;

public class CostumeListAdapter extends RecyclerView.Adapter<CostumeListAdapter.BookViewHolder> {
    private List<Book> bookList;
    private MainActivity mainActivity;
    private DataBaseConnection db;
    private User loggedInUser;
    private UIMaster uiMaster;

    public CostumeListAdapter(MainActivity mainActivity, List<Book> bookList, DataBaseConnection db, User loggedInUser, UIMaster uiMaster) {
        this.mainActivity = mainActivity;
        this.bookList = bookList;
        this.db = db;
        this.loggedInUser = loggedInUser;
        this.uiMaster = uiMaster;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a FrameLayout as the root view for each book
        FrameLayout frameLayout = new FrameLayout(mainActivity);
        return new BookViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bookBox.removeAllViews();  // Clear previous views
        holder.bookBox.addView(createBookBoxView(book));  // Create the new book box view and add it
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        FrameLayout bookBox;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookBox = (FrameLayout) itemView;
        }
    }

    private View createBookBoxView(Book book) {
        // Create a container for the book box with rounded corners (using LinearLayout)
        LinearLayout bookBox = new LinearLayout(mainActivity);
        bookBox.setOrientation(LinearLayout.VERTICAL);
        bookBox.setPadding(16, 16, 16, 16);
        bookBox.setBackgroundColor(Color.WHITE);

        // Apply rounded corners to the bookBox background
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(24);
        bookBox.setBackground(background);

        // Set book box width to match the parent (RecyclerView's width)
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,  // Match parent width
                LinearLayout.LayoutParams.WRAP_CONTENT  // Wrap content height
        );
        boxParams.setMargins(12, 12, 12, 12); // Set margins between each book box
        bookBox.setLayoutParams(boxParams);

        // Remove Button (Always in Top-Right)
        ImageButton actionButton = getBtnRemove(book);
        if (actionButton != null) {
            if (actionButton.getParent() != null) {
                ((ViewGroup) actionButton.getParent()).removeView(actionButton);
            }
            actionButton.setVisibility(View.VISIBLE);

            // Set button size and position it at the top-right
            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(80, 80);
            btnParams.gravity = Gravity.TOP | Gravity.END; // Position it in the top-right
            btnParams.setMargins(12, 12, 0, 0); // Top and right margin
            actionButton.setLayoutParams(btnParams);

            // Add the button to the book box (LinearLayout)
            bookBox.addView(actionButton);
        }

        // Create a horizontal layout for image + text (book details)
        LinearLayout horizontalLayout = new LinearLayout(mainActivity);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setPadding(12, 12, 12, 12);
        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Create an ImageView for the book cover
        ImageView bookImage = new ImageView(mainActivity);
        bookImage.setId(View.generateViewId());
        String imageUrl = book.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(mainActivity).load(imageUrl).into(bookImage);
        }

        // Set ImageView layout params
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 160);
        bookImage.setLayoutParams(imageParams);

        // Swap author and release date if needed
        String author = book.getRelease_date();
        String releaseDate = book.getAuthor();
        book.setAuthor(author);
        book.setRelease_date(releaseDate);

        // Create a TextView for the book details
        TextView bookDetails = new TextView(mainActivity);
        StringBuilder details = new StringBuilder();
        details.append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");

        bookDetails.setText(details.toString());
        bookDetails.setTextColor(Color.BLACK);
        bookDetails.setTextSize(14);
        bookDetails.setPadding(12, 0, 0, 0);

        // Add Image and Details to horizontal layout
        horizontalLayout.addView(bookImage);
        horizontalLayout.addView(bookDetails);

        // Add the horizontal layout to the vertical container
        bookBox.addView(horizontalLayout);

        // Add Notes input
        String note = "Add notes here...";  // Default note
        try {
            note = db.getNotesFromUser(loggedInUser.getUid(), book.getId()).get();
        } catch (Exception e) {
            Log.e("CostumeListAdapter", "Error retrieving notes: " + e.getMessage());
        }

        EditText noteField = new EditText(mainActivity);
        noteField.setHint(note);
        noteField.setTextColor(Color.BLACK);
        noteField.setBackgroundColor(Color.LTGRAY);
        noteField.setPadding(8, 8, 8, 8);
        noteField.setTextSize(14);
        noteField.setId(View.generateViewId());

        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        noteParams.setMargins(12, 12, 12, 12);
        noteField.setLayoutParams(noteParams);

        noteField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                db.notesChanged(loggedInUser.getUid(), book.getId(), s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        bookBox.addView(noteField);

        // Navigate to book details when clicked
        bookBox.setOnClickListener(v -> uiMaster.navigateToDetails(book));

        return bookBox;
    }

    @NonNull
    private ImageButton getBtnRemove(Book book) {
        ImageButton btnRemove = new ImageButton(mainActivity);
        btnRemove.setOnClickListener(v -> {
            try {
                // Remove the book from the database
                mainActivity.removeBook(book);

                // Get the parent container (FrameLayout) where the book box is located
                FrameLayout bookBox = (FrameLayout) btnRemove.getParent();  // This assumes the bookBox is the direct parent of the button
                if (bookBox != null) {
                    ViewGroup parentContainer = (ViewGroup) bookBox.getParent(); // Get the container (LinearLayout)
                    if (parentContainer != null) {
                        parentContainer.removeView(bookBox); // Remove the book box from the container
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        // Set image resource for remove button
        btnRemove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

        // Set background to transparent to avoid default button styling
        btnRemove.setBackgroundColor(Color.TRANSPARENT);

        // Set layout parameters for the button
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(80, 80);
        btnParams.gravity = Gravity.TOP | Gravity.END;
        btnParams.setMargins(12, 12, 0, 0); // Top and right margin
        btnRemove.setLayoutParams(btnParams);

        return btnRemove;
    }
}
