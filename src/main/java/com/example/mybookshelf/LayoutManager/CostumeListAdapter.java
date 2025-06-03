package com.example.mybookshelf.LayoutManager;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.mybookshelf.DataBaseConnection;
import com.example.mybookshelf.MainActivity;
import com.example.mybookshelf.R;
import com.example.mybookshelf.UIMaster;
import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CostumeListAdapter extends RecyclerView.Adapter<CostumeListAdapter.BookViewHolder> {
    private List<Book> bookList;
    private MainActivity mainActivity;
    private DataBaseConnection db;
    private User loggedInUser;
    private UIMaster uiMaster;
    private ExecutorService databaseExecutor;
    private RequestOptions imageRequestOptions;

    public CostumeListAdapter(MainActivity mainActivity, List<Book> bookList, DataBaseConnection db, User loggedInUser, UIMaster uiMaster) {
        this.mainActivity = mainActivity;
        this.bookList = new ArrayList<>(bookList); // Create a copy to avoid external modifications
        this.db = db;
        this.loggedInUser = loggedInUser;
        this.uiMaster = uiMaster;
        this.databaseExecutor = Executors.newSingleThreadExecutor();

        // Pre-configure Glide request options for reuse
        this.imageRequestOptions = new RequestOptions()
                .override(120, 160)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.drawable.ic_menu_report_image);
    }

    @NonNull
    @Override
    /**
     * Creates and configures a ViewHolder for displaying a book item in the RecyclerView.
     */
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a book item layout that matches your design
        FrameLayout frameLayout = new FrameLayout(mainActivity);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        frameLayout.setLayoutParams(params);

        // Create and configure the book item view
        LinearLayout bookBox = new LinearLayout(mainActivity);
        bookBox.setOrientation(LinearLayout.VERTICAL);
        bookBox.setPadding(16, 16, 16, 16);
        bookBox.setBackgroundColor(Color.WHITE);

        // Apply rounded corners
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(24);
        bookBox.setBackground(background);

        // Set layout params
        FrameLayout.LayoutParams boxParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        boxParams.setMargins(12, 12, 12, 12);
        bookBox.setLayoutParams(boxParams);

        // Create the button container
        FrameLayout buttonContainer = new FrameLayout(mainActivity);
        buttonContainer.setId(View.generateViewId());
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        buttonContainer.setLayoutParams(containerParams);

        // Create remove button
        ImageButton btnRemove = new ImageButton(mainActivity);
        btnRemove.setId(View.generateViewId());
        btnRemove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnRemove.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(80, 80);
        btnParams.gravity = Gravity.TOP | Gravity.END;
        btnRemove.setLayoutParams(btnParams);
        buttonContainer.addView(btnRemove);

        // Add button container to book box
        bookBox.addView(buttonContainer);

        // Create horizontal layout for book content
        LinearLayout horizontalLayout = new LinearLayout(mainActivity);
        horizontalLayout.setId(View.generateViewId());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setPadding(12, 12, 12, 12);
        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        horizontalLayout.setLayoutParams(horizontalParams);

        // Create book cover image view
        ImageView bookImage = new ImageView(mainActivity);
        bookImage.setId(View.generateViewId());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 160);
        bookImage.setLayoutParams(imageParams);

        // Create book details text view
        TextView bookDetails = new TextView(mainActivity);
        bookDetails.setId(View.generateViewId());
        bookDetails.setTextColor(Color.BLACK);
        bookDetails.setTextSize(14);
        bookDetails.setPadding(12, 0, 0, 0);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.weight = 1;
        bookDetails.setLayoutParams(detailsParams);

        // Add views to horizontal layout
        horizontalLayout.addView(bookImage);
        horizontalLayout.addView(bookDetails);

        // Add horizontal layout to book box
        bookBox.addView(horizontalLayout);

        // Create note field
        EditText noteField = new EditText(mainActivity);
        noteField.setId(View.generateViewId());
        noteField.setTextColor(Color.BLACK);
        noteField.setBackgroundColor(Color.LTGRAY);
        noteField.setPadding(8, 8, 8, 8);
        noteField.setTextSize(14);
        noteField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteField.setMaxLines(4);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        noteParams.setMargins(12, 12, 12, 12);
        noteField.setLayoutParams(noteParams);

        // Add note field to book box
        bookBox.addView(noteField);

        // Add book box to frame layout
        frameLayout.addView(bookBox);

        return new BookViewHolder(frameLayout, btnRemove, bookImage, bookDetails, noteField);
    }

    @Override
    /**
     * Binds book data from the list to the provided view holder at the specified position.
     */
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Bind book data to views
        bindBookData(holder, book, position);
    }

    /**
     * Binds data from a Book object to a BookViewHolder.
     *
     * This method updates the UI components of a book item in the RecyclerView,
     * including loading the book's image, setting its details, handling notes,
     * and managing click events for removal and navigation. It also ensures that
     * any existing text watchers are removed before adding new ones to prevent
     * duplicate callbacks. Additionally, it manages asynchronous database operations
     * for retrieving and updating notes.
     *
     * @param holder The BookViewHolder instance holding the book's UI components.
     * @param book   The Book object containing the data to bind.
     * @param position The position of the book in the RecyclerView.
     */
    private void bindBookData(BookViewHolder holder, Book book, int position) {
        // Load book image using Glide with pre-configured options
        String imageUrl = book.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(mainActivity)
                    .load(imageUrl)
                    .apply(imageRequestOptions)
                    .into(holder.bookImage);
        } else {
            holder.bookImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Set book details text
        StringBuilder details = new StringBuilder();
        details.append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");
        holder.bookDetails.setText(details.toString());

        // Setup note field - load notes asynchronously
        holder.noteField.setHint("Add notes here...");
        holder.noteField.setText("");

        // Remove existing text watchers to prevent duplicate callbacks
        if (holder.textWatcher != null) {
            holder.noteField.removeTextChangedListener(holder.textWatcher);
        }

        // Load notes from database in background
        databaseExecutor.execute(() -> {
            try {
                final String notes = db.getNotesFromUser(loggedInUser.getUid(), book.getId()).get();
                // Update UI on main thread
                mainActivity.runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(notes)) {
                        holder.noteField.setText(notes);
                    } else {
                        holder.noteField.setHint("Add notes here...");
                    }

                    // Create and set text watcher after loading notes
                    holder.textWatcher = createTextWatcher(book);
                    holder.noteField.addTextChangedListener(holder.textWatcher);
                });
            } catch (Exception e) {
                Log.e("CostumeListAdapter", "Error retrieving notes: " + e.getMessage());
            }
        });

        // Setup click listener for the remove button
        holder.btnRemove.setOnClickListener(v -> {
                try {
                    // Remove the book from the database
                    mainActivity.removeBook(book);

                    // Update UI on main thread
                    mainActivity.runOnUiThread(() -> {
                        // Remove from list and notify adapter
                        int currentPos = holder.getAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            bookList.remove(currentPos);
                            notifyItemRemoved(currentPos);
                            notifyItemRangeChanged(currentPos, bookList.size() - currentPos);
                        }
                    });
                } catch (SQLException e) {
                    Log.e("CostumeListAdapter", "Error removing book: " + e.getMessage());
                }
        });
        FrameLayout loadingOverlay = mainActivity.findViewById(R.id.home_loading_overlay);
        // Set click listener for the book item
        holder.itemView.setOnClickListener(v -> {
            loadingOverlay.setVisibility(View.VISIBLE);
            uiMaster.navigateToDetails(book);
        });
    }

    /**
     * Creates a DebouncedTextWatcher that updates notes in the database when text changes.
     */
    private DebouncedTextWatcher createTextWatcher(Book book) {
        return new DebouncedTextWatcher(500) {
            @Override
            public void onDebouncedTextChanged(String text) {
                databaseExecutor.execute(() -> {
                    db.notesChanged(loggedInUser.getUid(), book.getId(), text);
                });
            }
        };
    }

    @Override
    /**
     * Returns the number of books in the list.
     */
    public int getItemCount() {
        return bookList.size();
    }

    // Method to update data with DiffUtil for efficient updates
    /**
     * Updates the book list with new data and notifies the adapter of changes.
     */
    public void updateData(List<Book> newBooks) {
        if (newBooks == null) {
            return;
        }

        // Calculate the difference between old and new lists
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            /**
             * Returns the size of the book list.
             */
            public int getOldListSize() {
                return bookList.size();
            }

            @Override
            /**
             * Returns the size of the newBooks list.
             */
            public int getNewListSize() {
                return newBooks.size();
            }

            @Override
            /**
             * Checks if items at given positions have the same ID.
             */
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return bookList.get(oldItemPosition).getId() == newBooks.get(newItemPosition).getId();
            }

            @Override
            /**
             * Checks if the contents of two books at specified positions are the same.
             */
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Book oldBook = bookList.get(oldItemPosition);
                Book newBook = newBooks.get(newItemPosition);
                return oldBook.equals(newBook);
            }
        });

        // Update the list and notify adapter with calculated diff
        this.bookList = new ArrayList<>(newBooks);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Shuts down the database executor if it is not null.
     */
    public void cleanup() {
        if (databaseExecutor != null) {
            databaseExecutor.shutdown();
        }
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        final ImageButton btnRemove;
        final ImageView bookImage;
        final TextView bookDetails;
        final EditText noteField;
        DebouncedTextWatcher textWatcher;

        public BookViewHolder(@NonNull View itemView, ImageButton btnRemove,
                              ImageView bookImage, TextView bookDetails, EditText noteField) {
            super(itemView);
            this.btnRemove = btnRemove;
            this.bookImage = bookImage;
            this.bookDetails = bookDetails;
            this.noteField = noteField;
        }
    }

    /**
     * Custom TextWatcher that debounces text change events
     */
    private abstract class DebouncedTextWatcher implements TextWatcher {
        private final long delayMillis;
        private final Handler handler;
        private Runnable runnable;

        public DebouncedTextWatcher(long delayMillis) {
            this.delayMillis = delayMillis;
            this.handler = new Handler(Looper.getMainLooper());
        }

        @Override
        /**
         * Placeholder method for text change events.
         */
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not used
        }

        @Override
        /**
         * Cancels any pending text change callback when the text changes.
         */
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Remove any pending callbacks
            if (runnable != null) {
                handler.removeCallbacks(runnable);
            }
        }

        @Override
        /**
         * Schedules a delayed call to {@link #onDebouncedTextChanged(String)} with the current text from the Editable object.
         */
        public void afterTextChanged(final Editable s) {
            runnable = () -> onDebouncedTextChanged(s.toString());
            handler.postDelayed(runnable, delayMillis);
        }

        /**
         * Handles debounced text change events.
         */
        public abstract void onDebouncedTextChanged(String text);
    }
}