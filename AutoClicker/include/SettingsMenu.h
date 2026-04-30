#pragma once

#include <QMainWindow>
#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QPushButton>
#include <QSpinBox>
#include <QGroupBox>
#include <QCheckBox>
#include <QSettings>

class SettingsMenu : public QMainWindow {
    Q_OBJECT

public:
    explicit SettingsMenu(QWidget *parent = nullptr);

    // Left click
    bool    leftEnabled()     const;
    int     leftIntervalMs()  const;
    int     leftRandMin()     const;
    int     leftRandMax()     const;
    bool    leftAllowMining() const;

    // Right click
    bool    rightEnabled()       const;
    int     rightIntervalMs()    const;
    int     rightRandMin()       const;
    int     rightRandMax()       const;
    bool    rightOnlyWithBlock() const;

signals:
    void goBack();
    void settingsChanged();

private slots:
    void saveSettings();
    void resetDefaults();

private:
    void loadSettings();
    void updateLeftEnabled(bool enabled);
    void updateRightEnabled(bool enabled);

    QWidget     *centralWidget;
    QVBoxLayout *layout;
    QHBoxLayout *topBarLayout;
    QPushButton *btnBack;
    QPushButton *btnSave;
    QPushButton *btnReset;

    // Left click group
    QGroupBox *grpLeft;
    QCheckBox *chkLeftEnabled;
    QSpinBox  *spinLeftInterval;
    QSpinBox  *spinLeftRandMin;
    QSpinBox  *spinLeftRandMax;
    QCheckBox *chkLeftAllowMining;

    // Right click group
    QGroupBox *grpRight;
    QCheckBox *chkRightEnabled;
    QSpinBox  *spinRightInterval;
    QSpinBox  *spinRightRandMin;
    QSpinBox  *spinRightRandMax;
    QCheckBox *chkRightOnlyWithBlock;

    QSettings settings;
};
