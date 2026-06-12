package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ChatAdapter adapter;
    private List<ChatAdapter.ChatMessage> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvChat    = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);

        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Welcome message
        addBotMessage("👋 Hey! I'm your FlowFi assistant.\n\nYou can ask me about:\n• balance\n• goals\n• tips\n• challenges\n• how to add a transaction\n• savings advice");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        addUserMessage(text);
        etMessage.setText("");
        addBotMessage(getReply(text.toLowerCase()));
    }

    private String getReply(String msg) {

        if (contains(msg, "hello", "hi", "hey", "sup", "greet")) {
            return "Hey there! 👋 How can I help you manage your money today?";
        }
        if (contains(msg, "balance", "how much", "total", "net")) {
            return "Your balance = Total Income − Total Expenses.\n\nCheck the big green number on your Dashboard for your current balance. 💰";
        }
        if (contains(msg, "add", "transaction", "expense", "income", "record")) {
            return "To add a transaction:\n1. Tap the green ＋ button on the Dashboard\n2. Choose Expense or Income\n3. Enter the amount\n4. Pick a category\n5. Hit Save ✅";
        }
        if (contains(msg, "goal", "saving", "target", "save")) {
            return "Savings Goals help you save toward something specific.\n\nGo to Goals from the Dashboard → tap ＋ → set a title, target amount, and deadline. Then add funds anytime! 🎯";
        }
        if (contains(msg, "challenge", "streak", "habit")) {
            return "Challenges help you build better money habits.\n\nCreate one with a title and target days, then tap 'Mark Today Done' each day to grow your streak. 🔥";
        }
        if (contains(msg, "chart", "analytic", "report", "statistic", "graph")) {
            return "Open Analytics from the Dashboard to see:\n📊 Pie chart — spending by category\n📈 Bar chart — income vs expenses\n💡 Smart insights about your habits";
        }
        if (contains(msg, "history", "past", "previous", "transaction")) {
            return "Tap 'See all' on the Dashboard or go to History in the bottom nav.\n\nYou can search, filter by type, edit ✏️ or delete 🗑️ any transaction.";
        }
        if (contains(msg, "tip", "advice", "suggest", "help me", "improve")) {
            return "💡 FlowFi Tip:\nTry the 50/30/20 rule:\n• 50% on needs (food, rent, bills)\n• 30% on wants (fun, eating out)\n• 20% on savings\n\nCheck your Analytics to see where you stand!";
        }
        if (contains(msg, "delete", "remove", "clear")) {
            return "To delete a transaction:\n1. Go to History\n2. Find the transaction\n3. Tap the 🗑 Delete button\n4. Confirm in the dialog";
        }
        if (contains(msg, "edit", "update", "change", "fix")) {
            return "To edit a transaction:\n1. Go to History\n2. Find the transaction\n3. Tap ✏️ Edit\n4. Update the details and save";
        }
        if (contains(msg, "spend", "spending", "too much", "overspend")) {
            return "If you're overspending, try:\n1. Check Analytics to find your top category\n2. Set a weekly budget goal\n3. Start a Challenge to cut back\n4. Log every expense — awareness helps! 💪";
        }
        if (contains(msg, "logout", "log out", "sign out")) {
            return "To log out, just close the app. Session is saved securely on your device.";
        }
        if (contains(msg, "thank", "thanks", "appreciate")) {
            return "You're welcome! 😊 Keep tracking and stay on top of your finances!";
        }
        if (contains(msg, "good", "great", "awesome", "nice", "cool")) {
            return "Love the energy! 💚 Keep that up with your savings too!";
        }

        // Default
        return "Hmm, I'm not sure about that. Try asking me about:\n• balance\n• adding a transaction\n• goals\n• challenges\n• tips\n• analytics";
    }

    private boolean contains(String msg, String... keywords) {
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    private void addUserMessage(String text) {
        messages.add(new ChatAdapter.ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        messages.add(new ChatAdapter.ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }
}