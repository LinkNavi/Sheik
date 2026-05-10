#pragma once
#include <QMainWindow>
#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QPushButton>
#include <QProgressBar>
#include <QTimer>
#include <QSocketNotifier>
#include "IPC.h"
#include "Clicker.h"
#include "SettingsMenu.h"

class MainMenu : public QMainWindow {
    Q_OBJECT
public:
    explicit MainMenu(SettingsMenu *settings, QWidget *parent = nullptr);
    ~MainMenu();

signals:
    void goToDebug();
    void goToSettings();

private slots:
    void toggleRunning();
    void updateLoop();
    void clickLoop();
    void onEvdevReadable();
public slots:
    void onSettingsChanged();

private:
    bool openEvdev();

    SettingsMenu   *settings;
    SheikIPC        ipc;
    Clicker         clicker;
    bool            running = false;
    bool            leftHeld = false;
    bool            rightHeld = false;
    int             evdevFd = -1;

    QWidget     *centralWidget;
    QVBoxLayout *layout;
    QHBoxLayout *topBarLayout;
    QPushButton *btnSettings;
    QPushButton *btnToggle;
    QPushButton *btnDebugMenu;
    QPushButton *btnQuit;

    QLabel      *lblConnectionDot;
    QLabel      *lblConnectionText;
    QLabel      *lblTargetId;
    QLabel      *lblTargetHP;
    QProgressBar *hpBar;
    QWidget     *infoPanel;

    QTimer          *updateTimer;
    QTimer          *clickTimer;
    QSocketNotifier *evdevNotifier;
};
