#include "MainMenu.h"
#include <QDebug>

MainMenu::MainMenu(QWidget *parent) : QMainWindow(parent) {
    centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(0);
    layout->setContentsMargins(0, 0, 0, 0);

    // ── Top bar ──────────────────────────────────────────────────
    auto *topBar = new QWidget(this);
    topBar->setObjectName("topBar");
    topBarLayout = new QHBoxLayout(topBar);
    topBarLayout->setContentsMargins(16, 10, 16, 10);

    auto *appTitle = new QLabel("SHEIK", this);
    appTitle->setObjectName("appTitle");

    // Connection status
    lblConnectionDot  = new QLabel("●", this);
    lblConnectionText = new QLabel("Disconnected", this);
    lblConnectionDot->setObjectName("dotDisconnected");
    lblConnectionText->setObjectName("statusText");

    auto *statusRow = new QHBoxLayout();
    statusRow->setSpacing(5);
    statusRow->addWidget(lblConnectionDot);
    statusRow->addWidget(lblConnectionText);

    btnSettings = new QPushButton("⚙", this);
    btnSettings->setObjectName("btnIcon");
    btnSettings->setFixedSize(32, 32);

    topBarLayout->addWidget(appTitle);
    topBarLayout->addStretch();
    topBarLayout->addLayout(statusRow);
    topBarLayout->addSpacing(12);
    topBarLayout->addWidget(btnSettings);
    layout->addWidget(topBar);

    // ── Content area ─────────────────────────────────────────────
    auto *content = new QWidget(this);
    auto *contentLayout = new QVBoxLayout(content);
    contentLayout->setSpacing(12);
    contentLayout->setContentsMargins(20, 20, 20, 20);

    // Target info panel
    infoPanel = new QWidget(this);
    infoPanel->setObjectName("infoPanel");
    auto *infoPanelLayout = new QVBoxLayout(infoPanel);
    infoPanelLayout->setSpacing(6);
    infoPanelLayout->setContentsMargins(14, 12, 14, 12);

    auto *infoTitle = new QLabel("TARGET", this);
    infoTitle->setObjectName("panelTitle");

    lblTargetId = new QLabel("No target", this);
    lblTargetId->setObjectName("infoValue");

    lblTargetHP = new QLabel("HP: —", this);
    lblTargetHP->setObjectName("infoValueSub");

    hpBar = new QProgressBar(this);
    hpBar->setRange(0, 20);
    hpBar->setValue(0);
    hpBar->setTextVisible(false);
    hpBar->setFixedHeight(6);
    hpBar->setObjectName("hpBar");

    infoPanelLayout->addWidget(infoTitle);
    infoPanelLayout->addWidget(lblTargetId);
    infoPanelLayout->addWidget(lblTargetHP);
    infoPanelLayout->addWidget(hpBar);
    contentLayout->addWidget(infoPanel);

    // Toggle button (big, prominent)
    btnToggle = new QPushButton("START", this);
    btnToggle->setObjectName("btnToggleOff");
    btnToggle->setMinimumHeight(52);
    contentLayout->addWidget(btnToggle);

    // Secondary buttons row
    auto *secRow = new QHBoxLayout();
    btnDebugMenu = new QPushButton("Debug", this);
    btnQuit      = new QPushButton("Quit", this);
    btnDebugMenu->setObjectName("btnSecondary");
    btnQuit->setObjectName("btnDanger");
    secRow->addWidget(btnDebugMenu);
    secRow->addWidget(btnQuit);
    contentLayout->addLayout(secRow);

    contentLayout->addStretch();
    layout->addWidget(content);

    // ── Connections ──────────────────────────────────────────────
    connect(btnQuit,      &QPushButton::clicked, this, &QMainWindow::close);
    connect(btnToggle,    &QPushButton::clicked, this, &MainMenu::toggleRunning);
    connect(btnDebugMenu, &QPushButton::clicked, this, [this]() { emit goToDebug(); });
    connect(btnSettings,  &QPushButton::clicked, this, [this]() { emit goToSettings(); });

    // IPC timer
    updateTimer = new QTimer(this);
    connect(updateTimer, &QTimer::timeout, this, &MainMenu::updateLoop);
    updateTimer->start(100);
}

void MainMenu::toggleRunning() {
    running = !running;
    if (running) {
        btnToggle->setText("STOP");
        btnToggle->setObjectName("btnToggleOn");
    } else {
        btnToggle->setText("START");
        btnToggle->setObjectName("btnToggleOff");
    }
    // Force style re-evaluation after objectName change
    btnToggle->style()->unpolish(btnToggle);
    btnToggle->style()->polish(btnToggle);
}

void MainMenu::updateLoop() {
    if (!ipc.valid()) {
        lblConnectionDot->setObjectName("dotDisconnected");
        lblConnectionText->setText("Disconnected");
        lblConnectionDot->style()->unpolish(lblConnectionDot);
        lblConnectionDot->style()->polish(lblConnectionDot);
        return;
    }

    ipc.update();
    const auto* s = ipc.state();

    // Connection status
    bool connected = (s->lookingAtBlock || s->targetId != 0);
    QString dotObj = connected ? "dotConnected" : "dotIdle";
    QString statusStr = connected ? "Connected" : "Idle";

    if (lblConnectionDot->objectName() != dotObj) {
        lblConnectionDot->setObjectName(dotObj);
        lblConnectionText->setText(statusStr);
        lblConnectionDot->style()->unpolish(lblConnectionDot);
        lblConnectionDot->style()->polish(lblConnectionDot);
    }

    // Target info
    if (s->targetId != 0) {
        lblTargetId->setText(QString("Entity #%1").arg(s->targetId));
        float hp = s->targetHealth;
        lblTargetHP->setText(QString("HP: %1 / 20").arg(hp, 0, 'f', 1));
        hpBar->setValue(static_cast<int>(hp));
    } else {
        lblTargetId->setText("No target");
        lblTargetHP->setText("HP: —");
        hpBar->setValue(0);
    }
}