package co.edu.uptc.presenter;

public interface IConnectionView {
    void setPresenter(IClientPresenter presenter);

    void showConnectionWindow();

    void closeConnectionWindow();

    void showConnectionError(String message);

    void setConnectEnabled(boolean enabled);
}
