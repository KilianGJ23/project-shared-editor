package co.edu.uptc.view;

public interface LineInteractionHandler {
    void onLineClicked(int lineNumber);

    void onLineEdited(String newContent);

    void onInsertRequested();

    void onUnlockRequested();
}
