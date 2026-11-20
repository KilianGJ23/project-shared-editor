package co.edu.uptc.presenter;

import java.util.List;

public interface IEditorView {
    void setPresenter(IClientPresenter presenter);

    void showEditorWindow(String username, int colorId, boolean isAdmin);

    void closeEditorWindow();

    void updateConnectedUsers(List<String[]> usersData);

    void updateFileList(List<String> fileNames);

    void setAdminPermissions(boolean isAdmin);

    void displayFileLines(String fileName, List<String[]> linesData, String localUsername);

    void highlightLockedLine(int lineNumber, String ownerUsername, int colorId, boolean ownedByLocalUser);

    void clearLineLock(int lineNumber);

    void updateLineContent(int lineNumber, String content);

    String getFullDisplayedCode();

    void showStatusMessage(String message);

    void showErrorDialog(String message);
}
