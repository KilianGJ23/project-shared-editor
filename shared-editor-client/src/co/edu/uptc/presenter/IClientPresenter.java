package co.edu.uptc.presenter;

public interface IClientPresenter {
    void connectToServer(String host, int port, String username);

    void disconnectFromServer();

    void createNewFile(String fileName);

    void importJavaFile(String filePath);

    void exportCurrentFile(String filePath);

    void selectFile(String fileName);

    void requestLockLine(int lineNumber);

    void requestEditLine(String newContent);

    void requestInsertLine(String text);

    void requestDeleteLine();

    void requestUnlockLine();
}
