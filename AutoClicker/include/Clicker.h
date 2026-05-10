#pragma once
#include <cstdint>
#include <cstdlib>

// Forward declaration to avoid X11/Qt header conflicts
typedef struct _XDisplay Display;

class Clicker {
public:
    Clicker();
    ~Clicker();
    void leftClick();
    void rightClick();
    int randJitter(int min, int max);
private:
    Display* display_;
};
