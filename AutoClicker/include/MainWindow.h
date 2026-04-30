#pragma once

#include <QMainWindow>
#include <QStackedWidget>
#include "MainMenu.h"
#include "DebugMenu.h"
#include "SettingsMenu.h"
#include "Screens.h"
#include "qapplication.h"
#include <QApplication>
class MainWindow : public QMainWindow {
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = nullptr);

public slots:
    void setScreen(Screen screen);

private:


    QStackedWidget *stack;
    MainMenu       *mainMenu;
    DebugMenu      *debugMenu;
    SettingsMenu   *settingsMenu;
};
