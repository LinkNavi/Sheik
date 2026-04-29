#include "MainMenu.h"
#include <QDebug>

MainMenu::MainMenu(QWidget *parent) : QMainWindow(parent) {
    centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    // Top bar
    topBarLayout = new QHBoxLayout();
    connectedLabel = new QLabel("Not Connected", this);
    connectedLabel->setStyleSheet("color: red; font-weight: bold;");
    topBarLayout->setContentsMargins(0, 1, 0, 1);
    topBarLayout->addWidget(connectedLabel, 0, Qt::AlignCenter);

    // Options bar (right-aligned settings button)
    optionsLayout = new QHBoxLayout();  // FIX: actually create it
    btnSettings = new QPushButton("Settings", this);  // FIX: create before using
    optionsLayout->setContentsMargins(0, 1, 0, 1);
    optionsLayout->addWidget(btnSettings, 0, Qt::AlignRight);

    // Main layout
    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(10);
    layout->setContentsMargins(20, 20, 20, 20);
    layout->addLayout(topBarLayout);
    layout->addLayout(optionsLayout);  // Add the options bar

    // Create buttons
    btnStart = new QPushButton("Start AutoClicker", this);
    btnDebugMenu = new QPushButton("Debug", this);  // FIX: create this
    btnQuit = new QPushButton("Quit", this);

    layout->addWidget(btnStart);
    layout->addWidget(btnDebugMenu);  // Add debug button
    layout->addStretch();
    layout->addWidget(btnQuit);

    // Connections
    connect(btnQuit, &QPushButton::clicked, this, &QMainWindow::close);
    connect(btnDebugMenu, &QPushButton::clicked, this, [this]() {
        qDebug() << "Debug Menu button clicked";
        emit goToDebug();
    });
    connect(btnSettings, &QPushButton::clicked, this, []() {
        qDebug() << "Settings button clicked";
    });

    // IPC timer
    updateTimer = new QTimer(this);
    connect(updateTimer, &QTimer::timeout, this, &MainMenu::updateLoop);
    updateTimer->start(100);
}

void MainMenu::updateLoop() {
    if (!ipc.valid()) return;

    ipc.update();
    const auto* s = ipc.state();

    if (s->lookingAtBlock) {
        connectedLabel->setText("Connected: Looking at block");
        connectedLabel->setStyleSheet("color: green; font-weight: bold;");
    } else {
        connectedLabel->setText("Not Connected");
        connectedLabel->setStyleSheet("color: red; font-weight: bold;");
    }
}